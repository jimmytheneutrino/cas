package org.apereo.cas.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.cassandra.authentication.CassandraAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

/**
 * This is {@link CassandraAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class CassandraAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private final CassandraAuthenticationProperties cassandraAuthenticationProperties;
    private final CassandraRepository cassandraRepository;

    public CassandraAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                          final PrincipalFactory principalFactory, final Integer order,
                                          final CassandraAuthenticationProperties cassandraAuthenticationProperties,
                                          final CassandraRepository cassandraRepository) {
        super(name, servicesManager, principalFactory, order);
        this.cassandraAuthenticationProperties = cassandraAuthenticationProperties;
        this.cassandraRepository = cassandraRepository;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword) throws GeneralSecurityException {
        final var username = credential.getUsername();
        final var password = credential.getPassword();

        final var attributes = this.cassandraRepository.getUser(username);

        if (attributes == null || attributes.isEmpty()
                || !attributes.containsKey(cassandraAuthenticationProperties.getUsernameAttribute())
                || !attributes.containsKey(cassandraAuthenticationProperties.getPasswordAttribute())) {
            LOGGER.warn("Unable to find account [{}]: The account does not exist or it's missing username/password attributes", username);
            throw new AccountNotFoundException();
        }

        LOGGER.debug("Located account attributes [{}] for [{}]", attributes.keySet(), username);
        final var userPassword = attributes.get(cassandraAuthenticationProperties.getPasswordAttribute()).toString();
        if (!password.equals(userPassword)) {
            LOGGER.warn("Account password on record for [{}] does not match the given password", username);
            throw new FailedLoginException();
        }
        return createHandlerResult(credential,
                this.principalFactory.createPrincipal(username, attributes), new ArrayList<>());
    }
}
