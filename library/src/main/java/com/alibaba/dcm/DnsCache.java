package com.alibaba.dcm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * JVM whole dns cache info, including negative cache.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see DnsCacheEntry
 * @since 1.2.0
 */
@Immutable
public class DnsCache implements Serializable {
    private static final long serialVersionUID = -8614746635950970028L;

    private final List<DnsCacheEntry> cache;
    private final List<DnsCacheEntry> negativeCache;

    public DnsCache(@Nonnull List<DnsCacheEntry> cache, @Nonnull List<DnsCacheEntry> negativeCache) {
        this.cache = cache;
        this.negativeCache = negativeCache;
    }

    public List<DnsCacheEntry> getCache() {
        // defensive copy
        return new ArrayList<DnsCacheEntry>(cache);
    }

    public List<DnsCacheEntry> getNegativeCache() {
        // defensive copy
        return new ArrayList<DnsCacheEntry>(negativeCache);
    }

    @Override
    public String toString() {
        return "DnsCache{" +
                "cache=" + cache +
                ", negativeCache=" + negativeCache +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DnsCache dnsCache = (DnsCache) o;

        if (!cache.equals(dnsCache.cache))
            return false;
        return negativeCache.equals(dnsCache.negativeCache);
    }

    @Override
    public int hashCode() {
        int result = cache.hashCode();
        result = 31 * result + negativeCache.hashCode();
        return result;
    }
}
