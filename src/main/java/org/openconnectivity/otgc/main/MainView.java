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

package org.openconnectivity.otgc.main;

import com.jfoenix.controls.JFXTextField;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.controlsfx.control.StatusBar;
import org.iotivity.base.OcProvisioning;
import org.openconnectivity.otgc.common.constant.NotificationConstant;
import org.openconnectivity.otgc.common.util.Toast;
import org.openconnectivity.otgc.common.viewmodel.Response;
import org.openconnectivity.otgc.common.viewmodel.Status;
import org.openconnectivity.otgc.devicelist.DeviceListView;

import javax.inject.Inject;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainView implements FxmlView<MainViewModel>, Initializable {

    private final Logger LOG = Logger.getLogger(DeviceListView.class);

    @InjectViewModel
    private MainViewModel viewModel;

    @Inject
    private NotificationCenter notificationCenter;

    @Inject
    private Stage primaryStage;

    @FXML private StatusBar statusBar;
    @FXML private ProgressBar progressBar;

    private ResourceBundle resourceBundle;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;

        statusBar.setText("");
        progressBar.setProgress(1.0F);
        progressBar.progressProperty().bindBidirectional(viewModel.progressStatusProperty());
        viewModel.initOicStackResponseProperty().addListener(this::processInitOicStackResponse);
    }

    private String verifyPin = "";
    OcProvisioning.PinCallbackListener randomPinCallbackListener = () -> {
        LOG.debug("Inside randomPinListener");
        final Object lock = new Object();
        Platform.runLater(() -> {
            ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
            ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

            Alert alertDialog = new Alert(Alert.AlertType.CONFIRMATION);
            alertDialog.setTitle("Insert random PIN");
            alertDialog.setHeaderText("PIN: ");
            final JFXTextField input = new JFXTextField();
            alertDialog.getDialogPane().setGraphic(input);
            alertDialog.getButtonTypes().clear();
            alertDialog.getButtonTypes().add(yesButton);
            alertDialog.getButtonTypes().add(noButton);

            Optional<ButtonType> result = alertDialog.showAndWait();
            if (result.get() == yesButton) {
                alertDialog.close();
                try {
                    synchronized (lock) {
                        verifyPin = input.getText().toString();
                        lock.notifyAll();
                    }
                } catch (Exception ex) {
                    LOG.error(ex.getLocalizedMessage());
                }
            } else if (result.get() == noButton) {
                alertDialog.close();
                try {
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                } catch (Exception ex) {
                    LOG.error(ex.getLocalizedMessage());
                }
            }
        });

        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOG.error(ex.getLocalizedMessage());
            }
        }

        LOG.debug("Verify after submit = " + verifyPin);

        return verifyPin;
    };

    OcProvisioning.DisplayPinListener displayPinListener = pin -> {
        LOG.debug("Inside displayPinListener");
        Platform.runLater(() -> {
            ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);

            Alert alertDialog = new Alert(Alert.AlertType.WARNING);
            alertDialog.setTitle("Pin");
            alertDialog.setHeaderText("PIN: " + pin);
            alertDialog.getButtonTypes().clear();
            alertDialog.getButtonTypes().add(closeButton);

            Optional<ButtonType> result = alertDialog.showAndWait();
            if (result.get() == closeButton) {
                alertDialog.close();
            }
        });
    };


    private void processInitOicStackResponse(ObservableValue<? extends Response<Void>> observableValue, Response<Void> oldValue, Response<Void> newValue) {

        if (newValue.status == Status.SUCCESS) {
            // Notify OIC stack is initialized
            notificationCenter.publish(NotificationConstant.OIC_STACK_INITIALIZED);

            // Set listener for each OTM
            viewModel.setRandomPinListener(randomPinCallbackListener);
            viewModel.setDisplayPinListener(displayPinListener);

            Toast.show(primaryStage, resourceBundle.getString("main.init_oic_stack.success"));
        } else {
            Toast.show(primaryStage, resourceBundle.getString("main.init_oic_stack.error"));
            LOG.debug(newValue.message);
        }
    }
}
