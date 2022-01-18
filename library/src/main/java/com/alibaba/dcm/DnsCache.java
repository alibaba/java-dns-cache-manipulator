package com.alibaba.dcm;

import edu.umd.cs.findbugs.annotations.ReturnValuesAreNonnullByDefault;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * JVM whole dns cache info, including negative cache.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see DnsCacheEntry
 * @since 1.2.0
 */
@Immutable
@ParametersAreNonnullByDefault
@ReturnValuesAreNonnullByDefault
public final class DnsCache implements Serializable {
    private static final long serialVersionUID = -8614746635950970028L;

    private final List<DnsCacheEntry> cache;
    private final List<DnsCacheEntry> negativeCache;

    /**
     * Construct a {@link DnsCache}.
     */
    public DnsCache(@SuppressFBWarnings("EI_EXPOSE_REP2") List<DnsCacheEntry> cache,
                    @SuppressFBWarnings("EI_EXPOSE_REP2") List<DnsCacheEntry> negativeCache) {
        this.cache = cache;
        this.negativeCache = negativeCache;
    }

    /**
     * DNS positive cache.
     */
    public List<DnsCacheEntry> getCache() {
        // defensive copy
        return new ArrayList<>(cache);
    }

    /**
     * DNS negative cache.
     */
    public List<DnsCacheEntry> getNegativeCache() {
        // defensive copy
        return new ArrayList<>(negativeCache);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "DnsCache{" +
                "cache=" + cache +
                ", negativeCache=" + negativeCache +
                '}';
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DnsCache dnsCache = (DnsCache) o;

        if (!cache.equals(dnsCache.cache))
            return false;
        return negativeCache.equals(dnsCache.negativeCache);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = cache.hashCode();
        result = 31 * result + negativeCache.hashCode();
        return result;
    }
}
