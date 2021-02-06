package com.zekro.discord.bot.ronboard;

import com.google.gson.Gson;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.security.auth.login.LoginException;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class RonBoard {

    private final Settings settings;
    private final Database database;
    private JDA jda;

    public RonBoard() throws FileNotFoundException {
        Gson gson = new Gson();
        settings = gson.fromJson(new FileReader("/home/pi/Documents/RonBoard/settings.json"), Settings.class);
        database = new Database(settings.getJdbcurl(), settings.getUsername(), settings.getPassword());
    }

    public void start() throws LoginException, InterruptedException {
        database.connect();
        jda = JDABuilder.createDefault(settings.getToken()).build().awaitReady();
        jda.addEventListener(new EventListener(this));
    }

    public void stop() {
        jda.shutdown();
        database.disconnect();
        System.exit(0);
    }

    public Settings getSettings() {
        return settings;
    }

    public JDA getJda() {
        return jda;
    }

    public TextChannel getStarboardChannel() {
        return jda.getTextChannelById(settings.getChannelId());
    }

    public Database getDatabase() {
        return database;
    }
}
