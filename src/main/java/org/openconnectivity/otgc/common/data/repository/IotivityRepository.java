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
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import org.apache.log4j.Logger;
import org.iotivity.base.*;
import org.openconnectivity.otgc.common.constant.OcfResourceType;
import org.openconnectivity.otgc.common.data.entity.DeviceEntity;
import org.openconnectivity.otgc.common.data.persistence.dao.DeviceDao;
import org.openconnectivity.otgc.devicelist.domain.model.Device;
import org.openconnectivity.otgc.devicelist.domain.model.DeviceType;
import org.openconnectivity.otgc.common.domain.model.OcDevice;
import org.openconnectivity.otgc.common.util.IotivityUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.*;

@Singleton
public class IotivityRepository {

    private final Logger LOG = Logger.getLogger(ProvisionRepository.class);

    private static final EnumSet<OcConnectivityType> CONNECTIVITY_TYPES;
    static {
        CONNECTIVITY_TYPES = EnumSet.of(OcConnectivityType.CT_ADAPTER_IP);
    }

    private static final List<String> RESOURCE_TYPES_TO_FILTER;
    static {
        RESOURCE_TYPES_TO_FILTER = new ArrayList<>();

        RESOURCE_TYPES_TO_FILTER.add(OcfResourceType.DOXM);
        RESOURCE_TYPES_TO_FILTER.add("oic.r.pstat");
        RESOURCE_TYPES_TO_FILTER.add("oic.r.acl2");
        RESOURCE_TYPES_TO_FILTER.add("oic.r.cred");
        RESOURCE_TYPES_TO_FILTER.add("oic.r.crl");
        RESOURCE_TYPES_TO_FILTER.add("oic.r.csr");
        RESOURCE_TYPES_TO_FILTER.add("oic.r.roles");
        RESOURCE_TYPES_TO_FILTER.add(OcfResourceType.DEVICE);
        RESOURCE_TYPES_TO_FILTER.add(OcfResourceType.PLATFORM);
        RESOURCE_TYPES_TO_FILTER.add(OcfResourceType.INTROSPECTION);
    }

    private static final String OIC_CLIENT_CBOR_DB_FILE = "oic_svr_db_client.dat";
    private static final String INTROSPECTION_FILE = "introspection.dat";
    private static final String OIC_SQL_DB_FILE = "Pdm.db";
    private static final QualityOfService QUALITY_OF_SERVICE = QualityOfService.HIGH;

    private List<OcSecureResource> unownedDevices = new ArrayList<>();
    private List<OcSecureResource> ownedDevices = new ArrayList<>();

    private final SettingRepository settingRepository;
    private final DeviceDao deviceDao;

    @Inject
    public IotivityRepository(SettingRepository settingRepository, DeviceDao deviceDao) {
        this.settingRepository = settingRepository;
        this.deviceDao = deviceDao;
    }

    public Completable initOICStack() {
        return Completable.create(source -> {
            LOG.debug("initOICStack");

            String svrPath = new File(".").getAbsolutePath() + File.separator + "data" + File.separator + OIC_CLIENT_CBOR_DB_FILE;
            String introspectionPath = new File(".").getAbsolutePath() + File.separator + "data" + File.separator + INTROSPECTION_FILE;

            // Create platform config
            PlatformConfig cfg = new PlatformConfig(
                    ServiceType.IN_PROC,
                    ModeType.CLIENT_SERVER,
                    "0.0.0.0",
                    5683,
                    QUALITY_OF_SERVICE,
                    svrPath,
                    introspectionPath
            );
            OcPlatform.Configure(cfg);

            OcPlatform.setPropertyValue(PayloadType.DEVICE.getValue(), "n", "OTGC");
            OcPlatform.setPropertyValue(PayloadType.DEVICE.getValue(), "icv", "ocf.1.3.0");
            OcPlatform.setPropertyValue(PayloadType.DEVICE.getValue(), "dmv", "ocf.res.1.3.0");
            OcPlatform.setPropertyValue(PayloadType.PLATFORM.getValue(), "mnmn", "DEKRA Testing and Certification, S.A.U.");

            try {
                // Initialize database
                String sqlDbPath = File.separator + new File(".").getAbsolutePath() + File.separator + "db" + File.separator;

                File file = new File(sqlDbPath);
                if (!(file.exists())) {
                    file.mkdirs();
                    LOG.debug("Sql db directory created at " + sqlDbPath);
                }
                LOG.debug("Sql db directory exists at " + sqlDbPath);
                OcProvisioning.provisionInit(sqlDbPath + OIC_SQL_DB_FILE);

                if (Boolean.valueOf(settingRepository.get(SettingRepository.FIRST_RUN_KEY, SettingRepository.FIRST_RUN_DEFAULT_VALUE))) {
                    // OcProvisioning.setDeviceIdSeed(UUID.randomUUID().toString().getBytes());
                    try {
                        OcProvisioning.doSelfOwnershiptransfer();
                    } catch(OcException ex) {
                        LOG.debug(ex.getMessage());
                    }
                }
            } catch (OcException ex) {
                source.onError(ex);
            }

            try {
                OcProvisioning.setOwnershipTransferCBdata(OxmType.OIC_JUST_WORKS, () -> "");
            } catch (OcException ex) {
                LOG.error("Exception setting just-works callback: " + ex.getMessage());
            }

            source.onComplete();
        });
    }

