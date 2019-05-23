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
import de.saxsys.mvvmfx.ViewModel;
import io.reactivex.disposables.CompositeDisposable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import org.openconnectivity.otgc.domain.model.resource.secure.acl.OcAce;
import org.openconnectivity.otgc.domain.model.resource.secure.acl.OcAcl;
import org.openconnectivity.otgc.domain.usecase.UpdateDeviceTypeUseCase;
import org.openconnectivity.otgc.domain.usecase.accesscontrol.CreateAclUseCase;
import org.openconnectivity.otgc.domain.usecase.accesscontrol.DeleteAclUseCase;
import org.openconnectivity.otgc.domain.usecase.accesscontrol.RetrieveAclUseCase;
import org.openconnectivity.otgc.domain.usecase.RetrieveVerticalResourcesUseCase;
import org.openconnectivity.otgc.utils.constant.NotificationKey;
import org.openconnectivity.otgc.utils.rx.SchedulersFacade;
import org.openconnectivity.otgc.utils.viewmodel.Response;
import org.openconnectivity.otgc.domain.model.devicelist.Device;
import org.openconnectivity.otgc.domain.model.devicelist.DeviceType;
import org.openconnectivity.otgc.utils.scopes.DeviceListToolbarDetailScope;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class AccessControlViewModel implements ViewModel {

    private List<String> selectedVerticalResource = new ArrayList<>();
    public ObjectProperty<Device> deviceProperty;

    @InjectScope
    private DeviceListToolbarDetailScope deviceListToolbarDetailScope;

    @InjectResourceBundle
    private ResourceBundle resourceBundle;

    private final CompositeDisposable disposable = new CompositeDisposable();

    private final SchedulersFacade schedulersFacade;
    private final RetrieveAclUseCase retrieveAclUseCase;
    private final CreateAclUseCase createAclUseCase;
    private final DeleteAclUseCase deleteAclUseCase;
    private final RetrieveVerticalResourcesUseCase retrieveVerticalResourcesUseCase;
    private final UpdateDeviceTypeUseCase updateDeviceTypeUseCase;

    // Observable responses
    private final ObjectProperty<Response<OcAcl>> retrieveAclResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Boolean>> createAclResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Long>> deleteAclResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<List<String>>> retrieveVerticalResourcesResponse = new SimpleObjectProperty<>();

    private ListProperty<OcAce> aceList = new SimpleListProperty<>();
    public ListProperty<OcAce> aceListProperty() { return aceList; }
    private StringProperty selectedTab = new SimpleStringProperty();
    public StringProperty selectedTabProperty() {
        return selectedTab;
    }

    private ListProperty<String> verticalResourcesList = new SimpleListProperty<>();
    public ListProperty<String> verticalResourceListProperty() {
        return verticalResourcesList;
    }

    @Inject
    public AccessControlViewModel(SchedulersFacade schedulersFacade,
                                  RetrieveAclUseCase retrieveAclUseCase,
                                  CreateAclUseCase createAclUseCase,
                                  DeleteAclUseCase deleteAclUseCase,
                                  RetrieveVerticalResourcesUseCase retrieveVerticalResourcesUseCase,
                                  UpdateDeviceTypeUseCase updateDeviceTypeUseCase) {
        this.schedulersFacade = schedulersFacade;
        this.retrieveAclUseCase = retrieveAclUseCase;
        this.createAclUseCase = createAclUseCase;
        this.deleteAclUseCase = deleteAclUseCase;
        this.retrieveVerticalResourcesUseCase = retrieveVerticalResourcesUseCase;
        this.updateDeviceTypeUseCase = updateDeviceTypeUseCase;
    }

    public void initialize() {
        deviceProperty = deviceListToolbarDetailScope.selectedDeviceProperty();
        deviceProperty.addListener(this::loadAccessControl);
        selectedTab = deviceListToolbarDetailScope.selectedTabProperty();
        selectedTabProperty().addListener(this::loadAccessControl);
    }

    public ObservableBooleanValue amsVisibleProperty() {
        return Bindings.createBooleanBinding(() -> deviceProperty.get() != null
                    && deviceProperty.get().getDeviceType() != DeviceType.UNOWNED, deviceProperty);
    }

    public ObjectProperty<Response<OcAcl>> retrieveAclResponseProperty() {
        return retrieveAclResponse;
    }

    public ObjectProperty<Response<Boolean>> createAclResponseProperty() {
        return createAclResponse;
    }

    public ObjectProperty<Response<Long>> deleteAclResponseProperty() {
        return deleteAclResponse;
    }

    public ObjectProperty<Response<List<String>>> retrieveVerticalResourcesResponseProperty() {
        return retrieveVerticalResourcesResponse;
    }

    public void loadAccessControl(ObservableValue<? extends Device> observable, Device oldValue, Device newValue) {
        // Clean Info
        aceListProperty().clear();
        verticalResourceListProperty().clear();
        selectedVerticalResource.clear();

        if (selectedTabProperty().get() != null && selectedTabProperty().get().equals(resourceBundle.getString("client.tab.ams"))
                && newValue != null && newValue.getDeviceType() != DeviceType.UNOWNED) {
            retrieveAcl(newValue);
            retrieveVerticalResources(newValue);
        }
    }

    public void loadAccessControl(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        // Clean Info
        aceListProperty().clear();
        verticalResourceListProperty().clear();
        selectedVerticalResource.clear();

        if (newValue != null && newValue.equals(resourceBundle.getString("client.tab.ams")) && deviceProperty.get() != null
                && deviceProperty.get().getDeviceType() != DeviceType.UNOWNED) {
            retrieveAcl(deviceProperty.get());
            retrieveVerticalResources(deviceProperty.get());
        }
    }

    private void retrieveAcl(Device device) {
        disposable.add(retrieveAclUseCase.execute(device)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> retrieveAclResponse.setValue(Response.loading()))
                .subscribe(
                        acl -> {
                            retrieveAclResponse.setValue(Response.success(acl));

                            if (!device.hasACLpermit()
                                    && (device.getDeviceType() == DeviceType.OWNED_BY_OTHER
                                    || device.getDeviceType() == DeviceType.OWNED_BY_OTHER_WITH_PERMITS)) {
                                disposable.add(updateDeviceTypeUseCase.execute(device.getDeviceId(),
                                                                                DeviceType.OWNED_BY_OTHER_WITH_PERMITS,
                                                                                device.getPermits() | Device.ACL_PERMITS)
                                                .subscribeOn(schedulersFacade.io())
                                                .observeOn(schedulersFacade.ui())
                                                .subscribe(
                                                        () -> deviceListToolbarDetailScope.publish(NotificationKey.UPDATE_DEVICE_TYPE, device),
                                                        throwable -> retrieveAclResponse.setValue(Response.error(throwable))
                                                ));
                            }
                        },
                        throwable -> {
                            retrieveAclResponse.setValue(Response.error(throwable));

                            if (device.hasACLpermit()) {
                                disposable.add(updateDeviceTypeUseCase.execute(device.getDeviceId(),
                                        DeviceType.OWNED_BY_OTHER,
                                        device.getPermits() & ~Device.ACL_PERMITS)
                                        .subscribeOn(schedulersFacade.io())
                                        .observeOn(schedulersFacade.ui())
                                        .subscribe(
                                                () -> deviceListToolbarDetailScope.publish(NotificationKey.UPDATE_DEVICE_TYPE, device),
                                                throwable2 ->  retrieveAclResponse.setValue(Response.error(throwable))
                                        ));
                            }
                        }
                )
        );
    }

    private void retrieveVerticalResources(Device device) {
        disposable.add(retrieveVerticalResourcesUseCase.execute(device)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> retrieveAclResponse.setValue(Response.loading()))
                .subscribe(
                        verticalResources -> retrieveVerticalResourcesResponse.setValue(Response.success(verticalResources)),
                        throwable -> retrieveVerticalResourcesResponse.setValue(Response.error(throwable))
                )
        );
    }

    public void addVerticalResourcesToList(List<String> verticalResources) {
        List<String> vrList = verticalResourceListProperty().get();
        if (vrList == null) {
            vrList = new ArrayList<>();
        }

        for (String verticalResource : verticalResources) {
            vrList.add(verticalResource);
        }

        vrList.sort(Comparator.naturalOrder());
        this.verticalResourceListProperty().setValue(FXCollections.observableArrayList(vrList));
    }

    public void addSelectedVerticalResource(String verticalResource, boolean isAdded) {
        if (isAdded) {
            selectedVerticalResource.add(verticalResource);
        } else {
            selectedVerticalResource.remove(verticalResource);
        }
    }

    public void setAcl(OcAcl acl) {
        List<OcAce> aceList = aceListProperty().get();
        if (aceList == null) {
            aceList = new ArrayList<>();
        }

        for (OcAce ace : acl.getAceList()) {
            aceList.add(ace);
        }

        aceList.sort(Comparator.comparing(OcAce::getAceid));
        this.aceListProperty().setValue(FXCollections.observableArrayList(aceList));
    }

    public void createAce(String subjectId, long permission) {
        disposable.add(createAclUseCase.execute(deviceProperty.get(), subjectId, selectedVerticalResource, permission)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> createAclResponse.setValue(Response.loading()))
                .subscribe(
                        () -> createAclResponse.setValue(Response.success(true)),
                        throwable -> createAclResponse.setValue(Response.error(throwable))
                )
        );
    }

    public void createAce(String roleId, String roleAuthority, long permission) {
        disposable.add(createAclUseCase.execute(deviceProperty.get(), roleId, roleAuthority, selectedVerticalResource, permission)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> createAclResponse.setValue(Response.loading()))
                .subscribe(
                        () -> createAclResponse.setValue(Response.success(true)),
                        throwable -> createAclResponse.setValue(Response.error(throwable))
                )
        );
    }

    public void createAce(boolean isAuth, long permission) {
        disposable.add(createAclUseCase.execute(deviceProperty.get(), isAuth, selectedVerticalResource, permission)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> createAclResponse.setValue(Response.loading()))
                .subscribe(
                        () -> createAclResponse.setValue(Response.success(true)),
                        throwable -> createAclResponse.setValue(Response.error(throwable))
                )
        );
    }

    public void deleteACL(long aceId) {
        disposable.add(deleteAclUseCase.execute(deviceProperty.get(), aceId)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> deleteAclResponse.setValue(Response.loading()))
                .subscribe(
                        () -> deleteAclResponse.setValue(Response.success(aceId)),
                        throwable -> deleteAclResponse.setValue(Response.error(throwable))
        ));
    }
}
