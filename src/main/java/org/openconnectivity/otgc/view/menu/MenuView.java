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

package org.openconnectivity.otgc.view.menu;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.*;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.iotivity.OCCloudStatusMask;
import org.openconnectivity.otgc.utils.constant.NotificationKey;
import org.openconnectivity.otgc.utils.util.Toast;
import org.openconnectivity.otgc.utils.viewmodel.Response;
import org.openconnectivity.otgc.utils.viewmodel.Status;
import org.openconnectivity.otgc.view.cloud.CloudView;
import org.openconnectivity.otgc.view.toolbar.ToolbarView;
import org.openconnectivity.otgc.viewmodel.MenuViewModel;
import org.openconnectivity.otgc.view.about.AboutView;
import org.openconnectivity.otgc.utils.util.DialogHelper;
import org.openconnectivity.otgc.view.setting.SettingsView;
import org.openconnectivity.otgc.viewmodel.ToolbarViewModel;

import java.net.URL;
import java.util.ResourceBundle;

public class MenuView implements FxmlView<MenuViewModel>, Initializable {

    private final Logger LOG = Logger.getLogger(MenuView.class);

    @InjectViewModel
    private MenuViewModel viewModel;

    @Inject
    private Stage primaryStage;

    @javax.inject.Inject
    private NotificationCenter notificationCenter;

    private ResourceBundle resourceBundle;

    @FXML private Label deviceUuidLabel;
    @FXML private Menu cloud;
    @FXML private MenuItem cloudRegister;
    @FXML private MenuItem cloudLogged;
    @FXML private MenuItem cloudRefreshToken;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;

        deviceUuidLabel.textProperty().bind(viewModel.deviceUuidProperty());
        cloud.setOnShowing(this::retrieveCloudStatus);

        viewModel.retrieveStatusResponseProperty().addListener(this::processRetrieveStatusResponse);
    }

    @FXML
    public void discover() {
        viewModel.discover();
    }

    @FXML
    public void close() {
        viewModel.closeAction();
    }

    @FXML
    public void settings() {
        Parent view = FluentViewLoader.fxmlView(SettingsView.class).load().getView();
        DialogHelper.showDialog(view, primaryStage, resourceBundle.getString("settings.window.title"));
    }

    @FXML
    public void about() {
        Parent view = FluentViewLoader.fxmlView(AboutView.class).load().getView();
        DialogHelper.showDialog(view, primaryStage, resourceBundle.getString("about.window.title"), "/styles/about.css");
    }

    @FXML
    public void cloudConfiguration() {
        Parent view = FluentViewLoader.fxmlView(CloudView.class).load().getView();
        DialogHelper.showDialog(view, primaryStage, resourceBundle.getString("cloud.window.title"), "/styles/cloud.css");
    }

    private void retrieveCloudStatus(Event event) {
        if (event.getEventType() == Menu.ON_SHOWING) {
            viewModel.retrieveCloudStatus();
        }
    }

    private void processRetrieveStatusResponse(ObservableValue<? extends Response<Integer>> observableValue, Response<Integer> oldValue, Response<Integer> newValue) {
        if (newValue.status == Status.ERROR) {
            Toast.show(primaryStage, resourceBundle.getString("cloud.retrieve_status.error"));
            LOG.debug(newValue.message);
        } else if (newValue.status == Status.SUCCESS) {
            int status = newValue.data;
            if (status == OCCloudStatusMask.OC_CLOUD_INITIALIZED) {
                cloudRegister.visibleProperty().setValue(true);
                cloudRegister.textProperty().setValue(resourceBundle.getString("menu.cloud.register"));
                cloudRegister.onActionProperty().setValue(this::cloudRegister);
                cloudLogged.visibleProperty().setValue(false);
                cloudRefreshToken.visibleProperty().setValue(false);
                notificationCenter.publish(NotificationKey.CLOUD_UNREGISTER);
            }
            if ((status & OCCloudStatusMask.OC_CLOUD_REGISTERED) == OCCloudStatusMask.OC_CLOUD_REGISTERED) {
                cloudRegister.visibleProperty().setValue(true);
                cloudRegister.textProperty().setValue(resourceBundle.getString("menu.cloud.deregister"));
                cloudRegister.onActionProperty().setValue(this::cloudDeregister);
                cloudLogged.visibleProperty().setValue(true);
                cloudLogged.textProperty().setValue(resourceBundle.getString("menu.cloud.login"));
                cloudLogged.onActionProperty().setValue(this::cloudLogin);
                cloudRefreshToken.visibleProperty().setValue(true);
                notificationCenter.publish(NotificationKey.CLOUD_REGISTER);
            }
            if ((status & OCCloudStatusMask.OC_CLOUD_LOGGED_IN) == OCCloudStatusMask.OC_CLOUD_LOGGED_IN) {
                cloudRegister.visibleProperty().setValue(true);
                cloudRegister.textProperty().setValue(resourceBundle.getString("menu.cloud.deregister"));
                cloudRegister.onActionProperty().setValue(this::cloudDeregister);
                cloudLogged.visibleProperty().setValue(true);
                cloudLogged.textProperty().setValue(resourceBundle.getString("menu.cloud.logout"));
                cloudLogged.onActionProperty().setValue(this::cloudLogout);
                cloudRefreshToken.visibleProperty().setValue(true);
                notificationCenter.publish(NotificationKey.CLOUD_REGISTER);
            }
            if ((status & OCCloudStatusMask.OC_CLOUD_TOKEN_EXPIRY) == OCCloudStatusMask.OC_CLOUD_TOKEN_EXPIRY) {
                viewModel.retrieveTokenExpiry();
            }
            if ((status & OCCloudStatusMask.OC_CLOUD_LOGGED_OUT) == OCCloudStatusMask.OC_CLOUD_LOGGED_OUT) {
                cloudRegister.visibleProperty().setValue(true);
                cloudRegister.textProperty().setValue(resourceBundle.getString("menu.cloud.deregister"));
                cloudRegister.onActionProperty().setValue(this::cloudDeregister);
                cloudLogged.visibleProperty().setValue(true);
                cloudLogged.textProperty().setValue(resourceBundle.getString("menu.cloud.login"));
                cloudLogged.onActionProperty().setValue(this::cloudLogin);
                cloudRefreshToken.visibleProperty().setValue(true);
                notificationCenter.publish(NotificationKey.CLOUD_REGISTER);
            }
            if ((status & OCCloudStatusMask.OC_CLOUD_DEREGISTERED) == OCCloudStatusMask.OC_CLOUD_DEREGISTERED) {
                cloudRegister.visibleProperty().setValue(true);
                cloudRegister.textProperty().setValue(resourceBundle.getString("menu.cloud.register"));
                cloudRegister.onActionProperty().setValue(this::cloudRegister);
                cloudLogged.visibleProperty().setValue(false);
                cloudRefreshToken.visibleProperty().setValue(false);
                notificationCenter.publish(NotificationKey.CLOUD_UNREGISTER);
            }
        }
    }

    private void cloudRegister(ActionEvent event) {
        viewModel.cloudRegister();
    }

    private void cloudDeregister(ActionEvent event) {
        viewModel.cloudDeregister();
    }

    private void cloudLogin(ActionEvent event) {
        viewModel.cloudLogin();
    }

    private void cloudLogout(ActionEvent event) {
        viewModel.cloudLogout();
    }

    @FXML
    public void cloudRefreshToken() {
        viewModel.refreshToken();
    }
}
