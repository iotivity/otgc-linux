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

package org.openconnectivity.otgc;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.ViewTuple;
import de.saxsys.mvvmfx.guice.MvvmfxGuiceApplication;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.iotivity.OCMain;
import org.openconnectivity.otgc.data.persistence.DatabaseManager;
import org.openconnectivity.otgc.data.repository.SettingRepository;
import org.openconnectivity.otgc.utils.constant.NotificationKey;
import org.openconnectivity.otgc.utils.util.OpenScene;
import org.openconnectivity.otgc.view.main.MainView;
import org.openconnectivity.otgc.viewmodel.MainViewModel;


import javax.inject.Inject;
import java.util.*;

public class App extends MvvmfxGuiceApplication {

    private static final Logger LOG = Logger.getLogger(App.class);

    private static final String PREFERENCE_EULA_ACCEPTED = "eula_accepted";

    public static void main(String...args){
        Application.launch(args);
    }

    private ResourceBundle resourceBundle;

    @Inject
    private NotificationCenter notificationCenter;

    @Override
    public void startMvvmfx(Stage stage){
        LOG.debug("Starting the application");

        resourceBundle = ResourceBundle.getBundle("properties.Strings", new Locale("en", "EN"));
        MvvmFX.setGlobalResourceBundle(resourceBundle);

        stage.setTitle(resourceBundle.getString("window.title"));

        String eula = new SettingRepository().get(PREFERENCE_EULA_ACCEPTED, "false");
        if (!Boolean.valueOf(eula)) {
            // Load EULA dialog
            loadEula(stage);
        } else {
            // Load the login screen
            //ViewTuple<LoginView, LoginViewModel> loginTuple = FluentViewLoader.fxmlView(LoginView.class).load();
            // Load main screen
            loadMainScene(stage);
        }

        RxJavaPlugins.setErrorHandler(throwable -> {
            if (throwable instanceof UndeliverableException) {
                LOG.warn(throwable.getCause().getLocalizedMessage());
            } else {
                throw new RuntimeException(throwable);
            }

        });
    }

