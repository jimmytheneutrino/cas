package org.apereo.cas.configuration.model.core;

import lombok.Getter;
import lombok.Setter;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link CasServerProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core", automated = true)
@Getter
@Setter
public class CasServerProperties implements Serializable {

    private static final long serialVersionUID = 7876382696803430817L;

    /**
     * Full name of the CAS server. This is public-facing address
     * of the CAS deployment and not the individual node address,
     * in the event that CAS is clustered.
     */
    @RequiredProperty
    private String name = "https://cas.example.org:8443";

    /**
     * A concatenation of the server name plus the CAS context path.
     * Deployments at root likely need to blank out this value.
     */
    @RequiredProperty
    private String prefix = name.concat("/cas");

    /**
     * Configuration settings that control the embedded Apache Tomcat container.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatProperties tomcat = new CasEmbeddedApacheTomcatProperties();

    public String getLoginUrl() {
        return getPrefix().concat(CasProtocolConstants.ENDPOINT_LOGIN);
    }

    public String getLogoutUrl() {
        return getPrefix().concat(CasProtocolConstants.ENDPOINT_LOGOUT);
    }

}
