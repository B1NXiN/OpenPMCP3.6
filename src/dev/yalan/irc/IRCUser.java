package dev.yalan.irc;

import lombok.Getter;

@Getter
public class IRCUser {
    private final String clientName;
    private final String username;
    private final String rank;

    public IRCUser(String clientName, String username, String rank) {
        this.clientName = clientName;
        this.username = username;
        this.rank = rank;
    }
}