    private void loadEula(Stage stage) {
        WebView area = new WebView();
        area.getEngine().loadContent("<b>OCF ONBOARDING TOOL AND GENERIC CLIENT (OTGC)</b><br/>" +
                "<b>End User License Agreement</b><br />" +
                "<br />" +
                "This OCF Onboarding Tool and Generic Client End User License Agreement (this “<b>EULA</b>”) is a binding agreement between you and Open Connectivity Foundation, Inc. a Delaware nonprofit corporation (“<b>OCF</b>”) governing use of OCF’s proprietary software program known as the “OCF Onboarding Tool and Generic Client” (the “<b>Software</b>”).  In this EULA, unless the context clearly indicates otherwise, all references to “<b>you</b>” or “<b>your</b>,” regardless of capitalization, means you individually and any company or entity “<b>Licensee</b>”) you may be using the Software on behalf of.<br />" +
                "<br />" +
                "BY DOWNLOADING AND USING THE SOFTWARE, YOU ACCEPT AND AGREE TO THE TERMS OF THIS EULA ON YOUR OWN INDIVIDUAL BEHALF AND ON BEHALF OF ANY COMPANY OR ENTITY YOU ARE USING THE SOFTWARE ON BEHALF OF. IF YOU DO NOT OR CAN NOT AGREE TO THIS EULA ON YOUR OWN BEHALF AND ON BEHALF OF SUCH COMPANY OR ENTITY, YOU HAVE NO LICENSE TO, AND MUST NOT ACCESS OR USE, THE SOFTWARE.<br />" +
                "<br />" +
                "<ol type=\"1\">" +
                "<li><b>Definitions</b>. For purposes of this EULA, the following terms have the following meanings:</li>" +
                "<ol type=\"a\">" +
                "    <li><b>“Authorized User”</b> means those individual persons that Licensee has authorized to use the Software pursuant to the license granted under this EULA; provided however, in the event that the Licensee includes a company or entity, Licensee shall only authorize employees of Licensee or contractors that have agreed in writing or are otherwise legally bound to abide by the terms of this EULA.</li>" +
                "    <li><b>“CPL”</b> or <b>“Certified Product List”</b> means a list maintained by OCF of products that have passed the OCF Certification Testing and designated by OCF as certified products, such list provided in a machine readable format.</li>" +
                "    <li><b>“Documentation”</b> means any materials provided by OCF, in printed, electronic or other form, that describe the installation, operation, use or technical specifications of the Software.</li>" +
                "    <li>“Software”</b> means OCF’s proprietary software program known as the “OCF Onboarding Tool and Generic Client.”</li>" +
                "    <li>“OCF Certified Products”</b> shall mean the products that have passed the OCF Certification Testing and are designated by OCF as certified products.</li>" +
                "    <li>“Third Party”</b> means any individual or entity other than Licensee, OCF, or an Authorized User.</li>" +
                "</ol>" +
                "<li><b>License Grant</b>. Subject to your strict compliance with this EULA, OCF hereby grants you a non-exclusive, non-transferable, personal, non-sublicensable, limited license during the term of this EULA to use the Software solely in accordance with the Documentation and this EULA, solely for the purpose of managing, controlling and interfacing with OCF Certified Products within networks that you are authorized to use. This EULA and the foregoing license will terminate immediately, either with respect to just you individually or with respect to Licensee and all of its Authorized Users, as the case may be, on the earlier to occur of:</li>" +
                "<ol type=\"a\">" +
                "    <li>your ceasing to be an Authorized User for any reason;</li>" +
                "    <li>notice of termination by OCF in the event you breach the terms of this EULA and either (i) such breach, by its nature, is not curable or (ii) you have failed to cure such breach within thirty (30) days after OCF provided you with notice that such breach has occurred;</li>" +
                "    <li>upon notice if OCF ceases to provide or maintain the Software; or</li>" +
                "    <li>notice of termination by Licensee for any reason or no reason.  Upon termination of this EULA, all licenses granted hereunder shall terminate, and you shall promptly stop using the Software and the Documentation. Any terms that are intended to survive termination of this EULA, as indicated by their nature, express terms, or context, shall survive such termination, including without limitation <b>Section 10</b>.</li>" +
                "</ol>" +
                "<li><b>Scope of License</b>. The license granted herein includes the limited right and license to:</li>" +
                "<ol type=\"a\">" +
                "    <li>Use, access, and install the Software in accordance with this EULA and the Documentation, solely for the purpose of managing, controlling and interfacing with OCF Certified Products within networks that you are authorized to use.</li>" +
                "    <li>Download, print, or otherwise view and display any Documentation and use such Documentation, solely in support of your licensed use of the Software in accordance with this EULA.</li>" +
                "</ol>" +
                "<li><b>Copies</b>. All copies of the Documentation and Software made by you:</li>" +
                "<ol type=\"a\">" +
                "    <li>will be the exclusive property of OCF;</li>" +
                "    <li>will be subject to the terms and conditions of this EULA;</li>" +
                "    <li>must include all trademark, copyright, patent and other intellectual property rights notices contained in the original; and</li>" +
                "    <li>must include or be accompanied by a copy of this EULA.</li>" +
                "</ol>" +
                "<li><b>Use Restrictions</b>. You shall not, directly or indirectly:</li>" +
                "<ol type=\"a\">" +
                "    <li>use (including make any copies of) the Software or Documentation beyond the scope of the license granted hereunder;</li>" +
                "    <li>distribute the Software without a copy of this EULA;</li>" +
                "    <li>remove, delete, alter or obscure any trademarks or any copyright, trademark, patent or other intellectual property or proprietary rights notices from the Software or Documentation, including any copy thereof;</li>" +
                "    <li>use the Software in, or in association with, the design, construction, maintenance or operation of any hazardous environments or systems, including:</li>" +
                "    <ol type=\"i\">" +
                "        <li>power generation systems;</li>" +
                "        <li>aircraft navigation or communication systems, air traffic control systems or any other transport management systems;</li>" +
                "        <li>safety-critical applications, including medical or life-support systems, vehicle operation applications or any police, fire or other safety response systems; and</li>" +
                "        <li>military or aerospace applications, weapons systems or environments;</li>" +
                "    </ol>" +
                "    <li>use the Software in violation of any federal, state or local law, regulation or rule; or</li>" +
                "    <li>use the Software to attempt to, or to in fact, circumvent any OCF security system or otherwise breach any OCF-established security protocols.</li>" +
                "</ol>" +
                "<li><b>Modifications; Support</b>. You understand and agree that OCF may make modifications and updates to the Software and Documentation from time to time in its sole discretion, including but not limited to bug fixes and security patches.  However, OCF is not required to make such modifications or to otherwise provide support for the Software.</li>" +
                "<li><b>Compliance Measures</b>. The Software may contain technological copy protection or other security features designed to prevent unauthorized use of the Software, including features to protect against use of the Software that is beyond the scope of the license granted in this EULA or that is prohibited by the terms of this EULA.  You shall not, and shall not attempt to, remove, disable, circumvent or otherwise create or implement any workaround to any such copy protection or security features.</li>" +
                "<li><b>Intellectual Property Rights</b>. You acknowledge that the Software is provided under license, and not sold, to you. You do not acquire any ownership interest in the Software under this EULA, or any other rights to the Software other than to use the Software in accordance with the license granted under this EULA, subject to all terms, conditions and restrictions contained therein and herein. OCF reserves and shall retain its entire right, title and interest in and to the Software and all intellectual property rights arising out of or relating to the Software, subject to the licenses expressly granted in this EULA. You shall use commercially reasonable efforts to safeguard all Software (including all copies thereof) from infringement, misappropriation, theft, misuse or unauthorized access.</li>" +
                "<li><b>Export Regulation</b>. The Software may be subject to US export control laws, including the US Export Administration Act and its associated regulations. You shall not, directly or indirectly, export, re-export or release the Software to, or make the Software accessible from, any jurisdiction or country to which export, re-export or release is prohibited by law, rule or regulation. You shall comply with all applicable federal laws, regulations and rules, and complete all required undertakings (including obtaining any necessary export license or other governmental approval), prior to exporting, re-exporting, releasing or otherwise making the Software available outside the United States.</li>" +
                "<li><b>Disclaimer of Warranties and Liability For Use of The Software and CPL</b>. THE SOFTWARE AND CPL ARE PROVIDED “AS IS” WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.  THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE SOFTWARE IS WITH YOU.  SHOULD THE SOFTWARE PROVE DEFECTIVE, YOU ASSUME THE ENTIRE COST OF ALL NECESSARY REPAIR, SERVICING OR CORRECTION.  IN NO EVENT WILL OCF OR ITS AFFILIATES, OR ANY OF ITS OR THEIR RESPECTIVE AGENTS, DIRECTORS, OFFICERS, LICENSORS OR SERVICE PROVIDERS, BE LIABLE FOR ANY DAMAGES WHATSOEVER, INCLUDING WITHOUT LIMITATION, LOSS OF BUSINESS PROFITS, LOSS OF BUSINESS INFORMATION, LOSS OF BUSINESS INTERRUPTION OR OTHER COMPENSATORY, INCIDENTAL OR CONSEQUENTIAL DAMAGES, TO YOU FOR ANY USE, INTERRUPTION, DELAY OR INABILITY TO USE THE SOFTWARE OR CPL. YOU ARE PROVIDED THE SOFTWARE PURSUANT TO THE TERMS OF THIS EULA, SOLELY FOR THE USE IN CONNECTION WITH TERMS OF THE LICENSE GRANT CONTAINED IN SECTION 2 HEREOF. TO THE FULLEST EXTENT PERMITTED BY APPLICABLE LAW, YOU HEREBY RELEASE OCF, ITS OFFICERS, DIRECTORS AND AGENTS FROM ANY AND ALL LIABILITY ARISING FROM OR RELATED TO ALL CLAIMS CONCERNING THE SOFTWARE OR ITS USE.<br />" +
                "Without limiting the generality of the foregoing warranty disclaimers and limitations of liability, if you access the CPL, OCF does not and cannot guarantee that the CPL will be available at all times or that your ability to access the CPL will be uninterrupted, and neither OCF nor any third party acting on OCF’s behalf to provide such CPL, shall have any liability to you or any third party arising from (a) your inability to access the CPL for any reason, including without limitation due to scheduled or emergency maintenance, or (b) any delay, whether or not caused by OCF or its agents, in updating the CPL with current information, or (c) any errors or inaccuracies in the CPL for any reason, or for reasons related to the functioning of the OCF Certificate Management System, actions by unauthorized third parties, or otherwise. Furthermore, if you access the CPL or distribute software (including the Software) or products that access the CPL, you agree to indemnify, defend, and hold OCF and its affiliates, and any of its or their respective agents, directors, officers, licensors, and service providers from and against any and all claims, lawsuits, losses, damages, penalties, fines, liabilities, costs, and expenses (including without limitation attorneys’ fees) incurred by any of them arising from or related to use of the CPL by you, by any recipient of software that you distribute that is programmed to access the CPL, or by any recipient of any device that you manufacture or distribute that comes preinstalled with firmware that is programmed to access the CPL.</li>" +
                "<li><b>Governing Law</b>. This EULA shall be governed by and construed in accordance with the internal laws of the State of Delaware without giving effect to any choice or conflict of law provision or rule (whether of the State of Delaware or any other jurisdiction) that would cause the application of laws of any jurisdiction other than those of the State of Delaware.<br />" +
                "BY CLICKING THE BUTTON INDICATING YOUR ACCEPTANCE OR INSTALLING THE SOFTWARE, YOU ACKNOWLEDGE THAT YOU HAVE READ THIS AGREEMENT, UNDERSTOOD IT, AND AGREE TO BE BOUND BY ITS TERMS AND CONDITIONS.  YOU ALSO AGREE THAT THIS AGREEMENT IS THE COMPLETE AND EXCLUSIVE STATEMENT OF AGREEMENT BETWEEN YOU AND OCF CONCERNING THE SUBJECT MATTER HEREOF AND SUPERSEDES ALL PROPOSALS OR PRIOR AGREEMENTS, VERBAL OR WRITTEN, AND ANY OTHER COMMUNICATIONS BETWEEN YOU AND OCF RELATING TO THE SUBJECT MATTER HEREOF.  NO AMENDMENT TO THIS AGREEMENT SHALL BE EFFECTIVE UNLESS SIGNED BY A DULY AUTHORIZED REPRESENTATIVE OF OCF.</li>" +
                "</ol>");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("End User License Agreement");
        alert.getDialogPane().setContent(area);
        alert.setResizable(true);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            new SettingRepository().set(PREFERENCE_EULA_ACCEPTED, "true").blockingAwait();
            loadMainScene(stage);
        } else {
            Platform.exit();
        }
    }

    private void loadMainScene(Stage stage) {
        ViewTuple<MainView, MainViewModel> mainTuple = FluentViewLoader.fxmlView(MainView.class).load();
        List<String> styles = new ArrayList<>();
        // styles.add("styles/login.css");
        try {
            OpenScene.start(stage, mainTuple, styles);
            stage.setOnCloseRequest(event -> closeApp());
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    private void closeApp() {
        LOG.debug("Calling OCMain.mainShutdown()");
        notificationCenter.publish(NotificationKey.SHUTDOWN_OIC_STACK);

        notificationCenter.subscribe(NotificationKey.CANCEL_ALL_OBSERVERS,
                (key, payload) -> {
                    OCMain.mainShutdown();

                    DatabaseManager.closeEntityManager();
                    DatabaseManager.closeEntityManagerFactory();
                });
    }

}