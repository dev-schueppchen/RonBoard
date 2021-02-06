package com.zekro.discord.bot.ronboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.FileNotFoundException;

public class Bootstrapper {

    private static final Logger log = LoggerFactory.getLogger(Bootstrapper.class);

    public static void main(String[] args) {

        RonBoard ronBoard;
        try {
            ronBoard = new RonBoard();
            ronBoard.start();
            Runtime.getRuntime().addShutdownHook(new Thread(ronBoard::stop));
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> log.error("An unexpected error has occurred!", e));
        } catch (LoginException | FileNotFoundException | InterruptedException e) {
            log.error("Unable to start bot! Aborting!", e);
            System.exit(1);
        }
    }

}
