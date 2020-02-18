package uk.gov.hmcts.reform.mi.miextractionservice.component.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.mi.miextractionservice.component.CheckWhitelistComponent;

import java.util.List;

@Component
public class CheckWhitelistComponentImpl implements CheckWhitelistComponent {

    @Value("${container-whitelist}")
    private List<String> whiteList;

    /**
     * Check if there is a whitelist. If there is, check whether the passed in name is whitelisted.
     *
     * @param name of the container to be checked.
     * @return true is container is in whitelist or if whitelist is empty (nothing to be whitelisted).
     */
    @Override
    public boolean isContainerWhitelisted(String name) {
        return whiteList.isEmpty() || whiteList.contains(name);
    }
}
