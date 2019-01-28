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
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

@Singleton
public class SettingRepository {
    private final Logger LOG = Logger.getLogger(SettingRepository.class);

    private static final String CONFIG_FILE = "config.properties";
    private File configFile;

    // Constants
    public static final String DISCOVERY_TIMEOUT_KEY = "discovery_timeout";
    public static final String DISCOVERY_TIMEOUT_DEFAULT_VALUE = "5";
    public static final String FIRST_RUN_KEY = "FIRSTRUN";
    public static final String FIRST_RUN_DEFAULT_VALUE = "true";


    @Inject
    public SettingRepository() {
            configFile = new File(new File(".").getAbsolutePath() + File.separator + "data" + File.separator + CONFIG_FILE);
    }

    public String get(String key, String defaultValue) {
        Properties props = getProperties();

        return props.getProperty(key, defaultValue);
    }

    public Completable set(String key, String value) {
        return Completable.create(emitter -> {
            Properties props = getProperties();
            FileWriter writer = null;
            try {
                writer = new FileWriter(configFile);
                props.setProperty(key, value);
                props.store(writer, "Settings");

            } catch (IOException ex) {
                LOG.error(ex.getLocalizedMessage());
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        LOG.error(e.getLocalizedMessage());
                    }
                }
            }
            emitter.onComplete();
        });
    }

    private Properties getProperties() {
        Properties props = new Properties();
        FileReader reader = null;
        try {
            reader = new FileReader(configFile);
            props.load(reader);
        } catch (IOException ex) {
            LOG.error(ex.getLocalizedMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOG.error(e.getLocalizedMessage());
                }
            }
        }

        return props;
    }
}
