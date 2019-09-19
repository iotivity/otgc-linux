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

package org.openconnectivity.otgc.domain.usecase.link;

import io.reactivex.Completable;
import org.openconnectivity.otgc.data.repository.*;
import org.openconnectivity.otgc.domain.model.devicelist.Device;

import javax.inject.Inject;
import java.util.Arrays;

public class LinkRoleForServerUseCase {
    private final CmsRepository cmsRepository;
    private final AmsRepository amsRepository;

    @Inject
    public LinkRoleForServerUseCase(CmsRepository cmsRepository,
                                    AmsRepository amsRepository)
    {
        this.cmsRepository = cmsRepository;
        this.amsRepository = amsRepository;
    }

    public Completable execute(Device device, String roleId, String roleAuthority) {
        return cmsRepository.provisionIdentityCertificate(device.getDeviceId())
                .andThen(amsRepository.provisionRoleWildcardAce(device.getDeviceId(), roleId, roleAuthority))
                .andThen(amsRepository.provisionConntypeAce(device.getDeviceId(), true, Arrays.asList("/oic/sec/roles"), 31));
    }
}
