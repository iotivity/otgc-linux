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

package org.openconnectivity.otgc.domain.usecase.cloud;

import io.reactivex.Completable;
import org.iotivity.OCCredUsage;
import org.iotivity.OCCredUtil;
import org.openconnectivity.otgc.data.repository.*;
import org.openconnectivity.otgc.domain.model.devicelist.Device;
import org.openconnectivity.otgc.domain.model.resource.secure.cred.OcCredential;
import org.openconnectivity.otgc.domain.model.resource.secure.cred.OcCredentials;
import org.openconnectivity.otgc.utils.rx.SchedulersFacade;
import org.openconnectivity.otgc.domain.model.resource.cloud.OcCloudConfiguration;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class RegisterDeviceCloudUseCase {
    /* Repositories */
    private final IotivityRepository iotivityRepository;
    private final CloudRepository cloudRepository;
    private final AmsRepository amsRepository;
    private final SettingRepository settingRepository;
    private final CmsRepository cmsRepository;
    /* Scheduler */
    private final SchedulersFacade schedulersFacade;

    @Inject
    public RegisterDeviceCloudUseCase(IotivityRepository iotivityRepository,
                                      AmsRepository amsRepository,
                                      CloudRepository cloudRepository,
                                      SettingRepository settingRepository,
                                      CmsRepository cmsRepository,
                                      SchedulersFacade schedulersFacade) {
        this.iotivityRepository = iotivityRepository;
        this.amsRepository = amsRepository;
        this.settingRepository = settingRepository;
        this.cloudRepository = cloudRepository;
        this.cmsRepository = cmsRepository;

        this.schedulersFacade = schedulersFacade;
    }

    public Completable execute(Device deviceToRegister, String accessToken) {

        //int delay = Integer.parseInt(settingRepository.get(SettingRepository.REQUESTS_DELAY_KEY, SettingRepository.REQUESTS_DELAY_DEFAULT_VALUE));

        OcCloudConfiguration configuration = cloudRepository.retrieveCloudConfiguration().blockingGet();

        OcCredentials creds = cmsRepository.retrieveOwnCredentials().blockingGet();
        for(OcCredential cred : creds.getCredList()) {
            if (cred.getCredusage() != null && !cred.getCredusage().isEmpty()
                    && (OCCredUtil.parseCredUsage(cred.getCredusage()) == OCCredUsage.OC_CREDUSAGE_MFG_TRUSTCA
                    || OCCredUtil.parseCredUsage(cred.getCredusage()) == OCCredUsage.OC_CREDUSAGE_TRUSTCA)) {
                byte[] pem = cred.getPublicData().getPemData().getBytes();
                cmsRepository.provisionTrustAnchor(pem, configuration.getCloudUuid(), deviceToRegister.getDeviceId()).blockingGet();
            }
        }

        return  amsRepository.provisionConntypeAce(deviceToRegister.getDeviceId(), true, Arrays.asList("/CoapCloudConfResURI"), 6).
                andThen(cmsRepository.registerDeviceCloud(deviceToRegister.getDeviceId(), "/CoapCloudConfResURI", configuration.getAuthProvider(), configuration.getCloudUrl(),
                        configuration.getCloudUuid(), accessToken));
    }
}
