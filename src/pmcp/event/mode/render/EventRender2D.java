package pmcp.event.mode.render;

import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import pmcp.event.Event;

@Getter
public class EventRender2D extends Event {
    private final float partialTicks;
    private final GuiGraphics guiGraphics;
    private final Type type;

    public EventRender2D(float partialTicks, GuiGraphics guiGraphics, Type type) {
        this.partialTicks = partialTicks;
        this.guiGraphics = guiGraphics;
        this.type = type;
    }

    public enum Type {
        PRE,
        POST,
    }
}
