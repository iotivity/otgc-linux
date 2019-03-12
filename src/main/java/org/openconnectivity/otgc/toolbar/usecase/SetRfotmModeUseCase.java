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

package org.openconnectivity.otgc.toolbar.usecase;

import io.reactivex.Completable;
import org.openconnectivity.otgc.common.data.repository.IotivityRepository;
import org.openconnectivity.otgc.common.data.repository.ProvisionRepository;
import org.openconnectivity.otgc.common.data.repository.SettingRepository;
import org.openconnectivity.otgc.devicelist.domain.model.DeviceType;
import org.openconnectivity.otgc.toolbar.data.repository.DoxsRepository;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class SetRfotmModeUseCase {

    private final IotivityRepository iotivityRepository;
    private final ProvisionRepository provisionRepository;
    private final DoxsRepository doxsRepository;
    private final SettingRepository settingRepository;

    @Inject
    public SetRfotmModeUseCase(IotivityRepository iotivityRepository,
                               ProvisionRepository provisionRepository,
                               DoxsRepository doxsRepository,
                               SettingRepository settingRepository) {
        this.iotivityRepository = iotivityRepository;
        this.provisionRepository = provisionRepository;
        this.doxsRepository = doxsRepository;
        this.settingRepository = settingRepository;
    }

    public Completable execute() {
        return iotivityRepository.scanOwnedDevices()
                .filter(device -> device.getDeviceType() == DeviceType.OWNED_BY_SELF)
                .flatMapCompletable(device ->
                        doxsRepository.resetDevice(device.getOcSecureResource(),
                                Integer.parseInt(settingRepository.get(SettingRepository.DISCOVERY_TIMEOUT_KEY, SettingRepository.DISCOVERY_TIMEOUT_DEFAULT_VALUE))))
                .delay(500, TimeUnit.MILLISECONDS)
                .andThen(provisionRepository.resetSvrDb());
    }
}
