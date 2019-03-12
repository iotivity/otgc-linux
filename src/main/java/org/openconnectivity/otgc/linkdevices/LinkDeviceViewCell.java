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

package org.openconnectivity.otgc.linkdevices;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;

public class LinkDeviceViewCell extends ListCell<String> {

    private GridPane gridPane = new GridPane();
    private Label deviceId = new Label();

    public LinkDeviceViewCell() {
        configureGrid();
        addControlsToGrid();
    }

    private void configureGrid() {
        gridPane.setHgap(10);
        gridPane.setVgap(4);
    }

    private void addControlsToGrid() {
        gridPane.add(deviceId, 0, 0);
    }

    @Override
    public void updateItem(String linkedDevice, boolean empty) {
        super.updateItem(linkedDevice, empty);

        if (empty || linkedDevice == null) {
            clearContent();
        } else {
            addContent(linkedDevice);
        }
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }

    private void addContent(String linkedDevice) {
        setText(null);

        deviceId.setText("UUID: " + linkedDevice);

        setGraphic(gridPane);
    }
}
