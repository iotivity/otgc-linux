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
import de.saxsys.mvvmfx.ScopeProvider;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import io.reactivex.disposables.CompositeDisposable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import org.apache.log4j.Logger;
import org.openconnectivity.otgc.domain.usecase.accesscontrol.CreateAclUseCase;
import org.openconnectivity.otgc.domain.usecase.cloud.RegisterDeviceCloudUseCase;
import org.openconnectivity.otgc.utils.constant.NotificationKey;
import org.openconnectivity.otgc.domain.model.devicelist.Device;
import org.openconnectivity.otgc.domain.model.devicelist.DeviceType;
import org.openconnectivity.otgc.utils.constant.OcfOxmType;
import org.openconnectivity.otgc.utils.constant.OtgcMode;
import org.openconnectivity.otgc.utils.rx.SchedulersFacade;
import org.openconnectivity.otgc.utils.viewmodel.Response;
import org.openconnectivity.otgc.domain.usecase.*;
import org.openconnectivity.otgc.utils.scopes.DeviceListToolbarDetailScope;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

@ScopeProvider(scopes = DeviceListToolbarDetailScope.class)
public class ToolbarViewModel implements ViewModel {

    private final Logger LOG = Logger.getLogger(ToolbarViewModel.class);

    public ObjectProperty<List<Device>> deviceProperty;
    private IntegerProperty positionDevice;
    public IntegerProperty positionDeviceProperty() {
        return this.positionDevice;
    }

    @InjectScope
    private DeviceListToolbarDetailScope deviceListToolbarDetailScope;

    private final CompositeDisposable disposables = new CompositeDisposable();

    private final SchedulersFacade schedulersFacade;
    private final GetOTMethodsUseCase getOTMethodsUseCase;
    private final OnboardUseCase onboardUseCase;
    private final OnboardDevicesUseCase onboardDevicesUseCase;
    private final CreateAclUseCase createAclUseCase;
    private final GetDeviceInfoUseCase getDeviceInfoUseCase;
    private final GetDeviceNameUseCase getDeviceNameUseCase;
    private final SetDeviceNameUseCase setDeviceNameUseCase;
    private final SetClientModeUseCase setClientModeUseCase;
    private final ResetClientModeUseCase resetClientModeUseCase;
    private final SetObtModeUseCase setObtModeUseCase;
    private final ResetObtModeUseCase resetObtModeUseCase;
    private final GetModeUseCase getModeUseCase;
    private final GetDeviceIdUseCase getDeviceIdUseCase;
    private final OffboardDeviceUseCase offboardDeviceUseCase;
    private final GetDeviceRoleUseCase getDeviceRoleUseCase;
    private final RegisterDeviceCloudUseCase registerDeviceCloudUseCase;

    // Observable responses
    private final ObjectProperty<Response<Device>> otmResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Device>> deviceInfoResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Device>> deviceRoleResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Device>> provisionAceOtmResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Device>> offboardResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Void>> clientModeResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Void>> obtModeResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Void>> registerDeviceCloudResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<String>> modeProperty = new SimpleObjectProperty<>();

    // Onboard selected devices responses
    private final ObjectProperty<Response<Boolean>> onboardWaiting = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Device>> otmMultiResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Device>> deviceInfoMultiResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Device>> deviceRoleMultiResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Device>> provisionAceOtmMultiResponse = new SimpleObjectProperty<>();

    private SelectOxMListener oxmListener;
    private SelectAccessTokenListener accessTokenListener;

    @Inject
    private NotificationCenter notificationCenter;

    public void initialize() {
        deviceProperty = deviceListToolbarDetailScope.selectedDeviceProperty();
        positionDevice = deviceListToolbarDetailScope.positionSelectedDeviceProperty();
    }

