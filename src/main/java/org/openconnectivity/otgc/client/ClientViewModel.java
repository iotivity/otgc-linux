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

package org.openconnectivity.otgc.client;

import de.saxsys.mvvmfx.InjectResourceBundle;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.SceneLifecycle;
import de.saxsys.mvvmfx.ViewModel;
import io.reactivex.disposables.CompositeDisposable;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.*;
import org.controlsfx.control.ToggleSwitch;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;
import org.openconnectivity.otgc.client.domain.model.DynamicUiElement;
import org.openconnectivity.otgc.client.domain.model.SerializableResource;
import org.openconnectivity.otgc.client.domain.usecase.*;
import org.openconnectivity.otgc.client.model.Info;
import org.openconnectivity.otgc.client.model.OicPlatform;
import org.openconnectivity.otgc.common.constant.OcfInterface;
import org.openconnectivity.otgc.common.domain.model.OcDevice;
import org.openconnectivity.otgc.common.domain.usecase.GetDeviceInfoUseCase;
import org.openconnectivity.otgc.common.rx.SchedulersFacade;
import org.openconnectivity.otgc.common.viewmodel.Response;
import org.openconnectivity.otgc.devicelist.domain.model.Device;
import org.openconnectivity.otgc.devicelist.domain.model.DeviceType;
import org.openconnectivity.otgc.scopes.DeviceListToolbarDetailScope;

import javax.inject.Inject;
import java.util.*;

public class ClientViewModel implements ViewModel, SceneLifecycle {

    public ObjectProperty<Device> deviceProperty;

    @InjectScope
    private DeviceListToolbarDetailScope deviceListToolbarDetailScope;

    @InjectResourceBundle
    private ResourceBundle resourceBundle;

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

    // Observable responses
    private final ObjectProperty<Response<OcDevice>> deviceInfoResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<OicPlatform>> platformInfoResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<List<DynamicUiElement>>> introspectResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<List<SerializableResource>>> getResourcesResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<SerializableResource>> getRequestResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<SerializableResource>> postRequestResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<SerializableResource>> observeResourceResponse = new SimpleObjectProperty<>();

