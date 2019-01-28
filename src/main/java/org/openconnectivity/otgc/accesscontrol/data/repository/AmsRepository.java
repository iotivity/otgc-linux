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

package org.openconnectivity.otgc.accesscontrol.data.repository;

import io.reactivex.Completable;
import io.reactivex.Single;
import org.apache.log4j.Logger;
import org.iotivity.base.OcException;
import org.iotivity.base.OcSecureResource;
import org.iotivity.base.OicSecAce;
import org.iotivity.base.OicSecAcl;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class AmsRepository {

    private final Logger LOG = Logger.getLogger(AmsRepository.class);

    @Inject
    public AmsRepository(){}

    public Single<OicSecAcl> getAcl(OcSecureResource ocSecureResource) {
        return Single.create(emitter -> {
            try {
                ocSecureResource.getACL((acl, hasErrors) -> {
                    if (hasErrors == 0) {
                        emitter.onSuccess(acl);
                    } else {
                        emitter.onError(new IOException("Get ACL error"));
                    }
                });
            } catch (OcException ex) {
                LOG.error(ex.getMessage());
                emitter.onError(ex);
            }
        });
    }

    public Completable provisionAcl(OcSecureResource ocSecureResource, OicSecAce ace) {
        return Completable.create(emitter -> {
            try {
                List<OicSecAce> aces = new ArrayList<>();
                aces.add(ace);
                OicSecAcl acl = new OicSecAcl(null, aces);
                ocSecureResource.provisionACL(acl, (provisionResults, hasError) -> {
                    if (hasError == 0) {
                        LOG.debug("Provision ACL succeeded");
                        emitter.onComplete();
                    } else {
                        emitter.onError(new IOException("Provision ACL error"));
                    }
                });
            } catch (OcException e) {
                LOG.error(e.getMessage());
                emitter.onError(e);
            }
        });
    }

    public Completable deleteAcl(OcSecureResource ocSecureResource, int aceId) {
        return Completable.create(emitter -> {
            try {
                ocSecureResource.deleteACE(aceId, (provisionResults, hasError) -> {
                    if (hasError == 0) {
                        LOG.debug("Delete ACE succeeded");
                        emitter.onComplete();
                    } else {
                        emitter.onError(new IOException("Delete ACE error"));
                    }
                });
            } catch (OcException e) {
                LOG.error(e.getLocalizedMessage());
                emitter.onError(e);
            }
        });
    }
}
