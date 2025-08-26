package pmcp.utils.category;

import lombok.Getter;

@Getter
public enum Category {

    CLIENT("客户端类", "Client", "pmcp/clickgui/Client.png"),
    COMBAT("战斗类", "ComBat", "pmcp/clickgui/Combat.png"),
    EXPLOIT("其他类", "Exploit", "pmcp/clickgui/Exploit.png"),
    MISC("杂项类", "Misc", "pmcp/clickgui/Misc.png"),
    MOVE("移动类", "Move", "pmcp/clickgui/Movement.png"),
    PLAYER("玩家类", "Player", "pmcp/clickgui/Player.png"),
    WORLD("世界类", "World", "pmcp/clickgui/World.png"),
    RENDER("渲染类", "Render", "pmcp/clickgui/Render.png"),
    HUD("视觉类", "HUD", "pmcp/clickgui/HUD.png");

    private final String chinese;
    private final String english;
    private final String resources;

    Category(String c, String e, String r) {
        chinese = c;
        english = e;
        resources = r;
    }
}
