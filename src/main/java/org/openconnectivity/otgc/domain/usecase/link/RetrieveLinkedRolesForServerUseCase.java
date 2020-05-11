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

import io.reactivex.Single;
import org.iotivity.OCAceSubjectType;
import org.openconnectivity.otgc.data.repository.AmsRepository;
import org.openconnectivity.otgc.domain.model.devicelist.Device;
import org.openconnectivity.otgc.domain.model.resource.secure.acl.OcAce;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class RetrieveLinkedRolesForServerUseCase {
    private final AmsRepository amsRepository;

    @Inject
    public RetrieveLinkedRolesForServerUseCase(AmsRepository amsRepository)
    {
        this.amsRepository = amsRepository;
    }

    public Single<List<String>> execute(Device device)
    {
        return amsRepository.getAcl(device.getDeviceId())
            .map(acl -> {
                List<String> roles = new ArrayList<>();

                for (OcAce ace : acl.getAceList()) {
                    if (OCAceSubjectType.valueOf(ace.getSubject().getType()) == OCAceSubjectType.OC_SUBJECT_ROLE
                            && ace.getSubject().getRoleId() != null) {
                        roles.add(ace.getSubject().getRoleId());
                    }
                }

                return roles;
            });
    }
}
