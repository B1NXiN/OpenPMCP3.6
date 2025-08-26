package pmcp.ui.hud.Notification.NotiBox;

import lombok.Getter;

import java.awt.*;

@Getter
public enum NotiCategory {
    OPEN("A", Color.green),
    CLOSE("B", Color.red),
    TIPS("C", Color.white),
    WARN("D", Color.yellow);

    private final String tile;
    private final Color color;

     NotiCategory(String tile, Color color) {
         this.tile = tile;
         this.color = color;
    }
}
