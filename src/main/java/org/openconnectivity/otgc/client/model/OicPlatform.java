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

package org.openconnectivity.otgc.client.model;

import org.apache.log4j.Logger;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class OicPlatform {
    private final Logger LOG = Logger.getLogger(OicPlatform.class);

    private static final String PLATFORM_ID_KEY = "pi";
    private static final String MAN_NAME_KEY = "mnmn";
    private static final String MAN_URL_KEY = "mnml";
    private static final String MAN_MODEL_NO_KEY = "mnmo";
    private static final String MAN_DATE_KEY = "mndt";
    private static final String MAN_PLATFORM_VER_KEY = "mnpv";
    private static final String MAN_OS_VER_KEY = "mnos";
    private static final String MAN_HW_VER_KEY = "mnhw";
    private static final String MAN_FW_VER_KEY = "mnfv";
    private static final String MAN_SUPPORT_URL_KEY = "mnsl";
    private static final String MAN_SYSTEM_TIME_KEY = "st";

    private String platformId;
    private String manufacturerName;
    private String manufacturerUrl;
    private String manufacturerModelNumber;
    private String manufacturedDate;
    private String manufacturerPlatformVersion;
    private String manufacturerOsVersion;
    private String manufacturerHwVersion;
    private String manufacturerFwVersion;
    private String manufacturerSupportUrl;
    private String manufacturerSystemTime;

    public OicPlatform() {
        platformId = "";
        manufacturerName = "";
        manufacturerUrl = "";
        manufacturerModelNumber = "";
        manufacturedDate = "";
        manufacturerPlatformVersion = "";
        manufacturerOsVersion = "";
        manufacturerHwVersion = "";
        manufacturerFwVersion = "";
        manufacturerSupportUrl = "";
        manufacturerSystemTime = "";
    }

    public void setOcRepresentation(OcRepresentation rep) {
        try {
            platformId = rep.getValue(PLATFORM_ID_KEY);
        } catch (OcException ex) {
            LOG.debug("Field " + PLATFORM_ID_KEY + " not found");
        }

        try {
            manufacturerName = rep.getValue(MAN_NAME_KEY);
        } catch (OcException ex) {
            LOG.debug("Field " + MAN_NAME_KEY + " not found");
        }

        try {
            manufacturerUrl = rep.getValue(MAN_URL_KEY);
        } catch (OcException ex) {
            LOG.debug("Field " + MAN_URL_KEY + " not found");
        }

        try {
            manufacturerModelNumber = rep.getValue(MAN_MODEL_NO_KEY);
        } catch (OcException ex) {
            LOG.debug("Field " + MAN_MODEL_NO_KEY + " not found");
        }

        try {
            manufacturedDate = rep.getValue(MAN_DATE_KEY);
        } catch (OcException ex) {
            LOG.debug("Field " + MAN_DATE_KEY + " not found");
        }

        try {
            manufacturerPlatformVersion = rep.getValue(MAN_PLATFORM_VER_KEY);
        } catch (OcException ex) {
            LOG.debug("Field " + MAN_PLATFORM_VER_KEY + " not found");
        }

        try {
            manufacturerOsVersion = rep.getValue(MAN_OS_VER_KEY);
        } catch (OcException ex) {
            LOG.debug("Field " + MAN_OS_VER_KEY + " not found");
        }

        try {
            manufacturerHwVersion = rep.getValue(MAN_HW_VER_KEY);
        } catch (OcException ex) {
            LOG.debug("Field " + MAN_HW_VER_KEY + " not found");
        }

        try {
            manufacturerFwVersion = rep.getValue(MAN_FW_VER_KEY);
        } catch (OcException ex) {
            LOG.debug("Field " + MAN_FW_VER_KEY + " not found");
        }

        try {
            manufacturerSupportUrl = rep.getValue(MAN_SUPPORT_URL_KEY);
        } catch (OcException ex) {
            LOG.debug("Field " + MAN_SUPPORT_URL_KEY + " not found");
        }

        try {
            manufacturerSystemTime = rep.getValue(MAN_SYSTEM_TIME_KEY);
        } catch (OcException ex) {
            LOG.debug("Field " + MAN_SYSTEM_TIME_KEY + " not found");
        }
    }

    public OcRepresentation getOcRepresentation() throws OcException {
        OcRepresentation rep = new OcRepresentation();
        rep.setValue(PLATFORM_ID_KEY, platformId);
        rep.setValue(MAN_NAME_KEY, manufacturerName);
        rep.setValue(MAN_URL_KEY, manufacturerUrl);
        rep.setValue(MAN_MODEL_NO_KEY, manufacturerModelNumber);
        rep.setValue(MAN_DATE_KEY, manufacturedDate);
        rep.setValue(MAN_PLATFORM_VER_KEY, manufacturerPlatformVersion);
        rep.setValue(MAN_OS_VER_KEY, manufacturerOsVersion);
        rep.setValue(MAN_HW_VER_KEY, manufacturerHwVersion);
        rep.setValue(MAN_FW_VER_KEY, manufacturerFwVersion);
        rep.setValue(MAN_SUPPORT_URL_KEY, manufacturerSupportUrl);
        rep.setValue(MAN_SYSTEM_TIME_KEY, manufacturerSystemTime);

        return rep;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getManufacturerUrl() {
        return manufacturerUrl;
    }

    public void setManufacturerUrl(String manufacturerUrl) {
        this.manufacturerUrl = manufacturerUrl;
    }

    public String getManufacturerModelNumber() {
        return manufacturerModelNumber;
    }

    public void setManufacturerModelNumber(String manufacturerModelNumber) {
        this.manufacturerModelNumber = manufacturerModelNumber;
    }

    public String getManufacturedDate() {
        return manufacturedDate;
    }

    public void setManufacturedDate(String manufacturedDate) {
        this.manufacturedDate = manufacturedDate;
    }

    public String getManufacturerPlatformVersion() {
        return manufacturerPlatformVersion;
    }

    public void setManufacturerPlatformVersion(String manufacturerPlatformVersion) {
        this.manufacturerPlatformVersion = manufacturerPlatformVersion;
    }

    public String getManufacturerOsVersion() {
        return manufacturerOsVersion;
    }

    public void setManufacturerOsVersion(String manufacturerOsVersion) {
        this.manufacturerOsVersion = manufacturerOsVersion;
    }

    public String getManufacturerHwVersion() {
        return manufacturerHwVersion;
    }

    public void setManufacturerHwVersion(String manufacturerHwVersion) {
        this.manufacturerHwVersion = manufacturerHwVersion;
    }

    public String getManufacturerFwVersion() {
        return manufacturerFwVersion;
    }

    public void setManufacturerFwVersion(String manufacturerFwVersion) {
        this.manufacturerFwVersion = manufacturerFwVersion;
    }

    public String getManufacturerSupportUrl() {
        return manufacturerSupportUrl;
    }

    public void setManufacturerSupportUrl(String manufacturerSupportUrl) {
        this.manufacturerSupportUrl = manufacturerSupportUrl;
    }

    public String getManufacturerSystemTime() {
        return manufacturerSystemTime;
    }

    public void setManufacturerSystemTime(String manufacturerSystemTime) {
        this.manufacturerSystemTime = manufacturerSystemTime;
    }

    @Override
    public String toString() {
        return "\t" + PLATFORM_ID_KEY + ": " + platformId
                + "\n\t" + MAN_NAME_KEY + ": " + manufacturerName
                + "\n\t" + MAN_URL_KEY + ": " + manufacturerUrl
                + "\n\t" + MAN_MODEL_NO_KEY + ": " + manufacturerModelNumber
                + "\n\t" + MAN_DATE_KEY + ": " + manufacturedDate
                + "\n\t" + MAN_PLATFORM_VER_KEY + ": " + manufacturerPlatformVersion
                + "\n\t" + MAN_OS_VER_KEY + ": " + manufacturerOsVersion
                + "\n\t" + MAN_HW_VER_KEY + ": " + manufacturerHwVersion
                + "\n\t" + MAN_FW_VER_KEY + ": " + manufacturerFwVersion
                + "\n\t" + MAN_SUPPORT_URL_KEY + ": " + manufacturerSupportUrl
                + "\n\t" + MAN_SYSTEM_TIME_KEY + ": " + manufacturerSystemTime;
    }

}
