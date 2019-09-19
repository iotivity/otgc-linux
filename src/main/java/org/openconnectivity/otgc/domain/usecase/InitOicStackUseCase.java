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
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.iotivity.OCFactoryPresetsHandler;
import org.iotivity.OCObt;
import org.iotivity.OCPki;
import org.openconnectivity.otgc.utils.constant.OtgcConstant;
import org.openconnectivity.otgc.data.repository.*;

import javax.inject.Inject;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;

public class InitOicStackUseCase {

    private final IotivityRepository iotivityRepository;
    private final CertRepository certRepository;
    private final IORepository ioRepository;
    private final SettingRepository settingRepository;

    @Inject
    public InitOicStackUseCase(IotivityRepository iotivityRepository,
                               CertRepository certRepository,
                               IORepository ioRepository,
                               SettingRepository settingRepository) {
        this.iotivityRepository = iotivityRepository;
        this.certRepository = certRepository;
        this.ioRepository = ioRepository;
        this.settingRepository = settingRepository;
    }

    public Completable execute() {
        Completable initOic = iotivityRepository.setFactoryResetHandler(factoryReset)
                .andThen(iotivityRepository.initOICStack());

        Completable completable;
        if (Boolean.valueOf(settingRepository.get(SettingRepository.FIRST_RUN_KEY, SettingRepository.FIRST_RUN_DEFAULT_VALUE))) {
            completable = initOic
                    .andThen(settingRepository.set(SettingRepository.FIRST_RUN_KEY, "false"));
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
        String uuid = iotivityRepository.getDeviceId().blockingGet();

        // Store root CA as trusted anchor
        X509Certificate caCertificate = ioRepository.getAssetAsX509Certificate(OtgcConstant.ROOT_CERTIFICATE).blockingGet();
        PrivateKey caPrivateKey = ioRepository.getAssetAsPrivateKey(OtgcConstant.ROOT_PRIVATE_KEY).blockingGet();

        String strCACertificate = certRepository.x509CertificateToPemString(caCertificate).blockingGet();
        if (OCPki.addTrustAnchor(device, strCACertificate.getBytes()) == -1) {
            throw new Exception("Add trust anchor error");
        }
        if (OCPki.addMfgTrustAnchor(device, strCACertificate.getBytes()) == -1) {
            throw new Exception("Add manufacturer trust anchor error");
        }

        // public/private key pair that we are creating certificate for
        ECGenParameterSpec ecParamSpec = new ECGenParameterSpec("secp256r1");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
        keyPairGenerator.initialize(ecParamSpec);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // Public key
        PublicKey publicKey = keyPair.getPublic();
        // PrivateKey
        ASN1Sequence pkSeq = (ASN1Sequence)ASN1Sequence.fromByteArray(keyPair.getPrivate().getEncoded());
        PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(pkSeq);
        ECPrivateKey privateKey = ECPrivateKey.getInstance(pkInfo.parsePrivateKey());
        String strPrivateKey = certRepository.privateKeyToPemString(privateKey).blockingGet();

        X509Certificate identityCertificate = certRepository.generateIdentityCertificate(uuid, publicKey, caPrivateKey).blockingGet();
        String strIdentityCertificate = certRepository.x509CertificateToPemString(identityCertificate).blockingGet();
        if (OCPki.addMfgCert(device, strIdentityCertificate.getBytes(), strPrivateKey.getBytes()) == -1) {
            throw  new Exception("Add identity certificate error");
        }

        OCObt.shutdown();
    }

}
