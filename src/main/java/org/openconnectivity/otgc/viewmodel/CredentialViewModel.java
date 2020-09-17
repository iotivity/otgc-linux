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

import de.saxsys.mvvmfx.InjectResourceBundle;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import io.reactivex.disposables.CompositeDisposable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import org.openconnectivity.otgc.domain.model.resource.secure.cred.OcCredential;
import org.openconnectivity.otgc.domain.model.resource.secure.cred.OcCredentials;
import org.openconnectivity.otgc.domain.usecase.UpdateDeviceTypeUseCase;
import org.openconnectivity.otgc.utils.constant.NotificationKey;
import org.openconnectivity.otgc.utils.rx.SchedulersFacade;
import org.openconnectivity.otgc.utils.viewmodel.Response;
import org.openconnectivity.otgc.domain.usecase.credential.DeleteCredentialUseCase;
import org.openconnectivity.otgc.domain.usecase.credential.ProvisionIdentityCertificateUseCase;
import org.openconnectivity.otgc.domain.usecase.credential.ProvisionRoleCertificateUseCase;
import org.openconnectivity.otgc.domain.usecase.credential.RetrieveCredentialsUseCase;
import org.openconnectivity.otgc.domain.model.devicelist.Device;
import org.openconnectivity.otgc.domain.model.devicelist.DeviceType;
import org.openconnectivity.otgc.utils.scopes.DeviceListToolbarDetailScope;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class CredentialViewModel implements ViewModel {
    public ObjectProperty<List<Device>> deviceProperty;

    @InjectScope
    private DeviceListToolbarDetailScope deviceListToolbarDetailScope;

    @InjectResourceBundle
    private ResourceBundle resourceBundle;

    private final CompositeDisposable disposable = new CompositeDisposable();

    private final SchedulersFacade schedulersFacade;
    private final ProvisionIdentityCertificateUseCase provisionIdentityCertificateUseCase;
    private final ProvisionRoleCertificateUseCase provisionRoleCertificateUseCase;
    private final RetrieveCredentialsUseCase retrieveCredentialsUseCase;
    private final DeleteCredentialUseCase deleteCredentialUseCase;
    private final UpdateDeviceTypeUseCase updateDeviceTypeUseCase;

    // Observable responses
    private final ObjectProperty<Response<Boolean>> createCredResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<OcCredentials>> retrieveCredsResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Boolean>> deleteCredResponse = new SimpleObjectProperty<>();

    private ListProperty<OcCredential> credList = new SimpleListProperty<>();
    public ListProperty<OcCredential> credListProperty() { return credList; }
    private StringProperty selectedTab = new SimpleStringProperty();
    public StringProperty selectedTabProperty() {
        return this.selectedTab;
    }

    @Inject
    public CredentialViewModel(SchedulersFacade schedulersFacade,
                               ProvisionIdentityCertificateUseCase provisionIdentityCertificateUseCase,
                               ProvisionRoleCertificateUseCase provisionRoleCertificateUseCase,
                               RetrieveCredentialsUseCase retrieveCredentialsUseCase,
                               DeleteCredentialUseCase deleteCredentialUseCase,
                               UpdateDeviceTypeUseCase updateDeviceTypeUseCase) {
        this.schedulersFacade = schedulersFacade;
        this.provisionIdentityCertificateUseCase = provisionIdentityCertificateUseCase;
        this.provisionRoleCertificateUseCase = provisionRoleCertificateUseCase;
        this.retrieveCredentialsUseCase = retrieveCredentialsUseCase;
        this.deleteCredentialUseCase = deleteCredentialUseCase;
        this.updateDeviceTypeUseCase = updateDeviceTypeUseCase;
    }

    public void initialize() {
        deviceProperty = deviceListToolbarDetailScope.selectedDeviceProperty();
        deviceProperty.addListener(this::loadCredentials);
        selectedTab = deviceListToolbarDetailScope.selectedTabProperty();
        selectedTabProperty().addListener(this::loadCredentials);
    }

    public ObservableBooleanValue cmsVisibleProperty() {
        return Bindings.createBooleanBinding(() -> deviceProperty.get() != null && deviceProperty.get().size() == 1
                && (deviceProperty.get().get(0).getDeviceType() != DeviceType.UNOWNED
                    && deviceProperty.get().get(0).getDeviceType() != DeviceType.CLOUD), deviceProperty);
    }

    public ObjectProperty<Response<Boolean>> createCredResponseProperty() {
        return createCredResponse;
    }

    public ObjectProperty<Response<OcCredentials>> retrieveCredsResponseProperty() {
        return retrieveCredsResponse;
    }

    public ObjectProperty<Response<Boolean>> deleteCredResponseProperty() {
        return deleteCredResponse;
    }

    public void loadCredentials(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        // Clean Info
        credListProperty().clear();

        if ((newValue != null) && newValue.equals(resourceBundle.getString("client.tab.cms")) && deviceProperty.get() != null
                && deviceProperty.get().size() == 1 && (deviceProperty.get().get(0).getDeviceType() != DeviceType.UNOWNED)) {
            retrieveCreds(deviceProperty.get().get(0));
        }
    }

    public void loadCredentials(ObservableValue<? extends List<Device>> observable, List<Device> oldValue, List<Device> newValue) {
        // Clean Info
        credListProperty().clear();

        if (selectedTabProperty().get() != null && selectedTabProperty().get().equals(resourceBundle.getString("client.tab.cms")) && (newValue != null)
                && newValue.size() == 1 && (newValue.get(0).getDeviceType() != DeviceType.UNOWNED)) {
            retrieveCreds(newValue.get(0));
        }
    }

    public void retrieveCreds(Device device) {
        if (device.getDeviceType() != DeviceType.CLOUD) {
            disposable.add(retrieveCredentialsUseCase.execute(device)
                    .subscribeOn(schedulersFacade.io())
                    .observeOn(schedulersFacade.ui())
                    .doOnSubscribe(__ -> retrieveCredsResponse.setValue(Response.loading()))
                    .subscribe(
                            credentials -> {
                                retrieveCredsResponse.setValue(Response.success(credentials));

                                if (!device.hasCREDpermit()
                                        && (device.getDeviceType() == DeviceType.OWNED_BY_OTHER
                                        || device.getDeviceType() == DeviceType.OWNED_BY_OTHER_WITH_PERMITS)) {
                                    disposable.add(updateDeviceTypeUseCase.execute(device.getDeviceId(),
                                            DeviceType.OWNED_BY_OTHER_WITH_PERMITS,
                                            device.getPermits() | Device.CRED_PERMITS)
                                            .subscribeOn(schedulersFacade.io())
                                            .observeOn(schedulersFacade.ui())
                                            .subscribe(
                                                    () -> deviceListToolbarDetailScope.publish(NotificationKey.UPDATE_DEVICE_TYPE, device),
                                                    throwable -> retrieveCredsResponse.setValue(Response.error(throwable))
                                            ));
                                }
                            },
                            throwable -> {
                                retrieveCredsResponse.setValue(Response.error(throwable));

                                if (device.hasCREDpermit()) {
                                    disposable.add(updateDeviceTypeUseCase.execute(device.getDeviceId(),
                                            DeviceType.OWNED_BY_OTHER,
                                            device.getPermits() & ~Device.CRED_PERMITS)
                                            .subscribeOn(schedulersFacade.io())
                                            .observeOn(schedulersFacade.ui())
                                            .subscribe(
                                                    () -> deviceListToolbarDetailScope.publish(NotificationKey.UPDATE_DEVICE_TYPE, device),
                                                    throwable2 -> retrieveCredsResponse.setValue(Response.error(throwable))
                                            ));
                                }
                            }
                    )
            );
        }
    }

    public void setCred(OcCredentials credential) {
        List<OcCredential> creds = credListProperty().get();
        if (creds == null) {
            creds = new ArrayList<>();
        }

        for (OcCredential cred : credential.getCredList()) {
            creds.add(cred);
        }

        creds.sort(Comparator.comparing(OcCredential::getCredid));
        this.credListProperty().setValue(FXCollections.observableArrayList(creds));
    }

    public void provisionIdentityCertificate() {
        if (deviceProperty.get().size() == 1){
            disposable.add(provisionIdentityCertificateUseCase.execute(deviceProperty.get().get(0))
                    .subscribeOn(schedulersFacade.io())
                    .observeOn(schedulersFacade.ui())
                    .doOnSubscribe(__ -> createCredResponse.setValue(Response.loading()))
                    .subscribe(
                            () -> createCredResponse.setValue(Response.success(true)),
                            throwable -> createCredResponse.setValue(Response.error(throwable))
                    )
            );
        }
    }

    public void provisionRoleCertificate(String roleId, String roleAuthority) {
        if (deviceProperty.get().size() == 1){
            disposable.add(provisionRoleCertificateUseCase.execute(deviceProperty.get().get(0), roleId, roleAuthority)
                    .subscribeOn(schedulersFacade.io())
                    .observeOn(schedulersFacade.ui())
                    .doOnSubscribe(__ -> createCredResponse.setValue(Response.loading()))
                    .subscribe(
                            () -> createCredResponse.setValue(Response.success(true)),
                            throwable -> createCredResponse.setValue(Response.error(throwable))
                    )
            );
        }
    }

    public void deleteCred(long credId) {
        if (deviceProperty.get().size() == 1) {
            disposable.add(deleteCredentialUseCase.execute(deviceProperty.get().get(0), credId)
                    .subscribeOn(schedulersFacade.io())
                    .observeOn(schedulersFacade.ui())
                    .doOnSubscribe(__ -> deleteCredResponse.setValue(Response.loading()))
                    .subscribe(
                            () -> deleteCredResponse.setValue(Response.success(true)),
                            throwable -> deleteCredResponse.setValue(Response.error(throwable))
                    ));
        }
    }
}
