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

package org.openconnectivity.otgc.common.data.persistence;

import javax.inject.Singleton;
import javax.persistence.*;
import java.util.Map;

@Singleton
public class DatabaseManager {
    private static final String PERSISTENCE_UNIT_NAME = "otgc-pu";
    private static final EntityManagerFactory emf;
    private static final ThreadLocal<EntityManager> threadLocal;

    static {
        emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        threadLocal = new ThreadLocal<>();
    }

    public static EntityManager getEntityManager() {
        EntityManager em = threadLocal.get();

        if (em == null) {
            em = emf.createEntityManager();
            threadLocal.set(em);
        }

        return em;
    }

    public static void beginTransaction() {
        getEntityManager().getTransaction().begin();
    }

    public static void rollback() {
        getEntityManager().getTransaction().rollback();
    }

    public static void commit() {
        getEntityManager().getTransaction().commit();
    }

    public static boolean isOpen() {
        return getEntityManager().isOpen();
    }

    public static void insertOrUpdate(Object entity) {
        beginTransaction();
        getEntityManager().merge(entity);
        commit();
    }

    public static Query createNamedQuery(String queryName, Map<String, Object> parameters) {
        beginTransaction();
        Query q = getEntityManager().createNamedQuery(queryName);
        if (parameters != null && parameters.size() > 0) {
            for (String key : parameters.keySet()) {
                q.setParameter(key, parameters.get(key));
            }
        }
        commit();
        return q;
    }

    public static Query updateWithNamedQuery(String queryName, Map<String, Object> parameters) {
        beginTransaction();
        Query q = getEntityManager().createNamedQuery(queryName);
        if (parameters != null && parameters.size() > 0) {
            for (String key : parameters.keySet()) {
                q.setParameter(key, parameters.get(key));
            }
        }
        q.executeUpdate();
        commit();
        return q;
    }

    public static void remove(Object entity) {
        beginTransaction();
        getEntityManager().remove(entity);
        commit();
    }

    public static void closeEntityManager() {
        EntityManager em = threadLocal.get();
        if (em != null) {
            em.close();
            threadLocal.set(null);
        }
    }

    public static void closeEntityManagerFactory() {
        emf.close();
    }
}
