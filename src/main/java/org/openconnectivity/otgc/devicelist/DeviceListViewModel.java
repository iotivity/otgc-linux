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

package org.openconnectivity.otgc.devicelist;

import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import io.reactivex.disposables.CompositeDisposable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import org.openconnectivity.otgc.common.constant.NotificationConstant;
import org.openconnectivity.otgc.common.domain.usecase.GetDeviceInfoUseCase;
import org.openconnectivity.otgc.devicelist.domain.model.Device;
import org.openconnectivity.otgc.common.rx.SchedulersFacade;
import org.openconnectivity.otgc.common.viewmodel.Response;
import org.openconnectivity.otgc.devicelist.domain.model.DeviceType;
import org.openconnectivity.otgc.common.domain.usecase.GetDeviceNameUseCase;
import org.openconnectivity.otgc.common.domain.usecase.GetDeviceRoleUseCase;
import org.openconnectivity.otgc.devicelist.domain.usecase.ScanDevicesUseCase;
import org.openconnectivity.otgc.scopes.DeviceListToolbarDetailScope;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Singleton
public class DeviceListViewModel implements ViewModel {

    private final SchedulersFacade schedulersFacade;

    private final ScanDevicesUseCase scanDevicesUseCase;
    private final GetDeviceInfoUseCase getDeviceInfoUseCase;
    private final GetDeviceNameUseCase getDeviceNameUseCase;
    private final GetDeviceRoleUseCase getDeviceRoleUseCase;

    private final CompositeDisposable disposables = new CompositeDisposable();

    // Observable responses
    private final ObjectProperty<Response<Device>> scanResponse = new SimpleObjectProperty<>();

    private ListProperty<Device> devicesList = new SimpleListProperty<>();
    public ListProperty<Device> devicesListProperty() { return devicesList; }

    private final ObjectProperty<Device> selectedDevice = new SimpleObjectProperty<>();
    public final ObjectProperty<Device> selectedDeviceProperty() {
        return selectedDevice;
    }
    private final IntegerProperty positionSelectedDevice = new SimpleIntegerProperty();
    public final IntegerProperty positionSelectedDeviceProperty() {
        return this.positionSelectedDevice;
    }

    @InjectScope
    private DeviceListToolbarDetailScope deviceListToolbarDetailScope;


    public void initialize() {
        deviceListToolbarDetailScope.selectedDeviceProperty().bind(selectedDeviceProperty());
        deviceListToolbarDetailScope.positionSelectedDeviceProperty().bind(positionSelectedDeviceProperty());
        deviceListToolbarDetailScope.devicesListProperty().bind(devicesList);
        // Notification subscribe
        deviceListToolbarDetailScope.subscribe(NotificationConstant.SCAN_DEVICES,
                (key, payload) -> {
                    onPullRequest();
                });
        deviceListToolbarDetailScope.subscribe(NotificationConstant.UPDATE_DEVICE,
                (key, payload) -> {
                    updateItem((int) payload[0], (Device) payload[1]);
                });
    }

    @Inject
    public DeviceListViewModel(SchedulersFacade schedulersFacade,
                               ScanDevicesUseCase scanDevicesUseCase,
                               GetDeviceInfoUseCase getDeviceInfoUseCase,
                               GetDeviceNameUseCase getDeviceNameUseCase,
                               GetDeviceRoleUseCase getDeviceRoleUseCase) {
        this.schedulersFacade = schedulersFacade;
        this.scanDevicesUseCase = scanDevicesUseCase;
        this.getDeviceInfoUseCase = getDeviceInfoUseCase;
        this.getDeviceNameUseCase = getDeviceNameUseCase;
        this.getDeviceRoleUseCase = getDeviceRoleUseCase;
    }

    public ObjectProperty<Response<Device>> scanResponseProperty() {
        return scanResponse;
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

    public void onPullRequest() {
        devicesList.clear();

        disposables.add(scanDevicesUseCase.execute()
            .map(device -> {
                device.setDeviceInfo(getDeviceInfoUseCase.execute(device.getDeviceId()).blockingGet());
                return device;
            })
            .map(device -> {
                device.setRole(getDeviceRoleUseCase.execute(device.getDeviceId()).blockingGet());
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
            })
            .subscribeOn(schedulersFacade.io())
            .observeOn(schedulersFacade.ui())
            .doOnSubscribe(__ -> scanResponse.setValue(Response.loading()))
            .doOnComplete(() -> scanResponse.setValue(Response.complete()))
            .subscribe(
                    device -> scanResponse.setValue(Response.success(device)),
                    throwable -> scanResponse.setValue(Response.error(throwable))
            ));
    }

    private void updateItem(int positionToUpdate, Device deviceToUpdate) {
        devicesListProperty().set(positionToUpdate, deviceToUpdate);
        devicesListProperty().get().sort(Comparator.naturalOrder());
    }
}
