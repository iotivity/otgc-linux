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

package org.openconnectivity.otgc.accesscontrol.domain.usecase;

import io.reactivex.Completable;
import org.iotivity.base.*;
import org.openconnectivity.otgc.accesscontrol.data.repository.AmsRepository;
import org.openconnectivity.otgc.common.data.repository.IotivityRepository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class CreateAclUseCase {
    private final AmsRepository amsRepository;
    private final IotivityRepository iotivityRepository;

    @Inject
    public CreateAclUseCase(AmsRepository amsRepository,
                            IotivityRepository iotivityRepository) {
        this.amsRepository = amsRepository;
        this.iotivityRepository = iotivityRepository;
    }

    public Completable execute(String targetDeviceId, String subjectId, List<String> verticalResources, int permission) {
        return iotivityRepository.findOcSecureResource(targetDeviceId)
                .flatMapCompletable(ocSecureResource -> {
                    OicSecAceSubject subject = new OicSecAceSubject(AceSubjectType.SUBJECT_UUID.getValue(), subjectId, null, null);

                    OicSecAce ace = new OicSecAce(0, subject, permission, getResources(verticalResources), new ArrayList<>());
                    return amsRepository.provisionAcl(ocSecureResource, ace);
                });
    }

    public Completable execute(String targetDeviceId, String roleId, String roleAuthority, List<String> verticalResources, int permission) {
        return iotivityRepository.findOcSecureResource(targetDeviceId)
                .flatMapCompletable(ocSecureResource -> {
                    AceSubjectRole role = new AceSubjectRole(roleId, roleAuthority);
                    OicSecAceSubject subject = new OicSecAceSubject(AceSubjectType.SUBJECT_ROLE.getValue(), null, role, null);

                    OicSecAce ace = new OicSecAce(0, subject, permission, getResources(verticalResources), new ArrayList<>());
                    return amsRepository.provisionAcl(ocSecureResource, ace);
                });
    }

    public Completable execute(String targetDeviceId, boolean isAuthCrypt, List<String> verticalResources, int permission) {
        return iotivityRepository.findOcSecureResource(targetDeviceId)
                .flatMapCompletable(ocSecureResource -> {
                    OicSecAceSubject subject =
                            new OicSecAceSubject(
                                    AceSubjectType.SUBJECT_CONNTYPE.getValue(),
                                    null,
                                    null,
                                    isAuthCrypt ? "auth-crypt" : "anon-clear");


                    OicSecAce ace = new OicSecAce(0, subject, permission, getResources(verticalResources), new ArrayList<>());
                    return amsRepository.provisionAcl(ocSecureResource, ace);
                });
    }

    private List<OicSecResr> getResources(List<String> verticalResources) {
        List<OicSecResr> resources = new ArrayList<>();
        for (String verticalResource : verticalResources) {
            OicSecResr res = new OicSecResr();
            res.setHref(verticalResource);
            List<String> types = new ArrayList<>();
            types.add("*");
            res.setTypes(types);
            res.setTypeLen(types.size());
            List<String> interfaces = new ArrayList<>();
            interfaces.add("*");
            res.setInterfaces(interfaces);
            res.setInterfaceLen(interfaces.size());
            resources.add(res);
        }

        return resources;
    }
}
