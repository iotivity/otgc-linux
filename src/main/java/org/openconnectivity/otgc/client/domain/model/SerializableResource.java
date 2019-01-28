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

import io.reactivex.annotations.NonNull;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcResource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SerializableResource implements Serializable {
    private String uri;
    private List<String> hosts;
    private List<String> types;
    private List<String> interfaces;
    private OcRepresentation ocRepresentation;
    private boolean observable = false;
    private boolean observing = false;

    public SerializableResource(OcResource ocResource) {
        this.uri = ocResource.getUri();
        this.hosts = ocResource.getAllHosts();
        this.types = ocResource.getResourceTypes();
        this.interfaces = ocResource.getResourceInterfaces();
    }

    public SerializableResource(){}

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public @NonNull List<String> getHosts() {
        if (hosts == null) {
            this.hosts = new ArrayList<>();
        }

        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public @NonNull List<String> getResourceTypes() {
        if (types == null) {
            this.types = new ArrayList<>();
        }

        return types;
    }

    public void setResourceTypes(List<String> types) {
        this.types = types;
    }

    public @NonNull List<String> getResourceInterfaces() {
        if (interfaces == null) {
            this.interfaces = new ArrayList<>();
        }

        return interfaces;
    }

    public void setResourceInterfaces(List<String> interfaces) {
        this.interfaces = interfaces;
    }

    public OcRepresentation getOcRepresentation(){
        return this.ocRepresentation;
    }

    public void setOcRepresentation(OcRepresentation ocRepresentation) {
        this.ocRepresentation = ocRepresentation;
    }

    public boolean isObservable() {
        return this.observable;
    }

    public void setObservable(boolean observable) {
        this.observable = observable;
    }

    public boolean isObserving() {
        return this.observing;
    }

    public void setObserving(boolean observing) {
        this.observing = observing;
    }
}
