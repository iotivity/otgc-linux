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
import io.reactivex.disposables.CompositeDisposable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import org.apache.log4j.Logger;
import org.openconnectivity.otgc.domain.usecase.accesscontrol.CreateAclUseCase;
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

    public ObjectProperty<Device> deviceProperty;
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
    private final CreateAclUseCase createAclUseCase;
    private final GetDeviceInfoUseCase getDeviceInfoUseCase;
    private final GetDeviceNameUseCase getDeviceNameUseCase;
    private final SetDeviceNameUseCase setDeviceNameUseCase;
    private final SetClientModeUseCase setClientModeUseCase;
    private final SetObtModeUseCase setObtModeUseCase;
    private final GetModeUseCase getModeUseCase;
    private final GetDeviceIdUseCase getDeviceIdUseCase;
    private final OffboardDeviceUseCase offboardDeviceUseCase;
    private final GetDeviceRoleUseCase getDeviceRoleUseCase;

    // Observable responses
    private final ObjectProperty<Response<Device>> otmResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Device>> offboardResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Void>> clientModeResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Void>> obtModeResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<String>> modeProperty = new SimpleObjectProperty<>();

    private SelectOxMListener oxmListener;

    public void initialize() {
        deviceProperty = deviceListToolbarDetailScope.selectedDeviceProperty();
        positionDevice = deviceListToolbarDetailScope.positionSelectedDeviceProperty();
    }

    @Inject
    public ToolbarViewModel(SchedulersFacade schedulersFacade,
                    GetOTMethodsUseCase getOTMethodsUseCase,
                    OnboardUseCase onboardUseCase,
                    CreateAclUseCase createAclUseCase,
                    GetDeviceInfoUseCase getDeviceInfoUseCase,
                    GetDeviceNameUseCase getDeviceNameUseCase,
                    SetDeviceNameUseCase setDeviceNameUseCase,
                    SetClientModeUseCase setClientModeUseCase,
                    SetObtModeUseCase setObtModeUseCase,
                    GetModeUseCase getModeUseCase,
                    GetDeviceIdUseCase getDeviceIdUseCase,
                    OffboardDeviceUseCase offboardDeviceUseCase,
                    GetDeviceRoleUseCase getDeviceRoleUseCase) {
        this.schedulersFacade = schedulersFacade;
        this.getOTMethodsUseCase = getOTMethodsUseCase;
        this.onboardUseCase = onboardUseCase;
        this.createAclUseCase = createAclUseCase;
        this.getDeviceInfoUseCase = getDeviceInfoUseCase;
        this.getDeviceNameUseCase = getDeviceNameUseCase;
        this.setDeviceNameUseCase = setDeviceNameUseCase;
        this.setClientModeUseCase = setClientModeUseCase;
        this.setObtModeUseCase = setObtModeUseCase;
        this.getModeUseCase = getModeUseCase;
        this.getDeviceIdUseCase = getDeviceIdUseCase;
        this.offboardDeviceUseCase = offboardDeviceUseCase;
        this.getDeviceRoleUseCase = getDeviceRoleUseCase;
    }

    public ObservableBooleanValue onboardButtonDisabled() {
        return Bindings.createBooleanBinding(() -> deviceProperty.get() == null
                        || deviceProperty.get().getDeviceType() != DeviceType.UNOWNED, deviceProperty);
    }

    public ObservableBooleanValue offboardButtonDisabled() {
        return Bindings.createBooleanBinding(() -> deviceProperty.get() == null
                || deviceProperty.get().getDeviceType() != DeviceType.OWNED_BY_SELF, deviceProperty);
    }

    public void setOxmListener(SelectOxMListener listener) {
        this.oxmListener = listener;
    }

    public ObjectProperty<Response<Device>> otmResponseProperty() {
        return otmResponse;
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
                                        .doOnSubscribe(__ -> otmResponse.setValue(Response.loading()))
                                        .subscribe(
                                                oxm -> onboardUseCase.execute(deviceToOnboard, oxm)
                                                        .map(device -> {
                                                            device.setDeviceInfo(getDeviceInfoUseCase.execute(device).blockingGet());
                                                            return device;
                                                        })
                                                        .map(device -> {
                                                            device.setDeviceRole(getDeviceRoleUseCase.execute(device).blockingGet());
                                                            return device;
                                                        })
                                                        .subscribeOn(schedulersFacade.io())
                                                        .observeOn(schedulersFacade.ui())
                                                        .doOnSubscribe(__ -> otmResponse.setValue(Response.loading()))
                                                        .subscribe(
                                                                ownedDevice -> createAclUseCase.execute(ownedDevice, true, Arrays.asList("*"), 31)
                                                                        .subscribeOn(schedulersFacade.io())
                                                                        .observeOn(schedulersFacade.ui())
                                                                        .subscribe(
                                                                                () -> otmResponse.setValue(Response.success(ownedDevice)),
                                                                                throwable -> otmResponse.setValue(Response.error(throwable))
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
                .doOnSubscribe(__ -> otmResponse.setValue(Response.loading()))
                .subscribe(
                        mode -> {
                            if (mode.equals(OtgcMode.OBT)) {
                                offboardDeviceUseCase.execute(deviceToOffboard)
                                        .map(device -> {
                                            device.setDeviceInfo(getDeviceInfoUseCase.execute(device).blockingGet());
                                            return device;
                                        })
                                        .map(device -> {
                                            device.setDeviceRole(getDeviceRoleUseCase.execute(device).blockingGet());
                                            return device;
                                        })
                                        .subscribeOn(schedulersFacade.io())
                                        .observeOn(schedulersFacade.ui())
                                        .doOnSubscribe(__ -> offboardResponse.setValue(Response.loading()))
                                        .subscribe(
                                                unownedDevice -> offboardResponse.setValue(Response.success(unownedDevice)),
                                                throwable -> offboardResponse.setValue(Response.error(throwable))
                                        );
                            } else {
                                otmResponse.setValue(Response.success(null));
                            }
                        },
                        throwable -> otmResponse.setValue(Response.error(throwable))
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

    public interface SelectOxMListener {
        OcfOxmType onGetOxM(List<OcfOxmType> supportedOxm);
    }
}

