package org.apereo.cas.configuration.model.core.audit;

import lombok.Getter;
import lombok.Setter;
import org.apereo.cas.configuration.model.support.couchbase.BaseCouchbaseProperties;
import org.apereo.cas.configuration.support.RequiresModule;

/**
 * This is {@link AuditCouchbaseProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-audit-couchbase")
@Getter
@Setter
public class AuditCouchbaseProperties extends BaseCouchbaseProperties {
    private static final long serialVersionUID = 580545095591694L;

    /**
     * Whether audit records should be executed asynchronously.
     */
    private boolean asynchronous;
}
