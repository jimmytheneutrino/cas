package org.apereo.cas.authentication;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apereo.cas.authentication.principal.Principal;

import java.util.Collection;

/**
 * This is {@link SurrogatePrincipalElectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class SurrogatePrincipalElectionStrategy extends DefaultPrincipalElectionStrategy {
    private static final long serialVersionUID = -3112906686072339162L;

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    @Override
    protected Principal getPrincipalFromAuthentication(final Collection<Authentication> authentications) {
        final var result = authentications
            .stream()
            .map(Authentication::getPrincipal)
            .filter(SurrogatePrincipal.class::isInstance)
            .map(SurrogatePrincipal.class::cast)
            .findFirst();
        if (result.isPresent()) {
            return result.get().getSurrogate();
        }
        return super.getPrincipalFromAuthentication(authentications);
    }
}
