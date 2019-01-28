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

package org.openconnectivity.otgc.settings;

import de.saxsys.mvvmfx.ViewModel;
import io.reactivex.disposables.CompositeDisposable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import org.openconnectivity.otgc.common.rx.SchedulersFacade;
import org.openconnectivity.otgc.settings.domain.usecase.GetSettingUseCase;
import org.openconnectivity.otgc.settings.domain.usecase.UpdateSettingUseCase;

import javax.inject.Inject;

public class SettingsViewModel implements ViewModel {

    private CompositeDisposable disposables = new CompositeDisposable();

    private final SchedulersFacade schedulersFacade;
    private final GetSettingUseCase getSettingUseCase;
    private final UpdateSettingUseCase updateSettingUseCase;

    private static final String DISCOVERY_TIMEOUT_KEY = "discovery_timeout";
    private static final String DISCOVERY_TIMEOUT_DEFAULT = "5";

    private StringProperty discoveryTimeout = new SimpleStringProperty();
    public StringProperty discoveryTimeoutProperty() {
        return discoveryTimeout;
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
    }

    public void discoveryTimeoutListener(ObservableValue<? extends  String> observableValue, String oldValue, String newValue) {
        disposables.add(updateSettingUseCase.execute(DISCOVERY_TIMEOUT_KEY, newValue)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe());
    }
}
