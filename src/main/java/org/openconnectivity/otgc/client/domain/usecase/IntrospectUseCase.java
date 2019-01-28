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

package org.openconnectivity.otgc.client.domain.usecase;

import io.reactivex.Single;
import org.iotivity.base.OcException;
import org.iotivity.base.OcPlatform;
import org.iotivity.base.OcRepresentation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openconnectivity.otgc.client.domain.model.OcIntrospection;
import org.openconnectivity.otgc.client.domain.model.OcIntrospectionUrlInfo;
import org.openconnectivity.otgc.common.constant.OcfInterface;
import org.openconnectivity.otgc.common.constant.OcfResourceType;
import org.openconnectivity.otgc.common.data.repository.IotivityRepository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IntrospectUseCase {
    private final IotivityRepository iotivityRepository;

    @Inject
    public IntrospectUseCase(IotivityRepository iotivityRepository) {
        this.iotivityRepository = iotivityRepository;
    }

    public Single<JSONObject> execute(String deviceId) {
        return iotivityRepository.getDeviceCoapIpv6Host(deviceId)
                .flatMap(host -> iotivityRepository.findResource(host, OcfResourceType.INTROSPECTION))
                .flatMap(ocResource -> iotivityRepository.get(ocResource, true))
                .flatMap(ocRepresentation -> {
                    OcIntrospection ocIntrospection = new OcIntrospection();
                    ocIntrospection.setOcRepresentation(ocRepresentation);
                    OcIntrospectionUrlInfo ocIntrospectionUrlInfo = ocIntrospection.getCoapsIpv6Endpoint();
                    if (ocIntrospectionUrlInfo != null) {
                        List<String> resourceTypes = new ArrayList<>();
                        resourceTypes.add(OcfResourceType.INTROSPECTION_PAYLOAD);

                        List<String> interfaceList = new ArrayList<>();
                        interfaceList.add(OcPlatform.DEFAULT_INTERFACE);
                        interfaceList.add(OcfInterface.READ);

                        return iotivityRepository.constructResource(
                                iotivityRepository.getDeviceCoapsIpv6Host(deviceId).blockingGet(),
                                ocIntrospectionUrlInfo.getUri(),
                                resourceTypes,
                                interfaceList
                        );
                    } else {
                        return null;
                    }
                }).flatMap(ocResource -> iotivityRepository.get(ocResource, true))
                .map((this::parseOcRepresentationToJson));

    }

    private JSONObject parseOcRepresentationToJson(OcRepresentation rep) throws OcException {
        JSONObject jsonObject = new JSONObject();
        for (String key : rep.getValues().keySet()) {
            if (rep.getValue(key) instanceof String
                    || rep.getValue(key) instanceof Boolean
                    || rep.getValue(key) instanceof Integer) {
                try {
                    jsonObject.put(key, (Map<?, ?>)rep.getValue(key));
                } catch (JSONException e) {

                }
            } else if (rep.getValue(key) instanceof String[]) {
                try {
                    JSONArray array = new JSONArray();
                    for (String value : (String[]) rep.getValue(key)) {
                        array.put(value);
                    }
                    jsonObject.put(key, array);
                } catch (JSONException e) {

                }
            } else if (rep.getValue(key) instanceof OcRepresentation) {
                JSONObject childObject = parseOcRepresentationToJson(rep.getValue(key));
                try {
                    jsonObject.put(key, childObject);
                } catch (JSONException e) {

                }
            } else if (rep.getValue(key) instanceof OcRepresentation[]) {
                JSONArray array = new JSONArray();
                for (OcRepresentation childRep : (OcRepresentation[]) rep.getValue(key)) {
                    JSONObject childObject = parseOcRepresentationToJson(childRep);
                    array.put(childObject);
                }
                try {
                    jsonObject.put(key, array);
                } catch (JSONException e) {

                }
            }
        }

        return jsonObject;
    }

}
