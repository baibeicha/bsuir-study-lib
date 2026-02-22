package com.github.baibeicha.database.connection;

import com.github.baibeicha.database.DataSource;
import com.github.baibeicha.database.exception.DatabaseException;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManager implements AutoCloseable {

    private volatile ConnectionPool connectionPool;

    public ConnectionManager(DataSource source, int poolSize) throws SQLException {
        connectionPool = ConnectionPool.create(
                source.url(),
                source.username(),
                source.password(),
                poolSize
        );
    }

    public Connection getConnection() {
        try {
            return connectionPool.getConnection();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DatabaseException("Thread was interrupted while waiting for a connection.", e);
        }
    }

    public void shutdownPool() throws SQLException {
        if (connectionPool != null) {
            synchronized (ConnectionManager.class) {
                if (connectionPool != null) {
                    connectionPool.shutdown();
                    connectionPool = null;
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        shutdownPool();
    }
}
