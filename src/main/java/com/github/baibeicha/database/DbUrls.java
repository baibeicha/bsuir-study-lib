package com.github.baibeicha.database;

public class DbUrls {

    public static String getBase(DatabaseType type) {
        return switch (type) {
            case MYSQL -> "jdbc:mysql://";
            case POSTGRESQL -> "jdbc:postgresql://";
        };
    }

    public static String getUrl(DatabaseType type, String host, int port, String database) {
        return getBase(type) + host + ":" + port + "/" + database;
    }
}
