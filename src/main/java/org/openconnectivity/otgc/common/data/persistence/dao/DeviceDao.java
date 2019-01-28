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

import org.openconnectivity.otgc.common.data.entity.DeviceEntity;
import org.openconnectivity.otgc.common.data.persistence.DatabaseManager;

import javax.inject.Inject;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceDao implements Dao {

    private static final String FIND_ALL_DEVICES = "Device.findAll";
    private static final String FIND_DEVICE_BY_ID = "Device.findById";
    private static final String UPDATE_DEVICE_NAME = "Device.updateName";

    @Inject
    public DeviceDao() {
        DatabaseManager.getEntityManager();
    }

    /**
     * Get all devices from the table
     * @return list of devices from the table
     */
    @Override
    public List<DeviceEntity> findAll() {
        Query q = DatabaseManager.createNamedQuery(FIND_ALL_DEVICES, null);
        List<DeviceEntity> devices = q.getResultList();
        return devices;
    }

    /**
     * Get a device from the table using its UUID to filter
     * @param id device UUID
     * @return the device from the table
     */
    @Override
    public DeviceEntity findById(String id) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", id);
        Query q = DatabaseManager.createNamedQuery(FIND_DEVICE_BY_ID, parameters);

        return (q.getResultList().size() > 0) ? (DeviceEntity) q.getSingleResult() : null;
    }

    /**
     * Insert a device in the database. If the device exists, replace it.
     * @param device the device to be inserted
     */
    @Override
    public void insert(Object device) {
        DatabaseManager.insertOrUpdate(device);
    }

    /**
     * Update the device name
     * @param deviceId Device ID to update
     * @param deviceName Device name value
     */
    public void updateDeviceName(String deviceId, String deviceName) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", deviceName);
        parameters.put("id", deviceId);
        Query q = DatabaseManager.updateWithNamedQuery(UPDATE_DEVICE_NAME, parameters);
    }

    /**
     * Delete all devices
     */
    @Override
    public void deleteAll() {
        Query q = DatabaseManager.createNamedQuery(FIND_ALL_DEVICES, null);
        List<DeviceEntity> devices = q.getResultList();
        for (DeviceEntity device : devices) {
            DatabaseManager.remove(device);
        }
    }
}
