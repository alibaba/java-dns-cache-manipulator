package com.alibaba.dcm;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * DNS cache entry(DNS record).
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see DnsCache
 */
@Immutable
public final class DnsCacheEntry implements Serializable {
    private static final long serialVersionUID = -7476648934387757732L;

    private final String host;
    private final String[] ips;
    private final long expiration;

    /**
     * get host name/domain name of DNS cache entry(DNS record).
     */
    public String getHost() {
        return host;
    }

    /**
     * get ips of DNS cache entry(DNS record).
     */
    public String[] getIps() {
        return ips.clone(); // defensive copy
    }

    /**
     * get the first ip of {@link #getIps()}
     */
    public String getIp() {
        return ips[0];
    }

    /**
     * get the expiration of DNS cache entry(DNS record).
     * <p>
     * return value {@link Long#MAX_VALUE} means "never expiration".
     */
    public Date getExpiration() {
        return new Date(expiration);
    }

    /**
     * Construct a {@link DnsCacheEntry}.
     *
     * @deprecated use {@link #DnsCacheEntry(String, String[], long)} instead
     */
    @Deprecated
    public DnsCacheEntry(String host,
                         @SuppressFBWarnings("EI_EXPOSE_REP2") String[] ips,
                         @SuppressFBWarnings("EI_EXPOSE_REP2") Date expiration) {
        this.host = host;
        this.ips = ips;
        this.expiration = expiration.getTime();
    }

    /**
     * Construct a {@link DnsCacheEntry}.
     *
     * @since 1.6.0
     */
    public DnsCacheEntry(String host,
                         @SuppressFBWarnings("EI_EXPOSE_REP2") String[] ips,
                         long expiration) {
        this.host = host;
        this.ips = ips;
        this.expiration = expiration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        return "DnsCacheEntry{host='" + host + '\'' + ", ips=" + Arrays.toString(ips) +
                ", expiration=" + dateFormat.format(expiration) + '}';
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DnsCacheEntry that = (DnsCacheEntry) o;

        if (expiration != that.expiration) return false;
        if (!Objects.equals(host, that.host)) return false;
        return Arrays.equals(ips, that.ips);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(ips);
        result = 31 * result + Long.hashCode(expiration);
        return result;
    }
}
