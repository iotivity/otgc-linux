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

package org.openconnectivity.otgc.viewmodel;

import de.saxsys.mvvmfx.ViewModel;
import io.reactivex.disposables.CompositeDisposable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.iotivity.OCCloudStatusMask;
import org.openconnectivity.otgc.domain.usecase.cloud.ProvisionCloudConfUseCase;
import org.openconnectivity.otgc.domain.usecase.cloud.RetrieveCloudConfigurationUseCase;
import org.openconnectivity.otgc.domain.usecase.cloud.RetrieveStatusUseCase;
import org.openconnectivity.otgc.utils.rx.SchedulersFacade;
import org.openconnectivity.otgc.utils.viewmodel.Response;

import javax.inject.Inject;


public class CloudViewModel implements ViewModel {

    private final SchedulersFacade schedulersFacade;
    private final CompositeDisposable disposables = new CompositeDisposable();

    // Use Cases
    private final RetrieveStatusUseCase retrieveStatusUseCase;
    private final RetrieveCloudConfigurationUseCase retrieveCloudConfigurationUseCase;
    private final ProvisionCloudConfUseCase provisionCloudConfUseCase;

    // Observable responses
    private final ObjectProperty<Response<Integer>> retrieveStatusResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Void>> retrieveCloudConfResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Void>> storeCloudConfigResponse = new SimpleObjectProperty<>();

    // Observable properties
    private final StringProperty statusProperty = new SimpleStringProperty();
    public StringProperty getStatusProperty() {
        return statusProperty;
    }
    private final StringProperty authProviderProperty = new SimpleStringProperty();
    public StringProperty getAuthProviderProperty() {
        return authProviderProperty;
    }
    private final StringProperty cloudUrlProperty = new SimpleStringProperty();
    public StringProperty getCloudUrlProperty() {
        return cloudUrlProperty;
    }
    private final StringProperty accessTokenProperty = new SimpleStringProperty();
    public StringProperty getAccessTokenProperty() {
        return accessTokenProperty;
    }
    private final StringProperty cloudUuidProperty = new SimpleStringProperty();
    public StringProperty getCloudUuidProperty() {
        return cloudUuidProperty;
    }

    @Inject
    public CloudViewModel(SchedulersFacade schedulersFacade,
                          RetrieveStatusUseCase retrieveStatusUseCase,
                          RetrieveCloudConfigurationUseCase retrieveCloudConfigurationUseCase,
                          ProvisionCloudConfUseCase provisionCloudConfUseCase) {
        this.schedulersFacade = schedulersFacade;
        this.retrieveStatusUseCase = retrieveStatusUseCase;
        this.retrieveCloudConfigurationUseCase = retrieveCloudConfigurationUseCase;
        this.provisionCloudConfUseCase = provisionCloudConfUseCase;

        retrieveStatus();
        retrieveCloudConfiguration();
    }

    public ObjectProperty<Response<Integer>> retrieveStatusResponseProperty() {
        return retrieveStatusResponse;
    }

    public ObjectProperty<Response<Void>> retrieveCloudConfResponseProperty() {
        return retrieveCloudConfResponse;
    }

    public ObjectProperty<Response<Void>> storeCloudConfigResponseProperty() {
        return storeCloudConfigResponse;
    }

    private void retrieveStatus() {
        disposables.add(retrieveStatusUseCase.execute()
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        (status) -> {
                            retrieveStatusResponse.setValue(Response.success(status));
                            updateValueStatus(status);
                        },
                        throwable -> retrieveStatusResponse.setValue(Response.error(throwable))
                ));
    }

    private void retrieveCloudConfiguration() {
        disposables.add(retrieveCloudConfigurationUseCase.execute()
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        (cloudConf) -> {
                            retrieveCloudConfResponse.setValue(Response.success(null));
                            authProviderProperty.setValue(cloudConf.getAuthProvider());
                            cloudUrlProperty.setValue(cloudConf.getCloudUrl());
                            accessTokenProperty.setValue(cloudConf.getAccessToken());
                            cloudUuidProperty.setValue(cloudConf.getCloudUuid());
                        },
                        throwable -> retrieveCloudConfResponse.setValue(Response.error(throwable))
                ));
    }

    public void updateValueStatus(int status) {
        String value = "";
        if (status == OCCloudStatusMask.OC_CLOUD_INITIALIZED) {
            value += "Initialized";
        }
        if ((status & OCCloudStatusMask.OC_CLOUD_REGISTERED) == OCCloudStatusMask.OC_CLOUD_REGISTERED) {
            value += "Registered";
        }
        if ((status & OCCloudStatusMask.OC_CLOUD_LOGGED_IN) == OCCloudStatusMask.OC_CLOUD_LOGGED_IN) {
            value += ", logged in";
        }
        if ((status & OCCloudStatusMask.OC_CLOUD_TOKEN_EXPIRY) == OCCloudStatusMask.OC_CLOUD_TOKEN_EXPIRY) {
            value += ", token expiry";
        }
        if ((status & OCCloudStatusMask.OC_CLOUD_REFRESHED_TOKEN) == OCCloudStatusMask.OC_CLOUD_REFRESHED_TOKEN) {
            value += ", refresh token";
        }
        if ((status & OCCloudStatusMask.OC_CLOUD_LOGGED_OUT) == OCCloudStatusMask.OC_CLOUD_LOGGED_OUT) {
            value += ", logged out";
        }
        if ((status & OCCloudStatusMask.OC_CLOUD_FAILURE) == OCCloudStatusMask.OC_CLOUD_FAILURE) {
            value += ", failure";
        }
        if ((status & OCCloudStatusMask.OC_CLOUD_DEREGISTERED) == OCCloudStatusMask.OC_CLOUD_DEREGISTERED) {
            value += "Deregistered";
        }

        if (value.isEmpty()) {
            value = "Unknown";
        }

        statusProperty.setValue(value);
    }

    public void provisionCloudConfiguration(String authProvider, String cloudUrl, String accessToken, String cloudUuid) {
        disposables.add(provisionCloudConfUseCase.execute(authProvider, cloudUrl, accessToken, cloudUuid)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        () -> storeCloudConfigResponse.setValue(Response.success(null)),
                        throwable -> storeCloudConfigResponse.setValue(Response.error(throwable))
                ));
    }
}
