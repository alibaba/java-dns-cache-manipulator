package com.alibaba.dcm;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.concurrent.Immutable;

@Immutable
public class DnsCacheEntry implements Serializable {
    private static final long serialVersionUID = -7476648934387757732L;

    private final String host;
    private final String ip;
    private final Date expiration;

    public String getHost() {
        return host;
    }

    public String getIp() {
        return ip;
    }

    public Date getExpiration() {
        return expiration;
    }

    public DnsCacheEntry(String host, String ip, Date expiration) {
        this.host = host;
        this.ip = ip;
        this.expiration = expiration;
    }

    @Override
    public String toString() {
        return "DnsCacheEntry{" +
                "host='" + host + '\'' +
                ", ip='" + ip + '\'' +
                ", expiration=" + expiration +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DnsCacheEntry dnsCacheEntry1 = (DnsCacheEntry) o;

        if (host != null ? !host.equals(dnsCacheEntry1.host) : dnsCacheEntry1.host != null)
            return false;
        if (ip != null ? !ip.equals(dnsCacheEntry1.ip) : dnsCacheEntry1.ip != null)
            return false;
        return !(expiration != null ? !expiration.equals(dnsCacheEntry1.expiration) : dnsCacheEntry1.expiration != null);
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        result = 31 * result + (expiration != null ? expiration.hashCode() : 0);
        return result;
    }
}
