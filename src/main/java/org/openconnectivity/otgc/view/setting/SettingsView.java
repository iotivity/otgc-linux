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

package org.openconnectivity.otgc.view.setting;

import com.jfoenix.controls.JFXTextField;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextFormatter;
import org.openconnectivity.otgc.utils.validator.PositiveIntegerValidator;
import org.openconnectivity.otgc.viewmodel.SettingsViewModel;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsView implements FxmlView<SettingsViewModel>, Initializable {

    @InjectViewModel
    private SettingsViewModel viewModel;

    @FXML private JFXTextField jfxDiscoveryTimeout;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        jfxDiscoveryTimeout.textProperty().bindBidirectional(viewModel.discoveryTimeoutProperty());
        jfxDiscoveryTimeout.setTextFormatter(new TextFormatter<>(PositiveIntegerValidator.getFilter()));
    }
}
