package com.youthlin.rpc.core;

/**
 * 创建: youthlin.chen
 * 时间: 2017-11-27 22:08
 */
public class Key {
    private String host;
    private int port;

    Key(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Key key = (Key) o;

        if (port != key.port)
            return false;
        return host != null ? host.equals(key.host) : key.host == null;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}