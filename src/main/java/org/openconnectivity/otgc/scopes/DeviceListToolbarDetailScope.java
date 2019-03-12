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

package org.openconnectivity.otgc.scopes;

import de.saxsys.mvvmfx.Scope;
import javafx.beans.property.*;
import org.openconnectivity.otgc.devicelist.domain.model.Device;

public class DeviceListToolbarDetailScope implements Scope {

    private final ObjectProperty<Device> selectedDevice = new SimpleObjectProperty<>(this, "selectedDevice");
    public ObjectProperty<Device> selectedDeviceProperty() {
        return this.selectedDevice;
    }

    private final IntegerProperty positionSelectedDevice = new SimpleIntegerProperty();
    public IntegerProperty positionSelectedDeviceProperty() {
        return this.positionSelectedDevice;
    }

    private ListProperty<Device> devicesList = new SimpleListProperty<>();
    public ListProperty<Device> devicesListProperty() { return devicesList; }

    private StringProperty selectedTab = new SimpleStringProperty();
    public StringProperty selectedTabProperty() {
        return this.selectedTab;
    }

    public final Device getSelectedDevice() {
        return this.selectedDeviceProperty().get();
    }
    public final void setSelectedDevice(final Device selectedDevice) {
        this.selectedDeviceProperty().set(selectedDevice);
    }

    public final int getPositionSelectedDevice() {
        return this.positionSelectedDeviceProperty().get();
    }
    public final void setPositionSelectedDevice(final int position) {
        this.positionSelectedDeviceProperty().set(position);
    }
}
