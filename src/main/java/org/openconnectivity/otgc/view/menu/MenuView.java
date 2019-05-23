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
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.openconnectivity.otgc.viewmodel.MenuViewModel;
import org.openconnectivity.otgc.view.about.AboutView;
import org.openconnectivity.otgc.utils.util.DialogHelper;
import org.openconnectivity.otgc.view.setting.SettingsView;

import java.net.URL;
import java.util.ResourceBundle;

public class MenuView implements FxmlView<MenuViewModel>, Initializable {

    @InjectViewModel
    private MenuViewModel viewModel;

    @Inject
    private Stage primaryStage;

    private ResourceBundle resourceBundle;

    @FXML private Label deviceUuidLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;

        deviceUuidLabel.textProperty().bind(viewModel.deviceUuidProperty());
    }

    @FXML
    public void discover() {
        // TODO
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
}
