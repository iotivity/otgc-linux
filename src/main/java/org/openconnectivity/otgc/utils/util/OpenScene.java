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

package org.openconnectivity.otgc.utils.util;

import de.saxsys.mvvmfx.ViewTuple;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

public class OpenScene {
    public static void start(Stage stage, ViewTuple<?, ?> tuple, List<String> styles) throws Exception {
        Scene root = new Scene(tuple.getView(), stage.getWidth(), stage.getHeight());

        // Add global style
        styles.add("styles/style.css");

        if (!styles.isEmpty()) {
            styles.forEach(style -> root.getStylesheets().add(style));
        }

        stage.setScene(root);
        stage.show();
    }
}
