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

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DialogHelper {

    public static Stage showDialog(Parent view, Stage parentStage, String title, String... sceneStyleSheets) {
        final Stage dialogStage = new Stage(StageStyle.UTILITY);
        dialogStage.initOwner(parentStage);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle(title);
        if (dialogStage.getScene() == null) {
            // ... we create a new scene and register it in the stage.
            Scene dialogScene = new Scene(view);
            dialogScene.getStylesheets().addAll(sceneStyleSheets);
            dialogStage.setScene(dialogScene);

            dialogStage.sizeToScene();
            dialogStage.show();
            return dialogStage;
        }
        return null;
    }
}
