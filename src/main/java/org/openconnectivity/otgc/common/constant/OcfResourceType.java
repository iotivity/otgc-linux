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

public class OcfResourceType {

    private OcfResourceType() {
        throw  new IllegalStateException("Constants class");
    }

    private static final String OIC_WK_PREFIX = "oic.wk.";
    public static final String DEVICE = OIC_WK_PREFIX + "d";
    public static final String PLATFORM = OIC_WK_PREFIX + "p";
    public static final String INTROSPECTION = OIC_WK_PREFIX + "introspection";
    public static final String INTROSPECTION_PAYLOAD = OIC_WK_PREFIX + "introspection.payload";

    private static final String OIC_RT_PREFIX = "oic.r.";
    public static final String DOXM = OIC_RT_PREFIX + "doxm";
    public static final String ACCELERATION_SENSOR = OIC_RT_PREFIX + "sensor.acceleration";
    public static final String ATMOSPHERIC_PRESSURE = OIC_RT_PREFIX + "sensor.atmosphericpressure";
    public static final String BINARY_SWITCH = OIC_RT_PREFIX + "switch.binary";
    public static final String BRIGHTNESS = OIC_RT_PREFIX + "light.brightness";
    public static final String COLOUR_RGB = OIC_RT_PREFIX + "colour.rgb";
    public static final String ELECTRICAL_ENERGY = OIC_RT_PREFIX + "energy.electrical";
    public static final String MAGNETIC_FIELD_DIRECTION = OIC_RT_PREFIX + "sensor.magneticfielddirection";
    public static final String TEMPERATURE = OIC_RT_PREFIX + "temperature";
}

