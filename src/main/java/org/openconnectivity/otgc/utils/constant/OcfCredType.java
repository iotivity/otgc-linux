/*
 *  Copyright 2018 DEKRA Testing and Certification, S.A.U. All Rights Reserved.
 *
 *  *****************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openconnectivity.otgc.utils.constant;

public enum OcfCredType {
    OC_CREDTYPE_UNKNOWN(-1),
    OC_CREDTYPE_PSK(1),
    OC_CREDTYPE_CERT(8);

    private int credType;

    OcfCredType(int credType) {
        this.credType = credType;
    }

    public int getValue() {
        return credType;
    }

    public static OcfCredType valueToEnum(int credType) {
        if (credType == OC_CREDTYPE_PSK.getValue()) {
            return OC_CREDTYPE_PSK;
        } else if (credType == OC_CREDTYPE_CERT.getValue()) {
            return OC_CREDTYPE_CERT;
        } else {
            return OC_CREDTYPE_UNKNOWN;
        }
    }
}
