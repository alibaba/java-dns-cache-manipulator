package com.alibaba.dcm;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.annotation.concurrent.Immutable;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see DnsCache
 */
@Immutable
public final class DnsCacheEntry implements Serializable {
    private static final long serialVersionUID = -7476648934387757732L;

    private final String host;
    private final String[] ips;
    private final Date expiration;

    public String getHost() {
        return host;
    }

    public String[] getIps() {
        if (ips != null) {
            String[] copy = new String[ips.length];
            System.arraycopy(ips, 0, copy, 0, ips.length); // defensive copy
            return copy;
        } else {
            return null;
        }
    }

    public String getIp() {
        return ips[0];
    }

    public Date getExpiration() {
        return expiration;
    }

    public DnsCacheEntry(String host, String[] ips, Date expiration) {
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

        if (host != null ? !host.equals(that.host) : that.host != null)
            return false;
        if (!Arrays.equals(ips, that.ips)) return false;
        return !(expiration != null ? !expiration.equals(that.expiration) : that.expiration != null);
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + (ips != null ? Arrays.hashCode(ips) : 0);
        result = 31 * result + (expiration != null ? expiration.hashCode() : 0);
        return result;
    }
}
