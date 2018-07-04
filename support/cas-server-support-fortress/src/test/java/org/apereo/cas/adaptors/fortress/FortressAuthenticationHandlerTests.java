package org.apereo.cas.adaptors.fortress;

import lombok.extern.slf4j.Slf4j;
import org.apache.directory.fortress.core.AccessMgr;
import org.apache.directory.fortress.core.GlobalErrIds;
import org.apache.directory.fortress.core.PasswordException;
import org.apache.directory.fortress.core.model.Session;
import org.apache.directory.fortress.core.model.User;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Before;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.security.auth.login.FailedLoginException;
import javax.xml.bind.JAXBContext;
import java.io.StringWriter;
import java.util.UUID;

/**
 * This is {@link FortressAuthenticationHandler}.
 *
 * @author yudhi.k.surtan
 * @since 5.2.0
 */
@Slf4j
public class FortressAuthenticationHandlerTests {


    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private AccessMgr accessManager;

    @InjectMocks
    private FortressAuthenticationHandler fortressAuthenticationHandler;

    @Before
    public void initializeTest() {
        MockitoAnnotations.initMocks(this);
        fortressAuthenticationHandler.setAccessManager(accessManager);
    }

    @Test
    public void verifyUnauthorizedUserLoginIncorrect() throws Exception {
        Mockito.when(accessManager.createSession(ArgumentMatchers.any(User.class), ArgumentMatchers.anyBoolean()))
            .thenThrow(new PasswordException(GlobalErrIds.USER_PW_INVLD, "error message"));
        this.thrown.expect(FailedLoginException.class);
        fortressAuthenticationHandler.authenticateUsernamePasswordInternal(
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), null);
    }

    @Test
    public void verifyAuthenticateSuccessfully() throws Exception {
        final var sessionId = UUID.randomUUID();
        final var session = new Session(new User(CoreAuthenticationTestUtils.CONST_USERNAME), sessionId.toString());
        session.setAuthenticated(true);
        Mockito.when(accessManager.createSession(ArgumentMatchers.any(User.class), ArgumentMatchers.anyBoolean())).thenReturn(session);
        try {
            final var handlerResult = fortressAuthenticationHandler.authenticateUsernamePasswordInternal(
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), null);
            Assert.assertEquals(CoreAuthenticationTestUtils.CONST_USERNAME,
                handlerResult.getPrincipal().getId());
            final var jaxbContext = JAXBContext.newInstance(Session.class);
            final var marshaller = jaxbContext.createMarshaller();
            final var writer = new StringWriter();
            marshaller.marshal(session, writer);
            Assert.assertEquals(writer.toString(), handlerResult.getPrincipal()
                .getAttributes().get(FortressAuthenticationHandler.FORTRESS_SESSION_KEY));
        } catch (final Exception e) {
            LOGGER.error("test failed", e);
        }
    }
}
