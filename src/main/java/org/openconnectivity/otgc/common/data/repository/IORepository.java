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

package org.openconnectivity.otgc.common.data.repository;

import com.upokecenter.cbor.CBOREncodeOptions;
import com.upokecenter.cbor.CBORObject;
import io.reactivex.Completable;
import io.reactivex.Single;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.json.JSONObject;

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

    public Single<byte[]> getBytesFromFile(String path) {
        return Single.fromCallable(() -> {
            byte[] fileBytes;
            try (InputStream inputStream = getClass().getClassLoader()
                                        .getResource("data" + File.separator + path).openStream()) {
                fileBytes = new byte[inputStream.available()];
                inputStream.read(fileBytes);
            }

            return fileBytes;
        });
    }

    public Single<JSONObject> getAssetAsJson(String fileName) {
        return getAssetAsString(fileName)
                .map(JSONObject::new);
    }

    public Single<String> getAssetAsString(String fileName) {
        return Single.create(emitter -> {
            int length;
            byte[] buffer = new byte[BUFFER_SIZE];
            try {
                InputStream inputStream = new FileInputStream(assetsPath + fileName);
                StringBuilder sb = new StringBuilder();
                while ((length = inputStream.read(buffer)) != -1) {
                    sb.append(new String(buffer, 0, length));
                }
                emitter.onSuccess(sb.toString());
            } catch (NullPointerException e) {
                LOG.error("Null pointer exception: " + e.getMessage());
                emitter.onError(e);
            } catch (FileNotFoundException e) {
                LOG.error("File not found: " + e.getMessage());
                emitter.onError(e);
            } catch (IOException e) {
                LOG.error(fileName + " file copy failed");
                emitter.onError(e);
            }
        });
    }

    public Completable saveCborToFiles(String fileName, CBORObject cborObject) {
        return Completable.create(emitter -> {
            LOG.debug("saveCborToFiles");
            try (OutputStream stream = new FileOutputStream(assetsPath + fileName)) {
                File file = new File(assetsPath);
                //check files directory exists
                if (!(file.exists() && file.isDirectory())) {
                    file.mkdirs();
                }
                CBORObject.Write(cborObject, stream, CBOREncodeOptions.Default);
            } catch (FileNotFoundException e) {
                LOG.error("File not found: " + e.getMessage());
                emitter.onError(e);
            } catch (IOException e) {
                LOG.error(fileName + " file storage failed");
                emitter.onError(e);
            }

            emitter.onComplete();
        });
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
        return Single.create(emitter -> {
            try (InputStream inputStream =
                         new FileInputStream(assetsPath + fileName)) {
                Security.addProvider(new BouncyCastleProvider());
                CertificateFactory factory = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
                X509Certificate caCert = (X509Certificate) factory.generateCertificate(inputStream);
                emitter.onSuccess(caCert);
            } catch (FileNotFoundException e) {
                LOG.error("File not found: " + e.getMessage());
                emitter.onError(e);
            } catch (IOException e) {
                LOG.error(fileName + " file storage failed");
                emitter.onError(e);
            }
        });
    }
}
