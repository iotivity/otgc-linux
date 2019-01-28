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

package org.openconnectivity.otgc.credential;

import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.iotivity.base.OicSecCred;
import org.iotivity.base.OicSecResr;

import java.util.ArrayList;
import java.util.List;

public class CredViewCell extends ListCell<OicSecCred> {
    private GridPane gridPane = new GridPane();
    private Label credId = new Label();
    private Label credSubject = new Label();
    private Label credType = new Label();
    private Label credRole = new Label();
    private Label credUsage = new Label();

    public CredViewCell() {
        configureGrid();
        addControlsToGrid();
    }

    private void configureGrid() {
        gridPane.setHgap(10);
        gridPane.setVgap(4);
    }

    private void addControlsToGrid() {
        gridPane.add(credId, 0, 0, 1, 3);
        gridPane.add(credSubject, 1, 0);
        gridPane.add(credType, 1, 1);
        gridPane.add(credUsage, 1, 2);
        gridPane.add(credRole, 1, 3);
    }

    @Override
    public void updateItem(OicSecCred cred, boolean empty) {
        super.updateItem(cred, empty);

        if (empty || cred == null) {
            clearContent();
        } else {
            addContent(cred);
        }
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }

    private void addContent(OicSecCred cred) {
        setText(null);

        credId.setText(String.valueOf(cred.getCredID()));
        credSubject.setText("UUID: " + cred.getSubject());
        credType.setText(cred.getCredType().toString());
        credUsage.setText(cred.getCredUsage());
        if (cred.getRole() != null) {
            credRole.setText("Role ID: " + cred.getRole().getId() + ", Role Authority: " + cred.getRole().getAuthority());
        }

        setGraphic(gridPane);
    }
}
