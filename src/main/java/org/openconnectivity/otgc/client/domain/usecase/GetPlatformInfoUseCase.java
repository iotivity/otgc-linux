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

package org.openconnectivity.otgc.client.domain.usecase;

import io.reactivex.Single;
import org.openconnectivity.otgc.client.model.OicPlatform;
import org.openconnectivity.otgc.common.data.repository.IotivityRepository;

import javax.inject.Inject;

public class GetPlatformInfoUseCase {
    private final IotivityRepository iotivityRepository;

    @Inject
    public GetPlatformInfoUseCase(IotivityRepository iotivityRepository) {
        this.iotivityRepository = iotivityRepository;
    }

    public Single<OicPlatform> execute(String deviceId) {
        return iotivityRepository.getDeviceCoapIpv6Host(deviceId)
                .flatMap(iotivityRepository::getPlatformInfo)
                .map(ocRepresentation -> {
                    OicPlatform platformInfo = new OicPlatform();

                    if (!ocRepresentation.getValues().isEmpty()) {
                        platformInfo.setOcRepresentation(ocRepresentation);
                    }

                    return platformInfo;
                });
    }

}
