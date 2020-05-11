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

package org.openconnectivity.otgc.data.repository;

import com.upokecenter.cbor.CBORObject;
import io.reactivex.Single;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.openconnectivity.otgc.utils.constant.OtgcConstant;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@Singleton
public class IORepository {
    private final Logger LOG = Logger.getLogger(IORepository.class);

    private static String assetsPath;

    private static final int BUFFER_SIZE = 1024;

    @Inject
    public IORepository(){
        assetsPath = new File(".").getAbsolutePath() + File.separator + "data" + File.separator;
    }

    public Single<PrivateKey> getAssetAsPrivateKey(String fileName) {
        return Single.create(emitter -> {
            try (PEMParser pemReader = new PEMParser(new InputStreamReader(new FileInputStream(assetsPath + fileName)))) {
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
                Object keyPair = pemReader.readObject();
                if (keyPair instanceof PrivateKeyInfo) {
                    PrivateKey pk = converter.getPrivateKey((PrivateKeyInfo) keyPair);
                    emitter.onSuccess(pk);
                } else {
                    PrivateKey pk = converter.getPrivateKey(((PEMKeyPair) keyPair).getPrivateKeyInfo());
                    emitter.onSuccess(pk);
                }
            } catch (IOException x) {
                // Shouldn't occur, since we're only reading from strings
                emitter.onError(x);
            }
        });
    }

    public Single<X509Certificate> getAssetAsX509Certificate(String fileName) {
        return getFileAsX509Certificate(assetsPath + fileName);
    }

    public Single<X509Certificate> getFileAsX509Certificate(String path) {
        return Single.create(emitter -> {
            try (InputStream inputStream =
                         new FileInputStream(path)) {
                Security.addProvider(new BouncyCastleProvider());
                CertificateFactory factory = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
                X509Certificate caCert = (X509Certificate) factory.generateCertificate(inputStream);
                emitter.onSuccess(caCert);
            } catch (FileNotFoundException e) {
                LOG.error("File not found: " + e.getMessage());
                emitter.onError(e);
            } catch (IOException e) {
                LOG.error(path + " file storage failed");
                emitter.onError(e);
            }
        });
    }

    public Single<byte[]> getBytesFromFile(String path) {
        return Single.fromCallable(() -> {
            byte[] fileBytes;
            try (InputStream inputStream = new FileInputStream(OtgcConstant.DATA_PATH + path)) {
                int numBytes = inputStream.available() + 1;
                fileBytes = new byte[numBytes];
                inputStream.read(fileBytes);
                fileBytes[numBytes - 1] = '\0';
            }

            return fileBytes;
        });
    }

    public Single<byte[]> getBytesFromPath(String path) {
        return Single.fromCallable(() -> {
            byte[] fileBytes;
            try (InputStream inputStream = new FileInputStream(path)) {
                int numBytes = inputStream.available() + 1;
                fileBytes = new byte[numBytes];
                inputStream.read(fileBytes);
                fileBytes[numBytes - 1] = '\0';
            }

            return fileBytes;
        });
    }

    public Single<CBORObject> getAssetSvrAsCbor(String resource, long device) {
        return Single.create(emitter -> {
            try (FileInputStream stream = new FileInputStream(OtgcConstant.OTGC_CREDS_DIR + File.separator + resource + "_" + device)) {
                CBORObject cbor = CBORObject.Read(stream);
                emitter.onSuccess(cbor);
            }
        });
    }
}