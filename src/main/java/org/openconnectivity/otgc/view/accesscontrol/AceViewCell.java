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

package org.openconnectivity.otgc.view.accesscontrol;

import com.jfoenix.controls.JFXCheckBox;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.openconnectivity.otgc.domain.model.resource.secure.acl.OcAce;
import org.openconnectivity.otgc.domain.model.resource.secure.acl.OcAceResource;

import java.util.ArrayList;
import java.util.List;

public class AceViewCell extends ListCell<OcAce> {

    private GridPane gridPane = new GridPane();
    private Label aceId = new Label();
    private Label subject = new Label();
    public HBox permissionBox = new HBox();
    private JFXCheckBox createCheck = new JFXCheckBox() {
        @Override
        public void arm() {}
    };
    private JFXCheckBox retrieveCheck = new JFXCheckBox() {
        @Override
        public void arm() {}
    };
    private JFXCheckBox updateCheck = new JFXCheckBox() {
        @Override
        public void arm() {}
    };
    private JFXCheckBox deleteCheck = new JFXCheckBox() {
        @Override
        public void arm() {}
    };
    private JFXCheckBox notifyCheck = new JFXCheckBox() {
        @Override
        public void arm() {}
    };
    private ListView<String> resourceList = new ListView<>();

    private final int ROW_HEIGHT = 24;

    public AceViewCell() {
        configureGrid();
        addControlsToGrid();
    }

    private void configureGrid() {
        gridPane.setHgap(10);
        gridPane.setVgap(4);
    }

    private void addControlsToGrid() {
        createCheck.setText("C");
        permissionBox.getChildren().add(createCheck);
        retrieveCheck.setText("R");
        permissionBox.getChildren().add(retrieveCheck);
        updateCheck.setText("U");
        permissionBox.getChildren().add(updateCheck);
        deleteCheck.setText("D");
        permissionBox.getChildren().add(deleteCheck);
        notifyCheck.setText("N");
        permissionBox.getChildren().add(notifyCheck);
        permissionBox.setSpacing(10.0);

        gridPane.add(aceId, 0, 0, 1, 3);
        gridPane.add(subject, 1, 0);
        gridPane.add(permissionBox, 1, 1);
        gridPane.add(resourceList, 1, 2);
    }

    @Override
    public void updateItem(OcAce ace, boolean empty) {
        super.updateItem(ace, empty);

        if (empty || ace == null) {
            clearContent();
        } else {
            addContent(ace);
        }
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }

    private void addContent(OcAce ace) {
        setText(null);

        aceId.setText(String.valueOf(ace.getAceid()));
        switch (ace.getSubject().getType()) {
            case UUID_TYPE:
                subject.setText("UUID: " + ace.getSubject().getUuid());
                break;
            case ROLE_TYPE:
                subject.setText("OcCredRole ID: " + ace.getSubject().getRoleId() +
                                ", OcCredRole Authority: " + ace.getSubject().getAuthority());
                break;
            case CONN_TYPE:
                if (ace.getSubject().getConnType().equals("anon-clear")) {
                    subject.setText("Connection type: Anonymous");
                } else if (ace.getSubject().getConnType().equals("auth-crypt")) {
                    subject.setText("Connection type: Authenticated");
                }
                break;
            default:
                break;
        }
        createCheck.selectedProperty().setValue((ace.getPermission() & 1) == 1);
        retrieveCheck.selectedProperty().setValue((ace.getPermission() & 2) == 2);
        updateCheck.selectedProperty().setValue((ace.getPermission() & 4) == 4);
        deleteCheck.selectedProperty().setValue((ace.getPermission() & 8) == 8);
        notifyCheck.selectedProperty().setValue((ace.getPermission() & 16) == 16);

        if (ace.getResources() != null) {
            List<String> hrefList = new ArrayList<>();
            for (OcAceResource resource : ace.getResources()) {
                hrefList.add(resource.getHref() != null ?
                        resource.getHref() : "All resources");
            }

            resourceList.setItems(FXCollections.observableArrayList(hrefList));
            resourceList.setPrefHeight(hrefList.size() * ROW_HEIGHT + 2);
        }

        setGraphic(gridPane);
    }
}
