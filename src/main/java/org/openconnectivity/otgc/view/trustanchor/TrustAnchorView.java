/*
 *  Copyright 2018 DEKRA Testing and Certification, S.A.U. All Rights Reserved.
 *
 *  *****************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openconnectivity.otgc.view.trustanchor;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXRadioButton;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.openconnectivity.otgc.domain.model.resource.secure.cred.OcCredential;
import org.openconnectivity.otgc.utils.util.Toast;
import org.openconnectivity.otgc.utils.viewmodel.Response;
import org.openconnectivity.otgc.viewmodel.TrustAnchorViewModel;

import javax.inject.Inject;
import java.io.*;
import java.net.URL;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

public class TrustAnchorView implements FxmlView<TrustAnchorViewModel>, Initializable {

    @InjectViewModel
    private TrustAnchorViewModel viewModel;

    @Inject
    private Stage primaryStage;

    private ResourceBundle resourceBundle;

    @FXML private ListView<OcCredential> listView;
    @FXML private JFXRadioButton rootRadioButton;
    @FXML private JFXRadioButton intermediateRadioButton;
    @FXML private JFXRadioButton endentityRadioButton;
    @FXML private VBox selectEndEntityLayout;
    @FXML private JFXComboBox<OcCredential> selectEndEntityCertificate;
    @FXML private JFXButton selectCertificateButton;
    @FXML private Label selectCertificateText;
    @FXML private VBox selectKeyLayout;
    @FXML private JFXButton selectKeyButton;
    @FXML private Label selectKeyText;
    @FXML private JFXButton infoCertificateButton;
    @FXML private JFXButton saveCertificateButton;
    @FXML private JFXButton removeCertificateButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resourceBundle = resourceBundle;

        selectCertificateText.setText("No selected certificate");
        selectKeyText.setText("No selected key");

        viewModel.retrieveCertificates();

        listView.itemsProperty().bind(viewModel.trustAnchorListProperty());
        listView.setCellFactory(deviceListView -> new TrustAnchorViewCell());

        selectEndEntityCertificate.itemsProperty().bind(viewModel.trustAnchorListProperty());
        selectEndEntityCertificate.setCellFactory(new Callback<ListView<OcCredential>, ListCell<OcCredential>>(){
            @Override
            public ListCell<OcCredential> call(ListView<OcCredential> p) {
                final ListCell<OcCredential> cell = new ListCell<OcCredential>() {
                    @Override
                    protected void updateItem(OcCredential t, boolean bln) {
                        super.updateItem(t, bln);

                        if (t != null){
                            setText("ID: " + t.getCredid());
                        } else {
                            setText(null);
                        }
                    }

                };
                return cell;
            }
        });

        viewModel.saveCertificateResponseProperty().addListener(this::processStoreTrustAnchorResponse);

        infoCertificateButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
                                                listView.getSelectionModel().getSelectedItem() == null,
                                                    listView.getSelectionModel().selectedItemProperty()));

        removeCertificateButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
                                                listView.getSelectionModel().getSelectedItem() == null,
                                                    listView.getSelectionModel().selectedItemProperty()));
    }

    @FXML
    public void handleInfoCertificateButton() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setResizable(true);
        alert.setTitle("Trust Anchor - Information");
        alert.setHeaderText(null);

        if (listView.getSelectionModel().getSelectedItem().getPublicData().getDerData() != null) {
            alert.setContentText(showX509CertificateInformation(listView.getSelectionModel().getSelectedItem().getPublicData().getDerData()));
        } else if (listView.getSelectionModel().getSelectedItem().getPublicData().getPemData() != null) {
            String pem = listView.getSelectionModel().getSelectedItem().getPublicData().getPemData();
            String base64 = pem.replaceAll("\\s", "")
                        .replaceAll("\\r\\n", "")
                        .replace("-----ENDCERTIFICATE-----", "")
                        .replace("\\u0000", "");
            String[] certList = base64.split("-----BEGINCERTIFICATE-----");
            String res = "";
            for (String cert : certList) {
                if (!cert.isEmpty()) {
                    byte[] byteArr = cert.getBytes();
                    byte[] der = Base64.decode(byteArr);
                    res += showX509CertificateInformation(der);
                    res += "\n";
                }
            }
            if (res.isEmpty()) {
                alert.setContentText("No information to show");
            }
            TextArea area = new TextArea(res);
            area.setWrapText(true);
            area.setEditable(false);

            alert.getDialogPane().setContent(area);
            alert.setResizable(true);
        }

        alert.getDialogPane().setMinWidth(600.0);

        alert.showAndWait();
    }

    private String showX509CertificateInformation(byte[] cert) {
        try (InputStream inputStream = new ByteArrayInputStream(cert)) {
            Security.addProvider(new BouncyCastleProvider());
            CertificateFactory factory = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
            X509Certificate caCert = (X509Certificate) factory.generateCertificate(inputStream);

            String pattern = "EEE, d MMM yyyy HH:mm:ss Z";
            SimpleDateFormat format = new SimpleDateFormat(pattern);

            String ret = "Subject\n\t" + caCert.getSubjectDN() + "\n" +
                    "Issuer\n\t" + caCert.getIssuerDN() + "\n" +
                    "Version " + caCert.getVersion() + "\n" +
                    "Serial Number\n" + caCert.getSerialNumber() + "\n" +
                    "Signature algorithm\n\t" + caCert.getSigAlgName() + "\n" +
                    "Validity\n" +
                    "\tNot Before\n" + format.format(caCert.getNotBefore()) + "\n" +
                    "\tNot After\n" + format.format(caCert.getNotAfter()) + "\n";


            return ret;
        } catch (Exception e) {
            // TODO:
        }

        return null;
    }

    @FXML
    public void handleCertificateGroup() {
        if (rootRadioButton.isSelected()) {
            selectEndEntityLayout.setDisable(true);
            selectKeyLayout.setDisable(true);
        } else if (intermediateRadioButton.isSelected()) {
            selectEndEntityLayout.setDisable(false);
            selectKeyLayout.setDisable(true);
        } else if (endentityRadioButton.isSelected()) {
            selectEndEntityLayout.setDisable(true);
            selectKeyLayout.setDisable(false);
        }
    }

    private File selectedCertificateFile = null;
    private File selectedKeyFile = null;

    @FXML
    public void handleSelectCertificateButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a new certificate");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All files", "*.*"),
                new FileChooser.ExtensionFilter("CER", "*.cer"),
                new FileChooser.ExtensionFilter("CRT", "*.crt")
        );
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            selectedCertificateFile = file;
            selectCertificateText.setText(selectedCertificateFile.getName());
        }
    }

    @FXML
    public void handleSelectKeyButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a new key for the selected certificate");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All files", "*.*"),
                new FileChooser.ExtensionFilter("KEY", "*.key")
        );
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            selectedKeyFile = file;
            selectKeyText.setText(selectedKeyFile.getName());
        }
    }

    private void processStoreTrustAnchorResponse(ObservableValue<? extends Response<Void>> obs, Response<Void> oldValue, Response<Void> newValue) {
        switch (newValue.status) {
            case ERROR:
                Toast.show(primaryStage, resourceBundle.getString("trustanchor.create_cred.error"));
                break;
            default:
                break;
        }
    }

    @FXML
    public void handleSaveCertificateButton() {
        if (selectedCertificateFile != null) {
            if (rootRadioButton.isSelected()) {
                viewModel.addTrustAnchor(selectedCertificateFile);
            } else if (intermediateRadioButton.isSelected()) {
                if (selectEndEntityCertificate.getSelectionModel().getSelectedItem() != null) {
                    viewModel.saveIntermediateCertificate(selectEndEntityCertificate.getSelectionModel().getSelectedItem().getCredid(), selectedCertificateFile);
                }
            } else if (endentityRadioButton.isSelected()) {
                viewModel.saveEndEntityCertificate(selectedCertificateFile, selectedKeyFile);
            }
        }
    }

    @FXML
    public void handleRemoveCertificateButton() {
        viewModel.removeCertificateByCredid(listView.getSelectionModel().getSelectedItem().getCredid());
    }
}