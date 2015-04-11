package com.alibaba.dcm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.concurrent.Immutable;

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
        String[] copy = new String[ips.length];
        System.arraycopy(ips, 0, copy, 0, ips.length); // defensive copy
        return copy;
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
        return "DnsCacheEntry{" +
                "host='" + host + '\'' +
                ", ips=" + Arrays.toString(ips) +
                ", expiration=" + expiration +
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
