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

package org.openconnectivity.otgc.main;

import de.saxsys.mvvmfx.ScopeProvider;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import io.reactivex.disposables.CompositeDisposable;
import javafx.beans.property.*;
import javafx.scene.control.ProgressBar;
import org.iotivity.base.OcProvisioning;
import org.openconnectivity.otgc.common.constant.NotificationConstant;
import org.openconnectivity.otgc.common.rx.SchedulersFacade;
import org.openconnectivity.otgc.common.viewmodel.Response;
import org.openconnectivity.otgc.main.domain.usecase.InitOicStackUseCase;
import org.openconnectivity.otgc.main.domain.usecase.SetDisplayPinListenerUseCase;
import org.openconnectivity.otgc.main.domain.usecase.SetRandomPinListenerUseCase;
import org.openconnectivity.otgc.scopes.DeviceListToolbarDetailScope;

import javax.inject.Inject;

@ScopeProvider(scopes = {DeviceListToolbarDetailScope.class})
public class MainViewModel implements ViewModel {

    private final InitOicStackUseCase initOicStackUseCase;
    private final SchedulersFacade schedulersFacade;

    @Inject
    private NotificationCenter notificationCenter;

    // Listeners
    private final SetRandomPinListenerUseCase setRandomPinListenerUseCase;
    private final SetDisplayPinListenerUseCase setDisplayPinListenerUseCase;

    private final CompositeDisposable disposables = new CompositeDisposable();

    // Observable responses
    private final ObjectProperty<Response<Void>> initOicStackResponse = new SimpleObjectProperty<>();

    private DoubleProperty progressStatus = new SimpleDoubleProperty();
    public DoubleProperty progressStatusProperty() {
        return progressStatus;
    }

    @Inject
    public MainViewModel(SchedulersFacade schedulersFacade,
                         InitOicStackUseCase initOicStackUseCase,
                         SetRandomPinListenerUseCase setRandomPinListenerUseCase,
                         SetDisplayPinListenerUseCase setDisplayPinListenerUseCase) {
        this.schedulersFacade = schedulersFacade;
        this.initOicStackUseCase = initOicStackUseCase;
        this.setRandomPinListenerUseCase = setRandomPinListenerUseCase;
        this.setDisplayPinListenerUseCase = setDisplayPinListenerUseCase;

        disposables.add(this.initOicStackUseCase.execute()
                .subscribeOn(schedulersFacade.computation())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        () -> initOicStackResponse.setValue(Response.success(null)),
                        throwable -> initOicStackResponse.setValue(Response.error(throwable))
                ));
    }

    public void initialize() {
        notificationCenter.subscribe(NotificationConstant.SET_PROGRESS_STATUS, (key, payload) -> {
            Boolean isLoading = (Boolean) payload[0];

            if (isLoading) {
                progressStatus.setValue(ProgressBar.INDETERMINATE_PROGRESS);
            } else {
                progressStatus.setValue(1.0);
            }
        });
    }

    public ObjectProperty<Response<Void>> initOicStackResponseProperty() {
        return initOicStackResponse;
    }

    public void setRandomPinListener(OcProvisioning.PinCallbackListener randomPinCallbackListener) {
        setRandomPinListenerUseCase.execute(randomPinCallbackListener);
    }

    public void setDisplayPinListener(OcProvisioning.DisplayPinListener displayPinListener) {
        setDisplayPinListenerUseCase.execute(displayPinListener);
    }
}
