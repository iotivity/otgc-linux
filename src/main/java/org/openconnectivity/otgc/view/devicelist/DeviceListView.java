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

package org.openconnectivity.otgc.view.devicelist;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.openconnectivity.otgc.utils.constant.NotificationKey;
import org.openconnectivity.otgc.viewmodel.DeviceListViewModel;
import org.openconnectivity.otgc.domain.model.devicelist.Device;
import org.openconnectivity.otgc.utils.util.Toast;
import org.openconnectivity.otgc.utils.viewmodel.Response;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class DeviceListView implements FxmlView<DeviceListViewModel>, Initializable {

    private final Logger LOG = Logger.getLogger(DeviceListView.class);

    @InjectViewModel
    private DeviceListViewModel viewModel;

    @FXML private ListView<Device> listView;

    @Inject
    private Stage primaryStage;

    @Inject
    private NotificationCenter notificationCenter;

    private ResourceBundle resourceBundle;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;

        notificationCenter.subscribe(NotificationKey.OIC_STACK_INITIALIZED, ((key, payload) -> viewModel.onDiscoverRequest()));

        listView.itemsProperty().bind(viewModel.devicesListProperty());
        listView.setCellFactory(deviceListView -> new DeviceListViewCell());

        listView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            viewModel.selectedDeviceProperty().setValue(newValue);
            viewModel.positionSelectedDeviceProperty().setValue(viewModel.devicesListProperty().indexOf(newValue));
        }));

        viewModel.scanResponseProperty().addListener(this::processScanResponse);
        viewModel.updateDeviceResponseProperty().addListener(this::processUpdateDeviceResponse);
    }

    private void processScanResponse(ObservableValue<? extends Response<Device>> observableValue, Response<Device> oldValue, Response<Device> newValue) {
        switch (newValue.status) {
            case LOADING:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, true);
                Toast.show(primaryStage, resourceBundle.getString("devicelist.scan.load"));
                break;
            case SUCCESS:
                viewModel.addDeviceToList(newValue.data);
                break;
            case ERROR:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                Toast.show(primaryStage, resourceBundle.getString("devicelist.scan.error"));
                LOG.debug(newValue.message);
                break;
            case COMPLETE:
                notificationCenter.publish(NotificationKey.SET_PROGRESS_STATUS, false);
                Toast.show(primaryStage, resourceBundle.getString("devicelist.scan.success"));
                break;
        }
    }

    private void processUpdateDeviceResponse(ObservableValue<? extends Response<Device>> observableValue, Response<Device> oldValue, Response<Device> newValue) {
        switch (newValue.status) {
            case SUCCESS:
                listView.getSelectionModel().getSelectedItem().setDeviceType(newValue.data.getDeviceType());
                listView.refresh();
                break;
        }
    }
}
