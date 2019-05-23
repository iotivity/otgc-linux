/*
 * Copyright 2018 DEKRA Testing and Certification, S.A.U. All Rights Reserved.
 *
 * *****************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openconnectivity.otgc.viewmodel;

import de.saxsys.mvvmfx.InjectResourceBundle;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.SceneLifecycle;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import io.reactivex.disposables.CompositeDisposable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import org.iotivity.OCRepresentation;
import org.openconnectivity.otgc.domain.model.client.DynamicUiElement;
import org.openconnectivity.otgc.domain.model.client.SerializableResource;
import org.openconnectivity.otgc.domain.model.client.Info;
import org.openconnectivity.otgc.domain.model.resource.virtual.p.OcPlatformInfo;
import org.openconnectivity.otgc.utils.constant.NotificationKey;
import org.openconnectivity.otgc.utils.constant.OcfInterface;
import org.openconnectivity.otgc.domain.model.resource.virtual.d.OcDeviceInfo;
import org.openconnectivity.otgc.domain.usecase.GetDeviceInfoUseCase;
import org.openconnectivity.otgc.utils.rx.SchedulersFacade;
import org.openconnectivity.otgc.utils.viewmodel.Response;
import org.openconnectivity.otgc.domain.model.devicelist.Device;
import org.openconnectivity.otgc.domain.model.devicelist.DeviceType;
import org.openconnectivity.otgc.domain.usecase.*;
import org.openconnectivity.otgc.domain.usecase.client.*;
import org.openconnectivity.otgc.utils.scopes.DeviceListToolbarDetailScope;

import javax.inject.Inject;
import java.util.*;

public class ClientViewModel implements ViewModel, SceneLifecycle {

    public ObjectProperty<Device> deviceProperty;

    @InjectScope
    private DeviceListToolbarDetailScope deviceListToolbarDetailScope;

    @InjectResourceBundle
    private ResourceBundle resourceBundle;

    @Inject
    private NotificationCenter notificationCenter;

    private final CompositeDisposable disposables = new CompositeDisposable();

    private final SchedulersFacade schedulersFacade;
    private final GetDeviceInfoUseCase getDeviceInfoUseCase;
    private final GetPlatformInfoUseCase getPlatformInfoUseCase;
    private final IntrospectUseCase introspectUseCase;
    private final UiFromSwaggerUseCase uiFromSwaggerUseCase;
    private final GetResourcesUseCase getResourcesUseCase;
    private final GetRequestUseCase getRequestUseCase;
    private final PostRequestUseCase postRequestUseCase;
    private final ObserveResourceUseCase observeResourceUseCase;
    private final CancelObserveResourceUseCase cancelObserveResourceUseCase;
    private final CancelAllObserveResourceUseCase cancelAllObserveResourceUseCase;
    private final UpdateDeviceTypeUseCase updateDeviceTypeUseCase;

    // Observable responses
    private final ObjectProperty<Response<OcDeviceInfo>> deviceInfoResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<OcPlatformInfo>> platformInfoResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<List<DynamicUiElement>>> introspectResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<List<SerializableResource>>> getResourcesResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<SerializableResource>> getRequestResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Boolean>> postRequestResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<SerializableResource>> observeResourceResponse = new SimpleObjectProperty<>();

    private ListProperty<Info> infoList = new SimpleListProperty<>();
    public ListProperty<Info> infoListProperty() { return infoList; }
    private final StringProperty selectedTab = new SimpleStringProperty();
    public final StringProperty selectedTabProperty(){
        return this.selectedTab;
    }

    @Inject
    public ClientViewModel(SchedulersFacade schedulersFacade,
                           GetDeviceInfoUseCase getDeviceInfoUseCase,
                           GetPlatformInfoUseCase getPlatformInfoUseCase,
                           IntrospectUseCase introspectUseCase,
                           UiFromSwaggerUseCase uiFromSwaggerUseCase,
                           GetResourcesUseCase getResourcesUseCase,
                           GetRequestUseCase getRequestUseCase,
                           PostRequestUseCase postRequestUseCase,
                           ObserveResourceUseCase observeResourceUseCase,
                           CancelObserveResourceUseCase cancelObserveResourceUseCase,
                           CancelAllObserveResourceUseCase cancelAllObserveResourceUseCase,
                           UpdateDeviceTypeUseCase updateDeviceTypeUseCase) {
        this.schedulersFacade = schedulersFacade;
        this.getDeviceInfoUseCase = getDeviceInfoUseCase;
        this.getPlatformInfoUseCase = getPlatformInfoUseCase;
        this.introspectUseCase = introspectUseCase;
        this.uiFromSwaggerUseCase = uiFromSwaggerUseCase;
        this.getResourcesUseCase = getResourcesUseCase;
        this.getRequestUseCase = getRequestUseCase;
        this.postRequestUseCase = postRequestUseCase;
        this.observeResourceUseCase = observeResourceUseCase;
        this.cancelObserveResourceUseCase = cancelObserveResourceUseCase;
        this.cancelAllObserveResourceUseCase = cancelAllObserveResourceUseCase;
        this.updateDeviceTypeUseCase = updateDeviceTypeUseCase;
    }

    public void initialize() {
        deviceProperty = deviceListToolbarDetailScope.selectedDeviceProperty();
        deviceProperty.addListener(this::loadInfoDevice);

        deviceListToolbarDetailScope.selectedTabProperty().bindBidirectional(selectedTabProperty());
        selectedTabProperty().addListener(this::loadGenericClient);

        notificationCenter.subscribe(NotificationKey.SHUTDOWN_OIC_STACK,
                (key, payload) -> cancellAllObserveResource());
    }

    @Override
    public void onViewAdded() {}

    @Override
    public void onViewRemoved() {
        disposables.clear();
    }

    public ObjectProperty<Response<OcDeviceInfo>> deviceInfoResponseProperty() {
        return deviceInfoResponse;
    }

    public ObjectProperty<Response<OcPlatformInfo>> platformInfoResponseProperty() {
        return platformInfoResponse;
    }

    public ObjectProperty<Response<List<DynamicUiElement>>> introspectResponseProperty() {
        return introspectResponse;
    }

    public ObjectProperty<Response<List<SerializableResource>>> getResourcesResponseProperty() {
        return getResourcesResponse;
    }

    public ObjectProperty<Response<SerializableResource>> getRequestResponseProperty() {
        return getRequestResponse;
    }

    public ObjectProperty<Response<Boolean>> postRequestResponseProperty() {
        return postRequestResponse;
    }

    public ObjectProperty<Response<SerializableResource>> observeResourceResponseProperty() {
        return observeResourceResponse;
    }

    public ObservableBooleanValue clientVisibleProperty() {
        return Bindings.createBooleanBinding(() -> deviceProperty.get() != null
                    && deviceProperty.get().getDeviceType() != DeviceType.UNOWNED, deviceProperty);
    }

    public void loadInfoDevice(ObservableValue<? extends Device> observable, Device oldValue, Device newValue) {
        // Clean Info
        infoListProperty().clear();

        if (newValue == null || (oldValue != null && !newValue.getDeviceId().equals(oldValue.getDeviceId()))) {
            cancellAllObserveResource();
        }

        if ((newValue != null) && newValue.getDeviceType() != DeviceType.UNOWNED) {

            // Load Info
            loadDeviceInfo(newValue);
            loadPlatformInfo(newValue);
            if (selectedTabProperty().get() != null  && selectedTabProperty().get().equals(resourceBundle.getString("client.tab.generic_client"))) {
                introspect(newValue);
            }
        }
    }

    public void loadGenericClient(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (newValue != null  && newValue.equals(resourceBundle.getString("client.tab.generic_client")) && deviceProperty.get() != null
            && deviceProperty.get().getDeviceType() != DeviceType.UNOWNED){
            introspect(deviceProperty.get());
        }
    }

    public void loadDeviceInfo(Device device) {
        disposables.add(getDeviceInfoUseCase.execute(device)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> deviceInfoResponse.setValue(Response.loading()))
                .subscribe(
                        deviceInfo -> deviceInfoResponse.setValue(Response.success(deviceInfo)),
                        throwable -> deviceInfoResponse.setValue(Response.error(throwable))
                ));
    }

    public void setDeviceInfo(OcDeviceInfo deviceInfo) {
        List<Info> tmpInfoList = infoListProperty().get();
        if (tmpInfoList == null) {
            tmpInfoList = new ArrayList<>();
        }

        if (deviceInfo != null) {
            tmpInfoList.add(new Info(resourceBundle.getString("client.device.device_name"), deviceInfo.getName()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.device.spec_version_url"), deviceInfo.getSpecVersionUrl()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.device.device_id"), deviceInfo.getDeviceId()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.device.data_model"), deviceInfo.getDataModel()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.device.piid"), deviceInfo.getPiid()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.device.sw_version"), deviceInfo.getSoftwareVersion()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.device.man_name"), deviceInfo.getManufacturerName()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.device.model_no"), deviceInfo.getModelNumber()));
        }

        this.infoListProperty().setValue(FXCollections.observableArrayList(tmpInfoList));
    }

    public void loadPlatformInfo(Device device) {
        disposables.add(getPlatformInfoUseCase.execute(device)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> platformInfoResponse.setValue(Response.loading()))
                .subscribe(
                        platformInfo -> platformInfoResponse.setValue(Response.success(platformInfo)),
                        throwable -> platformInfoResponse.setValue(Response.error(throwable))
                ));
    }

    public void setPlatformInfo(OcPlatformInfo platformInfo) {
        List<Info> tmpInfoList = infoListProperty().get();
        if (tmpInfoList == null) {
            tmpInfoList = new ArrayList<>();
        }

        if (platformInfo != null) {
            tmpInfoList.add(new Info(resourceBundle.getString("client.platform.platform_id"), platformInfo.getPlatformId()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.platform.man_name"), platformInfo.getManufacturerName()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.platform.man_url"), platformInfo.getManufacturerUrl()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.platform.man_model_no"), platformInfo.getManufacturerModelNumber()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.platform.man_date"), platformInfo.getManufacturedDate()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.platform.man_info"), platformInfo.getManufacturerInfo()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.platform.man_platform_version"), platformInfo.getManufacturerPlatformVersion()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.platform.man_os_version"), platformInfo.getManufacturerOsVersion()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.platform.man_hw_version"), platformInfo.getManufacturerHwVersion()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.platform.man_fw_version"), platformInfo.getManufacturerFwVersion()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.platform.man_support_url"), platformInfo.getManufacturerSupportUrl()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.platform.man_system_time"), platformInfo.getManufacturerSystemTime()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.platform.man_serial_number"), platformInfo.getManufacturerSerialNumber()));
        }

        this.infoListProperty().setValue(FXCollections.observableArrayList(tmpInfoList));
    }

    public void introspect(Device device) {
        disposables.add(introspectUseCase.execute(device)
                .flatMap(uiFromSwaggerUseCase::execute)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> introspectResponse.setValue(Response.loading()))
                .subscribe(
                        introspection -> introspectResponse.setValue(Response.success(introspection)),
                        throwable -> introspectResponse.setValue(Response.error(throwable))
                ));
    }

    public void buildUiForIntrospect(List<DynamicUiElement> resources) {
        for (DynamicUiElement resource : resources) {
            if (resource.getSupportedOperations().contains("get")) {
                SerializableResource serializableResource = new SerializableResource();
                serializableResource.setUri(resource.getPath());
                serializableResource.setResourceTypes(resource.getResourceTypes());
                serializableResource.setResourceInterfaces(resource.getInterfaces());

                getRequest(deviceProperty.get(), serializableResource);
            }
        }
    }

    public void findResources() {
        disposables.add(getResourcesUseCase.execute(deviceProperty.get())
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> getResourcesResponse.setValue(Response.loading()))
                .subscribe(
                        serializableResources -> getResourcesResponse.setValue(Response.success(serializableResources)),
                        throwable -> getResourcesResponse.setValue(Response.error(throwable))
                ));
    }

    public void buildUiForRetrieveResources(List<SerializableResource> resources) {
        for(SerializableResource resource : resources) {
            getRequest(deviceProperty.get(), resource);
        }
    }

    private void getRequest(Device device, SerializableResource resource) {
        disposables.add(getRequestUseCase.execute(device, resource)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        res -> {
                            getRequestResponse.setValue(Response.success(res));

                            if (!device.hasDOXSpermit()
                                    && (device.getDeviceType() == DeviceType.OWNED_BY_OTHER
                                    || device.getDeviceType() == DeviceType.OWNED_BY_OTHER_WITH_PERMITS)) {
                                disposables.add(updateDeviceTypeUseCase.execute(device.getDeviceId(),
                                        DeviceType.OWNED_BY_OTHER_WITH_PERMITS,
                                        device.getPermits() | Device.DOXS_PERMITS)
                                        .subscribeOn(schedulersFacade.io())
                                        .observeOn(schedulersFacade.ui())
                                        .subscribe(
                                                () -> deviceListToolbarDetailScope.publish(NotificationKey.UPDATE_DEVICE_TYPE, device),
                                                throwable -> getRequestResponse.setValue(Response.error(throwable))
                                        ));

                            }
                        },
                        throwable -> {
                            getRequestResponse.setValue(Response.error(throwable));

                            if (device.hasDOXSpermit()) {
                                disposables.add(updateDeviceTypeUseCase.execute(device.getDeviceId(),
                                        DeviceType.OWNED_BY_OTHER,
                                        device.getPermits() & ~Device.DOXS_PERMITS)
                                        .subscribeOn(schedulersFacade.io())
                                        .observeOn(schedulersFacade.ui())
                                        .subscribe(
                                                () -> deviceListToolbarDetailScope.publish(NotificationKey.UPDATE_DEVICE_TYPE, device),
                                                throwable2 -> getRequestResponse.setValue(Response.error(throwable2))
                                        ));
                            }
                        }
                ));
    }

    public void postRequest(SerializableResource resource, OCRepresentation rep, Object valueArray) {
        disposables.add(postRequestUseCase.execute(deviceProperty.get(), resource, rep, valueArray)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        () -> postRequestResponse.setValue(Response.success(true)),
                        throwable -> postRequestResponse.setValue(Response.error(throwable))
                ));
    }

    public void registerResourceObserve(SerializableResource resource) {
        disposables.add(observeResourceUseCase.execute(deviceProperty.get(), resource)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        serializableResource -> observeResourceResponse.setValue(Response.success(serializableResource)),
                        throwable -> observeResourceResponse.setValue(Response.error(throwable))
                ));
    }

    public void cancelResourceObserve(SerializableResource resource) {
        disposables.add(cancelObserveResourceUseCase.execute(resource)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        () -> {},
                        throwable -> {}
                ));
    }

    public void cancellAllObserveResource() {
        disposables.add(
                cancelAllObserveResourceUseCase.execute()
                        .subscribeOn(schedulersFacade.io())
                        .observeOn(schedulersFacade.ui())
                        .subscribe(
                                () -> notificationCenter.publish(NotificationKey.CANCEL_ALL_OBSERVERS)
                        )
        );
    }

    public boolean isViewEnabled(List<String> resourceInterfaces) {
        return resourceInterfaces.contains(OcfInterface.ACTUATOR)
                || resourceInterfaces.contains(OcfInterface.READ_WRITE)
                || resourceInterfaces.isEmpty();
    }
}
