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

public enum OcfDosType {
    OC_DOSTYPE_UNKNOWN(-1),
    OC_DOSTYPE_RESET(0),
    OC_DOSTYPE_RFOTM(1),
    OC_DOSTYPE_RFPRO(2),
    OC_DOSTYPE_RFNOP(3),
    OC_DOSTYPE_SRESET(4);

    private int dos;

    OcfDosType(int dos) {
        this.dos = dos;
    }

    public int getValue() {
        return dos;
    }

    public static OcfDosType valueToEnum(int dos) {
        if (dos == OC_DOSTYPE_RESET.getValue()) {
            return OC_DOSTYPE_RESET;
        } else if (dos == OC_DOSTYPE_RFOTM.getValue()) {
            return OC_DOSTYPE_RFOTM;
        } else if (dos == OC_DOSTYPE_RFPRO.getValue()) {
            return OC_DOSTYPE_RFPRO;
        } else if (dos == OC_DOSTYPE_RFNOP.getValue()) {
            return OC_DOSTYPE_RFNOP;
        } else if (dos == OC_DOSTYPE_SRESET.getValue()) {
            return OC_DOSTYPE_SRESET;
        } else {
            return OC_DOSTYPE_UNKNOWN;
        }
    }
}