    public Observable<OcSecureResource> scanUnownedDevices() {
        return Observable.create(emitter -> {
            try {
                unownedDevices = OcProvisioning.discoverUnownedDevices(getDiscoveryTimeout());

                for (OcSecureResource ocSecureResource : unownedDevices) {
                    emitter.onNext(ocSecureResource);
                }
            } catch (OcException ex) {
                LOG.error(ex.getMessage());
                emitter.onError(ex);
            }

            emitter.onComplete();
        });
    }

    public Observable<OcSecureResource> scanOwnedDevices() {
        return Observable.create(emitter -> {
            try {
                ownedDevices = OcProvisioning.discoverOwnedDevices(getDiscoveryTimeout());

                for (OcSecureResource ocSecureResource : ownedDevices) {
                    emitter.onNext(ocSecureResource);
                }
            } catch (OcException ex) {
                LOG.error(ex.getMessage());
                emitter.onError(ex);
            }

            emitter.onComplete();
        });
    }

    public Observable<OcSecureResource> scanOwnedByOtherDevices() {
        return findObsResources("")
                .map(ocResources -> ocResources.get(0))
                .filter(ocResource -> {
                    boolean isNotUnowned = true;
                    for (OcSecureResource secureResource : unownedDevices) {
                        if (secureResource.getDeviceID().equals(ocResource.getServerId())) {
                            isNotUnowned = false;
                        }
                    }
                    return isNotUnowned;
                })
                .filter(ocResource -> {
                    boolean isNotOwned = true;
                    for (OcSecureResource secureResource : ownedDevices) {
                        if (secureResource.getDeviceID().equals(ocResource.getServerId())) {
                            isNotOwned = false;
                        }
                    }
                    return isNotOwned;
                })
                .filter(ocResource -> !ocResource.getServerId().equals(getDeviceId()))
                .flatMapSingle(ocResource -> findOcSecureResource(ocResource.getServerId()));
    }

    private Observable<List<OcResource>> findObsResources(String host) {
        return Observable.create(emitter -> {
            try {
                OcPlatform.findResources(host,
                        OcPlatform.WELL_KNOWN_QUERY,
                        CONNECTIVITY_TYPES,
                        new OcPlatform.OnResourcesFoundListener() {

                            @Override
                            public void onResourcesFound(OcResource[] ocResources) {
                                DeviceEntity device = deviceDao.findById(ocResources[0].getServerId());
                                if (device == null) {
                                    deviceDao.insert(new DeviceEntity(ocResources[0].getServerId(), "", ocResources[0].getAllHosts()));
                                } else {
                                    deviceDao.insert(new DeviceEntity(device.getId(), device.getName(), ocResources[0].getAllHosts()));
                                }
                                emitter.onNext(Arrays.asList(ocResources));
                            }

                            @Override
                            public void onFindResourcesFailed(Throwable throwable, String s) {
                                if (throwable instanceof OcException) {
                                    OcException ex = (OcException) throwable;
                                    if (ex.getErrorCode() == ErrorCode.ADAPTER_NOT_ENABLED) {
                                        LOG.warn(ex.getLocalizedMessage());
                                    } else {
                                        LOG.error(ex.getLocalizedMessage());
                                    }
                                } else {
                                    LOG.error(throwable.getLocalizedMessage());
                                }
                            }
                        });
            } catch (OcException ex) {
                LOG.error(ex.getLocalizedMessage());
                emitter.onError(ex);
            }

            try {
                Thread.sleep(getDiscoveryTimeout() * 1_000L);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOG.error(ex.getMessage());
            }

            emitter.onComplete();
        });
    }

