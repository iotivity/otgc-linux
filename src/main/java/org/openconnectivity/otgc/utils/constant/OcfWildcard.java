/*
 * Copyright 2018 DEKRA Testing and Certification, S.A.U. All Rights Reserved.
 *
 * ****************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openconnectivity.otgc.utils.constant;

public class OcfWildcard {

    private OcfWildcard() {
        throw  new IllegalStateException("Constants class");
    }

    public static final String OC_WILDCARD_ALL_NCR = "*";
    public static final String OC_WILDCARD_ALL_SECURE_NCR = "+";
    public static final String OC_WILDCARD_ALL_NON_SECURE_NCR = "-";

    public static boolean isWildcard(String resourceUri) {
        if (resourceUri.equals(OC_WILDCARD_ALL_NCR)
                || resourceUri.equals(OC_WILDCARD_ALL_SECURE_NCR)
                || resourceUri.equals(OC_WILDCARD_ALL_NON_SECURE_NCR)) {
            return true;
        }
        return false;
    }
}
