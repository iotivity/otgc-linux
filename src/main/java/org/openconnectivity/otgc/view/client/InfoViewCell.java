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

package org.openconnectivity.otgc.view.client;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import org.openconnectivity.otgc.domain.model.client.Info;

public class InfoViewCell extends ListCell<Info> {
    private GridPane gridPane = new GridPane();
    private Label infoTitle = new Label();
    private Label infoContent = new Label();

    private static final String DEVICE_INFO_VIEW_CELL_STYLE = "/styles/infocell.css";

    public InfoViewCell() {
        configureGrid();
        configureDevice();
        addControlsToGrid();
    }

    private void configureGrid() {
        gridPane.setHgap(10);
        gridPane.setVgap(4);
        gridPane.getStylesheets().add(DEVICE_INFO_VIEW_CELL_STYLE);
    }

    private void configureDevice() {
        infoTitle.getStyleClass().add("info_title");
        infoContent.getStyleClass().add("info_content");
    }

    private void addControlsToGrid() {
        gridPane.add(infoTitle, 0, 0);
        gridPane.add(infoContent, 1, 0);
    }

    @Override
    public void updateItem(Info info, boolean empty) {
        super.updateItem(info, empty);

        if (empty || info == null) {
            clearContent();
        } else {
            addContent(info);
        }
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }

    private void addContent(Info info) {
        setText(null);

        infoTitle.setText(info.getTitle());

        if (info.getContent() != null) {
            infoContent.setText(info.getContent());
        } else {
            infoContent.setText("");
        }

        setGraphic(gridPane);
    }

}
