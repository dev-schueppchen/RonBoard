package com.zekro.discord.bot.ronboard;

public class Settings {

    private String token;
    private String prefix;
    private int bound;
    private long channelId;
    private String jdbcurl;
    private String username;
    private String password;

    public Settings(String token, String prefix, int bound, long channelId, String jdbcurl, String username, String password) {
        this.token = token;
        this.prefix = prefix;
        this.bound = bound;
        this.channelId = channelId;
        this.jdbcurl = jdbcurl;
        this.username = username;
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getBound() {
        return bound;
    }

    public void setBound(int bound) {
        this.bound = bound;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public String getJdbcurl() {
        return jdbcurl;
    }

    public void setJdbcurl(String jdbcurl) {
        this.jdbcurl = jdbcurl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
