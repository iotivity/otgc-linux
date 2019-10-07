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
import org.iotivity.OCFactoryPresetsHandler;
import org.iotivity.OCObt;
import org.iotivity.OCPki;
import org.openconnectivity.otgc.utils.constant.OtgcConstant;
import org.openconnectivity.otgc.data.repository.*;
import org.openconnectivity.otgc.utils.constant.OtgcMode;

import javax.inject.Inject;

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
        /* my cert */
        byte[] eeCertificate = ioRepository.getBytesFromFile(OtgcConstant.KYRIO_EE_CERTIFICATE).blockingGet();

        /* private key of my cert */
        byte[] eeKey = ioRepository.getBytesFromFile(OtgcConstant.KYRIO_EE_KEY).blockingGet();

        /* intermediate cert */
        byte[] subcaCertificate = ioRepository.getBytesFromFile(OtgcConstant.KYRIO_SUBCA_CERTIFICATE).blockingGet();

        /* root cert */
        byte[] rootcaCertificate = ioRepository.getBytesFromFile(OtgcConstant.KYRIO_ROOT_CERTIFICATE).blockingGet();

        int credid = OCPki.addMfgCert(device, eeCertificate, eeKey);
        if (credid == -1) {
            throw new Exception("Add identity certificate error");
        }

        if (OCPki.addMfgIntermediateCert(device, credid, subcaCertificate) == -1) {
            throw new Exception("Add intermediate certificate error");
        }

        if (OCPki.addMfgTrustAnchor(device, rootcaCertificate) == -1) {
            throw new Exception("Add root certificate error");
        }

        OCObt.shutdown();
    }

}
