package org.apereo.cas.authentication.mfa;

import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * This is {@link DefaultMultifactorAuthenticationContextValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@DirtiesContext
public class DefaultMultifactorAuthenticationContextValidatorTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyContextFailsValidationWithNoProviders() {
        final AuthenticationContextValidator v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "OPEN", "trusted_authn", applicationContext);
        final var result = v.validate(
            MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
            "invalid-context", MultifactorAuthenticationTestUtils.getRegisteredService());
        assertFalse(result.getKey());
    }

    @Test
    public void verifyContextFailsValidationWithMissingProvider() {
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final AuthenticationContextValidator v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "OPEN", "trusted_authn", applicationContext);
        final var result = v.validate(
            MultifactorAuthenticationTestUtils.getAuthentication("casuser"),
            "invalid-context",
            MultifactorAuthenticationTestUtils.getRegisteredService());
        assertFalse(result.getKey());
    }

    @Test
    public void verifyContextPassesValidationWithProvider() {
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final AuthenticationContextValidator v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "OPEN", "trusted_authn", applicationContext);
        final var authentication = MultifactorAuthenticationTestUtils.getAuthentication(
            MultifactorAuthenticationTestUtils.getPrincipal("casuser"),
            CollectionUtils.wrap("authn_method", "mfa-dummy"));
        final var result = v.validate(authentication,
            "mfa-dummy", MultifactorAuthenticationTestUtils.getRegisteredService());
        assertTrue(result.getKey());
    }

    @Test
    public void verifyTrustedAuthnFoundInContext() {
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final AuthenticationContextValidator v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "OPEN", "trusted_authn", applicationContext);
        final var authentication = MultifactorAuthenticationTestUtils.getAuthentication(
            MultifactorAuthenticationTestUtils.getPrincipal("casuser"),
            CollectionUtils.wrap("authn_method", "mfa-other", "trusted_authn", "mfa-dummy"));
        final var result = v.validate(authentication,
            "mfa-dummy", MultifactorAuthenticationTestUtils.getRegisteredService());
        assertTrue(result.getKey());
    }

    @Test
    public void verifyBypassAuthnFoundInContext() {
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final AuthenticationContextValidator v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "OPEN", "trusted_authn", applicationContext);
        final var authentication = MultifactorAuthenticationTestUtils.getAuthentication(
            MultifactorAuthenticationTestUtils.getPrincipal("casuser"),
            CollectionUtils.wrap("authn_method", "mfa-other",
                MultifactorAuthenticationProviderBypass.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA, true,
                MultifactorAuthenticationProviderBypass.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA_PROVIDER, "mfa-dummy"));
        final var result = v.validate(authentication,
            "mfa-dummy", MultifactorAuthenticationTestUtils.getRegisteredService());
        assertTrue(result.getKey());
    }

    @Test
    public void verifyBypassAuthnNotFoundInContext() {
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final AuthenticationContextValidator v = new DefaultMultifactorAuthenticationContextValidator("authn_method",
            "OPEN", "trusted_authn", applicationContext);
        final var authentication = MultifactorAuthenticationTestUtils.getAuthentication(
            MultifactorAuthenticationTestUtils.getPrincipal("casuser"),
            CollectionUtils.wrap("authn_method", "mfa-other",
                MultifactorAuthenticationProviderBypass.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA, true,
                MultifactorAuthenticationProviderBypass.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA_PROVIDER, "mfa-other"));
        final var result = v.validate(authentication,
            "mfa-dummy", MultifactorAuthenticationTestUtils.getRegisteredService());
        assertFalse(result.getKey());
    }

}
