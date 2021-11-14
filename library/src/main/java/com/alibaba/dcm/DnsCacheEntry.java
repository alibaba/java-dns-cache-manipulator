package com.alibaba.dcm;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see DnsCache
 */
@Immutable
public final class DnsCacheEntry implements Serializable {
    private static final long serialVersionUID = -7476648934387757732L;

    private final String host;
    private final String[] ips;
    private final long expiration;

    public String getHost() {
        return host;
    }

    @Nonnull
    public String[] getIps() {
        return ips.clone(); // defensive copy
    }

    public String getIp() {
        return ips[0];
    }

    /**
     * return value {@link Long#MAX_VALUE} means "never expiration".
     */
    public Date getExpiration() {
        return new Date(expiration);
    }

    /**
     * @deprecated use {@link #DnsCacheEntry(String, String[], long)} instead
     */
    @Deprecated
    public DnsCacheEntry(String host,
                         @Nonnull @SuppressFBWarnings("EI_EXPOSE_REP2") String[] ips,
                         @Nonnull @SuppressFBWarnings("EI_EXPOSE_REP2") Date expiration) {
        this.host = host;
        this.ips = ips;
        this.expiration = expiration.getTime();
    }

    /**
     * @since 1.6.0
     */
    public DnsCacheEntry(String host,
                         @Nonnull @SuppressFBWarnings("EI_EXPOSE_REP2") String[] ips,
                         long expiration) {
        this.host = host;
        this.ips = ips;
        this.expiration = expiration;
    }

    @Override
    public String toString() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        return "DnsCacheEntry{" +
                "host='" + host + '\'' +
                ", ips=" + Arrays.toString(ips) +
                ", expiration=" + dateFormat.format(expiration) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DnsCacheEntry that = (DnsCacheEntry) o;

        if (expiration != that.expiration) return false;
        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        return Arrays.equals(ips, that.ips);
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(ips);
        result = 31 * result + (int) (expiration ^ (expiration >>> 32));
        return result;
    }
}
