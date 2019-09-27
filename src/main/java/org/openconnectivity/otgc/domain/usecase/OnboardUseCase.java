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

package org.openconnectivity.otgc.domain.usecase;

import io.reactivex.Single;
import org.openconnectivity.otgc.data.repository.IotivityRepository;
import org.openconnectivity.otgc.data.repository.SettingRepository;
import org.openconnectivity.otgc.domain.model.devicelist.Device;
import org.openconnectivity.otgc.data.repository.DoxsRepository;
import org.openconnectivity.otgc.utils.constant.OcfOxmType;
import org.openconnectivity.otgc.utils.rx.SchedulersFacade;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class OnboardUseCase {
    /* Repositories */
    private final IotivityRepository iotivityRepository;
    private final DoxsRepository doxsRepository;
    private final SettingRepository settingRepository;
    /* Scheduler */
    private final SchedulersFacade schedulersFacade;

    @Inject
    public OnboardUseCase(IotivityRepository iotivityRepository,
                          DoxsRepository doxsRepository,
                          SettingRepository settingRepository,
                          SchedulersFacade schedulersFacade) {
        this.iotivityRepository = iotivityRepository;
        this.doxsRepository = doxsRepository;
        this.settingRepository = settingRepository;

        this.schedulersFacade = schedulersFacade;
    }

    public Single<Device> execute(Device deviceToOnboard, OcfOxmType oxm) {
        int delay = Integer.parseInt(settingRepository.get(SettingRepository.REQUESTS_DELAY_KEY, SettingRepository.REQUESTS_DELAY_DEFAULT_VALUE));

        final Single<Device> getUpdatedOcSecureResource = iotivityRepository.scanOwnedDevices()
                .filter(device -> deviceToOnboard.getDeviceId().equals(device.getDeviceId())
                                    || deviceToOnboard.equalsHosts(device))
                .singleOrError();

        return doxsRepository.doOwnershipTransfer(deviceToOnboard.getDeviceId(), oxm)
                .delay(delay, TimeUnit.SECONDS, schedulersFacade.ui())
                .andThen(getUpdatedOcSecureResource
                            .onErrorResumeNext(error -> getUpdatedOcSecureResource
                                    .retry(2)
                                    .onErrorResumeNext(Single.error(error)))
                );
    }
}
