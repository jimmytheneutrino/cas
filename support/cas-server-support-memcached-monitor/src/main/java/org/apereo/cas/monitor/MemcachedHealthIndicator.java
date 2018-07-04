package org.apereo.cas.monitor;

import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.MemcachedClientIF;
import org.apache.commons.pool2.ObjectPool;
import org.springframework.boot.actuate.health.Health;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Monitors the memcached hosts known to an instance of {@link net.spy.memcached.MemcachedClientIF}.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
@Slf4j
public class MemcachedHealthIndicator extends AbstractCacheHealthIndicator {
    private final ObjectPool<MemcachedClientIF> connectionPool;

    public MemcachedHealthIndicator(final ObjectPool<MemcachedClientIF> client,
                                    final long evictionThreshold, final long threshold) {
        super(evictionThreshold, threshold);
        this.connectionPool = client;
    }

    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        try {
            final var client = (MemcachedClient) getClientFromPool();
            if (client.getAvailableServers().isEmpty()) {
                LOGGER.warn("No available memcached servers can be found");
                builder.outOfService().withDetail("message", "No memcached servers available.");
                return;
            }
            final var unavailableList = client.getUnavailableServers();
            if (!unavailableList.isEmpty()) {
                final var description = "One or more memcached servers is unavailable: " + unavailableList;
                builder.down().withDetail("message", description);
                return;
            }
            super.doHealthCheck(builder);
            return;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            builder.down()
                .withException(e)
                .withDetail("message", "Unable to determine memcached server status.");
        }

    }

    /**
     * Get cache statistics for all memcached hosts known to {@link MemcachedClientIF}.
     *
     * @return Statistics for all available hosts.
     */
    @Override
    protected CacheStatistics[] getStatistics() {
        final List<CacheStatistics> statsList = new ArrayList<>();
        try {
            final var client = getClientFromPool();
            client.getStats()
                .forEach((key, statsMap) -> {
                    if (!statsMap.isEmpty()) {
                        final var size = Long.parseLong(statsMap.get("bytes"));
                        final var capacity = Long.parseLong(statsMap.get("limit_maxbytes"));
                        final var evictions = Long.parseLong(statsMap.get("evictions"));

                        final String name;
                        if (key instanceof InetSocketAddress) {
                            name = ((InetSocketAddress) key).getHostName();
                        } else {
                            name = key.toString();
                        }
                        statsList.add(new SimpleCacheStatistics(size, capacity, evictions, name));
                    }
                });
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return statsList.toArray(new CacheStatistics[0]);
    }

    private MemcachedClientIF getClientFromPool() throws Exception {
        return this.connectionPool.borrowObject();
    }
}
