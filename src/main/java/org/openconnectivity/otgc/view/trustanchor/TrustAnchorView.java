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
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.openconnectivity.otgc.domain.model.resource.secure.cred.OcCredential;
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

    @FXML private ListView<OcCredential> listView;
    @FXML private JFXButton infoCaButton;
    @FXML private JFXButton removeCaButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        viewModel.retrieveTrustAnchors();

        listView.itemsProperty().bind(viewModel.trustAnchorListProperty());
        listView.setCellFactory(deviceListView -> new TrustAnchorViewCell());

        infoCaButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
                                                listView.getSelectionModel().getSelectedItem() == null,
                                                    listView.getSelectionModel().selectedItemProperty()));

        removeCaButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
                                                listView.getSelectionModel().getSelectedItem() == null,
                                                    listView.getSelectionModel().selectedItemProperty()));
    }

    @FXML
    public void handleInfoCaButton() {
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
                                .replace("-----BEGINCERTIFICATE-----", "")
                                .replace("-----ENDCERTIFICATE-----", "");
            byte[] der = Base64.decode(base64.getBytes());
            alert.setContentText(showX509CertificateInformation(der));
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
    public void handleAddCaButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a new Trust Anchor");
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
            viewModel.addTrustAnchor(file);
        }
    }

    @FXML
    public void handleRemoveCaButton() {
        viewModel.removeTrustAnchorByCredid(listView.getSelectionModel().getSelectedItem().getCredid());
    }
}