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

import io.reactivex.Single;
import org.iotivity.base.OicSecAce;
import org.openconnectivity.otgc.accesscontrol.data.repository.AmsRepository;
import org.openconnectivity.otgc.common.data.repository.IotivityRepository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class RetrieveLinkedRolesForServerUseCase {
    private final IotivityRepository iotivityRepository;
    private final AmsRepository amsRepository;

    @Inject
    public RetrieveLinkedRolesForServerUseCase(IotivityRepository iotivityRepository,
                                               AmsRepository amsRepository)
    {
        this.iotivityRepository = iotivityRepository;
        this.amsRepository = amsRepository;
    }

    public Single<List<String>> execute(String deviceId)
    {
        return iotivityRepository.findOcSecureResource(deviceId)
                .flatMap(amsRepository::getAcl)
                .map(acl -> {
                    List<String> roles = new ArrayList<>();

                    for (OicSecAce ace : acl.getOicSecAces()) {
                        if (ace.getSubject().getRole() != null) {
                            roles.add(ace.getSubject().getRole().getId());
                        }
                    }

                    return roles;
                });
    }
}
