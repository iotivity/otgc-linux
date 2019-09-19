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

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.openconnectivity.otgc.domain.model.resource.secure.acl.OcAce;
import org.openconnectivity.otgc.domain.model.resource.secure.acl.OcAcl;
import org.openconnectivity.otgc.utils.constant.OcfWildcard;
import org.openconnectivity.otgc.viewmodel.AccessControlViewModel;
import org.openconnectivity.otgc.utils.constant.NotificationKey;
import org.openconnectivity.otgc.utils.util.Toast;
import org.openconnectivity.otgc.utils.viewmodel.Response;

import javax.inject.Inject;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class AccessControlView implements FxmlView<AccessControlViewModel>, Initializable {

    @InjectViewModel
    private AccessControlViewModel viewModel;

    @Inject
    private Stage primaryStage;

    @Inject
    private NotificationCenter notificationCenter;

    private ResourceBundle resourceBundle;

    @FXML private GridPane amsGrid;
    @FXML private ListView<OcAce> amsListView;
    @FXML private JFXRadioButton uuidRadioButton;
    @FXML private JFXRadioButton roleRadioButton;
    @FXML private JFXRadioButton conntypeRadioButton;
    @FXML private JFXTextField uuidTextField;
    @FXML private HBox roleBox;
    @FXML private JFXTextField roleidTextField;
    @FXML private JFXTextField authorityTextField;
    @FXML private VBox conntypeBox;
    @FXML private JFXRadioButton anonRadioButton;
    @FXML private JFXRadioButton authRadioButton;
    @FXML private JFXCheckBox wildcardCheck;
    @FXML private VBox wildcardBox;
    @FXML private JFXCheckBox wcAll;
    @FXML private JFXCheckBox wcAllSecure;
    @FXML private JFXCheckBox wcAllPublic;
    @FXML private ListView<String> verticalResourcesList;
    @FXML private JFXCheckBox createCheck;
    @FXML private JFXCheckBox retrieveCheck;
    @FXML private JFXCheckBox updateCheck;
    @FXML private JFXCheckBox deleteCheck;
    @FXML private JFXCheckBox notifyCheck;
    @FXML private JFXButton saveButton;
    @FXML private JFXButton deleteButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;

        amsGrid.visibleProperty().bind(viewModel.amsVisibleProperty());
        deleteButton.disableProperty().bind(Bindings.createBooleanBinding(() -> amsListView.getSelectionModel().getSelectedItem() == null, amsListView.getSelectionModel().selectedItemProperty()));

        amsListView.itemsProperty().bind(viewModel.aceListProperty());
        amsListView.setCellFactory(aceListView -> new AceViewCell());

        wcAll.selectedProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                wcAllSecure.setSelected(false);
                wcAllPublic.setSelected(false);
            }
        });
        wcAllSecure.selectedProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                wcAll.setSelected(false);
                wcAllPublic.setSelected(false);
            }
        });
        wcAllPublic.selectedProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                wcAll.setSelected(false);
                wcAllSecure.setSelected(false);
            }
        });

        verticalResourcesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        verticalResourcesList.itemsProperty().bind(viewModel.verticalResourceListProperty());
        verticalResourcesList.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends String> c) -> {
            while (c.next()) {
                for (String remitem : c.getRemoved()) {
                    viewModel.addSelectedVerticalResource(remitem, false);
                }
                for (String additem : c.getAddedSubList()) {
                    viewModel.addSelectedVerticalResource(additem, true);
                }
            }
        });

        viewModel.retrieveAclResponseProperty().addListener(this::processRetrieveAclResponse);
        viewModel.createAclResponseProperty().addListener(this::processCreateAclResponse);
        viewModel.deleteAclResponseProperty().addListener(this::processDeleteAclResponse);
        viewModel.retrieveVerticalResourcesResponseProperty().addListener(this::processRetrieveVerticalResourcesResponse);
    }

    @FXML
    public void handleSubjectGroup() {
        if (uuidRadioButton.isSelected()) {
            uuidTextField.setDisable(false);
            roleBox.setDisable(true);
            conntypeBox.setDisable(true);
        } else if (roleRadioButton.isSelected()) {
            uuidTextField.setDisable(true);
            roleBox.setDisable(false);
            conntypeBox.setDisable(true);
        } else if (conntypeRadioButton.isSelected()) {
            uuidTextField.setDisable(true);
            roleBox.setDisable(true);
            conntypeBox.setDisable(false);
        }
    }

    @FXML
    public void handleWildcardCheck() {
        if (wildcardCheck.isSelected()) {
            verticalResourcesList.setDisable(true);
            wildcardBox.setDisable(false);
        } else {
            verticalResourcesList.setDisable(false);
            wildcardBox.setDisable(true);
        }
    }

    private void processRetrieveAclResponse(ObservableValue<? extends Response<OcAcl>> observableValue, Response<OcAcl> oldValue, Response<OcAcl> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, true);
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                viewModel.setAcl(newValue.data);
                break;
            default:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                Toast.show(primaryStage, resourceBundle.getString("accesscontrol.retrieve_acl.error"));
                break;
        }
    }

    @FXML
    public void handleSaveACL() {
        boolean ok = false;
        boolean isWildcard = false;

        if (!wildcardBox.isDisable()) {
            if (wcAll.isSelected()) {
                isWildcard = true;
                viewModel.setWildcardSelectedVerticalResource(OcfWildcard.OC_WILDCARD_ALL_NCR);
            } else if (wcAllSecure.isSelected()) {
                isWildcard = true;
                viewModel.setWildcardSelectedVerticalResource(OcfWildcard.OC_WILDCARD_ALL_SECURE_NCR);
            } else if (wcAllPublic.isSelected()) {
                isWildcard = true;
                viewModel.setWildcardSelectedVerticalResource(OcfWildcard.OC_WILDCARD_ALL_NON_SECURE_NCR);
            }
        }

        if (uuidRadioButton.isSelected()) {
            if (uuidTextField.getText() != null) {
                ok = true;
                viewModel.createAce(uuidTextField.getText(), calculatePermission(), isWildcard);
            }
        } else if (roleRadioButton.isSelected()) {
            if (roleidTextField.getText() != null && authorityTextField.getText() != null) {
                ok = true;
                String roleid = roleidTextField.getText();
                String roleAuthority = authorityTextField.getText();
                viewModel.createAce(roleid, roleAuthority, calculatePermission(), isWildcard);
            }
        } else if (conntypeRadioButton.isSelected()) {
            ok = true;
            viewModel.createAce(authRadioButton.isSelected(), calculatePermission(), isWildcard);
        }

        if (!ok) {
            Toast.show(primaryStage, "Please, fill all available fields");
        }
    }

    private void processCreateAclResponse(ObservableValue<? extends Response<Boolean>> observableValue, Response<Boolean> oldValue, Response<Boolean> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, true);
                Toast.show(primaryStage, resourceBundle.getString("accesscontrol.create_acl.load"));
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                viewModel.loadAccessControl(viewModel.deviceProperty, viewModel.deviceProperty.get(), viewModel.deviceProperty.get());
                break;
            default:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                Toast.show(primaryStage, resourceBundle.getString("accesscontrol.create_acl.error"));
                break;
        }
    }

    @FXML
    public void handleDeleteACL() {
        viewModel.deleteACL(amsListView.getSelectionModel().getSelectedItem().getAceid());
    }

    private void processDeleteAclResponse(ObservableValue<? extends Response<Long>> observableValue, Response<Long> oldValue, Response<Long> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, true);
                Toast.show(primaryStage, resourceBundle.getString("accesscontrol.delete_acl.load"));
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                viewModel.loadAccessControl(viewModel.deviceProperty, viewModel.deviceProperty.get(), viewModel.deviceProperty.get());
                break;
            default:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                Toast.show(primaryStage, resourceBundle.getString("accesscontrol.delete_acl.error"));
                break;
        }
    }

    private void processRetrieveVerticalResourcesResponse (ObservableValue<? extends Response<List<String>>> observableValue, Response<List<String>> oldValue, Response<List<String>> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, true);
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                viewModel.addVerticalResourcesToList(newValue.data);
                break;
            default:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                Toast.show(primaryStage, resourceBundle.getString("accesscontrol.get_vertical_resources.error"));
                break;
        }
    }

    private int calculatePermission() {
        int permission = 0;
        if (createCheck.isSelected()) {
            permission += 1;
        }
        if (retrieveCheck.isSelected()) {
            permission += 2;
        }
        if (updateCheck.isSelected()) {
            permission += 4;
        }
        if (deleteCheck.isSelected()) {
            permission += 8;
        }
        if (notifyCheck.isSelected()) {
            permission += 16;
        }

        return permission;
    }
}
