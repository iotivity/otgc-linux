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

package org.openconnectivity.otgc.view.trustanchor;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import org.iotivity.OCCredUtil;
import org.openconnectivity.otgc.domain.model.resource.secure.cred.OcCredential;

public class TrustAnchorViewCell extends ListCell<OcCredential> {

    private GridPane gridPane = new GridPane();
    private Label credId = new Label();
    private Label credSubject = new Label();
    private Label credUsage = new Label();

    public TrustAnchorViewCell() {
        configureGrid();
        addControlsToGrid();
    }

    private void configureGrid() {
        gridPane.setHgap(10);
        gridPane.setVgap(4);
    }

    private void addControlsToGrid() {
        gridPane.add(credId, 0, 0);
        gridPane.add(credSubject, 0, 1);
        gridPane.add(credUsage, 0, 2);
    }

    @Override
    public void updateItem(OcCredential cred, boolean empty) {
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

    private void addContent(OcCredential cred) {
        setText(null);

        credId.setText("Credential ID: " + String.valueOf(cred.getCredid()));
        credSubject.setText("UUID: " + cred.getSubjectuuid());
        credSubject.setText("Usage: " + cred.getCredusage());

        setGraphic(gridPane);
    }
}

