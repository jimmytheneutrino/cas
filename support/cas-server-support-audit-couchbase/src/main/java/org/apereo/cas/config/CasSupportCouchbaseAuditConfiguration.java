package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.CouchbaseAuditTrailManager;
import org.apereo.cas.audit.spi.AuditActionContextJsonSerializer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * This is {@link CasSupportCouchbaseAuditConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration("casSupportCouchbaseAuditConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasSupportCouchbaseAuditConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public CouchbaseClientFactory auditsCouchbaseClientFactory() {
        final var cb = casProperties.getAudit().getCouchbase();
        final var nodes = StringUtils.commaDelimitedListToSet(cb.getNodeSet());
        return new CouchbaseClientFactory(nodes, cb.getBucket(),
            cb.getPassword(),
            Beans.newDuration(cb.getTimeout()).toMillis(),
            CouchbaseAuditTrailManager.UTIL_DOCUMENT,
            CouchbaseAuditTrailManager.ALL_VIEWS);
    }

    @Bean
    public AuditTrailManager couchbaseAuditTrailManager() {
        final var cb = casProperties.getAudit().getCouchbase();
        return new CouchbaseAuditTrailManager(auditsCouchbaseClientFactory(),
            new AuditActionContextJsonSerializer(), cb.isAsynchronous());
    }

    @Bean
    public AuditTrailExecutionPlanConfigurer couchbaseAuditTrailExecutionPlanConfigurer() {
        return plan -> plan.registerAuditTrailManager(couchbaseAuditTrailManager());
    }
}
