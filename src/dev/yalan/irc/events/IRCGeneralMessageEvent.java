package dev.yalan.irc.events;

import lombok.Getter;
import pmcp.event.Event;

@Getter
public class IRCGeneralMessageEvent extends Event {
    private final String message;

    public IRCGeneralMessageEvent(String message) {
        this.message = message;
    }
}
