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
import org.iotivity.base.*;
import org.openconnectivity.otgc.accesscontrol.data.repository.AmsRepository;
import org.openconnectivity.otgc.common.constant.OcfResourceType;
import org.openconnectivity.otgc.common.data.repository.IotivityRepository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class LinkRoleForServerUseCase {
    private final IotivityRepository iotivityRepository;
    private final AmsRepository amsRepository;

    @Inject
    public LinkRoleForServerUseCase(IotivityRepository iotivityRepository,
                                    AmsRepository amsRepository)
    {
        this.iotivityRepository = iotivityRepository;
        this.amsRepository = amsRepository;
    }

    public Completable execute(String deviceId, String roleId, String roleAuthority) {
        return iotivityRepository.getDeviceCoapIpv6Host(deviceId)
                .flatMap(iotivityRepository::findResources)
                .map(ocResources -> {
                    List<OicSecResr> resources = new ArrayList<>();
                    for (OcResource resource : ocResources) {
                        for (String resourceType : resource.getResourceTypes()) {
                            if (OcfResourceType.isVerticalResourceType(resourceType)) {
                                OicSecResr res = new OicSecResr();
                                res.setHref(resource.getUri());
                                List<String> types = resource.getResourceTypes();
                                res.setTypes(types);
                                res.setTypeLen(types.size());
                                List<String> interfaces = resource.getResourceInterfaces();
                                res.setInterfaces(interfaces);
                                res.setInterfaceLen(interfaces.size());
                                resources.add(res);
                            }
                        }
                    }
                    return resources;
                })
                .flatMapCompletable(resources ->iotivityRepository.findOcSecureResource(deviceId)
                .flatMapCompletable(ocSecureResource -> {
                    AceSubjectRole role = new AceSubjectRole(roleId, roleAuthority);
                    OicSecAceSubject subject = new OicSecAceSubject(AceSubjectType.SUBJECT_ROLE.getValue(), null, role, null);

                    OicSecAce ace = new OicSecAce(0, subject, 31, resources, new ArrayList<>());
                    return amsRepository.provisionAcl(ocSecureResource, ace);
                }));
    }
}
