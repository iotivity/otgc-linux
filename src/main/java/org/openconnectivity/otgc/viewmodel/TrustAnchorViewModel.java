/*
 *  Copyright 2018 DEKRA Testing and Certification, S.A.U. All Rights Reserved.
 *
 *  *****************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openconnectivity.otgc.viewmodel;

import de.saxsys.mvvmfx.ViewModel;
import io.reactivex.disposables.CompositeDisposable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import org.openconnectivity.otgc.domain.model.resource.secure.cred.OcCredential;
import org.openconnectivity.otgc.domain.usecase.trustanchor.GetTrustAnchorUseCase;
import org.openconnectivity.otgc.domain.usecase.trustanchor.RemoveTrustAnchorByCredidUseCase;
import org.openconnectivity.otgc.domain.usecase.trustanchor.StoreTrustAnchorUseCase;
import org.openconnectivity.otgc.utils.rx.SchedulersFacade;
import org.openconnectivity.otgc.utils.viewmodel.Response;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TrustAnchorViewModel implements ViewModel {

    private CompositeDisposable disposable = new CompositeDisposable();

    private final SchedulersFacade schedulersFacade;

    // Use cases
    private final StoreTrustAnchorUseCase storeTrustAnchorUseCase;
    private final GetTrustAnchorUseCase getTrustAnchorUseCase;
    private final RemoveTrustAnchorByCredidUseCase remoteRemoveTrustAnchorByCredidUseCase;

    // Observable values
    private ListProperty<OcCredential> trustAnchorList = new SimpleListProperty<>();
    public ListProperty<OcCredential> trustAnchorListProperty() {
        return trustAnchorList;
    }

    @Inject
    public TrustAnchorViewModel(SchedulersFacade schedulersFacade,
                                StoreTrustAnchorUseCase storeTrustAnchorUseCase,
                                GetTrustAnchorUseCase getTrustAnchorUseCase,
                                RemoveTrustAnchorByCredidUseCase remoteRemoveTrustAnchorByCredidUseCase) {
        this.schedulersFacade = schedulersFacade;
        this.storeTrustAnchorUseCase = storeTrustAnchorUseCase;
        this.getTrustAnchorUseCase = getTrustAnchorUseCase;
        this.remoteRemoveTrustAnchorByCredidUseCase = remoteRemoveTrustAnchorByCredidUseCase;
    }

    public void retrieveTrustAnchors() {
        disposable.add(getTrustAnchorUseCase.execute()
            .subscribeOn(schedulersFacade.io())
            .observeOn(schedulersFacade.ui())
            .subscribe(
                    trustAnchors -> this.trustAnchorList.set(FXCollections.observableArrayList(trustAnchors)),
                    error -> {}
            )
        );
    }

    public void addTrustAnchor(File file) {
        disposable.add(storeTrustAnchorUseCase.execute(file.getPath())
            .subscribeOn(schedulersFacade.io())
            .observeOn(schedulersFacade.ui())
            .subscribe(
                    () -> retrieveTrustAnchors(),
                    throwable -> {}
            ));
    }

    public void removeTrustAnchorByCredid(long credid) {
        disposable.add(remoteRemoveTrustAnchorByCredidUseCase.execute(credid)
            .subscribeOn(schedulersFacade.io())
            .observeOn(schedulersFacade.ui())
            .subscribe(
                    () -> retrieveTrustAnchors(),
                    throwable -> {}
            ));
    }
}