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

package org.openconnectivity.otgc.common.constant;

import java.util.ArrayList;
import java.util.List;

public class OcfResourceType {

    private OcfResourceType() {
        throw  new IllegalStateException("Constants class");
    }

    private static final String OIC_WK_PREFIX = "oic.wk.";
    public static final String DEVICE = OIC_WK_PREFIX + "d";
    public static final String DEVICE_CONF = OIC_WK_PREFIX + "con";
    public static final String PLATFORM = OIC_WK_PREFIX + "p";
    public static final String PLATFORM_CONF = OIC_WK_PREFIX + "con.p";
    public static final String RES = OIC_WK_PREFIX + "res";
    public static final String RESOURCE_DIRECTORY = OIC_WK_PREFIX + "rd";
    public static final String MAINTENANCE = OIC_WK_PREFIX + "mnt";
    public static final String INTROSPECTION = OIC_WK_PREFIX + "introspection";
    public static final String INTROSPECTION_PAYLOAD = OIC_WK_PREFIX + "introspection.payload";

    private static final String OIC_RT_PREFIX = "oic.r.";
    public static final String ICON = OIC_RT_PREFIX + "icon";
    public static final String DOXM = OIC_RT_PREFIX + "doxm";
    public static final String PSTAT = OIC_RT_PREFIX + "pstat";
    public static final String ACL2 = OIC_RT_PREFIX + "acl2";
    public static final String CRED = OIC_RT_PREFIX + "cred";
    public static final String CRL = OIC_RT_PREFIX + "crl";
    public static final String CSR = OIC_RT_PREFIX + "csr";
    public static final String ROLES = OIC_RT_PREFIX + "roles";
    public static final String ACCELERATION_SENSOR = OIC_RT_PREFIX + "sensor.acceleration";
    public static final String ATMOSPHERIC_PRESSURE = OIC_RT_PREFIX + "sensor.atmosphericpressure";
    public static final String BINARY_SWITCH = OIC_RT_PREFIX + "switch.binary";
    public static final String BRIGHTNESS = OIC_RT_PREFIX + "light.brightness";
    public static final String COLOUR_RGB = OIC_RT_PREFIX + "colour.rgb";
    public static final String ELECTRICAL_ENERGY = OIC_RT_PREFIX + "energy.electrical";
    public static final String MAGNETIC_FIELD_DIRECTION = OIC_RT_PREFIX + "sensor.magneticfielddirection";
    public static final String TEMPERATURE = OIC_RT_PREFIX + "temperature";

    protected static final List<String> NON_VERTICAL_RESOURCE_TYPES;
    static {
        NON_VERTICAL_RESOURCE_TYPES = new ArrayList<>();

        NON_VERTICAL_RESOURCE_TYPES.add(OcfResourceType.DEVICE);
        NON_VERTICAL_RESOURCE_TYPES.add(OcfResourceType.DEVICE_CONF);
        NON_VERTICAL_RESOURCE_TYPES.add(OcfResourceType.PLATFORM);
        NON_VERTICAL_RESOURCE_TYPES.add(OcfResourceType.PLATFORM_CONF);
        NON_VERTICAL_RESOURCE_TYPES.add(OcfResourceType.RES);
        NON_VERTICAL_RESOURCE_TYPES.add(OcfResourceType.RESOURCE_DIRECTORY);
        NON_VERTICAL_RESOURCE_TYPES.add(OcfResourceType.MAINTENANCE);
        NON_VERTICAL_RESOURCE_TYPES.add(OcfResourceType.ICON);
        NON_VERTICAL_RESOURCE_TYPES.add(OcfResourceType.INTROSPECTION);
        NON_VERTICAL_RESOURCE_TYPES.add(OcfResourceType.INTROSPECTION_PAYLOAD);
        NON_VERTICAL_RESOURCE_TYPES.add(OcfResourceType.DOXM);
        NON_VERTICAL_RESOURCE_TYPES.add(OcfResourceType.PSTAT);
        NON_VERTICAL_RESOURCE_TYPES.add(OcfResourceType.ACL2);
        NON_VERTICAL_RESOURCE_TYPES.add(OcfResourceType.CRED);
        NON_VERTICAL_RESOURCE_TYPES.add(OcfResourceType.CRL);
        NON_VERTICAL_RESOURCE_TYPES.add(OcfResourceType.CSR);
        NON_VERTICAL_RESOURCE_TYPES.add(OcfResourceType.ROLES);
    }

    public static boolean isVerticalResourceType(String resourceType) {
        return !NON_VERTICAL_RESOURCE_TYPES.contains(resourceType);
    }
}

