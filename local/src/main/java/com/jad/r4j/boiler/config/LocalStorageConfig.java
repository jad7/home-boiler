package com.jad.r4j.boiler.config;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.function.Supplier;

@Slf4j
public class LocalStorageConfig implements Destroyable {

    private static final int RETRY_COUNT = 4;
    private final Supplier<Connection> connectionSupplier;
    private final Connection[] connectionCache = new Connection[1];

    public LocalStorageConfig(@Named("local.storage.file") String fileName,
                              Configuration configuration) throws Exception {

        connectionSupplier = () -> {
            try {
                File file = new File(fileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                return DriverManager.getConnection("jdbc:sqlite:" + fileName);
            } catch (SQLException e) {
                log.error("DB exception", e);
                throw new CreationException("Can not create connection to DB", e);
            } catch (IOException e) {
                throw new RuntimeException("DB file problem", e);
            }
        };
        connectionCache[0] = connectionSupplier.get();

        executeStatement(connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.setQueryTimeout(30);  // set timeout to 30 sec.
                statement.executeUpdate("create table if not exists config (name string PRIMARY KEY, value string)");
            }
        });
        executeStatement(connection -> {
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT name, value FROM config");
                while (resultSet.next()) {
                    configuration.update(resultSet.getString(1), String.class, resultSet.getString(2));
                }
            }
        });
        configuration.registerListener("", tuple -> {
            try {
                executeStatement(connection -> {
                    try (PreparedStatement statement = connection.prepareStatement("REPLACE INTO config (name, value) VALUES (?, ?)")) {
                        statement.setQueryTimeout(30);  // set timeout to 30 sec.
                        statement.setString(1, tuple.getA());
                        statement.setString(2, String.valueOf(tuple.getB()));
                        statement.executeUpdate();
                    }
                });
            } catch (SQLException e) {
                log.warn("Can not update property:" + tuple);
            }
        });
    }

    public void executeStatement(ThrCons<Connection> function) throws SQLException {
        executeStatement(c -> {function.apply(c); return Void.TYPE;});
    }

    public <T> T executeStatement(ThrFunc<Connection, T> function) throws SQLException {
        int retry = RETRY_COUNT;
        while (retry-- != 0) {
            try {
                if (connectionCache[0] == null) {
                    connectionCache[0] = connectionSupplier.get();
                }
                return function.apply(connectionCache[0]);
            } catch (CreationException e) {
                System.out.println("Create DB retry " + (RETRY_COUNT - retry) + "/" + RETRY_COUNT);
                closeConnection();
                //retry
            }
        }
        throw new RuntimeException("Can not create DB connection");
    }

    @Override
    public void destroy() {
        closeConnection();
    }

    private class CreationException extends RuntimeException {
        public CreationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private void closeConnection() {
        if (connectionCache[0] != null) {
            try {
                connectionCache[0].close();
            } catch(SQLException e) {
                System.err.println("Can not close connection");
                e.printStackTrace();
            }
            connectionCache[0] = null;
        }
    }

    @FunctionalInterface
    public interface ThrFunc<P, R> {
        R apply(P param) throws SQLException;
    }

    @FunctionalInterface
    public interface ThrCons<P> {
        void apply(P param) throws SQLException;
    }

}
