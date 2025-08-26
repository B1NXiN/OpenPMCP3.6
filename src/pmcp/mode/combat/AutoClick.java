package pmcp.mode.combat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import pmcp.event.EventTarget;
import pmcp.event.mode.player.EventPlace;
import pmcp.event.mode.update.EventTick;
import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.utils.category.NumberCategory;
import pmcp.utils.time.MSTimer;
import pmcp.value.values.BooleanValue;
import pmcp.value.values.NumberValue;

public class AutoClick extends Mode {
    private final BooleanValue<Boolean> left = new BooleanValue<>(this, "Left", true);
    public final NumberValue<Number> lCPS = new NumberValue<>(this, "LeftCPS", 10,1,20,1, left::getValue);

    private final BooleanValue<Boolean> right = new BooleanValue<>(this, "Right", true);

    public final NumberValue<Number> rCPS = new NumberValue<>(this, "RightCPS", 10,1,20,1, right::getValue);

    public AutoClick() {
        super("AutoClicker", "连点器", "自动连点", Category.COMBAT);
    }

    private final MSTimer attackTimer = new MSTimer();
    private final MSTimer placeTimer = new MSTimer();

    @Override
    public void onEnable() {
        attackTimer.reset();
        placeTimer.reset();
    }

    @EventTarget
    private void onPlace(EventPlace eventPlace) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        HitResult hitResult = mc.hitResult;

        if (hitResult != null && !mc.player.isUsingItem()) {
            switch (hitResult.getType()) {
                case MISS -> {

                    if (mc.options.keyAttack.isDown()) {
                        if (attackTimer.hasTimePassed((long) (1000.0 / (lCPS.getValue().intValue() * 1.5)))) {
                            mc.player.swing(InteractionHand.MAIN_HAND);
                            attackTimer.reset();
                        }
                    }
                }
                case ENTITY -> {
                    if (mc.options.keyAttack.isDown()) {
                        if (attackTimer.hasTimePassed((long) (1000.0 / (lCPS.getValue().intValue() * 1.5)))) {
                            mc.gameMode.attack(mc.player, ((EntityHitResult) hitResult).getEntity());
                            mc.player.swing(InteractionHand.MAIN_HAND);
                            attackTimer.reset();
                        }
                    }
                }
                case BLOCK -> {
                    if (mc.options.keyUse.isDown()) {
                        for(InteractionHand interactionhand : InteractionHand.values()) {
                            ItemStack itemstack = mc.player.getItemInHand(interactionhand);
                            if (!itemstack.isItemEnabled(mc.level.enabledFeatures())) {
                                return;
                            }

                            BlockHitResult blockhitresult = (BlockHitResult) hitResult;

                            if (placeTimer.hasTimePassed((long) (1000.0 / (rCPS.getValue().intValue() * 1.5)))) {
                                if (mc.gameMode.useItemOn(mc.player, interactionhand, blockhitresult) == InteractionResult.SUCCESS) {
                                    mc.player.swing(interactionhand);
                                }
                                placeTimer.reset();
                            }
                        }
                    }
                }
            }
        }
    }
}
