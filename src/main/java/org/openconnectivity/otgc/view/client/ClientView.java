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

package org.openconnectivity.otgc.view.client;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.controlsfx.control.ToggleSwitch;
import org.iotivity.*;
import org.openconnectivity.otgc.viewmodel.ClientViewModel;
import org.openconnectivity.otgc.domain.model.client.DynamicUiElement;
import org.openconnectivity.otgc.domain.model.client.SerializableResource;
import org.openconnectivity.otgc.domain.model.client.Info;
import org.openconnectivity.otgc.domain.model.resource.virtual.p.OcPlatformInfo;
import org.openconnectivity.otgc.utils.constant.NotificationKey;
import org.openconnectivity.otgc.domain.model.resource.virtual.d.OcDeviceInfo;
import org.openconnectivity.otgc.utils.util.Toast;
import org.openconnectivity.otgc.utils.viewmodel.Response;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ClientView  implements FxmlView<ClientViewModel>, Initializable {

    private final Logger LOG = Logger.getLogger(ClientView.class);

    @InjectViewModel
    private ClientViewModel viewModel;

    @Inject
    private Stage primaryStage;

    @Inject
    private NotificationCenter notificationCenter;

    @FXML private TabPane tabPane;
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
        viewModel.selectedTabProperty().setValue(tabPane.getSelectionModel().getSelectedItem().getText());
        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> viewModel.selectedTabProperty().bind(newValue.textProperty())
        );

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

    private void processDeviceInfoResponse(ObservableValue<? extends Response<OcDeviceInfo>> observable, Response<OcDeviceInfo> oldValue, Response<OcDeviceInfo> newValue) {
        switch (newValue.status)  {
            case LOADING:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, true);
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                viewModel.setDeviceInfo(newValue.data);
                break;
            case ERROR:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                LOG.error(newValue.message);
                Toast.show(primaryStage, resourceBundle.getString("client.device.error_response"));
                break;
        }
    }

    private void processPlatformInfoResponse(ObservableValue<? extends Response<OcPlatformInfo>> observableValue, Response<OcPlatformInfo> oldValue, Response<OcPlatformInfo> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, true);
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                viewModel.setPlatformInfo(newValue.data);
                break;
            default:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                LOG.error(newValue.message);
                Toast.show(primaryStage, resourceBundle.getString("client.platform.error_response"));
                break;
        }
    }

    private void processIntrospectResponse(ObservableValue<? extends Response<List<DynamicUiElement>>> observableValue, Response<List<DynamicUiElement>> oldValue, Response<List<DynamicUiElement>> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, true);
                break;
            case SUCCESS:
                if (!newValue.data.isEmpty()) {
                    notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                    viewModel.buildUiForIntrospect(newValue.data);
                    box.getChildren().clear();
                } else {
                    notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                    viewModel.findResources();
                }
                break;
            default:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                viewModel.findResources();
                break;
        }
    }

    private void processGetResourcesResponse(ObservableValue<? extends Response<List<SerializableResource>>> observableValue,
                                             Response<List<SerializableResource>> oldValue, Response<List<SerializableResource>> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, true);
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                viewModel.buildUiForRetrieveResources(newValue.data);
                box.getChildren().clear();
                break;
            default:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                Toast.show(primaryStage, resourceBundle.getString("client.introspection.error"));
                break;
        }
    }

    private void processGetRequestResponse(ObservableValue<? extends Response<SerializableResource>> observableValue,
                                             Response<SerializableResource> oldValue, Response<SerializableResource> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, true);
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                createResource(box, newValue.data);
                break;
            default:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                Toast.show(primaryStage, resourceBundle.getString("client.get_request.error"));
                break;
        }
    }

    private void processPostRequestResponse(ObservableValue<? extends Response<Boolean>> observableValue,
                                           Response<Boolean> oldValue, Response<Boolean> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, true);
                Toast.show(primaryStage, resourceBundle.getString("client_post_request.load"));
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                break;
            default:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                Toast.show(primaryStage, resourceBundle.getString("client.post_request.error"));
                break;
        }
    }

    private void processObserveResourceResponse(ObservableValue<? extends Response<SerializableResource>> observableValue,
                                           Response<SerializableResource> oldValue, Response<SerializableResource> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, true);
                break;
            case SUCCESS:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                // Update resource
                updateResource(box, newValue.data);
                break;
            default:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                Toast.show(primaryStage, "Observe request has failed");
                break;
        }
    }

    private void createResource(VBox container, SerializableResource resource) {
        Map<String, Object> resourceProperties = resource.getProperties();

        TitledPane paneResource = new TitledPane();
        paneResource.setText(resource.getUri());
        paneResource.setPrefHeight(Control.USE_COMPUTED_SIZE);
        paneResource.setPrefWidth(Control.USE_COMPUTED_SIZE);
        container.getChildren().add(paneResource);
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setPrefHeight(Control.USE_COMPUTED_SIZE);
        anchorPane.setPrefWidth(Control.USE_COMPUTED_SIZE);
        paneResource.setContent(anchorPane);

        createUI(anchorPane, resource, resourceProperties);
    }

    private void updateResource(VBox container, SerializableResource resource) {
        Map<String, Object> properties = resource.getProperties();

        ObservableList<Node> children = container.getChildren();
        for (Node child : children) {
            TitledPane pane = (TitledPane) child;
            String uri = resource.getUri();
            if (pane.getText().equals(uri)) {
                // Clean previous UI
                AnchorPane anchorPane = (AnchorPane) pane.getContent();
                if (Platform.isFxApplicationThread()) {
                    anchorPane.getChildren().clear();
                    createUI(anchorPane, resource, properties);
                } else {
                    Platform.runLater(() -> {
                        anchorPane.getChildren().clear();
                        createUI(anchorPane, resource, properties);
                    });
                }
            }
        }
    }

    private void createUI(AnchorPane anchorPane, SerializableResource resource, Map<String, Object> resourceProperties) {
        HBox hbox = new HBox();
        hbox.setPrefHeight(Control.USE_COMPUTED_SIZE);
        hbox.setPrefWidth(Control.USE_COMPUTED_SIZE);
        hbox.setPadding(new Insets(5, 10, 5, 10));
        hbox.setSpacing(5.0);
        anchorPane.getChildren().add(hbox);

        // Observable property
        VBox vboxObservable = new VBox();
        vboxObservable.setPrefHeight(Control.USE_COMPUTED_SIZE);
        vboxObservable.setPrefWidth(Control.USE_COMPUTED_SIZE);
        vboxObservable.setSpacing(5.0);
        Label labelObservable = new Label("Observe");
        labelObservable.setPadding(new Insets(0, 0, 0, 20));
        ToggleSwitch toggleSwitchObservable = new ToggleSwitch();
        toggleSwitchObservable.setSelected(resource.isObserving());
        vboxObservable.getChildren().add(labelObservable);
        vboxObservable.getChildren().add(toggleSwitchObservable);
        if (resource.isObservable()) {
            toggleSwitchObservable.selectedProperty().addListener(((observable, oldValue, newValue) -> {
                resource.setObserving(newValue);
                if (newValue) {
                    viewModel.registerResourceObserve(resource);
                } else {
                    viewModel.cancelResourceObserve(resource);
                }
            }));
        } else {
            vboxObservable.disableProperty().setValue(true);
        }
        hbox.getChildren().add(vboxObservable);

        for (String key : resourceProperties.keySet()) {
            VBox vbox = new VBox();
            vbox.setPrefHeight(Control.USE_COMPUTED_SIZE);
            vbox.setPrefWidth(Control.USE_COMPUTED_SIZE);
            vbox.setSpacing(5.0);

            if (resourceProperties.get(key) instanceof Boolean) {
                Label boolLabel = new Label(key);
                boolLabel.setPadding(new Insets(0, 0, 0, 20));
                ToggleSwitch toggleSwitch = new ToggleSwitch();
                toggleSwitch.setSelected((boolean)resourceProperties.get(key));
                vbox.getChildren().add(boolLabel);
                vbox.getChildren().add(toggleSwitch);
                if (viewModel.isViewEnabled(resource.getResourceInterfaces())) {
                    toggleSwitch.selectedProperty().addListener(((observable, oldValue, newValue) -> {
                        OCValue value = new OCValue();
                        value.setBool(newValue);

                        OCRepresentation rep = new OCRepresentation();
                        rep.setName(key);
                        rep.setType(OCType.OC_REP_BOOL);
                        rep.setValue(value);

                        viewModel.postRequest(resource, rep, null);
                    }));
                } else {
                    vbox.disableProperty().setValue(true);
                }
            } else if (resourceProperties.get(key) instanceof Integer) {
                Label integerLabel = new Label(key);
                TextField integerText = new TextField(String.valueOf((int)resourceProperties.get(key)));
                // TODO: TextField with integer format
                integerText.setMaxWidth(50.0);
                vbox.getChildren().add(integerLabel);
                vbox.getChildren().add(integerText);
                if (viewModel.isViewEnabled(resource.getResourceInterfaces())) {
                    integerText.focusedProperty().addListener(((observable, oldValue, newValue) -> {
                        if (newValue != oldValue && !newValue) {
                            Integer number;
                            try {
                                number = Integer.valueOf(integerText.getText());
                            } catch (NumberFormatException ex) {
                                return;
                            }
                            OCValue value = new OCValue();
                            value.setInteger(number);

                            OCRepresentation rep = new OCRepresentation();
                            rep.setName(key);
                            rep.setType(OCType.OC_REP_INT);
                            rep.setValue(value);

                            viewModel.postRequest(resource, rep, null);
                        }
                    }));
                } else {
                    vbox.disableProperty().setValue(true);
                }
            } else if (resourceProperties.get(key) instanceof Double) {
                Label doubleLabel = new Label(key);
                TextField doubleText = new TextField(String.valueOf((double)resourceProperties.get(key)));
                // TODO: TextField with decimal format
                doubleText.setMaxWidth(50.0);
                vbox.getChildren().add(doubleLabel);
                vbox.getChildren().add(doubleText);
                if (viewModel.isViewEnabled(resource.getResourceInterfaces())) {
                    doubleText.focusedProperty().addListener(((observable, oldValue, newValue) -> {
                        if (newValue != oldValue && !newValue) {
                            Double number;
                            try {
                                number = Double.valueOf(doubleText.getText());
                            } catch (NumberFormatException ex) {
                                return;
                            }

                            OCValue value = new OCValue();
                            value.setDouble(number);

                            OCRepresentation rep = new OCRepresentation();
                            rep.setName(key);
                            rep.setType(OCType.OC_REP_DOUBLE);
                            rep.setValue(value);

                            viewModel.postRequest(resource, rep, null);
                        }
                    }));
                } else {
                    vbox.disableProperty().setValue(true);
                }
            } else if (resourceProperties.get(key) instanceof String) {
                Label stringLabel = new Label(key);
                TextField stringText = new TextField((String)resourceProperties.get(key));
                stringText.setMaxWidth(150.0);
                vbox.getChildren().add(stringLabel);
                vbox.getChildren().add(stringText);
                if (viewModel.isViewEnabled(resource.getResourceInterfaces())) {
                    stringText.focusedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue != oldValue && !newValue) {
                            OCValue value = new OCValue();
                            value.setString(stringText.getText());

                            OCRepresentation rep = new OCRepresentation();
                            rep.setName(key);
                            rep.setType(OCType.OC_REP_STRING);
                            rep.setValue(value);

                            viewModel.postRequest(resource, rep, null);
                        }
                    });
                }
            } else if (resourceProperties.get(key) instanceof int[]) {
                Label integerArrayLabel = new Label(key);
                integerArrayLabel.setPadding(new Insets(0, 20, 0, 20));
                HBox intArrBox = new HBox();
                intArrBox.setPadding(new Insets(0, 10, 0, 10));
                intArrBox.setSpacing(10);
                intArrBox.setAlignment(Pos.CENTER);
                for (int v : (int[])resourceProperties.get(key)) {
                    TextField textResource = new TextField(String.valueOf(v));
                    textResource.setMaxWidth(50.0);
                    textResource.focusedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue != oldValue && !newValue) {
                            int[] ret = new int[intArrBox.getChildren().size()];
                            for (int i=0; i<intArrBox.getChildren().size(); i++) {
                                TextField tf = (TextField)intArrBox.getChildren().get(i);
                                ret[i] = Integer.parseInt(tf.getText());
                            }

                            OCRepresentation rep = new OCRepresentation();
                            rep.setName(key);
                            rep.setType(OCType.OC_REP_INT_ARRAY);

                            viewModel.postRequest(resource, rep, ret);
                        }
                    });

                    intArrBox.getChildren().add(textResource);
                }
                vbox.getChildren().add(integerArrayLabel);
                vbox.getChildren().add(intArrBox);
            } else if (resourceProperties.get(key) instanceof double[]) {
                Label doubleArrayLabel = new Label(key);
                doubleArrayLabel.setPadding(new Insets(0, 20, 0, 20));
                HBox doubleArrBox = new HBox();
                doubleArrBox.setPadding(new Insets(0, 10, 0, 10));
                doubleArrBox.setSpacing(10);
                for (double v : (double[])resourceProperties.get(key)) {
                    TextField textResource = new TextField(String.valueOf(v));
                    textResource.setMaxWidth(50.0);
                    textResource.focusedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue != oldValue && !newValue) {
                            double[] ret = new double[doubleArrBox.getChildren().size()];
                            for (int i=0; i<doubleArrBox.getChildren().size(); i++) {
                                TextField tf = (TextField)doubleArrBox.getChildren().get(i);
                                ret[i] = Double.parseDouble(tf.getText());
                            }

                            OCRepresentation rep = new OCRepresentation();
                            rep.setType(OCType.OC_REP_DOUBLE_ARRAY);
                            rep.setName(key);

                            viewModel.postRequest(resource, rep, ret);
                        }
                    });

                    doubleArrBox.getChildren().add(textResource);
                }
                vbox.getChildren().add(doubleArrayLabel);
                vbox.getChildren().add(doubleArrBox);
            } else if (resourceProperties.get(key) instanceof String[]) {
                Label stringArrayLabel = new Label(key);
                ComboBox<String> stringArrayComboBox = new ComboBox<>();
                stringArrayComboBox.getItems().addAll((String[])resourceProperties.get(key));
                stringArrayComboBox.getSelectionModel().selectFirst();
                vbox.getChildren().add(stringArrayLabel);
                vbox.getChildren().add(stringArrayComboBox);
            }

            if (vbox != null) {
                hbox.getChildren().add(vbox);
            }
        }
    }
}
