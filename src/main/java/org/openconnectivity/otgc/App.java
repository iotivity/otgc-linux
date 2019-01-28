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

package org.openconnectivity.otgc;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.ViewTuple;
import de.saxsys.mvvmfx.guice.MvvmfxGuiceApplication;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.iotivity.base.OcPlatform;
import org.openconnectivity.otgc.common.data.persistence.DatabaseManager;
import org.openconnectivity.otgc.login.LoginView;
import org.openconnectivity.otgc.login.LoginViewModel;
import org.openconnectivity.otgc.common.util.OpenScene;
import org.openconnectivity.otgc.main.MainView;
import org.openconnectivity.otgc.main.MainViewModel;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class App extends MvvmfxGuiceApplication {

    private static final Logger LOG = Logger.getLogger(App.class);

    public static void main(String...args){
        Application.launch(args);
    }

    private ResourceBundle resourceBundle;

    @Override
    public void startMvvmfx(Stage stage){
        LOG.debug("Starting the application");

        resourceBundle = ResourceBundle.getBundle("properties.Strings", new Locale("en", "EN"));
        MvvmFX.setGlobalResourceBundle(resourceBundle);

        stage.setTitle(resourceBundle.getString("window.title"));

        // Load the login screen
        //ViewTuple<LoginView, LoginViewModel> loginTuple = FluentViewLoader.fxmlView(LoginView.class).load();
        // Load main screen
        ViewTuple<MainView, MainViewModel> mainTuple = FluentViewLoader.fxmlView(MainView.class).load();
        List<String> styles = new ArrayList<>();
        //styles.add("styles/login.css");
        try {
            OpenScene.start(stage, mainTuple, styles);
            stage.setOnCloseRequest(event -> closeApp());
        } catch (Exception ex) {
            LOG.debug(ex.getMessage());
        }

        RxJavaPlugins.setErrorHandler(throwable -> {
            if (throwable instanceof UndeliverableException) {
                LOG.warn(throwable.getCause().getLocalizedMessage());
            } else {
                throw new RuntimeException(throwable);
            }

        });
    }

    private void closeApp() {
        OcPlatform.Shutdown();
        DatabaseManager.closeEntityManager();
        DatabaseManager.closeEntityManagerFactory();
        Platform.exit();
    }

}