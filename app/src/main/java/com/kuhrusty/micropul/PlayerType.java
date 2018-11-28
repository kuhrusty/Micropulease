package com.kuhrusty.micropul;

import android.content.res.Resources;

import com.kuhrusty.micropul.bot.Bot;
import com.kuhrusty.micropul.bot.DumbBot;

/**
 * You have to implement instantiateBot(), because I am too lazy to have a
 * ClassLoader instantiate it from a class name.
 */
public abstract class PlayerType {
    private static PlayerType[] cache = null;
    public static PlayerType[] getPlayerTypes(Resources res) {
        if (cache == null) {
            cache = new PlayerType[] {
                    //  Leave this one first; I think there are places where
                    //  it's assumed that element 0 is a human player holding
                    //  the device.
                    new PlayerType(res.getString(R.string.hotseat_name),
                            res.getString(R.string.hotseat_descr), false, true) {
                        @Override
                        public Bot instantiateBot(Resources res) {
                            return null;
                        }
                    },
                    new PlayerType(res.getString(R.string.dumbbot_name),
                            res.getString(R.string.dumbbot_descr), true, false) {
                        @Override
                        public Bot instantiateBot(Resources res) {
                            return new DumbBot(res);
                        }
                    },
            };
        }
        return cache;
    }
    private PlayerType(String name, String descr, boolean bot, boolean hotseat) {
        this.name = name;
        this.descr = descr;
        this.bot =  bot;
        this.hotseat = hotseat;
    }
    @Override
    public String toString() {
        return name;
    }
    public String getDescription() {
        return descr;
    }
    public boolean isBot() {
        return bot;
    }
    public boolean isHotSeat() {
        return hotseat;
    }
    abstract public Bot instantiateBot(Resources res);

    private String name;
    private String descr;
    private boolean bot;
    private boolean hotseat;
}
