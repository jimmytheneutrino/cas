package org.apereo.cas.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link RestfulServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class RestfulServiceRegistry extends AbstractServiceRegistry {
    private final transient RestTemplate restTemplate;
    private final String url;
    private final MultiValueMap<String, String> headers;

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        final var responseEntity = restTemplate.exchange(this.url, HttpMethod.POST,
            new HttpEntity<>(registeredService, this.headers), RegisteredService.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity.getBody();
        }
        return null;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        final var responseEntity = restTemplate.exchange(this.url, HttpMethod.DELETE,
            new HttpEntity<>(registeredService, this.headers), Integer.class);
        return responseEntity.getStatusCode().is2xxSuccessful();
    }

    @Override
    public List<RegisteredService> load() {
        final var responseEntity = restTemplate.exchange(this.url, HttpMethod.GET,
            new HttpEntity<>(this.headers), RegisteredService[].class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            final var results = responseEntity.getBody();
            return Stream.of(results).collect(Collectors.toList());
        }
        return new ArrayList<>(0);
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        final var url = StringUtils.appendIfMissing(this.url, "/").concat(String.valueOf(id));
        final var responseEntity = restTemplate.exchange(url, HttpMethod.GET,
            new HttpEntity<>(id, this.headers), RegisteredService.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity.getBody();
        }
        return null;
    }

    @Override
    public RegisteredService findServiceById(final String id) {
        final var url = StringUtils.appendIfMissing(this.url, "/").concat(String.valueOf(id));
        final var responseEntity = restTemplate.exchange(url, HttpMethod.GET,
            new HttpEntity<>(id, this.headers), RegisteredService.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity.getBody();
        }
        return null;
    }

    @Override
    public long size() {
        return load().size();
    }
}
