package org.openconnectivity.otgc.view.cloud;

import com.jfoenix.controls.JFXTextField;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.openconnectivity.otgc.utils.util.Toast;
import org.openconnectivity.otgc.utils.viewmodel.Response;
import org.openconnectivity.otgc.utils.viewmodel.Status;
import org.openconnectivity.otgc.viewmodel.CloudViewModel;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class CloudView implements FxmlView<CloudViewModel>, Initializable {

    private final Logger LOG = Logger.getLogger(CloudView.class);

    @InjectViewModel
    private CloudViewModel viewModel;

    @Inject
    private Stage primaryStage;

    @FXML private Label statusValue;
    @FXML private JFXTextField authProviderTextField;
    @FXML private JFXTextField cloudUrlTextField;
    @FXML private JFXTextField accessTokenTextField;
    @FXML private JFXTextField cloudUuidTextField;

    private ResourceBundle resourceBundle;

    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;

        this.statusValue.textProperty().bind(viewModel.getStatusProperty());
        this.authProviderTextField.textProperty().bindBidirectional(viewModel.getAuthProviderProperty());
        this.cloudUrlTextField.textProperty().bindBidirectional(viewModel.getCloudUrlProperty());
        this.accessTokenTextField.textProperty().bindBidirectional(viewModel.getAccessTokenProperty());
        this.cloudUuidTextField.textProperty().bindBidirectional(viewModel.getCloudUuidProperty());

        viewModel.retrieveStatusResponseProperty().addListener(this::processRetrieveStatusResponse);
        viewModel.retrieveCloudConfResponseProperty().addListener(this::processRetrieveCloudConfResponse);
        viewModel.storeCloudConfigResponseProperty().addListener(this::processStoreCloudConfigResponse);
    }

    private void processRetrieveStatusResponse(ObservableValue<? extends Response<Integer>> observableValue, Response<Integer> oldValue, Response<Integer> newValue) {
        if (newValue.status == Status.ERROR) {
            Toast.show(primaryStage, resourceBundle.getString("cloud.retrieve_status.error"));
            LOG.debug(newValue.message);
        }
    }

    private void processRetrieveCloudConfResponse(ObservableValue<? extends Response<Void>> observableValue, Response<Void> oldValue, Response<Void> newValue) {
        if (newValue.status == Status.ERROR) {
            Toast.show(primaryStage, resourceBundle.getString("cloud.retrieve_cloud_conf.error"));
            LOG.debug(newValue.message);
        }
    }

    private void processStoreCloudConfigResponse(ObservableValue<? extends Response<Void>> observableValue, Response<Void> oldValue, Response<Void> newValue) {
        if (newValue.status == Status.SUCCESS) {
            Toast.show(primaryStage, resourceBundle.getString("cloud.store_cloud_conf.success"));
        } else if (newValue.status == Status.ERROR){
            Toast.show(primaryStage, resourceBundle.getString("cloud.store_cloud_conf.error"));
        }
    }

    @FXML
    public void handleSaveCloudConf() {
        String authProvider = authProviderTextField.textProperty().getValue();
        String cloudUrl = cloudUrlTextField.textProperty().getValue();
        String accessToken = accessTokenTextField.textProperty().getValue();
        String cloudUuid = cloudUuidTextField.textProperty().getValue();
        viewModel.provisionCloudConfiguration(authProvider, cloudUrl, accessToken, cloudUuid);
    }
}
