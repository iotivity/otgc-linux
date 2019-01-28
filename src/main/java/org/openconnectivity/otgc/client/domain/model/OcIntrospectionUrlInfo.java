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

public class OcIntrospectionUrlInfo {
    private static final String URL_KEY = "url";
    private static final String PROTOCOL_KEY = "protocol";
    private static final String CONTENT_TYPE_KEY = "content-type";
    private static final String VERSION_KEY = "version";

    private String host;
    private String uri;
    private String protocol;
    private String contentType;
    private int version;

    public OcIntrospectionUrlInfo() {
        host = "";
        uri = "";
        protocol = "";
        contentType = "";
        version = 0;
    }

    public void setOcRepresentation(OcRepresentation ocRepresentation) throws IntrospectionException {
        try {
            separateUrlInHostAndUri(ocRepresentation.getValue(URL_KEY));
        } catch (OcException ex) {
            throw new IntrospectionException(ex);
        }

        try {
            protocol = ocRepresentation.getValue(PROTOCOL_KEY);
        } catch (OcException ex) {
            throw new IntrospectionException(ex);
        }

        try {
            contentType = ocRepresentation.getValue(CONTENT_TYPE_KEY);
        } catch (OcException ex) {
            throw new IntrospectionException(ex);
        }

        try {
            version = ocRepresentation.getValue(VERSION_KEY);
        } catch (OcException ex) {
            throw new IntrospectionException(ex);
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    private void separateUrlInHostAndUri(String url) {
        int separatorIndex = url.indexOf('/', 8);
        host = url.substring(0, separatorIndex);
        uri = url.substring(separatorIndex);
    }
}

