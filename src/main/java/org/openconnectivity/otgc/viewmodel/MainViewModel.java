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

import de.saxsys.mvvmfx.ScopeProvider;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import io.reactivex.disposables.CompositeDisposable;
import javafx.beans.property.*;
import javafx.scene.control.ProgressBar;
import org.iotivity.OCRandomPinHandler;
import org.openconnectivity.otgc.utils.constant.NotificationKey;
import org.openconnectivity.otgc.utils.handler.OCSetRandomPinHandler;
import org.openconnectivity.otgc.utils.rx.SchedulersFacade;
import org.openconnectivity.otgc.utils.viewmodel.Response;
import org.openconnectivity.otgc.domain.usecase.InitOicStackUseCase;
import org.openconnectivity.otgc.domain.usecase.SetDisplayPinListenerUseCase;
import org.openconnectivity.otgc.domain.usecase.SetRandomPinListenerUseCase;
import org.openconnectivity.otgc.utils.scopes.DeviceListToolbarDetailScope;

import javax.inject.Inject;

@ScopeProvider(scopes = {DeviceListToolbarDetailScope.class})
public class MainViewModel implements ViewModel {

    private final InitOicStackUseCase initOicStackUseCase;
    private final SchedulersFacade schedulersFacade;

    @Inject
    private NotificationCenter notificationCenter;

    private final CompositeDisposable disposables = new CompositeDisposable();

    // Listener
    private final SetDisplayPinListenerUseCase displayPinListenerUseCase;
    private final SetRandomPinListenerUseCase randomPinListenerUseCase;

    // Observable responses
    private final ObjectProperty<Response<Void>> initOicStackResponse = new SimpleObjectProperty<>();

    private DoubleProperty progressStatus = new SimpleDoubleProperty();
    public DoubleProperty progressStatusProperty() {
        return progressStatus;
    }

    @Inject
    public MainViewModel(SchedulersFacade schedulersFacade,
                         InitOicStackUseCase initOicStackUseCase,
                         SetDisplayPinListenerUseCase displayPinListenerUseCase,
                         SetRandomPinListenerUseCase randomPinListenerUseCase) {
        this.schedulersFacade = schedulersFacade;
        this.initOicStackUseCase = initOicStackUseCase;
        this.displayPinListenerUseCase = displayPinListenerUseCase;
        this.randomPinListenerUseCase = randomPinListenerUseCase;

        disposables.add(this.initOicStackUseCase.execute()
                .subscribeOn(schedulersFacade.computation())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        () -> initOicStackResponse.setValue(Response.success(null)),
                        throwable -> initOicStackResponse.setValue(Response.error(throwable))
                ));
    }

    public void initialize() {
        notificationCenter.subscribe(NotificationKey.SET_PROGRESS_STATUS, (key, payload) -> {
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

    public void setDisplayPinListener(OCRandomPinHandler displayPinListener) {
        displayPinListenerUseCase.execute(displayPinListener);
    }

    public void setRandomPinListener(OCSetRandomPinHandler randomPinCallbackListener) {
        randomPinListenerUseCase.execute(randomPinCallbackListener);
    }
}