    private String getDeviceId() {
        ByteBuffer bb = ByteBuffer.wrap(OcPlatform.getDeviceId());
        UUID uuid = new UUID(bb.getLong(), bb.getLong());
        return uuid.toString();
    }

    public Single<OcSecureResource> findOcSecureResource(@NonNull String deviceId) {
        return Single.create(emitter -> {
            try {
                OcSecureResource ocSecureResource = OcProvisioning.discoverSingleDevice(getDiscoveryTimeout(), deviceId);
                emitter.onSuccess(ocSecureResource);
            } catch (OcException ex) {
                LOG.error(ex.getLocalizedMessage());
                emitter.onError(ex);
            }
        });
    }

    public Completable scanHosts() {
        return findObsResources("").ignoreElements();
    }

    public Single<String> getDeviceCoapIpv6Host(String deviceId) {
        return Single.create(emitter -> {
            String host = getDeviceIpv6Host(deviceId, false);
            emitter.onSuccess(host);
        });
    }

    public Single<String> getDeviceCoapsIpv6Host(String deviceId) {
        return Single.create(emitter -> {
            String host = getDeviceIpv6Host(deviceId, true);
            emitter.onSuccess(host);
        });
    }

    private String getDeviceIpv6Host(String deviceId, boolean secure) {
        DeviceEntity device = deviceDao.findById(deviceId);

        String host = null;
        for (int i=0; i<device.getHosts().size(); i++) {
            if (device.getHosts().get(i).startsWith(secure ? "coaps://" : "coap://")
                    && !device.getHosts().get(i).contains(".")) {
                host = device.getHosts().get(i);
                break;
            } else if (device.getHosts().get(i).startsWith(secure ? "coaps://" : "coap://")) {
                host = device.getHosts().get(i);
            }
        }

        return host;
    }

    public Single<OcRepresentation> getDeviceInfo(String host) {
        return Single.create(emitter -> {
            try {
                OcPlatform.getDeviceInfo(host,
                        OcPlatform.WELL_KNOWN_DEVICE_QUERY,
                        CONNECTIVITY_TYPES,
                        emitter::onSuccess);
            } catch (OcException ex) {
                LOG.error(ex.getLocalizedMessage());
                emitter.onError(ex);
            }
        });
    }

    public Single<String> getDeviceName(String deviceId) {
        return Single.create(emitter -> {
            DeviceEntity device = deviceDao.findById(deviceId);
            emitter.onSuccess(device.getName());
        });
    }

    public Completable setDeviceName(String deviceId, String deviceName) {
        return Completable.fromAction(() -> {
            deviceDao.updateDeviceName(deviceId, deviceName);
        });
    }

