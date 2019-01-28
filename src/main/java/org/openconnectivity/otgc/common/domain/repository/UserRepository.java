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

package org.openconnectivity.otgc.common.domain.repository;

import io.reactivex.Completable;
import io.reactivex.Single;
import org.openconnectivity.otgc.common.domain.model.User;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.prefs.Preferences;

@Singleton
public class UserRepository {

    private Preferences prefs;

    @Inject
    UserRepository() {
        prefs = Preferences.userNodeForPackage(this.getClass());
    }

    public Single<User> getUser() {
        return Single.create(source -> {
            String id = prefs.get("Id", "");
            String username = prefs.get("Username", "");
            String password = prefs.get("Password", "");

            User user = new User(id, username, password);

            source.onSuccess(user);
        });
    }

    public Completable saveUser(String username, String password) {
        return Completable.create(emitter -> {
            User user = new User(username, password);

            prefs.put("Id", user.getId());
            prefs.put("Username", user.getUsername());
            prefs.put("Password", user.getPassword());

            emitter.onComplete();
        });
    }

    public Completable deleteUser() {
        return Completable.create(emitter -> {
            prefs.remove("Id");
            prefs.remove("Username");
            prefs.remove("Password");

            emitter.onComplete();
        });
    }
}
