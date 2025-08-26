package pmcp.mode.combat;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import pmcp.PMCP;
import pmcp.event.EventTarget;
import pmcp.event.mode.player.EventAttack;
import pmcp.event.mode.update.EventUpdate;
import pmcp.mode.Mode;
import pmcp.mode.world.Scaffold;
import pmcp.utils.category.Category;
import pmcp.utils.client.OldNaming;
import pmcp.value.values.ModeValue;
import pmcp.value.values.NumberValue;



public class ArmorBreak extends Mode {

    private final ModeValue<mode> modeValue = new ModeValue<>(this, "Mode", mode.values(), mode.Normal);

    enum mode {
        Normal,
        SharpBug
    }

    private final NumberValue<Double> Health = new NumberValue<>(this,"Health", 5.0, 0.1, 20.0, 0.1);
    private final ModeValue<sendMode> sendModeModeValue = new ModeValue<>(this, "SendMode", sendMode.values(), sendMode.C08UseItem, () -> modeValue.getValue().equals(mode.SharpBug));

    enum sendMode {
        C08UseItem,
        C17,
        None
    }

    public ArmorBreak() {
        super("ArmorBreak", "自动武器", "自动选择伤害最高的武器", Category.COMBAT);
    }

    @EventTarget
    private void onUpdate(EventUpdate update) {
        setSuffix(this.modeValue.getValue().toString());
    }

    @EventTarget
    public void onAttack(EventAttack eventAttack) {
        if (PMCP.Instance.getModeManager().getByClass(Scaffold.class).isEnable() || mc.player == null || mc.level == null || mc.gameMode == null) return;

        switch (modeValue.getValue()) {
            case Normal -> {
                if (eventAttack.getType() == EventAttack.Type.Pre) {
                    if (getCounter() >= 0 && getCounter() != mc.player.getInventory().selected) {
                        mc.player.getInventory().selected = getCounter();
                    }
                }
            }

            case SharpBug -> {
                if (mc.player.getHealth() > Health.getValue().floatValue() && (mc.player.getMainHandItem().getItem() instanceof SwordItem || mc.player.getMainHandItem().getItem() instanceof AxeItem)) {
                    if (eventAttack.getType() == EventAttack.Type.Pre) {
                        if (getGoodWeapon() >= 0) {
                            mc.player.connection.send(OldNaming.C09PacketHeldItemChange(getGoodWeapon()));
                            switch (sendModeModeValue.getValue()) {
                                case C17 -> mc.player.connection.send(OldNaming.C17CustomPayload(ResourceLocation.tryParse("bypass"), new FriendlyByteBuf(Unpooled.buffer())));
                                case C08UseItem -> mc.gameMode.useItem(mc.player, InteractionHand.OFF_HAND);
                            }
                        }
                    } else {
                        mc.player.connection.send(OldNaming.C09PacketHeldItemChange(mc.player.getInventory().selected));
                    }
                } else {
                    if (getCounter() >= 0 && getCounter() != mc.player.getInventory().selected) {
                        mc.player.getInventory().selected = getCounter();
                    }
                }
            }
        }
    }

    private int getCounter() {
        ItemStack itemAxe = null;
        int swordSlot = -1;
        int axeSlot = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.containerMenu.getSlot(i + 36).getItem();

            if (mc.player.containerMenu.getSlot(i + 36).getItem().isEmpty() || !(stack.getItem() instanceof AxeItem) || !(getDamage(stack) > 100))
                continue;

            itemAxe = stack;
            axeSlot = i;
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.containerMenu.getSlot(i + 36).getItem();

            if (mc.player.containerMenu.getSlot(i + 36).getItem().isEmpty() || !(stack.getItem() instanceof SwordItem) || !isBestSword(stack))
                continue;

            swordSlot = i;
        }

        if (mc.player.getHealth() <= Health.getValue().floatValue() || getPlayerSize() < 3 && getPlayerSize() != 0) {
            if (itemAxe != null) {

                return axeSlot;
            } else return swordSlot;
        } else return swordSlot;
    }

    private boolean isBestSword(ItemStack stack) {
        final float damage = getDamage(stack);

        for (int i = 9; i < 45; i++) {
            if (!mc.player.containerMenu.getSlot(i).getItem().isEmpty()) {
                final ItemStack is = mc.player.containerMenu.getSlot(i).getItem();
                if (getDamage(is) > damage && is.getItem() instanceof SwordItem) {
                    return false;
                }
            }
        }

        return stack.getItem() instanceof SwordItem;
    }

    private int getGoodWeapon() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.containerMenu.getSlot(i + 36).getItem();

            if (mc.player.containerMenu.getSlot(i + 36).getItem().isEmpty() || !(stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem) || i == mc.player.getInventory().selected)
                continue;

            if (stack.getItem() instanceof AxeItem) {
                if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, stack) > EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, mc.player.containerMenu.getSlot(mc.player.getInventory().selected + 36).getItem()) && getDamage(stack) < 100) {
                    return i;
                }
            }

            if (stack.getItem() instanceof SwordItem) {
                if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, stack) > EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, mc.player.containerMenu.getSlot(mc.player.getInventory().selected + 36).getItem())) {
                    return i;
                }
            }
        }

        return -1;
    }

    public float getDamage(ItemStack stack) {
        float damage = 0;
        final Item item = stack.getItem();

        if (item instanceof AxeItem) {
            damage += item.getMaxDamage();
        } else if (item instanceof SwordItem) {
            damage += item.getMaxDamage();
        }

        damage += EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, stack) * 1.25F + EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, stack) * 0.01F;
        return damage;
    }

    private int getPlayerSize() {
        if (mc.getConnection() != null) {
            return mc.getConnection().getOnlinePlayers().size() -1;
        }
        return 0;
    }
}
