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

package org.openconnectivity.otgc.view.login;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.openconnectivity.otgc.utils.util.OpenScene;
import org.openconnectivity.otgc.utils.viewmodel.Response;
import org.openconnectivity.otgc.utils.viewmodel.Status;
import org.openconnectivity.otgc.viewmodel.LoginViewModel;
import org.openconnectivity.otgc.view.main.MainView;
import org.openconnectivity.otgc.viewmodel.MainViewModel;

import javax.inject.Inject;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class LoginView implements FxmlView<LoginViewModel>, Initializable {

    private final Logger LOG = Logger.getLogger(LoginView.class);

    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Button loginButton;

    @InjectViewModel
    private LoginViewModel loginViewModel;

    @Inject
    private Stage primaryStage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        username.textProperty().bindBidirectional(loginViewModel.usernameProperty());
        password.textProperty().bindBidirectional(loginViewModel.passwordProperty());
        loginButton.disableProperty().bind(loginViewModel.loginButtonDisabled());

        loginViewModel.authenticatedResponseProperty().addListener(this::processAuthenticatedResponse);
        loginViewModel.isAuthResponseProperty().addListener(this::processAuthResponse);
    }

    private void processAuthenticatedResponse(ObservableValue<? extends Response<Void>> observableValue, Response<Void> oldValue, Response<Void> newValue) {
        if (newValue.status == Status.SUCCESS) {
            loadMainScene(true);
        } else {
            LOG.debug("Something went wrong with the authentication");
        }
    }

    private void processAuthResponse(ObservableValue<? extends Response<Boolean>> observableValue, Response<Boolean> oldValue, Response<Boolean> newValue) {
        if (newValue.status == Status.SUCCESS) {
            loadMainScene(newValue.data);
        } else {
            LOG.debug("Something went wrong with the authentication");
        }
    }

    @FXML
    public void handleLoginButton() {
        loginViewModel.authenticate();
    }

    private void loadMainScene(Boolean isAuth) {
        if (isAuth) {
            LOG.debug("Loading main screen");
            ViewTuple<MainView, MainViewModel> mainTuple = FluentViewLoader.fxmlView(MainView.class).load();
            List<String> styles = new ArrayList<>();
            try {
                OpenScene.start(primaryStage, mainTuple, styles);
            } catch(Exception ex) {
                LOG.debug(ex.getMessage());
            }
        }
    }
}
