package pmcp.mode.hud;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.world.entity.LivingEntity;
import pmcp.event.EventTarget;
import pmcp.event.mode.player.EventAttack;
import pmcp.event.mode.render.EventRender2D;
import pmcp.mode.Mode;
import pmcp.mode.render.NameTags;
import pmcp.utils.category.Category;
import pmcp.utils.player.NameTag;
import pmcp.utils.render.*;
import pmcp.utils.time.MSTimer;
import pmcp.value.values.ModeValue;
import pmcp.value.values.NumberValue;

public class TargetHUD extends Mode {


    private final ModeValue<mode> modeValue = new ModeValue<>(this, "Mode", mode.values(), mode.PMCP);

    enum mode {
        Loratadine,
        Chill,
        LSD,
        PMCP,
        Exhibition,
        Modern_Remix
    }

    private final NumberValue<Number> xPos = new NumberValue<>(this,"X", 20, -1000, 1000, 10);
    private final NumberValue<Number> yPos = new NumberValue<>(this,"Y", 20, -1000, 1000, 10);

    public TargetHUD() {
        super("TargetHUD", "攻击目标显示", "显示你正在看向或者正在攻击目标的一些基本信息", Category.HUD);
    }

    public static final MSTimer targetTimer = new MSTimer();
    private final Animation animation = new EaseBackIn(500, 1.0, 1.8f);

    private LivingEntity target = null;
    @Override
    public void onEnable() {
        target = null;
        targetTimer.reset();
    }

    @EventTarget
    public void onAttack(EventAttack eventAttack) {
        if (eventAttack.getTarget() instanceof LivingEntity entity) {
            target = entity;
        }
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.level == null) return;

        if (event.getType() == EventRender2D.Type.POST) {

            NameTag nameTag;
            String name = "NPC -" + mc.player.tickCount;
            if (target != null) {
                nameTag = NameTags.Instance.getName(target, true);

                name = nameTag.color() + nameTag.rank() + ChatFormatting.WHITE + nameTag.name();

                if (target.isRemoved() || mc.player.distanceTo(target) > 8.0 || !target.isAlive()) {
                    target = null;
                }
            }

            if (target != null || mc.screen instanceof ChatScreen) {
                targetTimer.reset();
                this.animation.setDirection(Direction.FORWARDS);
            } else {
                this.animation.setDirection(Direction.BACKWARDS);
            }

            if (!animation.finished(Direction.BACKWARDS) || mc.screen instanceof ChatScreen) {
                LivingEntity finalTarget = null;

                if (target != null) {
                    finalTarget = target;
                } else if (mc.screen instanceof ChatScreen) {
                    finalTarget = mc.player;
                }

                event.getGuiGraphics().pose().pushPose();

                switch (modeValue.getValue()) {
                    case Loratadine ->
                            TargetManager.drawLoratadineTargetInfo(event.getGuiGraphics().pose(), finalTarget, name, animation, xPos.getValue().intValue(), yPos.getValue().intValue());
                    case Exhibition ->
                            TargetManager.drawExhibitionTargetInfo(event.getGuiGraphics().pose(), finalTarget, name, animation, xPos.getValue().intValue(), yPos.getValue().intValue());
                    case Chill ->
                            TargetManager.drawChillTargetInfo(event.getGuiGraphics().pose(), target, name, animation, xPos.getValue().intValue(), yPos.getValue().intValue());
                    case LSD ->
                            TargetManager.drawLSDTargetInfo(event.getGuiGraphics().pose(), finalTarget, name, animation, xPos.getValue().intValue(), yPos.getValue().intValue());
                    case PMCP ->
                            TargetManager.drawPMCPTargetInfo(event.getGuiGraphics().pose(), event.getGuiGraphics(), finalTarget, name, animation, xPos.getValue().intValue(), yPos.getValue().intValue());
                    case Modern_Remix ->
                            TargetManager.drawModernRemixTargetInfo(event.getGuiGraphics().pose(), finalTarget, name, animation, xPos.getValue().intValue(), yPos.getValue().intValue());
                }
                event.getGuiGraphics().pose().popPose();
            }
        }
    }
}
