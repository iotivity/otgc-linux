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

import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import org.openconnectivity.otgc.domain.usecase.*;
import org.openconnectivity.otgc.domain.usecase.cloud.CloudDiscoverDevicesUseCase;
import org.openconnectivity.otgc.domain.usecase.cloud.CloudRetrieveDeviceInfoUseCase;
import org.openconnectivity.otgc.domain.usecase.cloud.CloudRetrieveDeviceRoleUseCase;
import org.openconnectivity.otgc.utils.constant.NotificationKey;
import org.openconnectivity.otgc.domain.model.devicelist.Device;
import org.openconnectivity.otgc.utils.rx.SchedulersFacade;
import org.openconnectivity.otgc.utils.viewmodel.Response;
import org.openconnectivity.otgc.domain.model.devicelist.DeviceType;
import org.openconnectivity.otgc.utils.scopes.DeviceListToolbarDetailScope;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Singleton
public class DeviceListViewModel implements ViewModel {

    private final SchedulersFacade schedulersFacade;

    private final ScanDevicesUseCase scanDevicesUseCase;
    private final GetDeviceInfoUseCase getDeviceInfoUseCase;
    private final GetDeviceNameUseCase getDeviceNameUseCase;
    private final GetDeviceRoleUseCase getDeviceRoleUseCase;
    private final GetDeviceDatabaseUseCase getDeviceDatabaseUseCase;
    private final CloudDiscoverDevicesUseCase cloudDiscoverDevicesUseCase;
    private final CloudRetrieveDeviceInfoUseCase cloudRetrieveDeviceInfoUseCase;
    private final CloudRetrieveDeviceRoleUseCase cloudRetrieveDeviceRoleUseCase;

    private final CompositeDisposable disposables = new CompositeDisposable();

    // Observable responses
    private final ObjectProperty<Response<Device>> updateDeviceResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Device>> scanResponse = new SimpleObjectProperty<>();

    private ListProperty<Device> devicesList = new SimpleListProperty<>();
    public ListProperty<Device> devicesListProperty() { return devicesList; }

    private final ObjectProperty<List<Device>> selectedDevice = new SimpleObjectProperty<>();
    public final ObjectProperty<List<Device>> selectedDeviceProperty() {
        return selectedDevice;
    }
    private final IntegerProperty positionSelectedDevice = new SimpleIntegerProperty();
    public final IntegerProperty positionSelectedDeviceProperty() {
        return this.positionSelectedDevice;
    }

    @InjectScope
    private DeviceListToolbarDetailScope deviceListToolbarDetailScope;

    @Inject
    private NotificationCenter notificationCenter;


    public void initialize() {
        deviceListToolbarDetailScope.selectedDeviceProperty().bind(selectedDeviceProperty());
        deviceListToolbarDetailScope.positionSelectedDeviceProperty().bind(positionSelectedDeviceProperty());
        deviceListToolbarDetailScope.devicesListProperty().bind(devicesList);
        // Notification subscribe
        deviceListToolbarDetailScope.subscribe(NotificationKey.SCAN_DEVICES,
                ((key, payload) -> {
                    notificationCenter.publish(NotificationKey.REFRESH_ID);
                    onDiscoverRequest();
                })
        );
        notificationCenter.subscribe(NotificationKey.SCAN_DEVICES,
                ((key, payload) -> {
                    notificationCenter.publish(NotificationKey.REFRESH_ID);
                    onDiscoverRequest();
                })
        );
        deviceListToolbarDetailScope.subscribe(NotificationKey.UPDATE_DEVICE, (key, payload) -> updateItem((int) payload[0], (Device) payload[1]));
        deviceListToolbarDetailScope.subscribe(NotificationKey.UPDATE_DEVICE_TYPE, (key, payload) -> updateDevice((Device)payload[0]));
    }

