package com.zekro.discord.bot.ronboard;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class Database {

    private final String jdbcurl;
    private final String username;
    private final String password;
    private final Logger logger = LoggerFactory.getLogger(Database.class);
    private boolean connected;
    private HikariDataSource dataSource;

    public Database(String jdbcurl, String username, String password) {
        this.jdbcurl = jdbcurl;
        this.username = username;
        this.password = password;
    }

    public void connect() {
        if (!connected) {
            HikariConfig config = new HikariConfig();
            logger.info("Connecting to database");
            config.setJdbcUrl(jdbcurl);
            config.setUsername(username);
            config.setPassword(password);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            try {
                dataSource = new HikariDataSource(config);
                connected = true;
                logger.info("Database connection pool successfully opened");
            } catch (HikariPool.PoolInitializationException e) {
                logger.error(" Error while connecting to database", e);
                System.exit(1);
            }
        }
    }

    public void disconnect() {
        if (connected) {
            dataSource.close();
            logger.info("Database disconnected");
            connected = false;
        }
    }

    public Optional<StarboardEntry> getStarboardEntry(long messageId) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("select * from messages where message_id = ?");
            statement.setLong(1, messageId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                StarboardEntry entry = new StarboardEntry(
                        resultSet.getLong("message_id"),
                        resultSet.getLong("channel_id"),
                        resultSet.getLong("starboard_id"),
                        resultSet.getBoolean("is_posted")
                );
                return Optional.of(entry);
            }
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
        return Optional.empty();
    }

    public boolean addStarboardEntry(StarboardEntry starboardEntry) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("insert into messages values (?, ?, ?, ?) ");
            statement.setLong(1, starboardEntry.getMessageId());
            statement.setLong(2, starboardEntry.getChannelId());
            statement.setLong(3, starboardEntry.getStarboardId());
            statement.setBoolean(4, starboardEntry.isPosted());
            return statement.execute();
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
        return false;
    }

    public boolean updateStarboardEntry(StarboardEntry starboardEntry) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("update messages set " +
                    "channel_id = ?," +
                    "starboard_id = ?," +
                    "is_posted = ? " +
                    "where message_id = ?");
            statement.setLong(1, starboardEntry.getChannelId());
            statement.setLong(2, starboardEntry.getStarboardId());
            statement.setBoolean(3, starboardEntry.isPosted());
            statement.setLong(4, starboardEntry.getMessageId());
            return statement.execute();
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
        return false;
    }

    public boolean deleteStarboardEntry(StarboardEntry starboardEntry) {
        return deleteStarboardEntry(starboardEntry.getMessageId());
    }

    public boolean deleteStarboardEntry(long messageId) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("delete from messages where message_id = ?");
            statement.setLong(1, messageId);
            return statement.execute();
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
        return false;
    }
}
