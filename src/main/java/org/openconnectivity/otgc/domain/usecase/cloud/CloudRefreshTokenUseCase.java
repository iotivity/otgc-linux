package org.openconnectivity.otgc.domain.usecase.cloud;

import io.reactivex.Single;
import org.openconnectivity.otgc.data.repository.CloudRepository;

import javax.inject.Inject;


public class CloudRefreshTokenUseCase {
    private final CloudRepository cloudRepository;

    @Inject
    public CloudRefreshTokenUseCase(CloudRepository cloudRepository) {
        this.cloudRepository = cloudRepository;
    }

    public Single<Integer> execute() {
        return cloudRepository.refreshToken()
                .andThen(cloudRepository.retrieveState());
    }
}
