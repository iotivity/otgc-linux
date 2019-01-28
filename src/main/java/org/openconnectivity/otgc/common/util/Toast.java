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

package org.openconnectivity.otgc.common.util;

import com.jfoenix.controls.JFXSnackbar;
import io.reactivex.annotations.NonNull;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Toast {

    private static Pane rootPane;
    private static JFXSnackbar jfxSnackbar = new JFXSnackbar();

    public Toast(@NonNull Stage stage) {
        if (rootPane != null) {
            this.jfxSnackbar.unregisterSnackbarContainer(rootPane);
        }
        this.rootPane = (Pane) stage.getScene().getRoot();
        this.jfxSnackbar.registerSnackbarContainer(rootPane);
    }

    public void show (String message) {
        JFXSnackbar.SnackbarEvent snackbarEvent = new JFXSnackbar.SnackbarEvent(message);
        this.jfxSnackbar.enqueue(snackbarEvent);
    }

    public static void show(Stage stage, String message) {
        Toast toast = new Toast(stage);
        toast.show(message);
    }
}