    @Inject
    public DeviceListViewModel(SchedulersFacade schedulersFacade,
                               ScanDevicesUseCase scanDevicesUseCase,
                               GetDeviceInfoUseCase getDeviceInfoUseCase,
                               GetDeviceNameUseCase getDeviceNameUseCase,
                               GetDeviceRoleUseCase getDeviceRoleUseCase,
                               GetDeviceDatabaseUseCase getDeviceDatabaseUseCase,
                               CloudDiscoverDevicesUseCase cloudDiscoverDevicesUseCase,
                               CloudRetrieveDeviceInfoUseCase cloudRetrieveDeviceInfoUseCase,
                               CloudRetrieveDeviceRoleUseCase cloudRetrieveDeviceRoleUseCase) {
        this.schedulersFacade = schedulersFacade;
        this.scanDevicesUseCase = scanDevicesUseCase;
        this.getDeviceInfoUseCase = getDeviceInfoUseCase;
        this.getDeviceNameUseCase = getDeviceNameUseCase;
        this.getDeviceRoleUseCase = getDeviceRoleUseCase;
        this.getDeviceDatabaseUseCase = getDeviceDatabaseUseCase;
        this.cloudDiscoverDevicesUseCase = cloudDiscoverDevicesUseCase;
        this.cloudRetrieveDeviceInfoUseCase = cloudRetrieveDeviceInfoUseCase;
        this.cloudRetrieveDeviceRoleUseCase = cloudRetrieveDeviceRoleUseCase;
    }

    public ObjectProperty<Response<Device>> scanResponseProperty() {
        return scanResponse;
    }

    public ObjectProperty<Response<Device>> updateDeviceResponseProperty() {
        return updateDeviceResponse;
    }

    public List<Device> getDevicesList() {
        return devicesList.get();
    }

    public void setDevicesList(List<Device> devices) {
        this.devicesList.set(FXCollections.observableArrayList(devices));
    }

    public void addDeviceToList(Device device) {
        List<Device> tmp;
        if (getDevicesList() == null) {
            tmp = new ArrayList<>();
        } else {
            tmp = getDevicesList();
        }

        tmp.add(device);
        tmp.sort(Comparator.naturalOrder());
        setDevicesList(tmp);
    }

    public void onDiscoverRequest() {
        devicesList.clear();

        Observable<Device> localScan = scanDevicesUseCase.execute()
                .map(device -> {
                    device.setDeviceInfo(getDeviceInfoUseCase.execute(device).blockingGet());
                    return device;
                })
                .map(device -> {
                    device.setDeviceRole(getDeviceRoleUseCase.execute(device).blockingGet());
                    return device;
                })
                .map(device -> {
                    if (device.getDeviceType().equals(DeviceType.OWNED_BY_SELF)) {
                        String storedDeviceName = getDeviceNameUseCase.execute(device.getDeviceId()).blockingGet();
                        if (storedDeviceName != null && !storedDeviceName.isEmpty()) {
                            device.getDeviceInfo().setName(storedDeviceName);
                        }
                    }
                    return device;
                });

        Observable<Device> cloudScan = cloudDiscoverDevicesUseCase.execute()
                .delay(5, TimeUnit.SECONDS)
                .map(device -> {
                    device.setDeviceInfo(cloudRetrieveDeviceInfoUseCase.execute(device).blockingGet());
                    return device;
                })
                .map(device -> {
                    device.setDeviceRole(cloudRetrieveDeviceRoleUseCase.execute(device).blockingGet());
                    return device;
                });

        disposables.add(Observable.concat(localScan, cloudScan)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> scanResponse.setValue(Response.loading()))
                .doOnComplete(() -> scanResponse.setValue(Response.complete()))
                .subscribe(
                        device -> scanResponse.setValue(Response.success(device)),
                        throwable -> scanResponse.setValue(Response.error(throwable))
                ));
    }

    public void updateDevice(Device device) {
        disposables.add(getDeviceDatabaseUseCase.execute(device)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        device1 -> updateDeviceResponse.setValue(Response.success(device1)),
                        throwable -> {}
                ));
    }

    private void updateItem(int positionToUpdate, Device deviceToUpdate) {
        devicesListProperty().set(positionToUpdate, deviceToUpdate);
        devicesListProperty().get().sort(Comparator.naturalOrder());
    }
}
