package com.kuhrusty.micropul.model;

/**
 * Thrown when we detect that a bot has attempted to make an illegal play.
 */
public class IllegalPlayException extends RuntimeException {
    public IllegalPlayException(String msg) {
        super(msg);
    }
}
