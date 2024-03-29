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

import io.reactivex.Completable;
import io.reactivex.Single;
import org.apache.log4j.Logger;
import org.iotivity.*;
import org.openconnectivity.otgc.domain.model.resource.secure.cred.*;
import org.openconnectivity.otgc.domain.model.resource.secure.csr.OcCsr;
import org.openconnectivity.otgc.utils.constant.NotificationKey;
import org.openconnectivity.otgc.utils.constant.OcfResourceUri;
import org.iotivity.OCClientResponse;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CmsRepository {
    private final Logger LOG = Logger.getLogger(CmsRepository.class);

    @Inject
    public CmsRepository() {}

    public Single<OcCredentials> getCredentials(String deviceId) {
        return Single.create(emitter -> {
            OCUuid uuid = OCUuidUtil.stringToUuid(deviceId);

            OCObtCredsHandler handler = (OCCreds credentials) -> {
                if (credentials != null) {
                    OcCredentials creds = new OcCredentials();
                    creds.parseOCRepresentation(credentials);
                    emitter.onSuccess(creds);
                    /* Freeing the credential structure */
                    OCObt.freeCreds(credentials);
                } else {
                    String error = "GET credentials error";
                    LOG.error(error);
                    emitter.onError(new Exception(error));
                }
            };

            int ret = OCObt.retrieveCreds(uuid, handler);
            if (ret >= 0) {
                System.out.println("Successfully issued request to RETRIEVE /oic/sec/cred");
                LOG.debug("Successfully issued request to retrieve the credentials");
            } else {
                String error = "GET credentials error";
                LOG.error(error);
                emitter.onError(new Exception(error));
            }
        });
    }

    public Single<OcCsr> retrieveCsr(String endpoint, String deviceId) {
        return Single.create(emitter -> {
            OCEndpoint ep = OCEndpointUtil.stringToEndpoint(endpoint, new String[1]);
            OCUuid uuid = OCUuidUtil.stringToUuid(deviceId);
            OCEndpointUtil.setDi(ep, uuid);

            OCResponseHandler handler = (OCClientResponse response)-> {
                OCStatus code = response.getCode();
                if (code == OCStatus.OC_STATUS_OK) {
                    OcCsr csr = new OcCsr();
                    csr.parseOCRepresentation(response.getPayload());
                    emitter.onSuccess(csr);
                } else {
                    emitter.onError(new Exception("Send GET to /oic/sec/csr error"));
                }
            };

            if (!OCMain.doGet(OcfResourceUri.CSR_URI, ep, null, handler, OCQos.HIGH_QOS)) {
                emitter.onError(new Exception("Send GET to /oic/sec/csr error"));
            }

            OCEndpointUtil.freeEndpoint(ep);
        });
    }

    public Completable provisionIdentityCertificate(String deviceId) {
        return Completable.create(emitter -> {
            OCUuid di = OCUuidUtil.stringToUuid(deviceId);

            OCObtStatusHandler handler = (int status) -> {
                if (status >= 0) {
                    LOG.debug("Provision identity certificate succeeded");
                    emitter.onComplete();
                } else {
                    emitter.onError(new Exception("Provision identity certificate error"));
                }
            };

            int ret = OCObt.provisionIdentityCertificate(di, handler);
            if (ret < 0) {
                emitter.onError(new Exception("Provision identity certificate error"));
            }
        });
    }

    public Completable provisionRoleCertificate(String deviceId, String roleId, String roleAuthority) {
        return Completable.create(emitter -> {
            OCUuid di = OCUuidUtil.stringToUuid(deviceId);

            OCRole roles = OCObt.addRoleId(null, roleId, roleAuthority);

            OCObtStatusHandler handler = (int status) -> {
                if (status >= 0) {
                    LOG.debug("Provision role certificate succeeded");
                    emitter.onComplete();
                } else {
                    emitter.onError(new Exception("Provision role certificate error"));
                }
            };

            int ret = OCObt.provisionRoleCertificate(roles, di, handler);
            if (ret < 0) {
                emitter.onError(new Exception("Provision role certificate error"));
                OCObt.freeRoleId(roles);
            }
        });
    }

    public Completable provisionTrustAnchor(byte[] certificate, String sid, String deviceId) {
        return Completable.create(emitter -> {
            OCUuid di = OCUuidUtil.stringToUuid(deviceId);

            OCObtStatusHandler handler = (int status) -> {
                if (status >= 0) {
                    LOG.debug("Provision trust anchor succeeded");
                    emitter.onComplete();
                } else {
                    emitter.onError(new Exception("Provision trust anchor error"));
                }
            };

            int ret = OCObt.provisionTrustAnchor(certificate, sid, di, handler);
            if (ret < 0) {
                emitter.onError(new Exception("Provision trust anchor error"));
            }
        });
    }

    public Completable provisionPairwiseCredential(String clientId, String serverId) {
        return Completable.create(emitter -> {
            OCUuid cliendDi = OCUuidUtil.stringToUuid(clientId);
            OCUuid serverDi = OCUuidUtil.stringToUuid(serverId);

            OCObtStatusHandler handler = (int status) -> {
                if (status >= 0) {
                    LOG.debug("Successfully provisioned pair-wise credentials");
                    emitter.onComplete();
                } else {
                    String errorMsg = "ERROR provisioning pair-wise credentials";
                    LOG.error(errorMsg);
                    emitter.onError(new Exception(errorMsg));
                }
            };

            int ret = OCObt.provisionPairwiseCredentials(cliendDi, serverDi, handler);
            if (ret >= 0) {
                LOG.debug("Successfully issued request to provision credentials");
            } else {
                String errorMsg = "ERROR issuing request to provision credentials";
                LOG.error(errorMsg);
                emitter.onError(new Exception(errorMsg));
            }
        });
    }

    public Completable deleteCredential(String deviceId, long credId) {
        return Completable.create(emitter -> {
            OCUuid uuid = OCUuidUtil.stringToUuid(deviceId);

            OCObtStatusHandler handler = (int status) -> {
                if (status >= 0) {
                    LOG.debug("Delete credential success");
                    emitter.onComplete();
                } else {
                    String error = "Delete credential error";
                    LOG.error(error);
                    emitter.onError(new Exception(error));
                }
            };

            int ret = OCObt.deleteCredByCredId(uuid, (int)credId, handler);
            if (ret >= 0) {
                LOG.debug("Successfully issued request to DELETE /oic/sec/cred");
            } else {
                String error = "DELETE request to /oic/sec/cred error";
                LOG.debug(error);
                emitter.onError(new Exception(error));
            }
        });
    }

    public Single<OcCredentials> retrieveOwnCredentials() {
        return Single.create(emitter -> {
            OcCredentials creds = new OcCredentials();
            creds.parseOCRepresentation(OCObt.retrieveOwnCreds());
            emitter.onSuccess(creds);
        });
    }

    public Completable addTrustAnchor(String pemCert) {
        return Completable.create(emitter -> {
            if (OCPki.addTrustAnchor(0 /* First device */, pemCert.getBytes()) == -1) {
                emitter.onError(new Exception("Add trust anchor error"));
            }

            if (OCPki.addMfgTrustAnchor(0 /* First device */, pemCert.getBytes()) == -1) {
                emitter.onError(new Exception("Add manufacturer trust anchor error"));
            }

            emitter.onComplete();
        });
    }

    public Completable addIntermediateCertificate(Integer credid, byte[] cert) {
        return Completable.create(emitter -> {
            if (OCPki.addMfgIntermediateCert(0 /* First device */, credid, cert) == -1) {
                emitter.onError(new Exception("Add intermediate certificate error"));
            }

            emitter.onComplete();
        });
    }

    public Completable addEndEntityCertificate(byte[] cert, byte[] key) {
        return Completable.create(emitter -> {
            if (OCPki.addMfgCert(0 /* First device */, cert, key) == -1) {
                emitter.onError(new Exception("Add end entity certificate error"));
            }

            emitter.onComplete();
        });
    }

    public Completable removeTrustAnchor(long credid) {
        return Completable.create(emitter -> {
            int ret = OCObt.deleteOwnCredByCredId((int)credid);
            if (ret >= 0) {
                LOG.debug("Successfully DELETED cred");
                emitter.onComplete();
            } else {
                String error = "ERROR DELETING cred";
                LOG.error(error);
                emitter.onError(new Exception(error));
            }
        });
    }

    public Completable registerDeviceCloud(String deviceId, String deviceURL, String authProvider, String cloudUrl, String cloudUuid, String accessToken) {
        return Completable.create(emitter -> {
            OCCloudContext ctx = OCCloud.getContext(0);

            OCUuid uuid = OCUuidUtil.stringToUuid(deviceId);

            OCResponseHandler handler = (OCClientResponse response) -> {
                if (response.getCode() == OCStatus.OC_STATUS_CHANGED) {
                    LOG.debug("Register device on cloud success");
                    emitter.onComplete();
                } else if (response.getCode() == OCStatus.OC_STATUS_CREATED) {
                    LOG.debug("Register device on cloud success");
                    emitter.onComplete();
                } else {
                    String error = "Register device on cloud error";
                    LOG.error(error + ": " + response.getCode());
                    emitter.onError(new Exception(error));
                }
            };

            if (ctx != null) {
                OCObt.UpdateCloudConfDevice(uuid, deviceURL, accessToken, authProvider, cloudUrl, cloudUuid, handler);
            }
            emitter.onComplete();
        });
    }
}
