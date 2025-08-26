package pmcp.event.mode.render;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import pmcp.event.Event;

@Getter
public class EventRender3D extends Event {

    private final float partialTicks;
    private final PoseStack poseStack;

    public EventRender3D(float partialTicks, PoseStack poseStack) {
        this.partialTicks = partialTicks;
        this.poseStack = poseStack;
    }
}
