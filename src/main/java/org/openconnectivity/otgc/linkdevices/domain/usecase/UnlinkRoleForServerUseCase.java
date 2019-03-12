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
import org.iotivity.base.OicSecAce;
import org.openconnectivity.otgc.accesscontrol.data.repository.AmsRepository;
import org.openconnectivity.otgc.common.data.repository.IotivityRepository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class UnlinkRoleForServerUseCase {
    private final IotivityRepository iotivityRepository;
    private final AmsRepository amsRepository;

    @Inject
    public UnlinkRoleForServerUseCase(IotivityRepository iotivityRepository,
                                      AmsRepository amsRepository)
    {
        this.iotivityRepository = iotivityRepository;
        this.amsRepository = amsRepository;
    }

    public Completable execute(String deviceId, String roleId) {
        return iotivityRepository.findOcSecureResource(deviceId)
                .flatMapCompletable(ocSecureResource -> amsRepository.getAcl(ocSecureResource)
                        .flatMapCompletable(oicSecAcl -> {
                            List<Completable> deleteAceList = new ArrayList<>();
                            for (OicSecAce ace : oicSecAcl.getOicSecAcesList()) {
                                if (ace.getSubject().getRole() != null && ace.getSubject().getRole().getId().equals(roleId)) {
                                    deleteAceList.add(amsRepository.deleteAcl(ocSecureResource, ace.getAceID()));
                                }
                            }
                            return Completable.merge(deleteAceList);
                        }));
    }
}
