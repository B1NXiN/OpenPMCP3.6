package dev.yalan.irc.events;

import lombok.Getter;
import pmcp.event.Event;

@Getter
public class IRCAuthenticationResultEvent extends Event {
    private final int result;
    private final String message;

    public IRCAuthenticationResultEvent(int result, String message) {
        this.result = result;
        this.message = message;
    }
}