    private ListProperty<Info> infoList = new SimpleListProperty<>();
    public ListProperty<Info> infoListProperty() { return infoList; }

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
                           CancelAllObserveResourceUseCase cancelAllObserveResourceUseCase) {
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
    }

    public void initialize() {
        deviceProperty = deviceListToolbarDetailScope.selectedDeviceProperty();
        deviceProperty.addListener(this::loadGenericClient);
    }

    @Override
    public void onViewAdded() {}

    @Override
    public void onViewRemoved() {
        disposables.clear();
    }

    public ObjectProperty<Response<OcDevice>> deviceInfoResponseProperty() {
        return deviceInfoResponse;
    }

    public ObjectProperty<Response<OicPlatform>> platformInfoResponseProperty() {
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

    public ObjectProperty<Response<SerializableResource>> postRequestResponseProperty() {
        return postRequestResponse;
    }

    public ObjectProperty<Response<SerializableResource>> observeResourceResponseProperty() {
        return observeResourceResponse;
    }

    public ObservableBooleanValue clientVisibleProperty() {
        return Bindings.createBooleanBinding(() -> deviceProperty.get() != null
                && (deviceProperty.get().getDeviceType() == DeviceType.OWNED_BY_SELF
                || deviceProperty.get().getDeviceType() == DeviceType.OWNED_BY_OTHER), deviceProperty);
    }

    public void loadGenericClient(ObservableValue<? extends Device> observable, Device oldValue, Device newValue) {
        // Clean Info
        infoListProperty().clear();

        if (newValue == null || (oldValue != null && !newValue.getDeviceId().equals(oldValue.getDeviceId()))) {
            cancellAllObserveResource();
        }

        if ((newValue != null) && (newValue.getDeviceType() == DeviceType.OWNED_BY_SELF
                || newValue.getDeviceType() == DeviceType.OWNED_BY_OTHER)) {
            String deviceId = newValue.getDeviceId();

            // Load Info
            loadDeviceInfo(deviceId);
            loadPlatformInfo(deviceId);
            introspect(deviceId);
        }
    }

    public void loadDeviceInfo(String deviceId) {
        disposables.add(getDeviceInfoUseCase.execute(deviceId)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> deviceInfoResponse.setValue(Response.loading()))
                .subscribe(
                        deviceInfo -> deviceInfoResponse.setValue(Response.success(deviceInfo)),
                        throwable -> deviceInfoResponse.setValue(Response.error(throwable))
                ));
    }

    public void setDeviceInfo(OcDevice deviceInfo) {
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

    public void loadPlatformInfo(String deviceId) {
        disposables.add(getPlatformInfoUseCase.execute(deviceId)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> platformInfoResponse.setValue(Response.loading()))
                .subscribe(
                        platformInfo -> platformInfoResponse.setValue(Response.success(platformInfo)),
                        throwable -> platformInfoResponse.setValue(Response.error(throwable))
                ));
    }

    public void setPlatformInfo(OicPlatform platformInfo) {
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
            tmpInfoList.add(new Info(resourceBundle.getString("client.platform.man_platform_version"), platformInfo.getManufacturerPlatformVersion()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.platform.man_os_version"), platformInfo.getManufacturerOsVersion()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.platform.man_hw_version"), platformInfo.getManufacturerHwVersion()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.platform.man_fw_version"), platformInfo.getManufacturerFwVersion()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.platform.man_support_url"), platformInfo.getManufacturerSupportUrl()));
            tmpInfoList.add(new Info(resourceBundle.getString("client.platform.man_system_time"), platformInfo.getManufacturerSystemTime()));
        }

        this.infoListProperty().setValue(FXCollections.observableArrayList(tmpInfoList));
    }

    public void introspect(String deviceId) {
        disposables.add(introspectUseCase.execute(deviceId)
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

                getRequest(deviceProperty.get().getDeviceId(), serializableResource);
            }
        }
    }

    public void findResources(String deviceId) {
        disposables.add(getResourcesUseCase.execute(deviceId)
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
            getRequest(deviceProperty.get().getDeviceId(), resource);
        }
    }

    private void getRequest(String deviceId, SerializableResource resource) {
        disposables.add(getRequestUseCase.execute(deviceId, resource.getUri(), resource.getResourceTypes(), resource.getResourceInterfaces())
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        ocRepresentation -> {
                            resource.setOcRepresentation(ocRepresentation);
                            getRequestResponse.setValue(Response.success(resource));
                        },
                        throwable -> getRequestResponse.setValue(Response.error(throwable))
                ));
    }

    private void postRequest(String deviceId, SerializableResource resource, OcRepresentation rep) {
        disposables.add(postRequestUseCase.execute(deviceId, resource.getUri(), resource.getResourceTypes(), resource.getResourceInterfaces(), rep)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                    ocRepresentation -> {
                        resource.setOcRepresentation(ocRepresentation);
                        postRequestResponse.setValue(Response.success(resource));
                    },
                    throwable -> postRequestResponse.setValue(Response.error(throwable))
                ));
    }

    public void createResource(VBox container, SerializableResource resource) {
        OcRepresentation ocRepresentation = resource.getOcRepresentation();

        TitledPane paneResource = new TitledPane();
        paneResource.setText(resource.getUri());
        paneResource.setPrefHeight(Control.USE_COMPUTED_SIZE);
        paneResource.setPrefWidth(Control.USE_COMPUTED_SIZE);
        container.getChildren().add(paneResource);
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setPrefHeight(Control.USE_COMPUTED_SIZE);
        anchorPane.setPrefWidth(Control.USE_COMPUTED_SIZE);
        paneResource.setContent(anchorPane);

        createUI(anchorPane, resource, ocRepresentation);
    }

    public void registerResourceObserve(SerializableResource resource) {
        disposables.add(observeResourceUseCase.execute(resource)
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
                        .subscribe()
        );
    }

    public void updateResource(VBox container, SerializableResource resource) {
        OcRepresentation ocRepresentation = resource.getOcRepresentation();

        ObservableList<Node> children = container.getChildren();
        for (Node child : children) {
            TitledPane pane = (TitledPane) child;
            String uri = resource.getUri();
            if (pane.getText().equals(uri)) {
                // Clean previous UI
                AnchorPane anchorPane = (AnchorPane) pane.getContent();
                if (Platform.isFxApplicationThread()) {
                    anchorPane.getChildren().clear();
                    createUI(anchorPane, resource, ocRepresentation);
                } else {
                    Platform.runLater(() -> {
                        anchorPane.getChildren().clear();
                        createUI(anchorPane, resource, ocRepresentation);
                    });
                }
            }
        }
    }

    private void createUI(AnchorPane anchorPane, SerializableResource resource, OcRepresentation ocRepresentation) {
        Map<String, Object> values = ocRepresentation.getValues();

        HBox hbox = new HBox();
        hbox.setPrefHeight(Control.USE_COMPUTED_SIZE);
        hbox.setPrefWidth(Control.USE_COMPUTED_SIZE);
        hbox.setPadding(new Insets(5, 10, 5, 10));
        hbox.setSpacing(5.0);
        anchorPane.getChildren().add(hbox);

        // Observable property
        VBox vboxObservable = new VBox();
        vboxObservable.setPrefHeight(Control.USE_COMPUTED_SIZE);
        vboxObservable.setPrefWidth(Control.USE_COMPUTED_SIZE);
        vboxObservable.setSpacing(5.0);
        Label labelObservable = new Label("Observe");
        labelObservable.setPadding(new Insets(0, 0, 0, 20));
        ToggleSwitch toggleSwitchObservable = new ToggleSwitch();
        toggleSwitchObservable.setSelected(resource.isObserving());
        vboxObservable.getChildren().add(labelObservable);
        vboxObservable.getChildren().add(toggleSwitchObservable);
        if (resource.isObservable()) {
            toggleSwitchObservable.selectedProperty().addListener(((observable, oldValue, newValue) -> {
                resource.setObserving(newValue.booleanValue());
                if (newValue.booleanValue()) {
                    registerResourceObserve(resource);
                } else {
                    cancelResourceObserve(resource);
                }
            }));
        } else {
            vboxObservable.disableProperty().setValue(true);
        }
        hbox.getChildren().add(vboxObservable);

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            VBox vbox = new VBox();
            vbox.setPrefHeight(Control.USE_COMPUTED_SIZE);
            vbox.setPrefWidth(Control.USE_COMPUTED_SIZE);
            vbox.setSpacing(5.0);

            if (entry.getValue() instanceof Boolean) {
                Label labelResource = new Label(entry.getKey());
                labelResource.setPadding(new Insets(0, 0, 0, 20));
                ToggleSwitch toggleSwitch = new ToggleSwitch();
                toggleSwitch.setSelected((Boolean)entry.getValue());
                vbox.getChildren().add(labelResource);
                vbox.getChildren().add(toggleSwitch);
                if (isViewEnabled(ocRepresentation.getResourceInterfaces())) {
                    toggleSwitch.selectedProperty().addListener(((observable, oldValue, newValue) -> {
                        OcRepresentation rep = new OcRepresentation();
                        try {
                            rep.setValue(entry.getKey(), newValue);
                        } catch (OcException e) {
                            // TODO
                        }

                        postRequest(deviceProperty.get().getDeviceId(), resource, rep);
                    }));
                } else {
                    vbox.disableProperty().setValue(true);
                }
            } else if (entry.getValue() instanceof Integer) {
                Label labelResource = new Label(entry.getKey());
                TextField textResource = new TextField(entry.getValue().toString());
                // TODO: TextField with integer format
                textResource.setMaxWidth(150.0);
                vbox.getChildren().add(labelResource);
                vbox.getChildren().add(textResource);
                if (isViewEnabled(ocRepresentation.getResourceInterfaces())) {
                    textResource.textProperty().addListener(((observable, oldValue, newValue) -> {
                        Integer number;
                        try {
                            number = Integer.valueOf(newValue);
                        } catch (NumberFormatException ex) {
                            return;
                        }

                        OcRepresentation rep = new OcRepresentation();
                        try {
                            rep.setValue(entry.getKey(), number);
                        } catch (OcException ex) {
                            // TODO
                        }

                        postRequest(deviceProperty.get().getDeviceId(), resource, rep);
                    }));
                } else {
                    vbox.disableProperty().setValue(true);
                }
            } else if (entry.getValue() instanceof Double) {
                Label labelResource = new Label(entry.getKey());
                TextField textResource = new TextField(entry.getValue().toString());
                // TODO: TextField with decimal format
                textResource.setMaxWidth(150.0);
                vbox.getChildren().add(labelResource);
                vbox.getChildren().add(textResource);
                if (isViewEnabled(ocRepresentation.getResourceInterfaces())) {
                    textResource.textProperty().addListener(((observable, oldValue, newValue) -> {
                        Double number;
                        try {
                            number = Double.valueOf(newValue);
                        } catch (NumberFormatException ex) {
                            return;
                        }

                        OcRepresentation rep = new OcRepresentation();
                        try {
                            rep.setValue(entry.getKey(), number);
                        } catch (OcException ex) {
                            // TODO
                        }

                        postRequest(deviceProperty.get().getDeviceId(), resource, rep);
                    }));
                } else {
                    vbox.disableProperty().setValue(true);
                }
            }

            if (vbox != null) {
                hbox.getChildren().add(vbox);
            }
        }
    }

    private boolean isViewEnabled(List<String> resourceInterfaces) {
        return resourceInterfaces.contains(OcfInterface.ACTUATOR)
                || resourceInterfaces.contains(OcfInterface.READ_WRITE)
                || resourceInterfaces.isEmpty();
    }
}
