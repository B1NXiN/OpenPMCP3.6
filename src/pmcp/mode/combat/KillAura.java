package pmcp.mode.combat;

import jnic.JNICInclude;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import pmcp.PMCP;
import pmcp.event.EventTarget;
import pmcp.event.mode.player.EventMotion;
import pmcp.event.mode.update.EventTick;
import pmcp.event.mode.update.EventUpdate;
import pmcp.mode.Mode;
import pmcp.mode.move.Fly;
import pmcp.mode.player.Blink;
import pmcp.mode.world.Scaffold;
import pmcp.utils.math.MathHelper;
import pmcp.utils.category.Category;
import pmcp.utils.category.MoveCategory;
import pmcp.utils.category.NumberCategory;
import pmcp.utils.client.RandomUtils;
import pmcp.utils.player.EntityUtils;
import pmcp.utils.rotation.Rotation;
import pmcp.utils.rotation.RotationUtils;
import pmcp.utils.rotation.SilentRotation;
import pmcp.utils.time.MSTimer;
import pmcp.value.values.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static pmcp.utils.client.OldNaming.C07PacketPlayerDigging;

@JNICInclude
public class KillAura extends Mode {

    public static KillAura Instance;

    private final BooleanValue<Boolean> legitAttack = new BooleanValue<>(this, "LegitAttack", false);
    private final BooleanValue<Boolean> rayTrace = new BooleanValue<>(this, "RayTrace", true);
    private final BooleanValue<Boolean> attackFix = new BooleanValue<>(this, "AttackFix", false);
    public final NumberValue<Number> maxCPS = new NumberValue<>(this, "MaxCPS", 10,1,20,1, NumberCategory.MAX, "CPS", () -> !legitAttack.getValue());
    public final NumberValue<Number> minCPS = new NumberValue<>(this, "MinCPS", 10,1,20,1, NumberCategory.MIN, "CPS", () -> !legitAttack.getValue());
    private final NumberValue<Number> cpsDelay = new NumberValue<>(this, "CPSDelay", 500,0,1000,10);
    private final NumberValue<Number> attackRange = new NumberValue<>(this, "Range", 3.3,1,8,0.1, NumberCategory.MIN, "Range");
    private final NumberValue<Number> throughWallsRange = new NumberValue<>(this, "ThroughWallsRange", 2.5, 0.1,8,0.1, NumberCategory.MIN, "Range", rayTrace::getValue);
    private final NumberValue<Number> rotationRange = new NumberValue<>(this, "RotationRange", 3.3,1,8,0.1, NumberCategory.MIN, "Range");
    private final NumberValue<Number> swingRange = new NumberValue<>(this, "SwingRange", 3.3,1,8,0.1, NumberCategory.MAX, "Range");

    private final ModeValue<attack> attackModeValue = new ModeValue<>(this, "AttackMode", attack.values(), attack.Switch);

    enum attack {
        Single,
        Switch
    }

    private final ModeValue<mode> rotModeValue = new ModeValue<>(this,"RotationMode", mode.values(), mode.silent);

    enum mode {
        Player,
        silent,
        OFF
    }

    private final ModeValue<block> blockMode = new ModeValue<>(this,"AutoBlock", block.values(), block.Fake);

    enum block {
        OFF,
        Fake,
        Normal
    }

    private final ModeValue<MoveCategory> moveValue = new ModeValue<>(this, "MoveMode", MoveCategory.values(), MoveCategory.Silent, () -> rotModeValue.getValue().equals(mode.silent));

    private final NumberValue<Number> switchDelay = new NumberValue<>(this, "SwitchDelay", 0,0,1000,50, () -> attackModeValue.getValue().equals(attack.Switch));

    private final BooleanValue<Boolean> playerValue = new BooleanValue<>(this, "Player", true);
    private final BooleanValue<Boolean> mobValue = new BooleanValue<>(this, "Mob", true);

    public KillAura() {
        super("KillAura", "杀戮光环", "攻击你附近的所有选择的实体", Category.COMBAT);
        Instance = this;
    }

    public static Entity target;
    private static Entity oldTarget;
    public static boolean blocking;
    public static List<LivingEntity> targets = new ArrayList<>();
    private final MSTimer switchTimer = new MSTimer();
    private final MSTimer attackTimer = new MSTimer();
    private final MSTimer cpsTimer = new MSTimer();
    private int index;
    private int cps;

    @Override
    public void onEnable() {
        target = null;
        blocking = false;

        index = 0;
        switchTimer.reset();
        attackTimer.reset();
        cpsTimer.reset();
        cps = maxCPS.getValue().intValue();
    }

    @Override
    public void onDisable() {
        target = null;

        if (mc.player == null || mc.level == null) return;

        if (blocking) {
            stopBlock();
        }
    }

