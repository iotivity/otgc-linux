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
import org.openconnectivity.otgc.data.repository.SettingRepository;
import org.openconnectivity.otgc.domain.model.resource.virtual.res.OcResource;
import org.openconnectivity.otgc.utils.constant.OcfResourceType;
import org.openconnectivity.otgc.data.repository.IotivityRepository;
import org.openconnectivity.otgc.domain.model.devicelist.Device;
import org.openconnectivity.otgc.domain.model.devicelist.DeviceRole;
import org.openconnectivity.otgc.utils.constant.OcfResourceUri;
import org.openconnectivity.otgc.utils.rx.SchedulersFacade;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class GetDeviceRoleUseCase {
    /* Repositories */
    private final IotivityRepository iotivityRepository;
    private final SettingRepository settingRepository;
    /* Scheduler */
    private final SchedulersFacade schedulersFacade;

    @Inject
    public GetDeviceRoleUseCase(IotivityRepository iotivityRepository,
                                SettingRepository settingRepository,
                                SchedulersFacade schedulersFacade) {
        this.iotivityRepository = iotivityRepository;
        this.settingRepository = settingRepository;

        this.schedulersFacade = schedulersFacade;
    }

    public Single<DeviceRole> execute(Device device) {
        int delay = Integer.parseInt(settingRepository.get(SettingRepository.REQUESTS_DELAY_KEY, SettingRepository.REQUESTS_DELAY_DEFAULT_VALUE));

        return iotivityRepository.getNonSecureEndpoint(device)
                .flatMap(endpoint ->
                        iotivityRepository.findResources(endpoint)
                        .timeout(iotivityRepository.getDiscoveryTimeout() + 5, TimeUnit.SECONDS)
                        .map(ocRes -> {
                           DeviceRole deviceRole = DeviceRole.CLIENT;
                           for (OcResource resource : ocRes.getResourceList()) {
                               for (String resourceType : resource.getResourceTypes()) {
                                   if (OcfResourceType.isVerticalResourceType(resourceType)
                                           && !resource.getHref().equals(OcfResourceUri.DEVICE_INFO_URI)) {
                                       deviceRole = DeviceRole.SERVER;
                                       break;
                                   }
                               }

                               if (deviceRole.equals(DeviceRole.SERVER))
                                   break;
                           }
                           return deviceRole;
                        }))
                .delay(delay, TimeUnit.SECONDS, schedulersFacade.ui());
    }
}
