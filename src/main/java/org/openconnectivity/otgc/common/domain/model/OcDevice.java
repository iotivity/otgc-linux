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

package org.openconnectivity.otgc.common.domain.model;

import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class OcDevice {
    private static Logger LOG = LoggerFactory.getLogger(OcDevice.class);

    private static final String NAME_KEY = "n";
    private static final String SPEC_VERSION_URL_KEY = "icv";
    private static final String DEVICE_ID_KEY = "di";
    private static final String DATA_MODEL_KEY = "dmv";
    private static final String PIID_KEY = "piid";
    private static final String DESCRIPTIONS_KEY ="ld";
    private static final String SW_VERSION_KEY = "sv";
    private static final String MAN_NAME_KEY = "dmn";
    private static final String MODEL_NO_KEY = "dmno";

    private String name;
    private String specVersionUrl;
    private String deviceId;
    private String dataModel;
    private String piid;
    private List<String> locDescriptions;
    private String swVersion;
    private String manufacturerName;
    private String modelNumber;

    private List<String> deviceTypes;

    public OcDevice() {
        name = "";
        specVersionUrl = "";
        deviceId = "";
        dataModel = "";
        piid = "";
        locDescriptions = new ArrayList<>();
        swVersion = "";
        manufacturerName = "";
        modelNumber = "";
        deviceTypes = new ArrayList<>();
    }

    public void setOcRepresentation(OcRepresentation rep) {
        try {
            name = rep.getValue(NAME_KEY);
        } catch (OcException e) {
            LOG.debug("Field %s not found", NAME_KEY);
        }

        try {
            specVersionUrl = rep.getValue(SPEC_VERSION_URL_KEY);
        } catch (OcException e) {
            LOG.debug("Field %s not found", SPEC_VERSION_URL_KEY);
        }

        try {
            deviceId = rep.getValue(DEVICE_ID_KEY);
        } catch (OcException e) {
            LOG.debug("Field %s not found", DEVICE_ID_KEY);
        }

        try {
            dataModel = rep.getValue(DATA_MODEL_KEY);
        } catch (OcException e) {
            LOG.debug("Field %s not found", DATA_MODEL_KEY);
        }

        try {
            piid = rep.getValue(PIID_KEY);
        } catch (OcException e) {
            LOG.debug("Field %s not found", PIID_KEY);
        }

        try {
            locDescriptions = rep.getValue(DESCRIPTIONS_KEY);
        } catch (OcException e) {
            LOG.debug("Field %s not found", DESCRIPTIONS_KEY);
        }

        try {
            swVersion = rep.getValue(SW_VERSION_KEY);
        } catch (OcException e) {
            LOG.debug("Field %s not found", SW_VERSION_KEY);
        }

        try {
            manufacturerName = rep.getValue(MAN_NAME_KEY);
        } catch (OcException e) {
            LOG.debug("Field %s not found", MAN_NAME_KEY);
        }

        try {
            modelNumber = rep.getValue(MODEL_NO_KEY);
        } catch (OcException e) {
            LOG.debug("Field %s not found", MODEL_NO_KEY);
        }

        deviceTypes = rep.getResourceTypes();
    }

    public OcRepresentation getOcRepresentation() throws OcException {
        OcRepresentation rep = new OcRepresentation();
        rep.setValue(NAME_KEY, name);
        rep.setValue(SPEC_VERSION_URL_KEY, specVersionUrl);
        rep.setValue(DEVICE_ID_KEY, deviceId);
        rep.setValue(DATA_MODEL_KEY, dataModel);
        rep.setValue(PIID_KEY, piid);
        if (locDescriptions.size() > 0) {
            rep.setValue(DESCRIPTIONS_KEY, locDescriptions.toArray(new String[locDescriptions.size()]));
        }
        rep.setValue(SW_VERSION_KEY, swVersion);
        rep.setValue(MAN_NAME_KEY, manufacturerName);
        rep.setValue(MODEL_NO_KEY, modelNumber);

        rep.setResourceTypes(deviceTypes);

        return rep;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecVersionUrl() {
        return specVersionUrl;
    }

    public void setSpecVersionUrl(String specVersionUrl) {
        this.specVersionUrl = specVersionUrl;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDataModel() {
        return dataModel;
    }

    public void setDataModel(String dataModel) {
        this.dataModel = dataModel;
    }

    public String getPiid() {
        return piid;
    }

    public void setPiid(String piid) {
        this.piid = piid;
    }

    public List<String> getLocalizedDescriptions() {
        return locDescriptions;
    }

    public void setLocalizedDescriptions(List<String> locDescriptions) {
        this.locDescriptions = locDescriptions;
    }

    public String getSoftwareVersion() {
        return swVersion;
    }

    public void setSoftwareVersion(String swVersion) {
        this.swVersion = swVersion;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public List<String> getDeviceTypes() {
        return deviceTypes;
    }

    public List<String> getFormattedDeviceTypes() {
        List<String> formattedDeviceTypes = new ArrayList<>();
        for (String deviceType : deviceTypes) {
            if (!deviceType.equals("oic.wk.d")) {
                formattedDeviceTypes.add(deviceType.replace("oic.d", ""));
            }
        }

        return formattedDeviceTypes;
    }

    public void setDeviceTypes(List<String> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }

    @Override
    public String toString() {
        return "\t" + NAME_KEY + ": " + name
                + "\n\t" + SPEC_VERSION_URL_KEY + ": " + specVersionUrl
                + "\n\t" + DEVICE_ID_KEY + ": " + deviceId
                + "\n\t" + DATA_MODEL_KEY + ": " + dataModel
                + "\n\t" + PIID_KEY + ": " + piid
                + "\n\t" + DESCRIPTIONS_KEY + ": " + locDescriptions
                + "\n\t" + SW_VERSION_KEY + ": " + swVersion
                + "\n\t" + MAN_NAME_KEY + ": " + manufacturerName
                + "\n\t" + MODEL_NO_KEY + ": " + modelNumber;
    }
}
