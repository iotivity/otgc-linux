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

package org.openconnectivity.otgc.devicelist.domain.model;

import org.iotivity.base.OcSecureResource;
import org.openconnectivity.otgc.common.domain.model.OcDevice;

public class Device implements Comparable<Device> {
    private DeviceType deviceType;
    private Role role;
    private String deviceId;
    private OcDevice deviceInfo;
    private OcSecureResource ocSecureResource;

    public Device(DeviceType type, String deviceId, OcDevice deviceInfo, OcSecureResource ocSecureResource) {
        this.deviceType = type;
        this.role = Role.UNKNOWN;
        this.deviceId = deviceId;
        this.deviceInfo = deviceInfo;
        this.ocSecureResource = ocSecureResource;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public Role getRole() {
        return this.role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public OcDevice getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(OcDevice deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public OcSecureResource getOcSecureResource() {
        return ocSecureResource;
    }

    public void setOcSecureResource(OcSecureResource ocSecureResource) {
        this.ocSecureResource = ocSecureResource;
    }

    @Override
    public int compareTo(Device device) {
        if (this.getDeviceInfo().getName().compareTo(device.getDeviceInfo().getName()) == 0) {
            return this.getDeviceId().compareTo(device.getDeviceId());
        } else if (this.getDeviceInfo().getName().isEmpty()) {
            return "Unnamed".compareTo(device.getDeviceInfo().getName());
        } else if (device.getDeviceInfo().getName().isEmpty()) {
            return this.getDeviceInfo().getName().compareTo("Unnamed");
        }
        return this.getDeviceInfo().getName().compareTo(device.getDeviceInfo().getName());
    }
}