    private void stopBlock() {
        switch (blockMode.getValue()) {
            case Fake -> blocking = false;

            case Normal -> {
                if (mc.player.getMainHandItem().getItem() instanceof SwordItem) {
                    mc.player.connection.send(C07PacketPlayerDigging(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
                }
                blocking = false;
            }
        }
    }

    @EventTarget
    private void onTick(EventTick tick) {

        int oldCPS = RandomUtils.next(minCPS.getValue().intValue(), maxCPS.getValue().intValue());

        if (cpsTimer.hasTimePassed(cpsDelay.getValue().intValue()) && oldCPS != cps) {
            cps = oldCPS;
            cpsTimer.reset();
        }
    }

    @EventTarget
    private void onUpdate(EventUpdate event) {
        setSuffix(attackModeValue.getValue().name());

        if (mc.player == null || mc.level == null) return;

        targets = getListEntity();

        if (!targets.isEmpty()) {
            if (this.index >= targets.size()) {
                this.index = 0;
            }


            switch (attackModeValue.getValue()) {
                case Single -> {
                    targets.sort(Comparator.comparingDouble(vec3 -> {
                        final double d0 = mc.player.getX() - vec3.getPosition().x;
                        final double d1 = mc.player.getY() - vec3.getPosition().y;
                        final double d2 = mc.player.getZ() - vec3.getPosition().z;
                        return MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    }));

                    target = targets.getFirst();
                }

                case Switch -> target = targets.get(this.index);
            }
        }

        if (targets.isEmpty()
                || Blink.Instance.isEnable()
                || mc.player.isUsingItem()
                || (Scaffold.isScaffold && PMCP.Instance.getMode(Scaffold.class).isEnable())
                || (Fly.Instance.isEnable() && Fly.Instance.modeValue.getValue().equals(Fly.mode.LongJump))
                || attackFix.getValue() && !(mc.player.getMainHandItem().getItem() instanceof SwordItem || mc.player.getMainHandItem().getItem() instanceof AxeItem
        )
        ) {
            stopBlock();
            target = null;
        }

        if (target != null) {
            if (mc.player.distanceTo(target) < rotationRange.getValue().doubleValue()) {
                Rotation rotation = RotationUtils.getRotation(target.getEyePosition(), mc.player.getEyePosition());

                switch (rotModeValue.getValue()) {
                    case silent -> SilentRotation.setRotation(rotation, moveValue.getValue(), true);

                    case Player -> {
                        mc.player.setYRot(rotation.getYaw());
                        mc.player.setXRot(rotation.getPitch());
                    }
                }
            }

            HitResult hitResult = new EntityHitResult(target);

            if (rayTrace.getValue()) {
                if (mc.player.distanceTo(target) < throughWallsRange.getValue().doubleValue()) {
                    hitResult = new EntityHitResult(target);
                } else {
                    Vec3 v1 = mc.player.getEyePosition();

                    hitResult = switch (mc.player.level().clip(new ClipContext(v1, target.getPosition(), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player)).getType()) {
                        case BLOCK ->
                                mc.player.level().clip(new ClipContext(v1, target.getEyePosition(), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player));
                        case MISS, ENTITY ->
                                mc.player.level().clip(new ClipContext(v1, target.getPosition(), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player));
                    };

                    if (hitResult.getType() == HitResult.Type.MISS) {
                        Vec3 vec34 = mc.player.calculateViewVector(SilentRotation.getRotations().getPitch(), SilentRotation.getRotations().getYaw()).scale(attackRange.getValue().doubleValue());
                        Vec3 vec3 = mc.player.getEyePosition();
                        AABB aabb = mc.player.getBoundingBox().expandTowards(vec34).inflate(1.0D);

                        Vec3 vec32 = vec3.add(vec34);
                        EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(mc.player, vec3, vec32, aabb, (entityIn) -> {
                            return !entityIn.isSpectator() && entityIn.isPickable();
                        }, attackRange.getValue().doubleValue() * attackRange.getValue().doubleValue());

                        hitResult = entityhitresult;
                    }
                }
            }

            if (!legitAttack.getValue()) {
                if (mc.player.distanceTo(target) < swingRange.getValue().doubleValue() && mc.player.distanceTo(target) > attackRange.getValue().doubleValue()) {
                    if (attackTimer.hasTimePassed((long) (1000.0 / (cps * 1.5)))) {
                        mc.player.swing(InteractionHand.MAIN_HAND);
                        attackTimer.reset();
                    }
                } else if (mc.player.distanceTo(target) < attackRange.getValue().doubleValue()) {

                    if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                        if (attackTimer.hasTimePassed((long) (1000.0 / (cps * 1.5)))) {
                            mc.gameMode.attack(mc.player, ((EntityHitResult) hitResult).getEntity());
                            mc.player.swing(InteractionHand.MAIN_HAND);
                            attackTimer.reset();
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    private void onMotion(EventMotion eventMotion) {
        if (mc.player == null || mc.level == null) return;

        if (eventMotion.getType() == EventMotion.Type.Pre) {
            if (target != null) {
                switch (blockMode.getValue()) {
                    case Fake -> {
                        if (mc.player.getMainHandItem().getItem() instanceof SwordItem) {
                            blocking = true;
                        }
                    }

                    case Normal -> {
                        if (mc.player.getMainHandItem().getItem() instanceof SwordItem) {
                            blocking = true;
                            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                        }
                    }
                }
            }
        }

        if (eventMotion.getType() == EventMotion.Type.Post) {
            if (targets.size() > 1) {
                if (switchTimer.hasTimePassed(switchDelay.getValue().intValue())) {
                    ++this.index;
                    switchTimer.reset();
                }
            }
        }
    }

    private List<LivingEntity> getListEntity() {
        final List<LivingEntity> entities = new ArrayList<>();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (mc.player.distanceTo(entity) > swingRange.getValue().doubleValue()) continue;

            if ((playerValue.getValue() && entity instanceof Player player && EntityUtils.isValidTarget(player, mc)) || (EntityUtils.isMob(entity) && mobValue.getValue())) {
                entities.add((LivingEntity) entity);
            }
        }

        return entities;
    }
}
