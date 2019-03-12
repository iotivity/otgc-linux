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

package org.openconnectivity.otgc.linkdevices.domain.usecase;

import io.reactivex.Completable;
import io.reactivex.Single;
import org.iotivity.base.OcSecureResource;
import org.openconnectivity.otgc.common.data.repository.IotivityRepository;
import org.openconnectivity.otgc.common.data.repository.ProvisionRepository;

import javax.inject.Inject;

public class UnlinkDevicesUseCase {
    private final IotivityRepository iotivityRepository;
    private final ProvisionRepository provisionRepository;

    @Inject
    public UnlinkDevicesUseCase(IotivityRepository iotivityRepository,
                              ProvisionRepository provisionRepository)
    {
        this.iotivityRepository = iotivityRepository;
        this.provisionRepository = provisionRepository;
    }

    public Completable execute(String clientId, String serverId)
    {
        Single<OcSecureResource> clientSecureResource = iotivityRepository.findOcSecureResource(clientId);
        Single<OcSecureResource> serverSecureResource = iotivityRepository.findOcSecureResource(serverId);

        return Single.concat(clientSecureResource, serverSecureResource)
                .toList()
                .flatMapCompletable(ocSecureResources -> provisionRepository.unlinkDevices(ocSecureResources.get(0), ocSecureResources.get(1)));
    }
}
