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
import org.apache.log4j.Logger;
import org.iotivity.*;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ProvisionRepository {

    private final Logger LOG = Logger.getLogger(ProvisionRepository.class);

    @Inject
    public ProvisionRepository(){}

    public Completable resetSvrDb() {
        return Completable.create(emitter -> {
            OCMain.reset();
            emitter.onComplete();
        });
    }

    public Completable doSelfOwnership() {
        return Completable.create(emitter -> {
            OCObt.init();
            emitter.onComplete();
        });
    }
}
