package org.apereo.cas.support.saml.web.idp.metadata;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * The {@link SamlIdPMetadataController} will attempt
 * to produce saml metadata for CAS as an identity provider.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Controller("samlIdPMetadataController")
@Slf4j
@AllArgsConstructor
public class SamlIdPMetadataController implements InitializingBean {
    private static final String CONTENT_TYPE = "text/xml;charset=UTF-8";

    private final SamlIdPMetadataGenerator metadataAndCertificatesGenerationService;
    private final SamlIdPMetadataLocator samlIdPMetadataLocator;


    @Override
    public void afterPropertiesSet() throws Exception {
        this.metadataAndCertificatesGenerationService.generate();
    }

    /**
     * Displays the identity provider metadata.
     * Checks to make sure metadata exists, and if not, generates it first.
     *
     * @param response servlet response
     * @throws IOException the iO exception
     */
    @GetMapping(path = SamlIdPConstants.ENDPOINT_IDP_METADATA)
    public void generateMetadataForIdp(final HttpServletResponse response) throws IOException {
        this.metadataAndCertificatesGenerationService.generate();
        final var md = this.samlIdPMetadataLocator.getMetadata().getInputStream();
        final var contents = IOUtils.toString(md, StandardCharsets.UTF_8);
        response.setContentType(CONTENT_TYPE);
        response.setStatus(HttpServletResponse.SC_OK);
        try (var writer = response.getWriter()) {
            LOGGER.debug("Producing metadata for the response");
            writer.write(contents);
            writer.flush();
        }
    }
}
