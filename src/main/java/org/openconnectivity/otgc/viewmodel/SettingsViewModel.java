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
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableListValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import org.openconnectivity.otgc.utils.constant.DiscoveryScope;
import org.openconnectivity.otgc.utils.rx.SchedulersFacade;
import org.openconnectivity.otgc.domain.usecase.setting.GetSettingUseCase;
import org.openconnectivity.otgc.domain.usecase.setting.UpdateSettingUseCase;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsViewModel implements ViewModel {

    private CompositeDisposable disposables = new CompositeDisposable();

    private final SchedulersFacade schedulersFacade;
    private final GetSettingUseCase getSettingUseCase;
    private final UpdateSettingUseCase updateSettingUseCase;

    private static final String DISCOVERY_TIMEOUT_KEY = "discovery_timeout";
    private static final String DISCOVERY_TIMEOUT_DEFAULT = "5";
    private static final String DISCOVERY_SCOPE_KEY = "discovery_scope";
    private static final String DISCOVERY_SCOPE_DEFAULT = "Link-Local";
    private static final String REQUESTS_DELAY_KEY = "requests_delay";
    private static final String REQUESTS_DELAY_DEFAULT_VALUE = "1";

    private StringProperty discoveryTimeout = new SimpleStringProperty();
    public StringProperty discoveryTimeoutProperty() {
        return discoveryTimeout;
    }

    private ListProperty<String> discoveryScope = new SimpleListProperty<>();
    public ListProperty<String> discoveryScopeProperty() {
        return discoveryScope;
    }

    private StringProperty selectedDiscoveryScope = new SimpleStringProperty();
    public StringProperty selectedDiscoveryScopeProperty() {
        return selectedDiscoveryScope;
    }

    private StringProperty requestsDelay = new SimpleStringProperty();
    public StringProperty requestsDelayProperty() {
        return requestsDelay;
    }

    @Inject
    public SettingsViewModel(SchedulersFacade schedulersFacade,
                             GetSettingUseCase getSettingUseCase,
                             UpdateSettingUseCase updateSettingUseCase) {
        this.schedulersFacade = schedulersFacade;
        this.getSettingUseCase = getSettingUseCase;
        this.updateSettingUseCase = updateSettingUseCase;
    }

    public void initialize() {
        discoveryTimeout.setValue(getSettingUseCase.execute(DISCOVERY_TIMEOUT_KEY, DISCOVERY_TIMEOUT_DEFAULT));
        discoveryTimeoutProperty().addListener(this::discoveryTimeoutListener);

        String scopeList[] = { DiscoveryScope.DISCOVERY_SCOPE_LINK,
                DiscoveryScope.DISCOVERY_SCOPE_SITE,
                DiscoveryScope.DISCOVERY_SCOPE_REALM};

        discoveryScopeProperty().setValue(FXCollections.observableArrayList(scopeList));
        selectedDiscoveryScope.setValue(getSettingUseCase.execute(DISCOVERY_SCOPE_KEY, DISCOVERY_SCOPE_DEFAULT));
        selectedDiscoveryScopeProperty().addListener(this::discoveryScopeListener);

        requestsDelay.setValue(getSettingUseCase.execute(REQUESTS_DELAY_KEY, REQUESTS_DELAY_DEFAULT_VALUE));
        requestsDelayProperty().addListener(this::requestsDelayListener);
    }

    public void discoveryTimeoutListener(ObservableValue<? extends  String> observableValue, String oldValue, String newValue) {
        disposables.add(updateSettingUseCase.execute(DISCOVERY_TIMEOUT_KEY, newValue)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe());
    }

    public void discoveryScopeListener (ObservableValue<? extends  String> observableValue, String oldValue, String newValue) {
        disposables.add(updateSettingUseCase.execute(DISCOVERY_SCOPE_KEY, newValue)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe());
    }

    public void requestsDelayListener(ObservableValue<? extends  String> observableValue, String oldValue, String newValue) {
        disposables.add(updateSettingUseCase.execute(REQUESTS_DELAY_KEY, newValue)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe());
    }
}
