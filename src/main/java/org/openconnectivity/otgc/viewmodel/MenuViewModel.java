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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.openconnectivity.otgc.utils.constant.NotificationKey;
import org.openconnectivity.otgc.domain.usecase.CloseIotivityUseCase;
import org.openconnectivity.otgc.domain.usecase.GetDeviceIdUseCase;

import javax.inject.Inject;

public class MenuViewModel implements ViewModel {

    @Inject
    private NotificationCenter notificationCenter;

    private final CloseIotivityUseCase closeIotivityUseCase;
    private final GetDeviceIdUseCase getDeviceIdUseCase;

    private StringProperty deviceUuid = new SimpleStringProperty();
    public StringProperty deviceUuidProperty() {
        return deviceUuid;
    }

    @Inject
    public MenuViewModel(CloseIotivityUseCase closeIotivityUseCase,
                         GetDeviceIdUseCase getDeviceIdUseCase) {
        this.closeIotivityUseCase = closeIotivityUseCase;
        this.getDeviceIdUseCase = getDeviceIdUseCase;
    }

    public void initialize() {
        notificationCenter.subscribe(NotificationKey.OIC_STACK_INITIALIZED,
                (key, payload) -> deviceUuid.setValue(getDeviceIdUseCase.execute().blockingGet()));
        notificationCenter.subscribe(NotificationKey.OTGC_RESET,
                (key, payload) -> deviceUuid.setValue(getDeviceIdUseCase.execute().blockingGet()));
        notificationCenter.subscribe(NotificationKey.REFRESH_ID,
                (key, payload) -> deviceUuid.setValue(getDeviceIdUseCase.execute().blockingGet()));
    }

    public void closeAction() {
        closeIotivityUseCase.execute();
    }
}
