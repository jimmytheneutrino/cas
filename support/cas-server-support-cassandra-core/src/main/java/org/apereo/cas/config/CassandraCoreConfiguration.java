package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.cassandra.CassandraSessionFactory;
import org.apereo.cas.cassandra.DefaultCassandraSessionFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CassandraCoreConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("cassandraCoreConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CassandraCoreConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    public CassandraSessionFactory cassandraSessionFactory() {
        final var cassandra = casProperties.getAuthn().getCassandra();
        return new DefaultCassandraSessionFactory(cassandra);
    }

}