    @Inject
    public ToolbarViewModel(SchedulersFacade schedulersFacade,
                            GetOTMethodsUseCase getOTMethodsUseCase,
                            OnboardUseCase onboardUseCase,
                            OnboardDevicesUseCase onboardDevicesUseCase,
                            CreateAclUseCase createAclUseCase,
                            GetDeviceInfoUseCase getDeviceInfoUseCase,
                            GetDeviceNameUseCase getDeviceNameUseCase,
                            SetDeviceNameUseCase setDeviceNameUseCase,
                            SetClientModeUseCase setClientModeUseCase,
                            ResetClientModeUseCase resetClientModeUseCase,
                            SetObtModeUseCase setObtModeUseCase,
                            ResetObtModeUseCase resetObtModeUseCase,
                            GetModeUseCase getModeUseCase,
                            GetDeviceIdUseCase getDeviceIdUseCase,
                            OffboardDeviceUseCase offboardDeviceUseCase,
                            GetDeviceRoleUseCase getDeviceRoleUseCase,
                            RegisterDeviceCloudUseCase registerDeviceCloudUseCase) {
        this.schedulersFacade = schedulersFacade;
        this.getOTMethodsUseCase = getOTMethodsUseCase;
        this.onboardUseCase = onboardUseCase;
        this.onboardDevicesUseCase = onboardDevicesUseCase;
        this.createAclUseCase = createAclUseCase;
        this.getDeviceInfoUseCase = getDeviceInfoUseCase;
        this.getDeviceNameUseCase = getDeviceNameUseCase;
        this.setDeviceNameUseCase = setDeviceNameUseCase;
        this.setClientModeUseCase = setClientModeUseCase;
        this.resetClientModeUseCase = resetClientModeUseCase;
        this.setObtModeUseCase = setObtModeUseCase;
        this.resetObtModeUseCase = resetObtModeUseCase;
        this.getModeUseCase = getModeUseCase;
        this.getDeviceIdUseCase = getDeviceIdUseCase;
        this.offboardDeviceUseCase = offboardDeviceUseCase;
        this.getDeviceRoleUseCase = getDeviceRoleUseCase;
        this.registerDeviceCloudUseCase = registerDeviceCloudUseCase;
    }

    public ObservableBooleanValue onboardButtonDisabled() {
        return Bindings.createBooleanBinding(() -> {
            boolean disabled = false;
            if (deviceProperty.get() == null || deviceProperty.get().isEmpty()) {
                disabled = true;
            } else {
                for (Device device : deviceProperty.get()) {
                    if (device.getDeviceType() != DeviceType.UNOWNED) {
                        disabled = true;
                    }
                }
            }

            return disabled;
        }, deviceProperty);
    }

    public ObservableBooleanValue offboardButtonDisabled() {
        return Bindings.createBooleanBinding(() -> deviceProperty.get() == null || deviceProperty.get().isEmpty()
                    || (deviceProperty.get().size() == 1 && deviceProperty.get().get(0).getDeviceType() != DeviceType.OWNED_BY_SELF)
                    || deviceProperty.get().size() > 1, deviceProperty);
    }

    public void setOxmListener(SelectOxMListener listener) {
        this.oxmListener = listener;
    }

    public void setAccessTokenListener(SelectAccessTokenListener listener) {
        this.accessTokenListener = listener;
    }

    public ObjectProperty<Response<Device>> otmResponseProperty() {
        return otmResponse;
    }

    public ObjectProperty<Response<Device>> deviceInfoProperty() {
        return deviceInfoResponse;
    }

    public ObjectProperty<Response<Device>> deviceRoleProperty() {
        return deviceRoleResponse;
    }

    public ObjectProperty<Response<Device>> provisionAceOtmProperty() {
        return provisionAceOtmResponse;
    }

    public ObjectProperty<Response<Device>> offboardResponseProperty() {
        return offboardResponse;
    }

    public ObjectProperty<Response<Void>> clientModeResponseProperty() {
        return clientModeResponse;
    }

    public ObjectProperty<Response<Void>> obtModeResponseProperty() {
        return obtModeResponse;
    }

    public ObjectProperty<Response<String>> modeResponseProperty() {
        return modeProperty;
    }

    public ObjectProperty<Response<Boolean>> onboardWaitingProperty() {
        return onboardWaiting;
    }

    public ObjectProperty<Response<Device>> otmMultiResponseProperty() {
        return otmMultiResponse;
    }

    public ObjectProperty<Response<Device>> deviceInfoMultiProperty() {
        return deviceInfoMultiResponse;
    }

    public ObjectProperty<Response<Device>> deviceRoleMultiProperty() {
        return deviceRoleMultiResponse;
    }

    public ObjectProperty<Response<Device>> provisionAceOtmMultiProperty() {
        return provisionAceOtmMultiResponse;
    }

