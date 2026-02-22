package com.github.baibeicha.database;

public record DataSource(
        DatabaseType databaseType,
        String host,
        int port,
        String database,
        String username,
        String password
) {

    public String url() {
        return DbUrls.getUrl(databaseType, host, port, database);
    }

    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder {

        private DatabaseType databaseType;
        private String host;
        private int port;
        private String database;
        private String username;
        private String password;

        public Builder type(DatabaseType databaseType) {
            this.databaseType = databaseType;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder database(String database) {
            this.database = database;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public DataSource build() {
            return new DataSource(databaseType, host, port, database, username, password);
        }
    }
}
