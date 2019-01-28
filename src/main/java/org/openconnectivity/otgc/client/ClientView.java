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

package org.openconnectivity.otgc.client;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.openconnectivity.otgc.client.domain.model.DynamicUiElement;
import org.openconnectivity.otgc.client.domain.model.SerializableResource;
import org.openconnectivity.otgc.client.model.Info;
import org.openconnectivity.otgc.client.model.OicPlatform;
import org.openconnectivity.otgc.common.constant.NotificationConstant;
import org.openconnectivity.otgc.common.domain.model.OcDevice;
import org.openconnectivity.otgc.common.util.Toast;
import org.openconnectivity.otgc.common.viewmodel.Response;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ClientView  implements FxmlView<ClientViewModel>, Initializable {

    private final Logger LOG = Logger.getLogger(ClientView.class);

    @InjectViewModel
    private ClientViewModel viewModel;

    @Inject
    private Stage primaryStage;

    @Inject
    private NotificationCenter notificationCenter;

    @FXML private Tab clientTab;
    private VBox box;

    @FXML
    private ListView<Info> listView;

    private ResourceBundle resourceBundle;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;

        listView.itemsProperty().bind(viewModel.infoListProperty());
        listView.setCellFactory(deviceListView -> new InfoViewCell());

        initClientTab();
        box.visibleProperty().bind(viewModel.clientVisibleProperty());
        box.visibleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                if (Platform.isFxApplicationThread()) {
                    box.getChildren().clear();
                } else {
                    Platform.runLater(() -> {
                        box.getChildren().clear();
                    });
                }
            }
        });

        primaryStage.setOnHiding((event) -> {
            viewModel.cancellAllObserveResource();
        });

        viewModel.deviceInfoResponseProperty().addListener(this::processDeviceInfoResponse);
        viewModel.platformInfoResponseProperty().addListener(this::processPlatformInfoResponse);
        viewModel.introspectResponseProperty().addListener(this::processIntrospectResponse);
        viewModel.getResourcesResponseProperty().addListener(this::processGetResourcesResponse);
        viewModel.getRequestResponseProperty().addListener(this::processGetRequestResponse);
        viewModel.postRequestResponseProperty().addListener(this::processPostRequestResponse);
        viewModel.observeResourceResponseProperty().addListener(this::processObserveResourceResponse);
    }

    private void initClientTab() {
        ScrollPane scroll = new ScrollPane();
        scroll.setPrefWidth(Control.USE_COMPUTED_SIZE);
        scroll.setPrefHeight(Control.USE_COMPUTED_SIZE);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(true);
        clientTab.setContent(scroll);
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setPrefHeight(Control.USE_COMPUTED_SIZE);
        anchorPane.setPrefWidth(Control.USE_COMPUTED_SIZE);
        scroll.setContent(anchorPane);
        box = new VBox();
        box.setSpacing(10.0);
        box.setPrefHeight(Control.USE_COMPUTED_SIZE);
        box.setPrefWidth(Control.USE_COMPUTED_SIZE);
        box.setPadding(new Insets(15, 12, 15, 12));
        anchorPane.getChildren().add(box);
    }

    private void processDeviceInfoResponse(ObservableValue<? extends Response<OcDevice>> observable, Response<OcDevice> oldValue, Response<OcDevice> newValue) {
        switch (newValue.status)  {
            case LOADING:
                notificationCenter.publish(NotificationConstant.SET_PROGRESS_STATUS, true);
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationConstant.SET_PROGRESS_STATUS, false);
                viewModel.setDeviceInfo(newValue.data);
                break;
            case ERROR:
                notificationCenter.publish(NotificationConstant.SET_PROGRESS_STATUS, false);
                LOG.error(newValue.message);
                Toast.show(primaryStage, resourceBundle.getString("client.device.error_response"));
                break;
        }
    }

    private void processPlatformInfoResponse(ObservableValue<? extends Response<OicPlatform>> observableValue, Response<OicPlatform> oldValue, Response<OicPlatform> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationConstant.SET_PROGRESS_STATUS, true);
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationConstant.SET_PROGRESS_STATUS, false);
                viewModel.setPlatformInfo(newValue.data);
                break;
            default:
                notificationCenter.publish(NotificationConstant.SET_PROGRESS_STATUS, false);
                LOG.error(newValue.message);
                Toast.show(primaryStage, resourceBundle.getString("client.platform.error_response"));
                break;
        }
    }

    private void processIntrospectResponse(ObservableValue<? extends Response<List<DynamicUiElement>>> observableValue, Response<List<DynamicUiElement>> oldValue, Response<List<DynamicUiElement>> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationConstant.SET_PROGRESS_STATUS, true);
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationConstant.SET_PROGRESS_STATUS, false);
                viewModel.buildUiForIntrospect(newValue.data);
                break;
            default:
                notificationCenter.publish(NotificationConstant.SET_PROGRESS_STATUS, false);
                viewModel.findResources(viewModel.deviceProperty.get().getDeviceId());
                break;
        }
    }

    private void processGetResourcesResponse(ObservableValue<? extends Response<List<SerializableResource>>> observableValue,
                                             Response<List<SerializableResource>> oldValue, Response<List<SerializableResource>> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationConstant.SET_PROGRESS_STATUS, true);
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationConstant.SET_PROGRESS_STATUS, false);
                viewModel.buildUiForRetrieveResources(newValue.data);
                break;
            default:
                notificationCenter.publish(NotificationConstant.SET_PROGRESS_STATUS, false);
                Toast.show(primaryStage, resourceBundle.getString("client.introspection.error"));
                break;
        }
    }

    private void processGetRequestResponse(ObservableValue<? extends Response<SerializableResource>> observableValue,
                                             Response<SerializableResource> oldValue, Response<SerializableResource> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationConstant.SET_PROGRESS_STATUS, true);
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationConstant.SET_PROGRESS_STATUS, false);
                viewModel.createResource(box, newValue.data);
                break;
            default:
                notificationCenter.publish(NotificationConstant.SET_PROGRESS_STATUS, false);
                Toast.show(primaryStage, resourceBundle.getString("client.get_request.error"));
                break;
        }
    }

    private void processPostRequestResponse(ObservableValue<? extends Response<SerializableResource>> observableValue,
                                           Response<SerializableResource> oldValue, Response<SerializableResource> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationConstant.SET_PROGRESS_STATUS, true);
                Toast.show(primaryStage, resourceBundle.getString("client_post_request.load"));
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationConstant.SET_PROGRESS_STATUS, false);
                // Update resource
                break;
            default:
                notificationCenter.publish(NotificationConstant.SET_PROGRESS_STATUS, false);
                Toast.show(primaryStage, resourceBundle.getString("client.post_request.error"));
                break;
        }
    }

    private void processObserveResourceResponse(ObservableValue<? extends Response<SerializableResource>> observableValue,
                                           Response<SerializableResource> oldValue, Response<SerializableResource> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationConstant.SET_PROGRESS_STATUS, true);
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationConstant.SET_PROGRESS_STATUS, false);
                // Update resource
                viewModel.updateResource(box, newValue.data);
                break;
            default:
                notificationCenter.publish(NotificationConstant.SET_PROGRESS_STATUS, false);
                Toast.show(primaryStage, "Observe request has failed");
                break;
        }
    }
}
