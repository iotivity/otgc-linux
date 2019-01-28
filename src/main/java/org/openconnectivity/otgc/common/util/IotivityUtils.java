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

package org.openconnectivity.otgc.common.util;

import org.iotivity.base.OcPlatform;

import java.nio.ByteBuffer;
import java.util.UUID;

public class IotivityUtils {

    private IotivityUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String getStringUuid() {
        ByteBuffer bb = ByteBuffer.wrap(OcPlatform.getDeviceId());
        UUID uuid = new UUID(bb.getLong(), bb.getLong());
        return uuid.toString();
    }

    public static String getValidIp(String ipAddress) {
        String validIp = ipAddress;
        if (validIp.contains("%")) {
            validIp = validIp.split("%")[0];
        }

        return validIp;
    }
}
