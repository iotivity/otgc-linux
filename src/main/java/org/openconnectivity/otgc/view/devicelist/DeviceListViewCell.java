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

package org.openconnectivity.otgc.view.devicelist;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.openconnectivity.otgc.domain.model.devicelist.Device;
import org.openconnectivity.otgc.domain.model.devicelist.DeviceRole;

import java.util.Arrays;
import java.util.ResourceBundle;

public class DeviceListViewCell extends ListCell<Device> {

    ResourceBundle resourceBundle;

    private GridPane grid = new GridPane();
    private Pane pane = new Pane();
    private Label deviceName = new Label();
    private Label deviceUuid = new Label();
    private Label deviceRole = new Label();
    private Label deviceType = new Label();

    private static final String DEVICE_LIST_VIEW_CELL_STYLE = "/styles/devicelistcell.css";
    private static final String DEVICE_LIST_VIEW_CELL_STYLE_TITLE = "device_cell_title";
    private static final String DEVICE_LIST_VIEW_CELL_STYLE_INFO = "device_cell_info";

    public DeviceListViewCell() {
        resourceBundle = ResourceBundle.getBundle("properties.Strings");

        configureGrid();
        configurePane();
        configureDevice();
        addControlsToGrid();
    }

    private void configureGrid() {
        grid.setHgap(10);
        grid.setVgap(4);
        grid.getStylesheets().add(DEVICE_LIST_VIEW_CELL_STYLE);
    }

    private void configurePane() {
        pane.setPrefSize(5.0, 5.0);
    }

    private void configureDevice() {
        deviceName.getStyleClass().add(DEVICE_LIST_VIEW_CELL_STYLE_TITLE);
        deviceUuid.getStyleClass().add(DEVICE_LIST_VIEW_CELL_STYLE_INFO);
        deviceRole.getStyleClass().add(DEVICE_LIST_VIEW_CELL_STYLE_INFO);
        deviceType.getStyleClass().add(DEVICE_LIST_VIEW_CELL_STYLE_INFO);
    }

    private void addControlsToGrid() {
        grid.add(pane, 0, 0, 1, 4);
        grid.add(deviceName, 1, 0);
        grid.add(deviceUuid, 1, 1);
        grid.add(deviceRole, 1, 2);
        grid.add(deviceType, 1, 3);
    }

    @Override
    public void updateItem(Device device, boolean empty) {
        super.updateItem(device, empty);

        if (empty || device == null) {
            clearContent();
        } else {
            addContent(device);
        }
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }

    private void addContent(Device device) {
        setText(null);

        if (device.getDeviceInfo() != null) {
            deviceName.setText(
                    device.getDeviceInfo().getName() == null || device.getDeviceInfo().getName().isEmpty()  ?
                    "Unnamed" :
                    device.getDeviceInfo().getName()
            );

            if (!device.getDeviceInfo().getFormattedDeviceTypes().isEmpty()) {
                deviceType.setText(
                        Arrays.toString(device.getDeviceInfo().getFormattedDeviceTypes().toArray())
                );
            } else {
                deviceType.setText("No standard device types");
            }
        } else {
            deviceName.setText("Unnamed");
            deviceType.setText("No standard device types");
        }

        deviceUuid.setText(device.getDeviceId());

        if (device.getDeviceRole().equals(DeviceRole.CLIENT)) {
            deviceRole.setText(resourceBundle.getString("devicelistcell.client"));
        } else if (device.getDeviceRole().equals(DeviceRole.SERVER)) {
            deviceRole.setText(resourceBundle.getString("devicelistcell.server"));
        } else {
            deviceRole.setText(resourceBundle.getString("devicelistcell.unknown"));
        }

        setStyleClassDependingOnFoundOwner(device);

        setGraphic(grid);
    }

    private void setStyleClassDependingOnFoundOwner(Device device) {
        pane.getStyleClass().clear();

        switch (device.getDeviceType()) {
            case UNOWNED:
                pane.getStyleClass().add("ocf_dark_blue");
                break;
            case OWNED_BY_SELF:
                pane.getStyleClass().add("ocf_green");
                break;
            case OWNED_BY_OTHER:
                pane.getStyleClass().add("ocf_orange");
                break;
            case OWNED_BY_OTHER_WITH_PERMITS:
                pane.getStyleClass().add("ocf_yellow");
                break;
            case CLOUD:
                pane.getStyleClass().add("ocf_purple");
                break;
            default:
                pane.getStyleClass().add("ocf_black");
                break;
        }
    }
}
