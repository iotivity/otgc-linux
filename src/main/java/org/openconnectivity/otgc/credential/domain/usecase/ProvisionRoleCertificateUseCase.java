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

package org.openconnectivity.otgc.credential.domain.usecase;

import io.reactivex.Completable;
import org.openconnectivity.otgc.common.data.repository.IotivityRepository;
import org.openconnectivity.otgc.credential.data.repository.CmsRepository;

import javax.inject.Inject;

public class ProvisionRoleCertificateUseCase {
    private final CmsRepository cmsRepository;
    private final IotivityRepository iotiviyRepository;

    @Inject
    public ProvisionRoleCertificateUseCase(CmsRepository cmsRepository,
                                           IotivityRepository iotiviyRepository) {
        this.cmsRepository = cmsRepository;
        this.iotiviyRepository = iotiviyRepository;
    }

    public Completable execute(String targetDeviceId, String roleId, String roleAuthority) {
        return iotiviyRepository.findOcSecureResource(targetDeviceId)
                .flatMapCompletable(ocSecureResource -> cmsRepository.provisionRoleCertificate(ocSecureResource, roleId, roleAuthority));
    }
}
