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

package org.openconnectivity.otgc.menu;

import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.openconnectivity.otgc.common.constant.NotificationConstant;
import org.openconnectivity.otgc.common.util.IotivityUtils;

import javax.inject.Inject;

public class MenuViewModel implements ViewModel {

    @Inject
    private NotificationCenter notificationCenter;

    private final CloseIotivityUseCase closeIotivityUseCase;

    private StringProperty deviceUuid = new SimpleStringProperty();
    public StringProperty deviceUuidProperty() {
        return deviceUuid;
    }

    @Inject
    public MenuViewModel(CloseIotivityUseCase closeIotivityUseCase) {
        this.closeIotivityUseCase = closeIotivityUseCase;
    }

    public void initialize() {
        notificationCenter.subscribe(NotificationConstant.OIC_STACK_INITIALIZED, (key, payload) -> {
            deviceUuid.setValue(IotivityUtils.getStringUuid());
        });
    }

    public void closeAction() {
        closeIotivityUseCase.execute();
        Platform.exit();
    }
}
