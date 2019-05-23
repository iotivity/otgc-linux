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

package org.openconnectivity.otgc.data.entity;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import org.iotivity.OCEndpoint;
import org.iotivity.OCEndpointUtil;
import org.openconnectivity.otgc.domain.model.devicelist.DeviceType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "DEVICES")
@NamedQueries({
        @NamedQuery(query = "SELECT d FROM DeviceEntity d", name="Device.findAll"),
        @NamedQuery(query = "SELECT d FROM DeviceEntity d WHERE deviceid=:id", name="Device.findById"),
        @NamedQuery(query = "UPDATE DeviceEntity SET name=:name WHERE deviceid=:id", name="Device.updateName"),
        @NamedQuery(query = "UPDATE DeviceEntity SET type=:type, permits=:permits WHERE deviceid=:id", name="Device.updateType")
})
public class DeviceEntity {
    @Id
    @Column(name = "deviceid")
    private String id;

    @Column(name = "name")
    private String name;

    @ElementCollection
    @CollectionTable(
            name = "HOSTS",
            joinColumns = @JoinColumn(name = "id", referencedColumnName = "deviceid")
    )
    @Column(name = "host")
    private List<String> hosts;

    @Column(name = "type")
    private DeviceType type;

    @Column(name = "permits")
    private int permits;

    public DeviceEntity(){}

    public DeviceEntity(@NonNull String id, String name, OCEndpoint endpoints, @Nullable DeviceType type, int permits) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.permits = permits;
        this.hosts = new ArrayList<>();

        while(endpoints != null) {
            String[] endpointStr = new String[1];
            OCEndpointUtil.toString(endpoints, endpointStr);
            this.hosts.add(endpointStr[0]);

            endpoints = endpoints.getNext();
        }
    }

    public DeviceEntity(@NonNull String id, String name, List<String> hosts, @Nullable DeviceType type, int permits) {
        this.id = id;
        this.name = name;
        this.hosts = hosts;
        this.type = type;
        this.permits = permits;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public DeviceType getType() {
        return type;
    }

    public int getPermits() {
        return permits;
    }
}
