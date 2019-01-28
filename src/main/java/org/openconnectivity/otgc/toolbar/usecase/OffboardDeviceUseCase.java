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

import io.reactivex.Single;
import org.iotivity.base.OcSecureResource;
import org.openconnectivity.otgc.common.data.repository.IotivityRepository;
import org.openconnectivity.otgc.common.data.repository.SettingRepository;
import org.openconnectivity.otgc.common.domain.model.OcDevice;
import org.openconnectivity.otgc.devicelist.domain.model.Device;
import org.openconnectivity.otgc.devicelist.domain.model.DeviceType;
import org.openconnectivity.otgc.toolbar.data.repository.DoxsRepository;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class OffboardDeviceUseCase {

    private final IotivityRepository iotivityRepository;
    private final DoxsRepository doxsRepository;
    private final SettingRepository settingRepository;

    @Inject
    public OffboardDeviceUseCase(IotivityRepository iotivityRepository,
                                 DoxsRepository doxsRepository,
                                 SettingRepository settingRepository) {
        this.iotivityRepository = iotivityRepository;
        this.doxsRepository = doxsRepository;
        this.settingRepository = settingRepository;
    }

    public Single<Device> execute(OcSecureResource deviceToOffboard) {
        final Single<OcSecureResource> getUpdatedOcSecureResource =
                iotivityRepository.scanUnownedDevices()
                    .filter(ocSecureResource -> (ocSecureResource.getDeviceID().equals(deviceToOffboard.getDeviceID())
                                                    || ocSecureResource.getIpAddr().equals(deviceToOffboard.getIpAddr())))
                    .singleOrError();

        return doxsRepository.resetDevice(deviceToOffboard,
                    Integer.parseInt(settingRepository.get(SettingRepository.DISCOVERY_TIMEOUT_KEY,
                                                        SettingRepository.DISCOVERY_TIMEOUT_DEFAULT_VALUE)))
                .delay(1, TimeUnit.SECONDS)
                .andThen(getUpdatedOcSecureResource)
                .onErrorResumeNext(error -> getUpdatedOcSecureResource
                        .retry(2)
                        .onErrorResumeNext(Single.error(error)))
                .map(ocSecureResource -> new Device(DeviceType.UNOWNED,
                                            ocSecureResource.getDeviceID(),
                                            new OcDevice(),
                                            ocSecureResource));
    }
}
