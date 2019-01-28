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

package org.openconnectivity.otgc.credential;

import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import io.reactivex.disposables.CompositeDisposable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import org.iotivity.base.OicSecCred;
import org.iotivity.base.OicSecCreds;
import org.openconnectivity.otgc.common.rx.SchedulersFacade;
import org.openconnectivity.otgc.common.viewmodel.Response;
import org.openconnectivity.otgc.credential.domain.usecase.DeleteCredentialUseCase;
import org.openconnectivity.otgc.credential.domain.usecase.ProvisionIdentityCertificateUseCase;
import org.openconnectivity.otgc.credential.domain.usecase.ProvisionRoleCertificateUseCase;
import org.openconnectivity.otgc.credential.domain.usecase.RetrieveCredentialsUseCase;
import org.openconnectivity.otgc.devicelist.domain.model.Device;
import org.openconnectivity.otgc.devicelist.domain.model.DeviceType;
import org.openconnectivity.otgc.scopes.DeviceListToolbarDetailScope;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CredentialViewModel implements ViewModel {
    public ObjectProperty<Device> deviceProperty;

    @InjectScope
    private DeviceListToolbarDetailScope deviceListToolbarDetailScope;

    private final CompositeDisposable disposable = new CompositeDisposable();

    private final SchedulersFacade schedulersFacade;
    private final ProvisionIdentityCertificateUseCase provisionIdentityCertificateUseCase;
    private final ProvisionRoleCertificateUseCase provisionRoleCertificateUseCase;
    private final RetrieveCredentialsUseCase retrieveCredentialsUseCase;
    private final DeleteCredentialUseCase deleteCredentialUseCase;

    // Observable responses
    private final ObjectProperty<Response<Boolean>> createCredResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<OicSecCreds>> retrieveCredsResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Boolean>> deleteCredResponse = new SimpleObjectProperty<>();

    private ListProperty<OicSecCred> credList = new SimpleListProperty<>();
    public ListProperty<OicSecCred> credListProperty() { return credList; }

    @Inject
    public CredentialViewModel(SchedulersFacade schedulersFacade,
                               ProvisionIdentityCertificateUseCase provisionIdentityCertificateUseCase,
                               ProvisionRoleCertificateUseCase provisionRoleCertificateUseCase,
                               RetrieveCredentialsUseCase retrieveCredentialsUseCase,
                               DeleteCredentialUseCase deleteCredentialUseCase) {
        this.schedulersFacade = schedulersFacade;
        this.provisionIdentityCertificateUseCase = provisionIdentityCertificateUseCase;
        this.provisionRoleCertificateUseCase = provisionRoleCertificateUseCase;
        this.retrieveCredentialsUseCase = retrieveCredentialsUseCase;
        this.deleteCredentialUseCase = deleteCredentialUseCase;
    }

    public void initialize() {
        deviceProperty = deviceListToolbarDetailScope.selectedDeviceProperty();
        deviceProperty.addListener(this::loadCredentials);
    }

    public ObservableBooleanValue cmsVisibleProperty() {
        return Bindings.createBooleanBinding(() -> deviceProperty.get() != null
                && (deviceProperty.get().getDeviceType() == DeviceType.OWNED_BY_SELF
                || deviceProperty.get().getDeviceType() == DeviceType.OWNED_BY_OTHER), deviceProperty);
    }

    public ObjectProperty<Response<Boolean>> createCredResponseProperty() {
        return createCredResponse;
    }

    public ObjectProperty<Response<OicSecCreds>> retrieveCredsResponseProperty() {
        return retrieveCredsResponse;
    }

    public ObjectProperty<Response<Boolean>> deleteCredResponseProperty() {
        return deleteCredResponse;
    }

    public void loadCredentials(ObservableValue<? extends Device> observable, Device oldValue, Device newValue) {
        // Clean Info
        credListProperty().clear();

        if ((newValue != null) && (newValue.getDeviceType() == DeviceType.OWNED_BY_SELF
                || newValue.getDeviceType() == DeviceType.OWNED_BY_OTHER)) {
            retrieveCreds(newValue.getDeviceId());
        }
    }

    public void retrieveCreds(String deviceId) {
        disposable.add(retrieveCredentialsUseCase.execute(deviceId)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> retrieveCredsResponse.setValue(Response.loading()))
                .subscribe(
                        credentials -> retrieveCredsResponse.setValue(Response.success(credentials)),
                        throwable -> retrieveCredsResponse.setValue(Response.error(throwable))
                )
        );
    }

    public void setCred(OicSecCreds credential) {
        List<OicSecCred> creds = credListProperty().get();
        if (creds == null) {
            creds = new ArrayList<>();
        }

        for (OicSecCred cred : credential.getOicSecCredsList()) {
            creds.add(cred);
        }

        creds.sort(Comparator.comparing(OicSecCred::getCredID));
        this.credListProperty().setValue(FXCollections.observableArrayList(creds));
    }

    public void provisionIdentityCertificate() {
        String deviceId = deviceProperty.get().getDeviceId();
        disposable.add(provisionIdentityCertificateUseCase.execute(deviceId)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> createCredResponse.setValue(Response.loading()))
                .subscribe(
                        () -> createCredResponse.setValue(Response.success(true)),
                        throwable -> createCredResponse.setValue(Response.error(throwable))
                )
        );
    }

    public void provisionRoleCertificate(String roleId, String roleAuthority) {
        String deviceId = deviceProperty.get().getDeviceId();
        disposable.add(provisionRoleCertificateUseCase.execute(deviceId, roleId, roleAuthority)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> createCredResponse.setValue(Response.loading()))
                .subscribe(
                        () -> createCredResponse.setValue(Response.success(true)),
                        throwable -> createCredResponse.setValue(Response.error(throwable))
                )
        );
    }

    public void deleteCred(String deviceId, int credId) {
        disposable.add(deleteCredentialUseCase.execute(deviceId, credId)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> deleteCredResponse.setValue(Response.loading()))
                .subscribe(
                        () -> deleteCredResponse.setValue(Response.success(true)),
                        throwable -> deleteCredResponse.setValue(Response.error(throwable))
                ));

    }
}
