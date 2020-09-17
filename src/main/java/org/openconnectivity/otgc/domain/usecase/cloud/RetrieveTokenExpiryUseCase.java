package org.openconnectivity.otgc.domain.usecase.cloud;

import io.reactivex.Completable;
import org.openconnectivity.otgc.data.repository.CloudRepository;

import javax.inject.Inject;


public class RetrieveTokenExpiryUseCase {
    private final CloudRepository cloudRepository;

    @Inject
    public RetrieveTokenExpiryUseCase(CloudRepository cloudRepository) {
        this.cloudRepository = cloudRepository;
    }

    public Completable execute() {
        return cloudRepository.retrieveTokenExpiry();
    }
}
