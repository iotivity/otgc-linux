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

package org.openconnectivity.otgc.common.data.entity;

import io.reactivex.annotations.NonNull;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "DEVICES")
@NamedQueries({
        @NamedQuery(query = "SELECT d FROM DeviceEntity d", name="Device.findAll"),
        @NamedQuery(query = "SELECT d FROM DeviceEntity d WHERE deviceid=:id", name="Device.findById"),
        @NamedQuery(query = "UPDATE DeviceEntity SET name=:name WHERE deviceid=:id", name="Device.updateName")
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

    public DeviceEntity(){}

    public DeviceEntity(@NonNull String id, String name, List<String> hosts) {
        this.id = id;
        this.name = name;
        this.hosts = hosts;
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
}
