package pmcp.mode.combat;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import pmcp.event.EventTarget;
import pmcp.event.mode.player.EventMotion;
import pmcp.event.mode.update.EventUpdate;
import pmcp.mode.Mode;
import pmcp.mode.player.InvManager;
import pmcp.utils.math.MathHelper;
import pmcp.utils.category.Category;
import pmcp.utils.item.InventoryUtils;
import pmcp.utils.player.EntityUtils;
import pmcp.utils.rotation.SilentRotation;
import pmcp.utils.time.MSTimer;
import pmcp.value.values.NumberValue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AutoProjecitle extends Mode {

    private final NumberValue<Number> range = new NumberValue<>(this, "MaxRange", 8.0,4.0,15.0,0.1);
    private final NumberValue<Number> minRange = new NumberValue<>(this, "MinRange", 3.0,1.0,10.0,0.1);
    private final NumberValue<Number> delay = new NumberValue<>(this, "Delay", 5,0,20,1);

    public AutoProjecitle() {
        super("AutoProjecitle", "自动投掷物", "自动扔副手的雪球或者鸡蛋", Category.COMBAT);
    }

    private Player target;
    private final MSTimer timer = new MSTimer();

    @Override
    public void onEnable() {
        timer.reset();
        target = null;
    }

    @Override
    public void onDisable() {
        target = null;
    }

    @EventTarget
    private void onMotion(EventMotion eventMotion) {
        if (mc.player == null || mc.level == null) return;

        if (eventMotion.getType() == EventMotion.Type.Pre) {
            List<Player> livingEntities = getListEntity();

            if (livingEntities.isEmpty()) return;

            livingEntities.sort(Comparator.comparingDouble(vec3 -> {
                final double d0 = mc.player.getX() - vec3.getPosition().x;
                final double d1 = mc.player.getY() - vec3.getPosition().y;
                final double d2 = mc.player.getZ() - vec3.getPosition().z;
                return MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
            }));

            target = livingEntities.getFirst();
        }
        if (eventMotion.getType() == EventMotion.Type.Post) {
            if (target != null) {
                if (target.isRemoved() || mc.player.distanceTo(target) > range.getValue().doubleValue() || mc.player.distanceTo(target) <= minRange.getValue().doubleValue()) {
                    target = null;
                }
            }
        }
    }

    @EventTarget
    private void onUpdate(EventUpdate eventUpdate) {

        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        HitResult hitResult;

        if (mc.screen instanceof InventoryScreen) {
            if (!InvManager.Instance.isEnable() || !InvManager.Instance.offHand.getValue()) {
                if (mc.player.getOffhandItem().isEmpty() || !InventoryUtils.isBestProjecitle(mc.player.containerMenu, mc.player.getOffhandItem())) {

                    int egg = 0;
                    int eggSlot = -1;
                    int snowball = 0;
                    int snowballSlot = -1;

                    for (int i = 9; i < 45; i++) {
                        ItemStack stack = mc.player.containerMenu.getSlot(i).getItem();

                        if (stack.isEmpty()) continue;

                        if (stack.getItem() == Items.EGG) {
                            egg = stack.getCount();
                            eggSlot = i;
                            break;
                        }

                        if (stack.getItem() == Items.SNOWBALL) {
                            snowball = stack.getCount();
                            snowballSlot = i;
                            break;
                        }
                    }

                    if (egg > 0) {
                        InventoryUtils.putItemInSlot(45, eggSlot);
                    } else if (egg > snowball) {
                        InventoryUtils.putItemInSlot(45, eggSlot);
                    } else if (snowball > egg) {
                        InventoryUtils.putItemInSlot(45, snowballSlot);
                    }
                }
            }
        }

        if (timer.hasTimePassed(delay.getValue().intValue() * 100L)) {
            if (target != null) {
                if (!mc.player.getOffhandItem().isEmpty() && (mc.player.getOffhandItem().getItem() == Items.EGG || mc.player.getOffhandItem().getItem() == Items.SNOWBALL)) {
                    hitResult = mc.hitResult;

                    if (hitResult.getType() == HitResult.Type.MISS) {
                        Vec3 vec34 = mc.player.calculateViewVector(SilentRotation.getRotations().getPitch(), SilentRotation.getRotations().getYaw()).scale(range.getValue().doubleValue());
                        Vec3 vec3 = mc.player.getEyePosition();
                        AABB aabb = mc.player.getBoundingBox().expandTowards(vec34).inflate(1.0D);

                        Vec3 vec32 = vec3.add(vec34);
                        EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(mc.player, vec3, vec32, aabb, (entityIn) -> {
                            return !entityIn.isSpectator() && entityIn.isPickable();
                        }, range.getValue().doubleValue() * range.getValue().doubleValue());

                        hitResult = entityhitresult;
                    }

                    if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                        mc.gameMode.useItem(mc.player, InteractionHand.OFF_HAND);
                        mc.player.swing(InteractionHand.OFF_HAND);
                        target = null;
                        timer.reset();
                    }
                }
            }
        }
    }


    private List<Player> getListEntity() {
        final List<Player> entities = new ArrayList<>();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (mc.player.distanceTo(entity) > range.getValue().doubleValue()) continue;

            if (entity instanceof Player player && EntityUtils.isValidTarget(player, mc)) {
                entities.add(player);
            }
        }

        return entities;
    }
}
