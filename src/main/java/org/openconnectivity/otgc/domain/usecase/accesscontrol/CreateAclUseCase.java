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

package org.openconnectivity.otgc.domain.usecase.accesscontrol;

import io.reactivex.Completable;
import org.openconnectivity.otgc.data.repository.AmsRepository;
import org.openconnectivity.otgc.domain.model.devicelist.Device;

import javax.inject.Inject;
import java.util.List;

public class CreateAclUseCase {
    private final AmsRepository amsRepository;

    @Inject
    public CreateAclUseCase(AmsRepository amsRepository) {
        this.amsRepository = amsRepository;
    }

    /**
     * Use case steps:
     * 1.   POST    /oic/sec/pstat  -> dos.s=2
     * 2.   POST    /oic/sec/acl2   -> create ACE (uuid, role or connection type)
     * 3.   POST    /oic/sec/pstat  -> dos.s=3
     */

    public Completable execute(Device targetDevice, String subjectId, List<String> verticalResources, long permission) {
        return amsRepository.provisionUuidAce(targetDevice.getDeviceId(), subjectId, verticalResources, permission);
    }

    public Completable execute(Device targetDevice, String roleId, String roleAuthority, List<String> verticalResources, long permission) {
        return amsRepository.provisionRoleAce(targetDevice.getDeviceId(), roleId, roleAuthority, verticalResources, permission);
    }

    public Completable execute(Device targetDevice, boolean isAuthCrypt, List<String> verticalResources, long permission) {
        return amsRepository.provisionConntypeAce(targetDevice.getDeviceId(), isAuthCrypt, verticalResources, permission);
    }
}
