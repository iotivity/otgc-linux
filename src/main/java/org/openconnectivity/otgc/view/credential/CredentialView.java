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

package org.openconnectivity.otgc.view.credential;

import com.jfoenix.controls.JFXButton;
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
import org.openconnectivity.otgc.domain.model.resource.secure.cred.OcCredentials;
import org.openconnectivity.otgc.utils.constant.NotificationKey;
import org.openconnectivity.otgc.utils.util.Toast;
import org.openconnectivity.otgc.utils.viewmodel.Response;
import org.openconnectivity.otgc.domain.model.resource.secure.cred.OcCredential;
import org.openconnectivity.otgc.viewmodel.CredentialViewModel;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class CredentialView implements FxmlView<CredentialViewModel>, Initializable {

    @InjectViewModel
    private CredentialViewModel viewModel;

    @Inject
    private Stage primaryStage;

    @Inject
    private NotificationCenter notificationCenter;

    private ResourceBundle resourceBundle;

    @FXML private GridPane cmsGrid;
    @FXML private ListView<OcCredential> cmsListView;
    @FXML private JFXRadioButton identityRadioButton;
    @FXML private JFXRadioButton roleRadioButton;
    @FXML private HBox roleBox;
    @FXML private JFXTextField roleidTextField;
    @FXML private JFXTextField authorityTextField;
    @FXML private JFXButton saveButton;
    @FXML private JFXButton deleteButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;

        cmsGrid.visibleProperty().bind(viewModel.cmsVisibleProperty());
        deleteButton.disableProperty().bind(Bindings.createBooleanBinding(() -> cmsListView.getSelectionModel().getSelectedItem() == null, cmsListView.getSelectionModel().selectedItemProperty()));

        cmsListView.itemsProperty().bind(viewModel.credListProperty());
        cmsListView.setCellFactory(credListView -> new CredViewCell());

        viewModel.createCredResponseProperty().addListener(this::processCreateCredResponse);
        viewModel.retrieveCredsResponseProperty().addListener(this::processRetrieveCredsResponse);
        viewModel.deleteCredResponseProperty().addListener(this::processDeleteCredResponse);
    }

    @FXML
    public void handleCredentialGroup() {
        if (identityRadioButton.isSelected()) {
            roleBox.setDisable(true);
        } else if (roleRadioButton.isSelected()) {
            roleBox.setDisable(false);
        }
    }

    @FXML
    public void handleSaveCredential() {
        boolean ok = false;
        if (roleRadioButton.isSelected()) {
            if (roleidTextField.getText() != null && authorityTextField.getText() != null) {
                ok = true;
                String roleid = roleidTextField.getText();
                String roleAuthority = authorityTextField.getText();
                viewModel.provisionRoleCertificate(roleid, roleAuthority);
            }
        } else if (identityRadioButton.isSelected()) {
            ok = true;
            viewModel.provisionIdentityCertificate();
        }

        if (!ok) {
            Toast.show(primaryStage, "Please, fill all available fields");
        }
    }

    private void processCreateCredResponse(ObservableValue<? extends Response<Boolean>> observableValue, Response<Boolean> oldValue, Response<Boolean> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, true);
                Toast.show(primaryStage, resourceBundle.getString("credential.create_credential.load"));
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                viewModel.loadCredentials(viewModel.deviceProperty, viewModel.deviceProperty.get(), viewModel.deviceProperty.get());
                break;
            default:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                Toast.show(primaryStage, resourceBundle.getString("credential.create_credential.error"));
                break;
        }
    }

    @FXML
    public void handleDeleteCredential() {
        viewModel.deleteCred(cmsListView.getSelectionModel().getSelectedItem().getCredid());
    }

    private void processDeleteCredResponse(ObservableValue<? extends Response<Boolean>> observableValue, Response<Boolean> oldValue, Response<Boolean> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, true);
                Toast.show(primaryStage, resourceBundle.getString("credential.delete_credential.load"));
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                viewModel.loadCredentials(viewModel.deviceProperty, viewModel.deviceProperty.get(), viewModel.deviceProperty.get());
                break;
            default:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                Toast.show(primaryStage, resourceBundle.getString("credential.delete_credential.error"));
                break;
        }
    }

    private void processRetrieveCredsResponse(ObservableValue<? extends Response<OcCredentials>> observableValue, Response<OcCredentials> oldValue, Response<OcCredentials> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, true);
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                viewModel.setCred(newValue.data);
                break;
            default:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                Toast.show(primaryStage, resourceBundle.getString("credential.retrieve_credentials.error"));
                break;
        }
    }
}
