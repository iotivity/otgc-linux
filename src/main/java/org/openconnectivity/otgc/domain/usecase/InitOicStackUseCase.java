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

package org.openconnectivity.otgc.domain.usecase;

import io.reactivex.Completable;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.iotivity.OCFactoryPresetsHandler;
import org.iotivity.OCObt;
import org.iotivity.OCPki;
import org.openconnectivity.otgc.utils.constant.OtgcConstant;
import org.openconnectivity.otgc.data.repository.*;
import org.openconnectivity.otgc.utils.constant.OtgcMode;

import javax.inject.Inject;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Optional;

public class InitOicStackUseCase {

    private final IotivityRepository iotivityRepository;
    private final IORepository ioRepository;
    private final SettingRepository settingRepository;

    @Inject
    public InitOicStackUseCase(IotivityRepository iotivityRepository,
                               IORepository ioRepository,
                               SettingRepository settingRepository) {
        this.iotivityRepository = iotivityRepository;
        this.ioRepository = ioRepository;
        this.settingRepository = settingRepository;
    }

    public Completable execute() {
        Completable initOic = iotivityRepository.setFactoryResetHandler(factoryReset)
                .andThen(iotivityRepository.initOICStack());

        Completable completable;
        if (Boolean.valueOf(settingRepository.get(SettingRepository.FIRST_RUN_KEY, SettingRepository.FIRST_RUN_DEFAULT_VALUE))) {
            completable = initOic
                    .andThen(settingRepository.set(SettingRepository.FIRST_RUN_KEY, "false"))
                    .andThen(settingRepository.set(SettingRepository.MODE_KEY, OtgcMode.OBT));
        } else {
            completable = initOic;
        }

        return completable;

    }

    private OCFactoryPresetsHandler factoryReset = (device -> {
        try {
            factoryResetHandler(device);
        } catch (Exception e) {
            // TODO:
        }
    });
    private void factoryResetHandler(long device) throws Exception {
        /* Current date */
        Date date = new Date();

        // End-entity certs only loaded when using Client Mode
        if (settingRepository.get(SettingRepository.MODE_KEY, OtgcMode.CLIENT).equals(OtgcMode.CLIENT)) {
            // Kyrio end-entity cert
            X509Certificate eeCert = ioRepository.getFileAsX509Certificate(OtgcConstant.DATA_PATH + OtgcConstant.KYRIO_EE_CERTIFICATE).blockingGet();
            if (date.after(eeCert.getNotBefore()) && date.before(eeCert.getNotAfter())) {
                byte[] kyrioEeCertificate = ioRepository.getBytesFromFile(OtgcConstant.KYRIO_EE_CERTIFICATE).blockingGet();
                // private key of Kyrio end-entity cert
                byte[] kyrioEeKey = ioRepository.getBytesFromFile(OtgcConstant.KYRIO_EE_KEY).blockingGet();
                int credid = OCPki.addMfgCert(device, kyrioEeCertificate, kyrioEeKey);
                if (credid == -1) {
                    throw new Exception("Add identity certificate error");
                }

                // Kyrio intermediate cert
                X509Certificate subCaCert = ioRepository.getFileAsX509Certificate(OtgcConstant.DATA_PATH + OtgcConstant.KYRIO_SUBCA_CERTIFICATE).blockingGet();
                if (date.after(subCaCert.getNotBefore()) && date.before(subCaCert.getNotAfter())) {
                    byte[] kyrioSubcaCertificate = ioRepository.getBytesFromFile(OtgcConstant.KYRIO_SUBCA_CERTIFICATE).blockingGet();
                    if (OCPki.addMfgIntermediateCert(device, credid, kyrioSubcaCertificate) == -1) {
                        throw new Exception("Add intermediate certificate error");
                    }
                } else {
                    showPopupNotValidCertificate("Kyrio intermediate certificate is not valid.");
                }
            } else {
                showPopupNotValidCertificate("Kyrio end entity certificate is not valid.");
            }
        }

        /* CloudCA root cert */
        X509Certificate cloudCert = ioRepository.getFileAsX509Certificate(OtgcConstant.DATA_PATH + OtgcConstant.CLOUD_ROOT_CERTIFICATE).blockingGet();
        if (date.after(cloudCert.getNotBefore()) && date.before(cloudCert.getNotAfter())) {
            byte[] cloudRootcaCertificate = ioRepository.getBytesFromFile(OtgcConstant.CLOUD_ROOT_CERTIFICATE).blockingGet();
            if (OCPki.addMfgTrustAnchor(device, cloudRootcaCertificate) == -1) {
                throw new Exception("Add root certificate error");
            }
        } else {
            showPopupNotValidCertificate("Cloud CA root certificate is not valid.");
        }

        /* Kyrio root cert */
        X509Certificate caCert1 = ioRepository.getFileAsX509Certificate(OtgcConstant.DATA_PATH + OtgcConstant.KYRIO_ROOT_CERTIFICATE).blockingGet();
        if (date.after(caCert1.getNotBefore()) && date.before(caCert1.getNotAfter())) {
            byte[] kyrioRootcaCertificate = ioRepository.getBytesFromFile(OtgcConstant.KYRIO_ROOT_CERTIFICATE).blockingGet();
            if (OCPki.addMfgTrustAnchor(device, kyrioRootcaCertificate) == -1) {
                throw new Exception("Add root certificate error");
            }
        } else {
            showPopupNotValidCertificate("Kyrio root certificate is not valid.");
        }

        /* EonTi root cert */
        X509Certificate caCert2 = ioRepository.getFileAsX509Certificate(OtgcConstant.DATA_PATH + OtgcConstant.EONTI_ROOT_CERTIFICATE).blockingGet();
        if (date.after(caCert2.getNotBefore()) && date.before(caCert2.getNotAfter())) {
            byte[] eontiRootcaCertificate = ioRepository.getBytesFromFile(OtgcConstant.EONTI_ROOT_CERTIFICATE).blockingGet();
            if (OCPki.addMfgTrustAnchor(device, eontiRootcaCertificate) == -1) {
                throw new Exception("Add root certificate error");
            }
        } else {
            showPopupNotValidCertificate("EonTi root certificate is not valid.");
        }


        OCObt.shutdown();
    }

    private void showPopupNotValidCertificate(String content) {
        Platform.runLater(() -> {
            ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);

            Alert alertDialog = new Alert(Alert.AlertType.WARNING);
            alertDialog.setTitle("Not Valid Certificate");
            alertDialog.setHeaderText(content);
            alertDialog.getButtonTypes().clear();
            alertDialog.getButtonTypes().add(closeButton);

            Optional<ButtonType> result = alertDialog.showAndWait();
            if (result.get() == closeButton) {
                alertDialog.close();
            }
        });
    }

}
