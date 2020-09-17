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
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import io.reactivex.disposables.CompositeDisposable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.openconnectivity.otgc.domain.usecase.cloud.*;
import org.openconnectivity.otgc.utils.constant.NotificationKey;
import org.openconnectivity.otgc.domain.usecase.CloseIotivityUseCase;
import org.openconnectivity.otgc.domain.usecase.GetDeviceIdUseCase;
import org.openconnectivity.otgc.utils.rx.SchedulersFacade;
import org.openconnectivity.otgc.utils.viewmodel.Response;

import javax.inject.Inject;

public class MenuViewModel implements ViewModel {

    @Inject
    private NotificationCenter notificationCenter;

    private CompositeDisposable disposable = new CompositeDisposable();

    private final SchedulersFacade schedulersFacade;

    private final CloseIotivityUseCase closeIotivityUseCase;
    private final GetDeviceIdUseCase getDeviceIdUseCase;
    private final RetrieveStatusUseCase retrieveStatusUseCase;
    private final CloudRegisterUseCase cloudRegisterUseCase;
    private final CloudDeregisterUseCase cloudDeregisterUseCase;
    private final CloudLoginUseCase cloudLoginUseCase;
    private final CloudLogoutUseCase cloudLogoutUseCase;
    private final CloudRefreshTokenUseCase refreshTokenUseCase;
    private final RetrieveTokenExpiryUseCase retrieveTokenExpiryUseCase;

    private final ObjectProperty<Response<Integer>> retrieveStatusResponse = new SimpleObjectProperty<>();

    private StringProperty deviceUuid = new SimpleStringProperty();
    public StringProperty deviceUuidProperty() {
        return deviceUuid;
    }

    @Inject
    public MenuViewModel(SchedulersFacade schedulersFacade,
                         CloseIotivityUseCase closeIotivityUseCase,
                         GetDeviceIdUseCase getDeviceIdUseCase,
                         RetrieveStatusUseCase retrieveStatusUseCase,
                         CloudRegisterUseCase cloudRegisterUseCase,
                         CloudDeregisterUseCase cloudDeregisterUseCase,
                         CloudLoginUseCase cloudLoginUseCase,
                         CloudLogoutUseCase cloudLogoutUseCase,
                         CloudRefreshTokenUseCase refreshTokenUseCase,
                         RetrieveTokenExpiryUseCase retrieveTokenExpiryUseCase) {
        this.schedulersFacade = schedulersFacade;

        this.closeIotivityUseCase = closeIotivityUseCase;
        this.getDeviceIdUseCase = getDeviceIdUseCase;
        this.retrieveStatusUseCase = retrieveStatusUseCase;
        this.cloudRegisterUseCase = cloudRegisterUseCase;
        this.cloudDeregisterUseCase = cloudDeregisterUseCase;
        this.cloudLoginUseCase = cloudLoginUseCase;
        this.cloudLogoutUseCase = cloudLogoutUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.retrieveTokenExpiryUseCase = retrieveTokenExpiryUseCase;
    }

    public void initialize() {
        notificationCenter.subscribe(NotificationKey.OIC_STACK_INITIALIZED,
                (key, payload) -> deviceUuid.setValue(getDeviceIdUseCase.execute().blockingGet()));
        notificationCenter.subscribe(NotificationKey.OTGC_RESET,
                (key, payload) -> deviceUuid.setValue(getDeviceIdUseCase.execute().blockingGet()));
        notificationCenter.subscribe(NotificationKey.REFRESH_ID,
                (key, payload) -> deviceUuid.setValue(getDeviceIdUseCase.execute().blockingGet()));
    }

    public ObjectProperty<Response<Integer>> retrieveStatusResponseProperty() {
        return retrieveStatusResponse;
    }

    public void discover() {
        notificationCenter.publish(NotificationKey.SCAN_DEVICES);
    }

    public void retrieveCloudStatus() {
        disposable.add(retrieveStatusUseCase.execute()
            .subscribeOn(schedulersFacade.io())
            .observeOn(schedulersFacade.ui())
            .subscribe(
                    status -> retrieveStatusResponse.setValue(Response.success(status)),
                    throwable -> retrieveStatusResponse.setValue(Response.error(throwable))
            ));
    }

    public void closeAction() {
        closeIotivityUseCase.execute();
        System.exit(1);
    }

    public void cloudRegister() {
        disposable.add(cloudRegisterUseCase.execute()
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        status -> retrieveStatusResponse.setValue(Response.success(status)),
                        throwable -> retrieveStatusResponse.setValue(Response.error(throwable))
                ));
    }

    public void cloudDeregister() {
        disposable.add(cloudDeregisterUseCase.execute()
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        status -> retrieveStatusResponse.setValue(Response.success(status)),
                        throwable -> retrieveStatusResponse.setValue(Response.error(throwable))
                ));
    }

    public void cloudLogin() {
        disposable.add(cloudLoginUseCase.execute()
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        status -> retrieveStatusResponse.setValue(Response.success(status)),
                        throwable -> retrieveStatusResponse.setValue(Response.error(throwable))
                ));
    }

    public void cloudLogout() {
        disposable.add(cloudLogoutUseCase.execute()
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        status -> retrieveStatusResponse.setValue(Response.success(status)),
                        throwable -> retrieveStatusResponse.setValue(Response.error(throwable))
                ));
    }

    public void retrieveTokenExpiry() {
        disposable.add(retrieveTokenExpiryUseCase.execute()
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        () -> {},
                        throwable -> {}
                ));
    }

    public void refreshToken() {
        disposable.add(refreshTokenUseCase.execute()
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        status -> retrieveStatusResponse.setValue(Response.success(status)),
                        throwable -> retrieveStatusResponse.setValue(Response.error(throwable))
                ));
    }
}
