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

package org.openconnectivity.otgc.credential.data.repository;

import io.reactivex.Completable;
import io.reactivex.Single;
import org.apache.log4j.Logger;
import org.iotivity.base.OcException;
import org.iotivity.base.OcSecureResource;
import org.iotivity.base.OicSecCreds;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class CmsRepository {
    private final Logger LOG = Logger.getLogger(CmsRepository.class);

    private int credId = 0;

    @Inject
    public CmsRepository() {}

    public Single<OicSecCreds> getCredentials(OcSecureResource ocSecureResource) {
        return Single.create(emitter -> {
            try {
                ocSecureResource.getCredentials((creds, hasError) -> {
                    if (hasError == 0) {
                        emitter.onSuccess(creds);
                    } else {
                        emitter.onError(new IOException("Get Credentials Exception"));
                    }
                });
            } catch (OcException ex) {
                LOG.error(ex.getLocalizedMessage());
                emitter.onError(ex);
            }
        });
    }

    public Completable provisionIdentityCertificate(OcSecureResource ocSecureResource) {
        return Completable.create(emitter -> {
            try {
                ocSecureResource.provisionIdentityCertificate((provisionResults, hasError) -> {
                    if (hasError == 0) {
                        emitter.onComplete();
                    } else {
                        emitter.onError(new IOException("Error provisioning identity certificate"));
                    }
                });
            } catch (OcException ex) {
                LOG.error(ex.getLocalizedMessage());
                emitter.onError(ex);
            }
            emitter.onComplete();
        });
    }

    public Completable provisionRoleCertificate(OcSecureResource ocSecureResource, String roleId, String roleAuthority) {
        return Completable.create(emitter -> {
            try {
                ocSecureResource.provisionRoleCertificate(roleId, roleAuthority, (provisionResults, hasError) -> {
                    if (hasError == 0) {
                        emitter.onComplete();
                    } else {
                        emitter.onError(new IOException("Error provisioning role certificate"));
                    }
                });
            } catch (OcException ex) {
                LOG.error(ex.getLocalizedMessage());
                emitter.onError(ex);
            }
            emitter.onComplete();
        });
    }

    public Completable deleteCredential(OcSecureResource ocSecureResource, int credId) {
        return Completable.create(emitter -> {
            try {
                ocSecureResource.deleteCredential(credId, (provisionResult, hasError) -> {
                    if (hasError == 0) {
                        LOG.debug("Delete Credential succeeded");
                        emitter.onComplete();
                    } else {
                        emitter.onError(new IOException("Delete Credential error"));
                    }
                });
            } catch (OcException ex) {
                LOG.error(ex.getLocalizedMessage());
                emitter.onError(ex);
            }
        });
    }
}
