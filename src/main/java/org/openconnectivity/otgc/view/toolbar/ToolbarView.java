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

package org.openconnectivity.otgc.view.toolbar;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.openconnectivity.otgc.utils.constant.NotificationKey;
import org.openconnectivity.otgc.domain.model.devicelist.Device;
import org.openconnectivity.otgc.utils.constant.OcfOxmType;
import org.openconnectivity.otgc.utils.constant.OtgcMode;
import org.openconnectivity.otgc.utils.util.DialogHelper;
import org.openconnectivity.otgc.utils.util.Toast;
import org.openconnectivity.otgc.utils.viewmodel.Response;
import org.openconnectivity.otgc.view.trustanchor.TrustAnchorView;
import org.openconnectivity.otgc.viewmodel.ToolbarViewModel;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;

public class ToolbarView implements FxmlView<ToolbarViewModel>, Initializable {

    private final Logger LOG = Logger.getLogger(ToolbarView.class);

    @InjectViewModel
    private ToolbarViewModel viewModel;

    private ResourceBundle resourceBundle;

    @Inject
    private Stage primaryStage;

    @Inject
    private NotificationCenter notificationCenter;

    @FXML private JFXButton onboardButton;
    @FXML private JFXButton offboardButton;
    @FXML private JFXButton clientModeButton;
    @FXML private JFXButton obtModeButton;
    @FXML private JFXButton trustAnchorButton;

    private int positionBeingUpdated = 0;

    private OcfOxmType selectedOxm;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;

        onboardButton.disableProperty().bind(viewModel.onboardButtonDisabled());
        offboardButton.disableProperty().bind(viewModel.offboardButtonDisabled());

        //clientModeButton.disableProperty().bind(viewModel.clientModeButtonDisabled());
        //obtModeButton.disableProperty().bind(viewModel.obtModeButtonDisabled());

        viewModel.setOxmListener(this::onGetOxM);

        viewModel.otmResponseProperty().addListener(this::processOtmResponse);
        viewModel.offboardResponseProperty().addListener(this::processOffboardResponse);
        viewModel.clientModeResponseProperty().addListener(this::processClientModeResponse);
        viewModel.obtModeResponseProperty().addListener(this::processObtModeResponse);
        viewModel.modeResponseProperty().addListener(this::processModeResponse);

