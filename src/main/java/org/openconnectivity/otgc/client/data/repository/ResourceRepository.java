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

package org.openconnectivity.otgc.client.data.repository;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import org.apache.log4j.Logger;
import org.iotivity.base.*;
import org.openconnectivity.otgc.client.domain.model.SerializableResource;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class ResourceRepository {

    private Logger LOG = Logger.getLogger(ResourceRepository.class);

    private Map<OcResource, OcResource.OnObserveListener> observeMap = new HashMap<>();
    private Map<OcResource, ObservableEmitter> emitterMap = new HashMap<>();

    @Inject
    public ResourceRepository() {}

    public Observable<SerializableResource> observeResource(OcResource ocResource, SerializableResource oldResource) {
        return Observable.create(emitter -> {
            // Add resource to map
            observeMap.put(ocResource, new OcResource.OnObserveListener() {
                @Override
                public void onObserveCompleted(List<OcHeaderOption> list, OcRepresentation ocRepresentation, int i) {
                    SerializableResource serializableResource = new SerializableResource(ocResource);
                    serializableResource.setObservable(oldResource.isObservable());
                    serializableResource.setOcRepresentation(ocRepresentation);
                    serializableResource.setObserving(oldResource.isObserving());
                    if (!emitterMap.containsKey(ocResource)) {
                        emitterMap.put(ocResource, emitter);
                    }
                    emitter.onNext(serializableResource);
                }

                @Override
                public void onObserveFailed(Throwable throwable) {
                    emitter.onError(throwable);
                }
            });

            try {
                ocResource.observe(ObserveType.OBSERVE, new HashMap<>(), observeMap.get(ocResource));
            } catch (OcException ex) {
                LOG.error(ex.getLocalizedMessage());
                emitter.onError(ex);
            }
        });
    }

    public Completable cancelObserve(SerializableResource resource) {
        return Completable.create(emitter -> {
            // Search resource in map
            OcResource res = null;
            for (OcResource ocResource : observeMap.keySet()) {
                if (ocResource.getUri().equals(resource.getUri())) {
                    res = ocResource;
                    break;
                }
            }

            try {
                res.cancelObserve(QualityOfService.HIGH);
            } catch (OcException ex) {
                LOG.error(ex.getLocalizedMessage());
                emitter.onError(ex);
            }

            // Delete callback from map
            observeMap.remove(res);
            ObservableEmitter observableEmitter = emitterMap.get(res);
            emitterMap.remove(res);
            observableEmitter.onComplete();

            emitter.onComplete();
        });
    }

    public Completable cancelAllObserve() {
        return Completable.create(emitter -> {
            for (OcResource ocResource : observeMap.keySet()) {
                try {
                    ocResource.cancelObserve(QualityOfService.HIGH);
                } catch (OcException ex) {
                    LOG.error(ex.getLocalizedMessage());
                }

                // Delete callback from map
                observeMap.remove(ocResource);
                ObservableEmitter observableEmitter = emitterMap.get(ocResource);
                emitterMap.remove(ocResource);
                observableEmitter.onComplete();
            }

            emitter.onComplete();
        });
    }
}
