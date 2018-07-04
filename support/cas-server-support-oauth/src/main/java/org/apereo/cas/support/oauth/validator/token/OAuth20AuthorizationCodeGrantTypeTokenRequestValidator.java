package org.apereo.cas.support.oauth.validator.token;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.OAuthToken;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.HttpRequestUtils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

/**
 * This is {@link OAuth20AuthorizationCodeGrantTypeTokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class OAuth20AuthorizationCodeGrantTypeTokenRequestValidator extends BaseOAuth20TokenRequestValidator {
    private final ServicesManager servicesManager;
    private final TicketRegistry ticketRegistry;

    public OAuth20AuthorizationCodeGrantTypeTokenRequestValidator(final ServicesManager servicesManager,
                                                                  final TicketRegistry ticketRegistry,
                                                                  final AuditableExecution registeredServiceAccessStrategyEnforcer) {
        super(registeredServiceAccessStrategyEnforcer);
        this.servicesManager = servicesManager;
        this.ticketRegistry = ticketRegistry;
    }

    @Override
    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.AUTHORIZATION_CODE;
    }

    @Override
    protected boolean validateInternal(final J2EContext context, final String grantType,
                                       final ProfileManager manager, final UserProfile uProfile) {
        final var request = context.getRequest();
        final var clientId = uProfile.getId();
        final var redirectUri = request.getParameter(OAuth20Constants.REDIRECT_URI);
        final var clientRegisteredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);

        LOGGER.debug("Received grant type [{}] with client id [{}] and redirect URI [{}]", grantType, clientId, redirectUri);
        final var valid = HttpRequestUtils.doesParameterExist(request, OAuth20Constants.REDIRECT_URI)
            && HttpRequestUtils.doesParameterExist(request, OAuth20Constants.CODE)
            && OAuth20Utils.checkCallbackValid(clientRegisteredService, redirectUri);

        if (valid) {
            final var code = context.getRequestParameter(OAuth20Constants.CODE);
            final OAuthToken token = ticketRegistry.getTicket(code, OAuthCode.class);
            if (token == null || token.isExpired()) {
                LOGGER.warn("Request OAuth code [{}] is not found or has expired", code);
                return false;
            }
            
            final var id = token.getService().getId();
            final var codeRegisteredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, id);

            final var audit = AuditableContext.builder()
                .service(token.getService())
                .authentication(token.getAuthentication())
                .registeredService(codeRegisteredService)
                .retrievePrincipalAttributesFromReleasePolicy(Boolean.TRUE)
                .build();
            final var accessResult = this.registeredServiceAccessStrategyEnforcer.execute(audit);
            accessResult.throwExceptionIfNeeded();

            if (!clientRegisteredService.equals(codeRegisteredService)) {
                LOGGER.warn("The OAuth code [{}] issued to service [{}] does not match the registered service [{}] provided in the request given the redirect URI [{}]",
                    code, id, clientRegisteredService.getName(), redirectUri);
                return false;
            }
            return true;
        }
        LOGGER.warn("Access token request cannot be validated for grant type [{}} and client id [{}] given the redirect URI [{}]", grantType, clientId, redirectUri);
        return false;
    }
}
