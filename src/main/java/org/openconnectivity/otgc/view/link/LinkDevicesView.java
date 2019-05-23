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

package org.openconnectivity.otgc.view.link;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.openconnectivity.otgc.utils.constant.NotificationKey;
import org.openconnectivity.otgc.utils.util.Toast;
import org.openconnectivity.otgc.utils.viewmodel.Response;
import org.openconnectivity.otgc.viewmodel.LinkDevicesViewModel;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class LinkDevicesView implements FxmlView<LinkDevicesViewModel>, Initializable {

    @InjectViewModel
    private LinkDevicesViewModel viewModel;

    private ResourceBundle resourceBundle;

    @Inject
    private Stage primaryStage;

    @Inject
    private NotificationCenter notificationCenter;

    @FXML
    private GridPane linkGrid;
    @FXML private ListView<String> linkDevicesList;
    @FXML private ListView<String> linkRoleList;
    @FXML private JFXRadioButton uuidRadioButton;
    @FXML private JFXRadioButton roleRadioButton;
    @FXML private JFXComboBox<String> deviceListUuidBox;
    @FXML private JFXTextField roleidTextField;
    @FXML private JFXTextField roleauthorityTextField;
    @FXML private HBox roleBox;
    @FXML private JFXButton unlinkButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resourceBundle = resources;

        linkGrid.visibleProperty().bind(viewModel.linkedDevicesVisibleProperty());

        linkDevicesList.itemsProperty().bind(viewModel.linkedDevicesListProperty());
        linkDevicesList.setCellFactory(credListView -> new LinkDeviceViewCell());
        linkRoleList.itemsProperty().bind(viewModel.linkedRoleListProperty());
        deviceListUuidBox.itemsProperty().bind(viewModel.ownedDevicesProperty());
        unlinkButton.disableProperty().bind(Bindings.createBooleanBinding(() -> (linkDevicesList.getSelectionModel().getSelectedItem() == null && linkRoleList.getSelectionModel().getSelectedItem() == null), linkDevicesList.getSelectionModel().selectedItemProperty(), linkRoleList.getSelectionModel().selectedItemProperty()));

        viewModel.retrieveLinkedDevicesResponseProperty().addListener(this::processRetrieveLinkedDevicesResponse);
        viewModel.retrieveLinkedRolesResponseProperty().addListener(this::processRetrieveLinkedRolesResponse);
    }

    @FXML
    public void handleLinkTypeGroup() {
        if (uuidRadioButton.isSelected()) {
            deviceListUuidBox.setDisable(false);
            roleBox.setDisable(true);
        } else if (roleRadioButton.isSelected()) {
            deviceListUuidBox.setDisable(true);
            roleBox.setDisable(false);
        }
    }

    private void processRetrieveLinkedDevicesResponse(ObservableValue<? extends Response<List<String>>> observableValue, Response<List<String>> oldValue, Response<List<String>> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, true);
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                viewModel.setLinkedDevices(newValue.data);
                break;
            default:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                Toast.show(primaryStage, resourceBundle.getString("linkeddevices.retrieve_linked_devices.error"));
                break;
        }
    }

    private void processRetrieveLinkedRolesResponse(ObservableValue<? extends Response<List<String>>> observableValue, Response<List<String>> oldValue, Response<List<String>> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, true);
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                viewModel.setLinkedRoles(newValue.data);
                break;
            default:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                Toast.show(primaryStage, resourceBundle.getString("linkeddevices.retrieve_linked_devices.error"));
                break;
        }
    }

    public void handleLinkDevicesButton() {
        if (uuidRadioButton.isSelected()) {
            viewModel.linkDevices(deviceListUuidBox.getSelectionModel().getSelectedItem());
        } else if (roleRadioButton.isSelected()) {
            if (roleidTextField.getText() != null) {
                viewModel.linkRoleCertificate(roleidTextField.getText(), roleauthorityTextField.getText());
            } else {
                Toast.show(primaryStage, "Please, fill all available fields");
            }
        }
    }

    public void handleUnlinkDevicesButton() {
        if (linkDevicesList.getSelectionModel().getSelectedItem() != null) {
            viewModel.unlinkDevices(linkDevicesList.getSelectionModel().getSelectedItem());
        }
        if (linkRoleList.getSelectionModel().getSelectedItem() != null) {
            viewModel.unlinkRole(linkRoleList.getSelectionModel().getSelectedItem());
        }
    }
}
