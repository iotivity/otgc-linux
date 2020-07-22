package org.openconnectivity.otgc.domain.usecase.cloud;

import io.reactivex.Completable;
import org.openconnectivity.otgc.data.repository.CloudRepository;

import javax.inject.Inject;

public class ProvisionCloudConfUseCase {

    private final CloudRepository cloudRepository;

    @Inject
    public ProvisionCloudConfUseCase(CloudRepository cloudRepository) {
        this.cloudRepository = cloudRepository;
    }

    public Completable execute(String authProvider, String cloudUrl, String accessToken, String cloudUuid) {
        return cloudRepository.provisionCloudConfiguration(authProvider, cloudUrl, accessToken, cloudUuid);
    }
}
