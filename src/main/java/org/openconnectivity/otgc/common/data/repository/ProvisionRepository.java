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

import io.reactivex.Completable;
import io.reactivex.Single;
import org.apache.log4j.Logger;
import org.iotivity.base.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Singleton
public class ProvisionRepository {

    private final Logger LOG = Logger.getLogger(ProvisionRepository.class);

    private final OcProvisioning.PinCallbackListener listener = () -> "";

    @Inject
    public ProvisionRepository(){}

    public Completable resetSvrDb() {
        return Completable.fromAction(OcProvisioning::resetSvrDb);
    }

    public Completable doSelfOwnership() {
        return Completable.fromAction(OcProvisioning::doSelfOwnershiptransfer);
    }

    public Single<List<String>> getLinkedDevices(OcSecureResource ocSecureResource) {
        return Single.create(emitter -> {
            try {
                List<String> linkedDevices = ocSecureResource.getLinkedDevices();
                emitter.onSuccess(linkedDevices);
            } catch (OcException e)
            {
                emitter.onError(e);
            }
        });
    }

    public Completable linkDevices(OcSecureResource clientResource, OcSecureResource serverResource, List<OicSecResr> resources) {
        return Completable.create(emitter -> {
            try {
                // Create ACE to allow client to manage the server
                List<OicSecAce> aces = new ArrayList<>();
                OicSecAceSubject subject = new OicSecAceSubject(AceSubjectType.SUBJECT_UUID.getValue(), clientResource.getDeviceID(), null, null);
                OicSecAce ace = new OicSecAce(0, subject, 31, resources, new ArrayList<>());
                aces.add(ace);
                OicSecAcl acl = new OicSecAcl(null, aces);

                EnumSet<CredType> credTypes = EnumSet.of(CredType.SYMMETRIC_PAIR_WISE_KEY);
                clientResource.provisionPairwiseDevices(credTypes, KeySize.OWNER_PSK_LENGTH_256, null, serverResource, acl, (results, hasError) ->
                {
                    if (hasError == 0) {
                        emitter.onComplete();
                    } else {
                        emitter.onError(new IOException("Link Devices Exception"));
                    }
                });
            } catch (OcException e) {
                emitter.onError(e);
            }
        });
    }

    public Completable unlinkDevices(OcSecureResource clientResource, OcSecureResource serverResource) {
        return Completable.create(emitter -> {
            try {
                clientResource.unlinkDevices(serverResource, (results, hasError) -> {
                    if (hasError == 0) {
                        emitter.onComplete();
                    } else {
                        emitter.onError(new IOException("Unlink Devices Exception"));
                    }
                });
            } catch (OcException e) {
                emitter.onError(e);
            }
        });
    }
}
