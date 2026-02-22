package com.github.baibeicha.database.connection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class ConnectionPool implements AutoCloseable {

    private final BlockingQueue<Connection> connectionQueue;
    private final String url;
    private final String user;
    private final String password;

    static ConnectionPool create(String url, String user, String password, int poolSize) throws SQLException {
        BlockingQueue<Connection> queue = new ArrayBlockingQueue<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            Connection connection = DriverManager.getConnection(url, user, password);
            queue.add(connection);
        }
        return new ConnectionPool(queue, url, user, password);
    }

    private ConnectionPool(BlockingQueue<Connection> connectionQueue, String url, String user, String password) {
        this.connectionQueue = connectionQueue;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() throws InterruptedException {
        final Connection originalConnection = connectionQueue.take();

        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                new ConnectionInvocationHandler(originalConnection)
        );
    }

    private void releaseConnection(Connection connection) {
        try {
            if (!connection.isClosed()) {
                connectionQueue.offer(connection);
            } else {
                connection = DriverManager.getConnection(url, user, password);
                connectionQueue.add(connection);
            }
        } catch (SQLException e) {
            System.err.println("Failed to check if connection is closed before release: " + e.getMessage());
        }
    }

    public void shutdown() throws SQLException {
        for (Connection connection : connectionQueue) {
            connection.close();
        }
        connectionQueue.clear();
    }

    @Override
    public void close() throws Exception {
        shutdown();
    }

    private class ConnectionInvocationHandler implements InvocationHandler {

        private final Connection originalConnection;

        public ConnectionInvocationHandler(Connection originalConnection) {
            this.originalConnection = originalConnection;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("close".equals(method.getName())) {
                releaseConnection(originalConnection);
                return null;
            }

            return method.invoke(originalConnection, args);
        }
    }
}
