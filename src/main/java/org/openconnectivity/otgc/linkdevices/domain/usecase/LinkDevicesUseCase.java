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
import org.iotivity.base.OcResource;
import org.iotivity.base.OcSecureResource;
import org.iotivity.base.OicSecResr;
import org.openconnectivity.otgc.common.constant.OcfResourceType;
import org.openconnectivity.otgc.common.data.repository.IotivityRepository;
import org.openconnectivity.otgc.common.data.repository.ProvisionRepository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class LinkDevicesUseCase {
    private final IotivityRepository iotivityRepository;
    private final ProvisionRepository provisionRepository;

    @Inject
    public LinkDevicesUseCase(IotivityRepository iotivityRepository,
                              ProvisionRepository provisionRepository)
    {
        this.iotivityRepository = iotivityRepository;
        this.provisionRepository = provisionRepository;
    }

    public Completable execute(String clientId, String serverId)
    {
        Single<OcSecureResource> clientSecureResource = iotivityRepository.findOcSecureResource(clientId);
        Single<OcSecureResource> serverSecureResource = iotivityRepository.findOcSecureResource(serverId);

        return iotivityRepository.getDeviceCoapIpv6Host(serverId)
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
                .flatMapCompletable(resources ->  Single.concat(clientSecureResource, serverSecureResource).toList()
                    .flatMapCompletable(ocSecureResources -> provisionRepository.linkDevices(ocSecureResources.get(0), ocSecureResources.get(1), resources)));

    }
}