    private OcDevice retrieveDeviceInfo(String ipAddress) {
        OcDevice[] deviceInfo = new OcDevice[1];
        deviceInfo[0] = new OcDevice();
        final Object lock = new Object();
        try {
            OcPlatform.getDeviceInfo(ipAddress,
                    OcPlatform.WELL_KNOWN_DEVICE_QUERY,
                    EnumSet.of(OcConnectivityType.CT_ADAPTER_IP),
                    ocRepresentation -> {
                        if (ocRepresentation != null) {
                            deviceInfo[0].setOcRepresentation(ocRepresentation);

                            try {
                                synchronized (lock) {
                                    lock.notifyAll();
                                }
                            } catch (Exception ex) {
                                LOG.error(ex.getLocalizedMessage());
                            }
                        }
                    });
        } catch (OcException ex) {
            LOG.error(ex.getMessage());
        }

        synchronized (lock)  {
            try {
                lock.wait(3000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOG.error(ex.getMessage());
            }
        }

        return deviceInfo[0];
    }

    private int getDiscoveryTimeout() {
        return Integer.parseInt(settingRepository.get(SettingRepository.DISCOVERY_TIMEOUT_KEY,
                                                        SettingRepository.DISCOVERY_TIMEOUT_DEFAULT_VALUE));
    }

    private String getCoapsIpv6Host(@NonNull List<String> hosts) {
        String coapsIpv6Host = null;
        String coapsHost = null;
        for (String host : hosts) {
            if (host.startsWith("coaps") && !host.contains(".")) {
                coapsIpv6Host = host;
                break;
            } else if (host.startsWith("coaps")) {
                coapsHost = host;
            }
        }

        return coapsIpv6Host != null ? coapsIpv6Host : coapsHost;
    }

    public Single<String> getCoapsHost(List<String> hosts) {
        return Single.create(emitter -> {
            String host = getCoapsIpv6Host(hosts);
            emitter.onSuccess(host);
        });
    }

    public Single<List<OcResource>> findResource(String host) {
        return Single.create(emitter -> {
            try {
                OcPlatform.findResources(host,
                        OcPlatform.WELL_KNOWN_QUERY,
                        CONNECTIVITY_TYPES,
                        new OcPlatform.OnResourcesFoundListener() {
                            @Override
                            public void onResourcesFound(OcResource[] ocResources) {
                                emitter.onSuccess(Arrays.asList(ocResources));
                            }

                            @Override
                            public void onFindResourcesFailed(Throwable throwable, String s) {
                                if (throwable instanceof OcException
                                        && ((OcException)throwable).getErrorCode().equals(ErrorCode.COMM_ERROR)) {
                                    LOG.warn(throwable.getLocalizedMessage());
                                } else {
                                    LOG.error(throwable.getLocalizedMessage());
                                    emitter.onError(throwable);
                                }
                            }
                        });
            } catch (OcException ex) {

            }
        });
    }

    public Single<OcResource> findResource(String host, String resourceType) {
        return Single.create(emitter -> {
            try {
                OcPlatform.findResource(host,
                        OcPlatform.WELL_KNOWN_QUERY + "?rt=" + resourceType,
                        CONNECTIVITY_TYPES,
                        new OcPlatform.OnResourceFoundListener() {
                            @Override
                            public void onResourceFound(OcResource ocResource) {
                                emitter.onSuccess(ocResource);
                            }

                            @Override
                            public void onFindResourceFailed(Throwable throwable, String s) {
                                if (throwable instanceof OcException
                                        && ((OcException)throwable).getErrorCode().equals(ErrorCode.COMM_ERROR)) {
                                    LOG.warn(throwable.getLocalizedMessage());
                                } else {
                                    LOG.error(throwable.getLocalizedMessage());
                                    emitter.onError(throwable);
                                }
                            }
                        });
            } catch (OcException e) {
                LOG.error(e.getMessage());
                emitter.onError(e);
            }
        });
    }

    public Single<OcRepresentation> get(OcResource ocResource, boolean secured) {
        return Single.create(emitter -> {
            if (secured) {
                String securedHost = getCoapsIpv6Host(ocResource.getAllHosts());
                if (securedHost != null) {
                    ocResource.setHost(securedHost);
                }
            }

            try {
                ocResource.get(
                        new HashMap<>(),
                        new OcResource.OnGetListener() {
                            @Override
                            public void onGetCompleted(List<OcHeaderOption> list, OcRepresentation ocRepresentation) {
                                emitter.onSuccess(ocRepresentation);
                            }

                            @Override
                            public void onGetFailed(Throwable throwable) {
                                LOG.error(throwable.getLocalizedMessage());
                                emitter.onError(throwable);
                            }
                        }
                );
            } catch (OcException ex) {
                LOG.error(ex.getMessage());
                emitter.onError(ex);
            }
        });
    }

    public Single<OcRepresentation> post(OcResource ocResource, boolean secured, OcRepresentation rep) {
        return Single.create(emitter -> {
            if (secured) {
                String securedHost = getCoapsIpv6Host(ocResource.getAllHosts());
                if (securedHost != null) {
                    ocResource.setHost(securedHost);
                }
            }

            try {
                ocResource.post(rep,
                        new HashMap<>(),
                        new OcResource.OnPostListener() {
                            @Override
                            public void onPostCompleted(List<OcHeaderOption> list, OcRepresentation ocRepresentation) {
                                emitter.onSuccess(ocRepresentation);
                            }

                            @Override
                            public void onPostFailed(Throwable throwable) {
                                LOG.error(throwable.getLocalizedMessage());
                                emitter.onError(throwable);
                            }
                        });
            } catch (OcException ex) {
                LOG.error(ex.getMessage());
                emitter.onError(ex);
            }
        });
    }

    public Single<OcResource> constructResource(String host,
                                                String uri,
                                                List<String> resourceTypes,
                                                List<String> interfaceList) {
        return Single.create(emitter -> {
            OcResource ocResource = null;
            try {
                ocResource = OcPlatform.constructResourceObject(host,
                        uri,
                        CONNECTIVITY_TYPES,
                        false,
                        resourceTypes,
                        interfaceList);
            } catch (OcException e) {
                LOG.error(e.getMessage());
                emitter.onError(e);
            }

            emitter.onSuccess(ocResource);
        });
    }

    public Single<Device> findSingleDevice(@NonNull String deviceId, @NonNull DeviceType deviceType) {
        return Single.create(emitter -> {
            try {
                OcSecureResource ownedDevice = OcProvisioning.discoverSingleDevice(getDiscoveryTimeout(), deviceId);
                if (ownedDevice != null) {
                    OcDevice deviceInfo = retrieveDeviceInfo(IotivityUtils.getValidIp(ownedDevice.getIpAddr()));
                    emitter.onSuccess(new Device(deviceType, ownedDevice.getDeviceID(), deviceInfo, ownedDevice));
                }
            } catch (OcException ex) {
                LOG.error(ex.getMessage());
                emitter.onError(ex);
            }
        });
    }

    public void setRandomPinCallbackListener(OcProvisioning.PinCallbackListener randomPinCallbackListener) {
        try {
            OcProvisioning.setOwnershipTransferCBdata(OxmType.OIC_RANDOM_DEVICE_PIN, randomPinCallbackListener);
        } catch (OcException ex) {
            LOG.error("Exception setting random PIN callback listener: " + ex.getMessage());
        }
    }

    public void setDisplayPinListener(OcProvisioning.DisplayPinListener displayPinListener) {
        try {
            OcProvisioning.setDisplayPinListener(displayPinListener);
        } catch (OcException ex) {
            LOG.error("Exception setting callback to display PIN: " + ex.getMessage());
        }
    }

    public Single<OcRepresentation> getPlatformInfo(String host) {
        return Single.create(emitter -> {
            try {
                OcPlatform.getPlatformInfo(host,
                        OcPlatform.WELL_KNOWN_PLATFORM_QUERY,
                        CONNECTIVITY_TYPES,
                        emitter::onSuccess);
            } catch (OcException ex) {
                LOG.error(ex.getMessage());
                emitter.onError(ex);
            }
        });
    }

    public Single<List<OcResource>> getResourceTypes(String ipAddress) {
        return Single.create(emitter -> {
            try {
                OcPlatform.findResources(ipAddress,
                        OcPlatform.WELL_KNOWN_QUERY,
                        CONNECTIVITY_TYPES,
                        new OcPlatform.OnResourcesFoundListener() {
                            @Override
                            public void onResourcesFound(OcResource[] resources) {
                                List<OcResource> resourceList = new ArrayList<>();
                                for (OcResource resource : resources) {
                                    for (String resourceType : resource.getResourceTypes()) {
                                        if (!RESOURCE_TYPES_TO_FILTER.contains(resourceType)
                                                && !resourceType.startsWith("oic.d.")) {
                                            resourceList.add(resource);
                                        }
                                    }
                                }

                                emitter.onSuccess(resourceList);
                            }

                            @Override
                            public void onFindResourcesFailed(Throwable ex, String uri) {
                                // TODO
                            }
                        });
            } catch (OcException e) {
                if (e.getErrorCode() == ErrorCode.ADAPTER_NOT_ENABLED) {
                    LOG.warn(e.getLocalizedMessage());
                } else {
                    LOG.error(e.getLocalizedMessage());
                }
            }
        });
    }

    public void close() {
        closeOICStack();
    }

    public void closeOICStack() {
        try {
            // Close database
            OcProvisioning.provisionClose();
        } catch (OcException ex) {
            LOG.error(ex.getMessage());
        }
        OcPlatform.Shutdown();
    }
}
