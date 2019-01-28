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

package org.openconnectivity.otgc.common.data.persistence.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openconnectivity.otgc.common.data.entity.DeviceEntity;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.is;

public class DeviceDaoTest {

    private DeviceDao deviceDao;
    private DeviceEntity device;

    private static final String DEVICE_ID = "12345678-1234-1234-1234-123456789012";

    @Before
    public void setup() {

        deviceDao = new DeviceDao();

        List<String> hosts = new ArrayList<>();
        hosts.add("coaps://192.168.1.10");
        hosts.add("coaps://192.168.1.11");
        device = new DeviceEntity(DEVICE_ID, "Device", hosts);
    }

    @After
    public void teardown() {
        deviceDao = null;
        device = null;
    }

    @Test
    public void insert() {
        deviceDao.insert(device);
        DeviceEntity d = deviceDao.findById(DEVICE_ID);

        assertThat(d.getId(), is(DEVICE_ID));
    }

    @Test
    public void insertAndUpdateDevice() {
        deviceDao.insert(device);

        device.getHosts().clear();
        device.getHosts().add("coap://192.168.1.15");
        deviceDao.insert(device);
        DeviceEntity d = deviceDao.findById(DEVICE_ID);

        assertThat(d.getId(), is(DEVICE_ID));
        assertThat(d.getHosts().size(), is(1));
    }

    @Test
    public void deleteAll() {
        deviceDao.insert(device);
        deviceDao.deleteAll();

        List<DeviceEntity> devices = deviceDao.findAll();
        assertThat(devices.size(), is(0));
    }

    @Test
    public void findByIdWithEmptyDatabase() {
        DeviceEntity entity = deviceDao.findById(DEVICE_ID);

        assertThat(entity, is(nullValue()));
    }
}
