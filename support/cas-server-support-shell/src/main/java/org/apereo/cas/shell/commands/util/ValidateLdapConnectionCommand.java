package org.apereo.cas.shell.commands.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import java.util.Arrays;
import java.util.Hashtable;

/**
 * This is {@link ValidateLdapConnectionCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@ShellComponent
@Slf4j
@ShellCommandGroup("Utilities")
public class ValidateLdapConnectionCommand {
    private static final int TIMEOUT = 5000;

    /**
     * Validate endpoint.
     *
     * @param url            the url
     * @param bindDn         the bind dn
     * @param bindCredential the bind credential
     * @param baseDn         the base dn
     * @param searchFilter   the search filter
     * @param userPassword   the user password
     * @param userAttributes the user attributes
     */
    @ShellMethod(key = "validate-ldap", value = "Test connections to an LDAP server to verify connectivity, SSL, etc")
    public void validateLdap(
        @ShellOption(value = {"url"},
            help = "LDAP URL to test, comma-separated.") final String url,
        @ShellOption(value = {"bindDn"},
            help = "bindDn to use when testing the LDAP server") final String bindDn,
        @ShellOption(value = {"bindCredential"},
            help = "bindCredential to use when testing the LDAP server") final String bindCredential,
        @ShellOption(value = {"baseDn"},
            help = "baseDn to use when testing the LDAP server, searching for accounts (i.e. OU=some,DC=org,DC=edu)") final String baseDn,
        @ShellOption(value = {"searchFilter"},
            help = "Filter to use when searching for accounts (i.e. (&(objectClass=*) (sAMAccountName=user)))") final String searchFilter,
        @ShellOption(value = {"userPassword"},
            help = "Password for the user found in the search result, to attempt authentication") final String userPassword,
        @ShellOption(value = {"userAttributes"},
            help = "User attributes, comma-separated, to fetch for the user found in the search result") final String userAttributes) {
        try {
            connect(url, bindDn, bindCredential, baseDn, searchFilter, userAttributes, userPassword);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void connect(final String ldapUrl, final String bindDn,
                         final String bindCredential,
                         final String baseDn, final String searchFilter,
                         final String userAttributes,
                         final String userPassword) throws Exception {
        final var pair = getContext(ldapUrl, bindDn, bindCredential);
        if (pair == null) {
            LOGGER.error("Could not connect to any of the provided LDAP urls based on the given credentials.");
            return;
        }

        DirContext ctx = null;
        try {
            ctx = pair.getValue();
            var log = "Successfully connected to the LDAP url [" + pair.getKey() + "] ";
            if (ctx.getNameInNamespace() != null && !ctx.getNameInNamespace().isEmpty()) {
                log += "with namespace [" + ctx.getNameInNamespace() + "].";
            }
            LOGGER.info(log);

            if (!StringUtils.hasText(searchFilter)) {
                return;
            }

            final var attrIDs = userAttributes.split(",");
            LOGGER.info("******* Ldap Search *******");
            LOGGER.info("Ldap filter: [{}]", searchFilter);
            LOGGER.info("Ldap search base: [{}]", baseDn);
            LOGGER.info("Returning attributes: [{}]\n", Arrays.toString(attrIDs));

            final var ctls = getSearchControls(attrIDs);
            final var answer = ctx.search(baseDn, searchFilter, ctls);
            if (answer.hasMoreElements()) {
                LOGGER.info("******* Ldap Search Results *******");
                while (answer.hasMoreElements()) {
                    final var result = answer.nextElement();
                    LOGGER.info("User name: [{}]", result.getName());
                    LOGGER.info("User full name: [{}]", result.getNameInNamespace());

                    if (userPassword != null) {
                        LOGGER.info("Attempting to authenticate [{}] with password [{}]", result.getName(), userPassword);

                        final var env = getLdapDirectoryContextSettings(result.getNameInNamespace(), userPassword, pair.getKey());
                        new InitialDirContext(env);
                        LOGGER.info("Successfully authenticated [{}] with password [{}]", result.getName(), userPassword);
                    }
                    final var attrs = result.getAttributes().getIDs();
                    while (attrs.hasMoreElements()) {
                        final var id = attrs.nextElement();
                        LOGGER.info("[{}] => [{}]", id, result.getAttributes().get(id));
                    }
                }
            } else {
                LOGGER.info("No search results could be found.");
            }
            LOGGER.info("Ldap search completed successfully.");
        } finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    private SearchControls getSearchControls(final String[] attrIDs) {
        final var ctls = new SearchControls();
        ctls.setDerefLinkFlag(true);
        ctls.setTimeLimit(TIMEOUT);
        ctls.setReturningAttributes(attrIDs);
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        return ctls;
    }

    private Pair<String, DirContext> getContext(final String ldapUrl, final String bindDn, final String bindCredential) {
        final var urls = StringUtils.commaDelimitedListToSet(ldapUrl);
        for (final var url: urls) {
            if (ldapUrl != null && !ldapUrl.isEmpty()) {
                LOGGER.info("Attempting connect to LDAP instance [{}]", url);

                final var env = getLdapDirectoryContextSettings(bindDn, bindCredential, url);
                try {
                    return Pair.of(ldapUrl, new InitialDirContext(env));
                } catch (final Exception e) {
                    LOGGER.error("Failed to connect to ldap instance [{}]", ldapUrl);
                }
            }
        }

        return null;
    }

    @SuppressWarnings("JdkObsolete")
    private Hashtable<String, String> getLdapDirectoryContextSettings(final String bindDn, final String bindCredential, final String url) {
        final Hashtable<String, String> env = new Hashtable<>(6);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, url.trim());
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, bindDn);
        env.put(Context.SECURITY_CREDENTIALS, bindCredential);
        env.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(TIMEOUT));
        return env;
    }
}
