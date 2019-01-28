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

package org.openconnectivity.otgc.client.domain.model;

import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

import java.util.ArrayList;
import java.util.List;

public class OcIntrospection {
    private static final String NAME_KEY = "n";
    private static final String URL_INFO_KEY = "urlInfo";

    private String name;
    private List<OcIntrospectionUrlInfo> urlInfo;

    public OcIntrospection() {
        this.name = "";
        this.urlInfo = new ArrayList<>();
    }

    public void setOcRepresentation(OcRepresentation ocRepresentation) throws IntrospectionException {
        try {
            this.name = ocRepresentation.getValue(NAME_KEY);
        } catch (OcException ex) {
            throw new IntrospectionException(ex);
        }

        try {
            OcRepresentation[] representations = ocRepresentation.getValue(URL_INFO_KEY);
            for (OcRepresentation representation : representations) {
                OcIntrospectionUrlInfo ocIntrospectionUrlInfo = new OcIntrospectionUrlInfo();
                ocIntrospectionUrlInfo.setOcRepresentation(representation);
                this.urlInfo.add(ocIntrospectionUrlInfo);
            }
        } catch (OcException ex) {
            throw new IntrospectionException(ex);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<OcIntrospectionUrlInfo> getUrlInfo() {
        return urlInfo;
    }

    public void setUrlInfo(List<OcIntrospectionUrlInfo> urlInfo) {
        this.urlInfo = urlInfo;
    }

    public OcIntrospectionUrlInfo getCoapsIpv6Endpoint() {
        OcIntrospectionUrlInfo url = null;
        for (OcIntrospectionUrlInfo urlInfo : urlInfo) {
            if (urlInfo.getProtocol().equals("coaps")
                    && !urlInfo.getHost().contains(".")) {
                return urlInfo;
            } else if (urlInfo.getProtocol().equals("coaps")) {
                url = urlInfo;
            }
        }

        return url;
    }
}

