package org.apereo.cas.metadata.rest;

import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is {@link ConfigurationMetadataSearchResultTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class ConfigurationMetadataSearchResultTests {
    @Test
    public void verifyAction() {
        final var repository = new CasConfigurationMetadataRepository();
        final var properties = repository.getRepository().getAllProperties();
        final var prop = properties.get("server.port");
        assertNotNull(prop);
        final var r = new ConfigurationMetadataSearchResult(prop, repository);
        assertEquals(prop.getDefaultValue(), r.getDefaultValue());
        assertEquals(prop.getId(), r.getId());
        assertEquals(prop.getName(), r.getName());
        assertEquals(prop.getType(), r.getType());
        assertEquals(prop.getShortDescription(), r.getShortDescription());
        assertEquals(prop.getDescription(), r.getDescription());
        assertEquals(prop.getDefaultValue(), r.getDefaultValue());
        assertNotNull(r.getGroup());

    }
}
