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
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import org.apache.log4j.Logger;
import org.openconnectivity.otgc.utils.rx.SchedulersFacade;
import org.openconnectivity.otgc.utils.viewmodel.Response;
import org.openconnectivity.otgc.domain.model.devicelist.Device;
import org.openconnectivity.otgc.domain.model.devicelist.DeviceType;
import org.openconnectivity.otgc.domain.model.devicelist.DeviceRole;
import org.openconnectivity.otgc.domain.usecase.link.*;
import org.openconnectivity.otgc.utils.scopes.DeviceListToolbarDetailScope;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class LinkDevicesViewModel implements ViewModel {

    private final Logger LOG = Logger.getLogger(LinkDevicesViewModel.class);

    private ListProperty<Device> devicesList = new SimpleListProperty<>();
    public ListProperty<Device> devicesListProperty() { return devicesList; }
    private ListProperty<String> ownedDevices = new SimpleListProperty<>();
    public ListProperty<String> ownedDevicesProperty() { return ownedDevices; }

    private ObjectProperty<List<Device>> device = new SimpleObjectProperty<>();
    public ObjectProperty<List<Device>> deviceProperty() {
        return device;
    }
    private StringProperty selectedTab = new SimpleStringProperty();
    public StringProperty selectedTabProperty() {
        return selectedTab;
    }

    @InjectScope
    private DeviceListToolbarDetailScope deviceListToolbarDetailScope;

    @InjectResourceBundle
    private ResourceBundle resourceBundle;

    private final CompositeDisposable disposables = new CompositeDisposable();

    private final SchedulersFacade schedulersFacade;
    private final LinkDevicesUseCase linkDevicesUseCase;
    private final LinkRoleForClientUseCase linkRoleForClientUseCase;
    private final LinkRoleForServerUseCase linkRoleForServerUseCase;
    private final UnlinkDevicesUseCase unlinkDevicesUseCase;
    private final UnlinkRoleForClientUseCase unlinkRoleForClientUseCase;
    private final UnlinkRoleForServerUseCase unlinkRoleForServerUseCase;
    private final RetrieveLinkedDevicesUseCase retrieveLinkedDevicesUseCase;
    private final RetrieveLinkedRolesForClientUseCase retrieveLinkedRolesForClientUseCase;
    private final RetrieveLinkedRolesForServerUseCase retrieveLinkedRolesForServerUseCase;

    // Observable responses
    private final ObjectProperty<Response<List<String>>> retrieveLinkedDevicesResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<List<String>>> retrieveLinkedRolesResponse = new SimpleObjectProperty<>();

    private ListProperty<String> linkedDevicesList = new SimpleListProperty<>();
    public ListProperty<String> linkedDevicesListProperty() { return linkedDevicesList; }
    private ListProperty<String> linkedRoleList = new SimpleListProperty<>();
    public ListProperty<String> linkedRoleListProperty() {
        return linkedRoleList;
    }

    public void initialize()
    {
        device = deviceListToolbarDetailScope.selectedDeviceProperty();
        device.addListener(this::loadLinked);
        selectedTab = deviceListToolbarDetailScope.selectedTabProperty();
        selectedTabProperty().addListener(this::loadLinked);
        devicesList = deviceListToolbarDetailScope.devicesListProperty();
    }

    @Inject
    public LinkDevicesViewModel(SchedulersFacade schedulersFacade,
                                RetrieveLinkedDevicesUseCase retrieveLinkedDevicesUseCase,
                                LinkDevicesUseCase linkDevicesUseCase,
                                LinkRoleForClientUseCase linkRoleForClientUseCase,
                                LinkRoleForServerUseCase linkRoleForServerUseCase,
                                RetrieveLinkedRolesForClientUseCase retrieveLinkedRolesForClientUseCase,
                                RetrieveLinkedRolesForServerUseCase retrieveLinkedRolesForServerUseCase,
                                UnlinkDevicesUseCase unlinkDevicesUseCase,
                                UnlinkRoleForClientUseCase unlinkRoleForClientUseCase,
                                UnlinkRoleForServerUseCase unlinkRoleForServerUseCase)
    {
        this.schedulersFacade = schedulersFacade;
        this.retrieveLinkedDevicesUseCase = retrieveLinkedDevicesUseCase;
        this.retrieveLinkedRolesForClientUseCase = retrieveLinkedRolesForClientUseCase;
        this.retrieveLinkedRolesForServerUseCase = retrieveLinkedRolesForServerUseCase;
        this.linkDevicesUseCase = linkDevicesUseCase;
        this.linkRoleForClientUseCase = linkRoleForClientUseCase;
        this.linkRoleForServerUseCase = linkRoleForServerUseCase;
        this.unlinkDevicesUseCase = unlinkDevicesUseCase;
        this.unlinkRoleForClientUseCase = unlinkRoleForClientUseCase;
        this.unlinkRoleForServerUseCase = unlinkRoleForServerUseCase;
    }

    public ObjectProperty<Response<List<String>>> retrieveLinkedDevicesResponseProperty() {
        return retrieveLinkedDevicesResponse;
    }

    public ObjectProperty<Response<List<String>>> retrieveLinkedRolesResponseProperty() {
        return retrieveLinkedRolesResponse;
    }

    public ObservableBooleanValue linkedDevicesVisibleProperty() {
        return Bindings.createBooleanBinding(() -> deviceProperty().get() != null && deviceProperty().get().size() == 1
                && (deviceProperty().get().get(0).getDeviceType() == DeviceType.OWNED_BY_SELF), device);
    }

    public void ownedDeviceListProperty() {
        List<String> tmp;
        if (ownedDevicesProperty().get() == null) {
            tmp = new ArrayList<>();
        } else {
            tmp = ownedDevicesProperty().get();
        }
        for (Device device : devicesListProperty().filtered(device ->
                (device.getDeviceType() == DeviceType.OWNED_BY_SELF
                && device.getDeviceRole() != deviceProperty().get().get(0).getDeviceRole()))) {
            if (!tmp.contains(device.getDeviceId())) {
                tmp.add(device.getDeviceId());
            }
        }
        ownedDevicesProperty().setValue(FXCollections.observableArrayList(tmp));
    }

    public void loadLinked(ObservableValue<? extends List<Device>> observable, List<Device> oldValue, List<Device> newValue) {
        if (selectedTabProperty().get() != null && selectedTabProperty().get().equals(resourceBundle.getString("client.tab.linkeddevices"))
                && newValue != null && newValue.size() == 1 && newValue.get(0).getDeviceType() == DeviceType.OWNED_BY_SELF) {
            // Load linked devices
            loadLinkedDevices(newValue.get(0));

            // Load linked roles
            loadLinkedRoles(newValue.get(0));
        }
    }

    public void loadLinked(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (newValue != null && newValue.equals(resourceBundle.getString("client.tab.linkeddevices"))
                && deviceProperty().get() != null && deviceProperty().get().size() == 1 && deviceProperty().get().get(0).getDeviceType() == DeviceType.OWNED_BY_SELF)
        {
            // Load linked devices
            loadLinkedDevices(deviceProperty().get().get(0));

            // Load linked roles
            loadLinkedRoles(deviceProperty().get().get(0));
        }
    }

    public void loadLinkedDevices(Device device) {
        // Clean Info
        linkedDevicesListProperty().clear();
        ownedDevicesProperty().clear();

        retrieveLinkedDevices(device);
        ownedDeviceListProperty();
    }

    public void loadLinkedRoles(Device device) {
        // Clean Info
        linkedRoleListProperty().clear();

        retrieveLinkedRoles(device);
    }

    public void retrieveLinkedDevices(Device device) {
        disposables.add(retrieveLinkedDevicesUseCase.execute(device)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> retrieveLinkedDevicesResponse.setValue(Response.loading()))
                .subscribe(
                        linkedDevices -> retrieveLinkedDevicesResponse.setValue(Response.success(linkedDevices)),
                        throwable -> retrieveLinkedDevicesResponse.setValue(Response.error(throwable))
                )
        );
    }

    public void retrieveLinkedRoles(Device device) {
        Single<List<String>> useCase;

        if (device.getDeviceRole().equals(DeviceRole.CLIENT)) {
            useCase = retrieveLinkedRolesForClientUseCase.execute(device);
        } else {
            useCase = retrieveLinkedRolesForServerUseCase.execute(device);
        }

        disposables.add(useCase
            .subscribeOn(schedulersFacade.io())
            .observeOn(schedulersFacade.ui())
            .doOnSubscribe(__ -> retrieveLinkedRolesResponse.setValue(Response.loading()))
            .subscribe(
                linkedRoles -> retrieveLinkedRolesResponse.setValue(Response.success(linkedRoles)),
                throwable -> retrieveLinkedRolesResponse.setValue(Response.error(throwable))
            ));
    }

    public void setLinkedDevices(List<String> linkedDevices) {
        List<String> links = linkedDevicesListProperty().get();
        if (links == null) {
            links = new ArrayList<>();
        }

        for (String link : linkedDevices) {
            links.add(link);
        }

        links.sort(Comparator.naturalOrder());
        this.linkedDevicesListProperty().setValue(FXCollections.observableArrayList(links));
    }

    public void linkDevices(String deviceId) {
        Device client;
        Device server;

        if (deviceProperty().get().size() == 1) {
            if (deviceProperty().get().get(0).getDeviceRole().equals(DeviceRole.SERVER)) {
                server = deviceProperty().get().get(0);
                client = devicesList.get().filtered(d -> d.getDeviceId().equals(deviceId)).get(0);
            } else {
                client = deviceProperty().get().get(0);
                server = devicesList.get().filtered(d -> d.getDeviceId().equals(deviceId)).get(0);
            }

            disposables.add(linkDevicesUseCase.execute(client, server)
                    .subscribeOn(schedulersFacade.io())
                    .observeOn(schedulersFacade.ui())
                    .subscribe(
                            ()-> {
                                LOG.debug("LinkDevices has been completed");
                                loadLinkedDevices(deviceProperty().get().get(0));
                            },
                            throwable -> LOG.error("LinkDevices has failed")
                    ));
        }
    }

    public void setLinkedRoles(List<String> linkedRoles) {
        List<String> links = linkedRoleListProperty().get();
        if (links == null) {
            links = new ArrayList<>();
        }

        for (String link : linkedRoles) {
            links.add(link);
        }

        links.sort(Comparator.naturalOrder());
        this.linkedRoleListProperty().setValue(FXCollections.observableArrayList(links));
    }

    public void linkRoleCertificate(String roleId, String roleAuthority) {
        Completable useCase;

        if (deviceProperty().get().size() == 1) {
            if (deviceProperty().get().get(0).getDeviceRole().equals(DeviceRole.CLIENT)) {
                useCase = linkRoleForClientUseCase.execute(deviceProperty().get().get(0), roleId, roleAuthority);
            } else {
                useCase = linkRoleForServerUseCase.execute(deviceProperty().get().get(0), roleId, roleAuthority);
            }

            disposables.add(useCase
                    .subscribeOn(schedulersFacade.io())
                    .observeOn(schedulersFacade.ui())
                    .subscribe(
                            ()-> {
                                LOG.debug("LinkRole has been completed");
                                loadLinkedRoles(deviceProperty().get().get(0));
                            },
                            throwable -> LOG.error("LinkRole has failed")
                    ));
        }

    }

    public void unlinkDevices(String serverId) {
        Device server = devicesList.get().filtered(d -> d.getDeviceId().equals(serverId)).get(0);

        if (deviceProperty().get().size() == 1) {
            disposables.add(unlinkDevicesUseCase.execute(deviceProperty().get().get(0), server)
                    .subscribeOn(schedulersFacade.io())
                    .observeOn(schedulersFacade.ui())
                    .subscribe(
                            ()-> {
                                LOG.debug("UnlinkDevices has been completed");
                                loadLinkedDevices(deviceProperty().get().get(0));
                            },
                            throwable -> LOG.error("UnlinkDevices has failed")
                    ));
        }
    }

    public void unlinkRole(String roleId) {
        Completable useCase;

        if (deviceProperty().get().size() == 1) {
            if (deviceProperty().get().get(0).getDeviceRole().equals(DeviceRole.CLIENT)) {
                useCase = unlinkRoleForClientUseCase.execute(deviceProperty().get().get(0), roleId);
            } else {
                useCase = unlinkRoleForServerUseCase.execute(deviceProperty().get().get(0), roleId);
            }

            disposables.add(useCase
                    .subscribeOn(schedulersFacade.io())
                    .observeOn(schedulersFacade.ui())
                    .subscribe(
                            ()-> {
                                LOG.debug("UnlinkRole has been completed");
                                loadLinkedRoles(deviceProperty().get().get(0));
                            },
                            throwable -> LOG.error("UnlinkRole has failed")
                    ));
        }

    }
}
