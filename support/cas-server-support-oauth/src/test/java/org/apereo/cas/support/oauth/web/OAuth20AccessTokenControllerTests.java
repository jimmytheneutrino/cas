package org.apereo.cas.support.oauth.web;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AccessTokenEndpointController;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.code.DefaultOAuthCodeFactory;
import org.apereo.cas.ticket.refreshtoken.DefaultRefreshTokenFactory;
import org.apereo.cas.ticket.support.AlwaysExpiresExpirationPolicy;
import org.junit.Test;
import org.junit.Before;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This class tests the {@link OAuth20AccessTokenEndpointController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@Slf4j
public class OAuth20AccessTokenControllerTests extends AbstractOAuth20Tests {

    @Before
    public void initialize() {
        clearAllServices();
    }

    @Test
    public void verifyClientNoClientId() throws Exception {
        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        final var principal = createPrincipal();
        final var service = addRegisteredService();
        final var code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        final var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoRedirectUri() throws Exception {
        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        final var principal = createPrincipal();
        final var service = addRegisteredService();
        final var code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        final var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoAuthorizationCode() throws Exception {
        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        final var principal = createPrincipal();
        final var service = addRegisteredService();
        final var code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        final var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientBadAuthorizationCode() throws Exception {
        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, "badValue");
        final var principal = createPrincipal();
        final var service = addRegisteredService();
        final var code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        final var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoClientSecret() throws Exception {
        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        final var principal = createPrincipal();
        final var service = addRegisteredService();
        final var code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        final var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoCode() throws Exception {
        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        final var principal = createPrincipal();
        final var service = addRegisteredService();

        addCode(principal, service);

        final var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoCasService() throws Exception {
        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        final var principal = createPrincipal();
        final var registeredService = getRegisteredService(REDIRECT_URI, CLIENT_SECRET);
        final var code = addCode(principal, registeredService);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        final var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientRedirectUriDoesNotStartWithServiceId() throws Exception {
        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, OTHER_REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        final var principal = createPrincipal();
        final var service = addRegisteredService();
        final var code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        final var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientWrongSecret() throws Exception {
        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, WRONG_CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        final var principal = createPrincipal();
        final var service = addRegisteredService();
        final var code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        final var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientExpiredCode() throws Exception {
        final RegisteredService registeredService = getRegisteredService(REDIRECT_URI, CLIENT_SECRET);
        servicesManager.save(registeredService);

        final Map<String, Object> map = new HashMap<>();
        map.put(NAME, VALUE);
        final var list = Arrays.asList(VALUE, VALUE);
        map.put(NAME2, list);

        final var principal = CoreAuthenticationTestUtils.getPrincipal(ID, map);
        final var authentication = getAuthentication(principal);
        final var expiringOAuthCodeFactory = new DefaultOAuthCodeFactory(new AlwaysExpiresExpirationPolicy());
        final var factory = new WebApplicationServiceFactory();
        final Service service = factory.createService(registeredService.getServiceId());
        final var code = expiringOAuthCodeFactory.create(service, authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>());
        this.ticketRegistry.addTicket(code);

        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        servicesManager.save(getRegisteredService(REDIRECT_URI, CLIENT_SECRET));

        final var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientAuthByParameter() throws Exception {
        final var service = addRegisteredService();
        internalVerifyClientOK(service, false, false);
    }

    @Test
    public void verifyClientAuthByHeader() throws Exception {
        final var service = addRegisteredService();
        internalVerifyClientOK(service, false, false);
    }

    @Test
    public void verifyClientAuthByParameterWithRefreshToken() throws Exception {
        final var service = addRegisteredService();
        service.setGenerateRefreshToken(true);
        internalVerifyClientOK(service, true, false);
    }

    @Test
    public void verifyClientAuthByHeaderWithRefreshToken() throws Exception {
        final var service = addRegisteredService();
        service.setGenerateRefreshToken(true);
        internalVerifyClientOK(service, true, false);
    }

    @Test
    public void verifyClientAuthJsonByParameter() throws Exception {
        final var service = addRegisteredService();
        service.setJsonFormat(true);
        internalVerifyClientOK(service, false, true);
    }

    @Test
    public void verifyClientAuthJsonByHeader() throws Exception {
        final var service = addRegisteredService();
        service.setJsonFormat(true);
        internalVerifyClientOK(service, false, true);
    }

    @Test
    public void verifyClientAuthJsonByParameterWithRefreshToken() throws Exception {
        final var service = addRegisteredService();
        service.setGenerateRefreshToken(true);
        service.setJsonFormat(true);
        internalVerifyClientOK(service, true, true);
    }

    @Test
    public void verifyClientAuthJsonByHeaderWithRefreshToken() throws Exception {
        final var service = addRegisteredService();
        service.setGenerateRefreshToken(true);
        service.setJsonFormat(true);
        internalVerifyClientOK(service, true, true);
    }

    @Test
    public void ensureOnlyRefreshTokenIsAcceptedForRefreshGrant() throws Exception {
        addRegisteredService(true);
        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        final var mockSession = new MockHttpSession();
        mockRequest.setSession(mockSession);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);

        var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        var response = mockResponse.getContentAsString();

        final var refreshToken = Arrays.stream(response.split("&"))
            .filter(f -> f.startsWith(OAuth20Constants.REFRESH_TOKEN))
            .map(f -> StringUtils.remove(f, OAuth20Constants.REFRESH_TOKEN + '='))
            .findFirst()
            .get();
        final var accessToken = Arrays.stream(response.split("&"))
            .filter(f -> f.startsWith(OAuth20Constants.ACCESS_TOKEN))
            .map(f -> StringUtils.remove(f, OAuth20Constants.ACCESS_TOKEN + '='))
            .findFirst()
            .get();

        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, accessToken);

        mockResponse = new MockHttpServletResponse();
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());

        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken);
        mockResponse = new MockHttpServletResponse();
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        response = mockResponse.getContentAsString();
        assertTrue(response.contains(OAuth20Constants.ACCESS_TOKEN));
    }

    @Test
    public void verifyUserNoClientId() throws Exception {
        addRegisteredService();

        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        final var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyUserNoCasService() throws Exception {
        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        final var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyUserBadAuthorizationCode() throws Exception {
        addRegisteredService();

        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        final var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyUserBadCredentials() throws Exception {
        addRegisteredService();

        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, "badPassword");
        final var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyUserAuth() throws Exception {
        addRegisteredService();
        internalVerifyUserAuth(false, false);
    }

    @Test
    public void verifyUserAuthWithRefreshToken() throws Exception {
        final var registeredService = addRegisteredService();
        registeredService.setGenerateRefreshToken(true);
        internalVerifyUserAuth(true, false);
    }

    @Test
    public void verifyJsonUserAuth() throws Exception {
        final var registeredService = addRegisteredService();
        registeredService.setJsonFormat(true);

        internalVerifyUserAuth(false, true);
    }

    @Test
    public void verifyJsonUserAuthWithRefreshToken() throws Exception {
        final var registeredService = addRegisteredService();
        registeredService.setGenerateRefreshToken(true);
        registeredService.setJsonFormat(true);
        internalVerifyUserAuth(true, true);
    }

    private void internalVerifyUserAuth(final boolean refreshToken, final boolean json) throws Exception {
        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        mockRequest.addHeader(CasProtocolConstants.PARAMETER_SERVICE, REDIRECT_URI);
        final var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        final var body = mockResponse.getContentAsString();

        final String accessTokenId;
        if (json) {
            assertEquals("application/json", mockResponse.getContentType());
            assertTrue(body.contains('"' + OAuth20Constants.ACCESS_TOKEN + "\":\"AT-"));
            if (refreshToken) {
                assertTrue(body.contains('"' + OAuth20Constants.REFRESH_TOKEN + "\":\"RT-"));
            }
            assertTrue(body.contains('"' + OAuth20Constants.EXPIRES_IN + "\":"));
            accessTokenId = StringUtils.substringBetween(body, OAuth20Constants.ACCESS_TOKEN + "\":\"", "\",\"");
        } else {
            assertEquals("text/plain", mockResponse.getContentType());
            assertTrue(body.contains(OAuth20Constants.ACCESS_TOKEN + '='));
            if (refreshToken) {
                assertTrue(body.contains(OAuth20Constants.REFRESH_TOKEN + '='));
            }
            assertTrue(body.contains(OAuth20Constants.EXPIRES_IN + '='));
            accessTokenId = StringUtils.substringBetween(body, OAuth20Constants.ACCESS_TOKEN + '=', "&");
        }

        final var accessToken = this.ticketRegistry.getTicket(accessTokenId, AccessToken.class);
        assertEquals(GOOD_USERNAME, accessToken.getAuthentication().getPrincipal().getId());

        final var timeLeft = getTimeLeft(body, refreshToken, json);
        assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);
    }

    @Test
    public void verifyRefreshTokenExpiredToken() throws Exception {
        final var principal = createPrincipal();
        final RegisteredService registeredService = addRegisteredService();
        final var authentication = getAuthentication(principal);
        final var factory = new WebApplicationServiceFactory();
        final Service service = factory.createService(registeredService.getServiceId());
        final var expiringRefreshTokenFactory = new DefaultRefreshTokenFactory(new AlwaysExpiresExpirationPolicy());
        final var refreshToken = expiringRefreshTokenFactory.create(service, authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>());
        this.ticketRegistry.addTicket(refreshToken);

        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        final var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyRefreshTokenBadCredentials() throws Exception {
        final var principal = createPrincipal();
        final var service = addRegisteredService();
        final var refreshToken = addRefreshToken(principal, service);

        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, WRONG_CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        final var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyRefreshTokenMissingToken() throws Exception {
        addRegisteredService();

        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        final var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyRefreshTokenOKWithExpiredTicketGrantingTicket() throws Exception {
        final var principal = createPrincipal();
        final var service = addRegisteredService();
        final var refreshToken = addRefreshToken(principal, service);

        refreshToken.getTicketGrantingTicket().markTicketExpired();

        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        final var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertEquals("text/plain", mockResponse.getContentType());
        final var body = mockResponse.getContentAsString();

        assertTrue(body.contains(OAuth20Constants.ACCESS_TOKEN + '='));
        assertFalse(body.contains(OAuth20Constants.REFRESH_TOKEN + '='));
        assertTrue(body.contains(OAuth20Constants.EXPIRES_IN + '='));

        final var accessTokenId = StringUtils.substringBetween(body, OAuth20Constants.ACCESS_TOKEN + '=', "&");

        final var accessToken = this.ticketRegistry.getTicket(accessTokenId, AccessToken.class);
        assertEquals(principal, accessToken.getAuthentication().getPrincipal());

        final var timeLeft = getTimeLeft(body, false, false);
        assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);
    }

    @Test
    public void verifyRefreshTokenOK() throws Exception {
        final var service = addRegisteredService();
        internalVerifyRefreshTokenOk(service, false);
    }

    @Test
    public void verifyRefreshTokenOKWithRefreshToken() throws Exception {
        final var service = addRegisteredService();
        service.setGenerateRefreshToken(true);
        internalVerifyRefreshTokenOk(service, false);
    }

    @Test
    public void verifyJsonRefreshTokenOK() throws Exception {
        final var service = addRegisteredService();
        service.setJsonFormat(true);
        internalVerifyRefreshTokenOk(service, true);
    }

    @Test
    public void verifyJsonRefreshTokenOKWithRefreshToken() throws Exception {
        final var service = addRegisteredService();
        service.setGenerateRefreshToken(true);
        service.setJsonFormat(true);
        internalVerifyRefreshTokenOk(service, true);
    }

}
