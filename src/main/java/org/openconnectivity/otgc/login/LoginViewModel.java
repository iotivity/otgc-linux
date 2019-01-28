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

package org.openconnectivity.otgc.login;

import de.saxsys.mvvmfx.ViewModel;
import io.reactivex.disposables.CompositeDisposable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.openconnectivity.otgc.common.rx.SchedulersFacade;
import org.openconnectivity.otgc.common.viewmodel.Response;

import javax.inject.Inject;

public class LoginViewModel implements ViewModel {

    private static final Logger LOG = Logger.getLogger(LoginViewModel.class);

    @Inject
    private Stage primaryStage;

    private final CompositeDisposable disposable = new CompositeDisposable();

    private final SaveCredentialsUseCase saveCredentialsUseCase;
    private final IsAuthenticatedUseCase isAuthenticatedUseCase;
    private final SchedulersFacade schedulersFacade;

    // Observable responses
    private final ObjectProperty<Response<Void>> authenticatedResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Boolean>> isAuthResponse = new SimpleObjectProperty<>();

    private StringProperty username = new SimpleStringProperty("");
    public StringProperty usernameProperty() { return username; }

    private StringProperty password = new SimpleStringProperty("");
    public StringProperty passwordProperty() { return password; }

    @Inject
    LoginViewModel(SaveCredentialsUseCase saveCredentialsUseCase,
                   SchedulersFacade schedulersFacade,
                   IsAuthenticatedUseCase isAuthenticatedUseCase) {
        this.saveCredentialsUseCase = saveCredentialsUseCase;
        this.schedulersFacade = schedulersFacade;
        this.isAuthenticatedUseCase = isAuthenticatedUseCase;

        disposable.add(this.isAuthenticatedUseCase.execute()
                    .subscribeOn(schedulersFacade.io())
                    .observeOn(schedulersFacade.ui())
                    .subscribe(
                            isAuth -> isAuthResponse.setValue(Response.success(isAuth)),
                            throwable -> isAuthResponse.setValue(Response.error(throwable))
                    ));
    }


    public String getUsername() { return username.get(); }
    public void setUsername(String value) { username.set(value); }

    public String getPassword() { return password.get(); }
    public void setPassword(String value) { password.set(value); }

    public BooleanBinding loginButtonDisabled() {
        return usernameProperty().isEmpty()
                .or(passwordProperty().isEmpty());
    }

    public ObjectProperty<Response<Void>> authenticatedResponseProperty() {
        return authenticatedResponse;
    }

    public ObjectProperty<Response<Boolean>> isAuthResponseProperty() {
        return isAuthResponse;
    }

    // Login button is clicked
    public void authenticate() {
        disposable.add(saveCredentialsUseCase.execute(getUsername(), getPassword())
                        .subscribeOn(schedulersFacade.io())
                        .observeOn(schedulersFacade.ui())
                        .subscribe(
                                () -> authenticatedResponse.set(Response.success(null)),
                                throwable -> authenticatedResponse.setValue(Response.error(throwable))
                        ));
    }

}