        notificationCenter.subscribe(NotificationKey.OIC_STACK_INITIALIZED, (key, payload) -> viewModel.getMode());
    }

    public OcfOxmType onGetOxM(List<OcfOxmType> supportedOxm) {
        selectedOxm = null;
        List<CharSequence> options = new ArrayList<>();
        if (supportedOxm.contains(OcfOxmType.OC_OXMTYPE_JW)) {
            options.add(resourceBundle.getString("oxm.just_works"));
        }
        if (supportedOxm.contains(OcfOxmType.OC_OXMTYPE_RDP)) {
            options.add(resourceBundle.getString("oxm.random_pin"));
        }
        if (supportedOxm.contains(OcfOxmType.OC_OXMTYPE_MFG_CERT)) {
            options.add(resourceBundle.getString("oxm.manufacturer_certificate"));
        }

        final Object lock = new Object();
        Platform.runLater(() -> {
            ChoiceDialog alertDialog = new ChoiceDialog(options.get(0), options);
            alertDialog.setTitle(resourceBundle.getString("dialog.title.select_oxm"));
            alertDialog.setHeaderText("OXM: ");

            Optional<String> result = alertDialog.showAndWait();
            if (result.isPresent()) {
                try {
                    synchronized (lock) {
                        String strSelect = result.get();
                        if (strSelect.equals(resourceBundle.getString("oxm.just_works"))) {
                            selectedOxm = OcfOxmType.OC_OXMTYPE_JW;
                        } else if (strSelect.equals(resourceBundle.getString("oxm.random_pin"))) {
                            selectedOxm = OcfOxmType.OC_OXMTYPE_RDP;
                        } else if (strSelect.equals(resourceBundle.getString("oxm.manufacturer_certificate"))) {
                            selectedOxm = OcfOxmType.OC_OXMTYPE_MFG_CERT;
                        }
                        lock.notifyAll();
                    }
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage());
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

        return selectedOxm;
    }

    @FXML
    public void handleDiscoverButton() {
        viewModel.onScanPressed();
    }

    @FXML
    public void handleOnboardButton() {
        switch(viewModel.deviceProperty.get().getDeviceType()) {
            case UNOWNED:
                positionBeingUpdated = viewModel.positionDeviceProperty().get();
                viewModel.doOwnershipTransfer(viewModel.deviceProperty.get());
                break;
            case OWNED_BY_SELF:
            case OWNED_BY_OTHER:
            default:
                break;
        }
    }

    @FXML
    public void handleOffboardButton() {
        positionBeingUpdated = viewModel.positionDeviceProperty().get();

        viewModel.offboard(viewModel.deviceProperty.get());
    }

    @FXML
    public void handleClientModeButton() {
        showConfirmSetMode(OtgcMode.CLIENT, false);
    }

    @FXML
    public void handleObtModeButton() {
        showConfirmSetMode(OtgcMode.OBT, false);
    }

    @FXML
    public void handleResetButton() {
        if (obtModeButton.isDisable()) {
            showConfirmSetMode(OtgcMode.OBT, true);
        } else if (clientModeButton.isDisable()) {
            showConfirmSetMode(OtgcMode.CLIENT, true);
        }
    }

    @FXML
    public void handleTrustAnchorButton() {
        Parent view = FluentViewLoader.fxmlView(TrustAnchorView.class).load().getView();
        DialogHelper.showDialog(view, primaryStage, resourceBundle.getString("trust_anchor.window.title"));
    }

    private void processOtmResponse(ObservableValue<? extends Response<Device>> observableValue, Response<Device> oldValue, Response<Device> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, true);
                Toast.show(primaryStage, resourceBundle.getString("toolbar.otm.load"));
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                if (newValue.data != null) {
                    showSetDeviceNameDialog(positionBeingUpdated, newValue.data, newValue.data.getDeviceId(), newValue.data.getDeviceInfo().getName());
                    positionBeingUpdated = 0;
                } else {
                    Toast.show(primaryStage, resourceBundle.getString("toolbar.otm.error_client_mode"));
                }
                break;
            case ERROR:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                LOG.debug(newValue.message);
                Toast.show(primaryStage, resourceBundle.getString("toolbar.otm.error"));
                break;
        }
    }

    private void processOffboardResponse(ObservableValue<? extends Response<Device>> observableValue, Response<Device> oldValue, Response<Device> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, true);
                Toast.show(primaryStage, resourceBundle.getString("toolbar.offboard.load"));
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                if (newValue.data != null) {
                    viewModel.updateItem(positionBeingUpdated, newValue.data);
                    positionBeingUpdated = 0;
                } else {
                    Toast.show(primaryStage, resourceBundle.getString("toolbar.otm.error_client_mode"));
                }
                break;
            default:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                Toast.show(primaryStage, resourceBundle.getString("toolbar.offboard.error"));
                break;
        }
    }

    private void processClientModeResponse(ObservableValue<? extends Response<Void>> observableValue, Response<Void> oldValue, Response<Void> newValue) {
        switch (newValue.status) {
            case LOADING:
                Toast.show(primaryStage, resourceBundle.getString("toolbar.client_mode.load"));
                break;
            case SUCCESS:
                Toast.show(primaryStage, resourceBundle.getString("toolbar.client_mode.success"));
                notificationCenter.publish(NotificationKey.OTGC_RESET);
                viewModel.onScanPressed();
                break;
            default:
                Toast.show(primaryStage, resourceBundle.getString("toolbar.client_mode.error"));
                break;
        }
    }

    private void processObtModeResponse(ObservableValue<? extends Response<Void>> observableValue, Response<Void> oldValue, Response<Void> newValue) {
        switch (newValue.status) {
            case LOADING:
                Toast.show(primaryStage, resourceBundle.getString("toolbar.obt_mode.load"));
                break;
            case SUCCESS:
                Toast.show(primaryStage, resourceBundle.getString("toolbar.obt_mode.success"));
                notificationCenter.publish(NotificationKey.OTGC_RESET);
                viewModel.onScanPressed();
                break;
            default:
                Toast.show(primaryStage, resourceBundle.getString("toolbar.obt_mode.error"));
                break;
        }
    }

    private void processModeResponse(ObservableValue<? extends Response<String>> observableValue, Response<String> oldValue, Response<String> newValue) {
        switch (newValue.status) {
            case LOADING:
                break;
            case SUCCESS:
                if (newValue.data.equals(OtgcMode.CLIENT)) {
                    clientModeButton.setDisable(true);
                    obtModeButton.setDisable(false);
                } else if (newValue.data.equals(OtgcMode.OBT)) {
                    obtModeButton.setDisable(true);
                    clientModeButton.setDisable(false);
                }
                break;
            case ERROR:
                break;
        }
    }

    private void showSetDeviceNameDialog(int position, Device device, String deviceId, String currentDeviceName) {
        Platform.runLater(() -> {
            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);

            Alert alertDialog = new Alert(Alert.AlertType.CONFIRMATION);
            alertDialog.setTitle(resourceBundle.getString("dialog.title.set_device_name"));
            alertDialog.setHeaderText("Device name: ");
            final JFXTextField input = new JFXTextField();
            input.setText(currentDeviceName);
            alertDialog.getDialogPane().setGraphic(input);
            alertDialog.getButtonTypes().clear();
            alertDialog.getButtonTypes().add(okButton);

            Optional<ButtonType> result = alertDialog.showAndWait();
            if (result.get() == okButton) {
                viewModel.setDeviceName(deviceId, input.getText());
                device.getDeviceInfo().setName(input.getText());
                viewModel.updateItem(position, device);
            }
        });
    }

    private void showConfirmSetMode(String mode, boolean reset) {
        Platform.runLater(() -> {
            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);

            Alert alertDialog = new Alert(Alert.AlertType.CONFIRMATION);
            alertDialog.setHeaderText(resourceBundle.getString("dialog.title.confirm_reset_mode"));
            alertDialog.setContentText(resourceBundle.getString("dialog.message.confirm_reset_mode"));
            alertDialog.getButtonTypes().clear();
            alertDialog.getButtonTypes().add(okButton);

            Optional<ButtonType> result = alertDialog.showAndWait();
            if (result.get() == okButton) {
                if (reset) {
                    if (mode.equals(OtgcMode.OBT)) {
                        viewModel.resetObtMode();
                    } else if (mode.equals(OtgcMode.CLIENT)) {
                        viewModel.resetClientMode();
                    }
                } else {
                    if (mode.equals(OtgcMode.OBT)) {
                        viewModel.setObtMode();
                    } else if (mode.equals(OtgcMode.CLIENT)) {
                        viewModel.setClientMode();
                    }
                }

            }
        });
    }
}