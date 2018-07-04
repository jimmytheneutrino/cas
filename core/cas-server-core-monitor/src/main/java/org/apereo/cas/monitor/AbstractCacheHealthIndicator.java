package org.apereo.cas.monitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract base class for monitors that observe cache storage systems.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public abstract class AbstractCacheHealthIndicator extends AbstractHealthIndicator {
    /**
     * CAS settings.
     */
    private final long evictionThreshold;
    private final long threshold;

    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        try {
            final var statistics = getStatistics();
            if (statistics == null || statistics.length == 0) {
                builder.outOfService().withDetail("message", "Cache statistics are not available.");
                return;
            }

            final var statuses = Arrays.stream(statistics)
                .map(this::status)
                .collect(Collectors.toSet());

            if (statuses.contains(Status.OUT_OF_SERVICE)) {
                builder.outOfService();
            } else if (statuses.contains(Status.DOWN)) {
                builder.down();
            } else if (statuses.contains(new Status("WARN"))) {
                builder.status("WARN");
            } else {
                builder.up();
            }

            Arrays.stream(statistics).forEach(s -> {
                final Map<String, Object> map = new HashMap<>();
                map.put("size", s.getSize());
                map.put("capacity", s.getCapacity());
                map.put("evictions", s.getEvictions());
                map.put("percentFree", s.getPercentFree());
                builder.withDetail(s.getName(), map);
            });
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            builder.down(e);
        }

    }

    /**
     * Gets the statistics from this monitor.
     *
     * @return the statistics
     */
    protected abstract CacheStatistics[] getStatistics();

    /**
     * Computes the status code for a given set of cache statistics.
     *
     * @param statistics Cache statistics.
     * @return WARN or OUT_OF_SERVICE OR UP.
     */
    protected Status status(final CacheStatistics statistics) {
        if (statistics.getEvictions() > 0 && statistics.getEvictions() > evictionThreshold) {
            return new Status("WARN");
        }
        if (statistics.getPercentFree() > 0 && statistics.getPercentFree() < threshold) {
            return Status.OUT_OF_SERVICE;
        }
        return Status.UP;
    }
}