    public void doOwnershipTransfer(Device deviceToOnboard) {
        disposables.add(getModeUseCase.execute()
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        mode -> {
                            if (mode.equals(OtgcMode.OBT)) {
                                getOTMethodsUseCase.execute(deviceToOnboard)
                                        .map(oxms -> {
                                            if (oxms.size() > 1) {
                                                return oxmListener.onGetOxM(oxms);
                                            } else {
                                                return oxms.get(0);
                                            }
                                        }).filter(oxm -> oxm != null)
                                        .subscribeOn(schedulersFacade.io())
                                        .observeOn(schedulersFacade.ui())
                                        .subscribe(
                                                oxm -> onboardUseCase.execute(deviceToOnboard, oxm)
                                                        .subscribeOn(schedulersFacade.io())
                                                        .observeOn(schedulersFacade.ui())
                                                        .doOnSubscribe(__ -> otmResponse.setValue(Response.loading()))
                                                        .subscribe(
                                                                ownedDevice -> getDeviceInfoUseCase.execute(ownedDevice)
                                                                        .subscribeOn(schedulersFacade.io())
                                                                        .observeOn(schedulersFacade.ui())
                                                                        .subscribe(
                                                                                deviceInfo -> {
                                                                                    ownedDevice.setDeviceInfo(deviceInfo);
                                                                                    getDeviceRoleUseCase.execute(ownedDevice)
                                                                                            .subscribeOn(schedulersFacade.io())
                                                                                            .observeOn(schedulersFacade.ui())
                                                                                            .subscribe(
                                                                                                    deviceRole -> {
                                                                                                        ownedDevice.setDeviceRole(deviceRole);
                                                                                                        deviceRoleResponse.setValue(Response.success(ownedDevice));
                                                                                                        String deviceId = getDeviceIdUseCase.execute().blockingGet();
                                                                                                        createAclUseCase.execute(ownedDevice, deviceId, Arrays.asList("*"), 6)
                                                                                                                .subscribeOn(schedulersFacade.io())
                                                                                                                .observeOn(schedulersFacade.ui())
                                                                                                                .subscribe(
                                                                                                                        () -> {},
                                                                                                                        throwable -> provisionAceOtmResponse.setValue(Response.error(throwable))
                                                                                                                );
                                                                                                    },
                                                                                                    throwable -> deviceRoleResponse.setValue(Response.error(throwable))
                                                                                            );
                                                                                },
                                                                                throwable -> deviceInfoResponse.setValue(Response.error(throwable))
                                                                        ),
                                                                throwable -> otmResponse.setValue(Response.error(throwable))
                                                        ),
                                                throwable -> otmResponse.setValue(Response.error(throwable))
                                        );
                            } else {
                                otmResponse.setValue(Response.success(null));
                            }
                        },
                        throwable -> otmResponse.setValue(Response.error(throwable))
                ));
    }

    public void onScanPressed() {
        deviceListToolbarDetailScope.publish(NotificationKey.SCAN_DEVICES);
    }

    public void updateItem(int positionToUpdate, Device deviceToUpdate) {
        deviceListToolbarDetailScope.publish(NotificationKey.UPDATE_DEVICE, positionToUpdate, deviceToUpdate);
    }

    public void offboard(Device deviceToOffboard) {
        disposables.add(getModeUseCase.execute()
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        mode -> {
                            if (mode.equals(OtgcMode.OBT)) {
                                offboardDeviceUseCase.execute(deviceToOffboard)
                                        .subscribeOn(schedulersFacade.io())
                                        .observeOn(schedulersFacade.ui())
                                        .doOnSubscribe(__ -> offboardResponse.setValue(Response.loading()))
                                        .subscribe(
                                                unownedDevice -> getDeviceInfoUseCase.execute(unownedDevice)
                                                        .subscribeOn(schedulersFacade.io())
                                                        .observeOn(schedulersFacade.ui())
                                                        .subscribe(
                                                                deviceInfo -> {
                                                                    unownedDevice.setDeviceInfo(deviceInfo);
                                                                    getDeviceRoleUseCase.execute(unownedDevice)
                                                                            .subscribeOn(schedulersFacade.io())
                                                                            .observeOn(schedulersFacade.ui())
                                                                            .subscribe(
                                                                                    deviceRole -> {
                                                                                        unownedDevice.setDeviceRole(deviceRole);
                                                                                        deviceRoleResponse.setValue(Response.success(unownedDevice));
                                                                                    },
                                                                                    throwable -> deviceRoleResponse.setValue(Response.error(throwable))
                                                                            );
                                                                },
                                                                throwable -> deviceInfoResponse.setValue(Response.error(throwable))
                                                        ),
                                                throwable -> offboardResponse.setValue(Response.error(throwable))
                                        );
                            } else {
                                offboardResponse.setValue(Response.success(null));
                            }
                        },
                        throwable -> offboardResponse.setValue(Response.error(throwable))

                ));
    }

    public void registerDeviceCloud(Device deviceToRegister) {
        disposables.add(getModeUseCase.execute()
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        mode -> {
                            if (mode.equals(OtgcMode.OBT)) {

                                getModeUseCase.execute()
                                        .map(oxms -> {
                                            return accessTokenListener.onGetAccessToken();
                                        }).filter(accessToken -> accessToken != null)
                                        .subscribeOn(schedulersFacade.io())
                                        .observeOn(schedulersFacade.ui())
                                        .subscribe(
                                                accessToken -> registerDeviceCloudUseCase.execute(deviceToRegister, accessToken)


                                        .subscribeOn(schedulersFacade.io())
                                        .observeOn(schedulersFacade.ui())
                                        .subscribe(
                                                () -> {
                                                    notificationCenter.publish(NotificationKey.SCAN_DEVICES);
                                                },
                                                throwable -> registerDeviceCloudResponse.setValue(Response.error(throwable))
                                        ));
                            } else {
                                registerDeviceCloudResponse.setValue(Response.success(null));
                            }
                        },
                        throwable -> registerDeviceCloudResponse.setValue(Response.error(throwable))
                ));
    }

    public void setDeviceName(String deviceId, String deviceName) {
        disposables.add(setDeviceNameUseCase.execute(deviceId, deviceName)
                .andThen(getDeviceNameUseCase.execute(deviceId))
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        name -> {},
                        throwable -> {}
                ));
    }

    public void setClientMode() {
        disposables.add(setClientModeUseCase.execute()
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> clientModeResponse.setValue(Response.loading()))
                .subscribe(
                        () -> {
                            clientModeResponse.setValue(Response.success(null));
                            getModeUseCase.execute()
                                    .subscribeOn(schedulersFacade.io())
                                    .observeOn(schedulersFacade.ui())
                                    .subscribe(
                                            mode -> modeProperty.setValue(Response.success(mode)),
                                            throwable -> {}
                                    );
                        },
                        throwable -> clientModeResponse.setValue(Response.error(throwable))
                ));
    }

    public void resetClientMode() {
        disposables.add(resetClientModeUseCase.execute()
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> clientModeResponse.setValue(Response.loading()))
                .subscribe(
                        () -> {
                            clientModeResponse.setValue(Response.success(null));
                            getModeUseCase.execute()
                                    .subscribeOn(schedulersFacade.io())
                                    .observeOn(schedulersFacade.ui())
                                    .subscribe(
                                            mode -> modeProperty.setValue(Response.success(mode)),
                                            throwable -> {}
                                    );
                        },
                        throwable -> clientModeResponse.setValue(Response.error(throwable))
                ));
    }

    public void setObtMode() {
        disposables.add(setObtModeUseCase.execute()
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> obtModeResponse.setValue(Response.loading()))
                .subscribe(
                        () -> {
                            obtModeResponse.setValue(Response.success(null));
                            getModeUseCase.execute()
                                    .subscribeOn(schedulersFacade.io())
                                    .observeOn(schedulersFacade.ui())
                                    .subscribe(
                                            mode -> modeProperty.setValue(Response.success(mode)),
                                            throwable -> {}
                                    );
                        },
                        throwable -> obtModeResponse.setValue(Response.error(throwable))
                )
        );
    }

    public void resetObtMode() {
        disposables.add(resetObtModeUseCase.execute()
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> obtModeResponse.setValue(Response.loading()))
                .subscribe(
                        () -> {
                            obtModeResponse.setValue(Response.success(null));
                            getModeUseCase.execute()
                                    .subscribeOn(schedulersFacade.io())
                                    .observeOn(schedulersFacade.ui())
                                    .subscribe(
                                            mode -> modeProperty.setValue(Response.success(mode)),
                                            throwable -> {}
                                    );
                        },
                        throwable -> obtModeResponse.setValue(Response.error(throwable))
                )
        );
    }

    public void getMode() {
        disposables.add(getModeUseCase.execute()
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        mode ->  {
                            System.out.println("Get Mode");
                            modeProperty.setValue(Response.success(mode));
                        },
                        throwable -> {}
                ));
    }

    public void onboardAllDevices(List<Device> devices) {
        disposables.add(getModeUseCase.execute()
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        mode -> {
                            if (mode.equals(OtgcMode.OBT)) {
                                int countOnboards = devices.size();
                                if (countOnboards > 0) {
                                    onboardWaiting.setValue(Response.success(true));

                                    final Device device = devices.get(0);
                                    disposables.add(
                                            getOTMethodsUseCase.execute(device)
                                                    .filter(oxms -> oxms != null)
                                                    .subscribeOn(schedulersFacade.io())
                                                    .observeOn(schedulersFacade.ui())
                                                    .subscribe(
                                                            oxms -> {
                                                                onboardDevicesUseCase.execute(device, oxms)
                                                                        .subscribeOn(schedulersFacade.io())
                                                                        .observeOn(schedulersFacade.ui())
                                                                        .subscribe(
                                                                                ownedDevice -> getDeviceInfoUseCase.execute(ownedDevice)
                                                                                        .subscribeOn(schedulersFacade.io())
                                                                                        .observeOn(schedulersFacade.ui())
                                                                                        .subscribe(
                                                                                                deviceInfo -> {
                                                                                                    ownedDevice.setDeviceInfo(deviceInfo);
                                                                                                    getDeviceRoleUseCase.execute(ownedDevice)
                                                                                                            .subscribeOn(schedulersFacade.io())
                                                                                                            .observeOn(schedulersFacade.ui())
                                                                                                            .subscribe(
                                                                                                                    deviceRole -> {
                                                                                                                        ownedDevice.setDeviceRole(deviceRole);
                                                                                                                        String deviceName;
                                                                                                                        if (ownedDevice.getDeviceInfo().getName() == null || ownedDevice.getDeviceInfo().getName().isEmpty()) {
                                                                                                                            deviceName = ownedDevice.getDeviceRole().toString() + "_" + ownedDevice.getDeviceId().substring(0, 5);
                                                                                                                        } else {
                                                                                                                            deviceName = ownedDevice.getDeviceInfo().getName() + "_" + ownedDevice.getDeviceId().substring(0, 5);
                                                                                                                        }
                                                                                                                        ownedDevice.getDeviceInfo().setName(deviceName);
                                                                                                                        setDeviceName(ownedDevice.getDeviceId(), deviceName);
                                                                                                                        deviceRoleMultiResponse.setValue(Response.success(ownedDevice));
                                                                                                                        String deviceId = getDeviceIdUseCase.execute().blockingGet();
                                                                                                                        createAclUseCase.execute(ownedDevice, deviceId, Arrays.asList("*"), 6)
                                                                                                                                .subscribeOn(schedulersFacade.io())
                                                                                                                                .observeOn(schedulersFacade.ui())
                                                                                                                                .subscribe(
                                                                                                                                        () -> {
                                                                                                                                            provisionAceOtmMultiResponse.setValue(Response.success(ownedDevice));

                                                                                                                                            devices.remove(device);
                                                                                                                                            onboardAllDevices(devices);
                                                                                                                                        },
                                                                                                                                        throwable -> {
                                                                                                                                            devices.remove(device);
                                                                                                                                            onboardAllDevices(devices);

                                                                                                                                            provisionAceOtmMultiResponse.setValue(Response.error(throwable));
                                                                                                                                        }
                                                                                                                                );
                                                                                                                    },
                                                                                                                    throwable -> {
                                                                                                                        devices.remove(device);
                                                                                                                        onboardAllDevices(devices);

                                                                                                                        deviceRoleMultiResponse.setValue(Response.error(throwable));
                                                                                                                    }
                                                                                                            );
                                                                                                },
                                                                                                throwable -> {
                                                                                                    devices.remove(device);
                                                                                                    onboardAllDevices(devices);

                                                                                                    deviceInfoMultiResponse.setValue(Response.error(throwable));
                                                                                                }
                                                                                        ),
                                                                                throwable -> {
                                                                                    devices.remove(device);
                                                                                    onboardAllDevices(devices);

                                                                                    otmMultiResponse.setValue(Response.error(throwable));
                                                                                }
                                                                        );
                                                            },
                                                            throwable -> {
                                                                devices.remove(device);
                                                                onboardAllDevices(devices);

                                                                otmMultiResponse.setValue(Response.error(throwable));
                                                            }
                                                    )
                                    );
                                } else {
                                    onboardWaiting.setValue(Response.success(false));
                                }
                            } else {
                                otmResponse.setValue(Response.error(new Exception()));
                            }
                        },
                        throwable -> otmResponse.setValue(Response.error(throwable))
                ));
    }

    public interface SelectOxMListener {
        OcfOxmType onGetOxM(List<OcfOxmType> supportedOxm);
    }

    public interface SelectAccessTokenListener {
        String onGetAccessToken();
    }
}

