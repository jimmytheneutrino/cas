package org.apereo.cas.authentication;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Slf4j
public class UsernamePasswordCredentialTests {

    @Test
    public void verifySetGetUsername() {
        final var c = new UsernamePasswordCredential();
        final var userName = "test";

        c.setUsername(userName);

        assertEquals(userName, c.getUsername());
    }

    @Test
    public void verifySetGetPassword() {
        final var c = new UsernamePasswordCredential();
        final var password = "test";

        c.setPassword(password);

        assertEquals(password, c.getPassword());
    }

    @Test
    public void verifyEquals() {
        assertNotEquals(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(), null);
        assertFalse(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword().equals(
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        assertTrue(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword().equals(
                CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword()));
    }
}
