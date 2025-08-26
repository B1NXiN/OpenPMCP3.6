package pmcp.utils.item;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import pmcp.utils.client.Helper;
import pmcp.utils.client.MinecraftInstance;

import java.util.Iterator;

public final class InventoryUtils extends MinecraftInstance {
    public static boolean isBestBow(AbstractContainerMenu handler, ItemStack itemStack) {
        double bestBowDmg = -1.0;
        ItemStack bestBow = ItemStack.EMPTY;

        for (int i = 9; i < 45; i++) {
            ItemStack stack = handler.getSlot(i).getItem();
            if (!stack.isEmpty() && stack.getItem() instanceof BowItem) {
                double damage = getBowDamage(stack);
                if (damage > bestBowDmg) {
                    bestBow = stack;
                    bestBowDmg = damage;
                }
            }
        }

        return itemStack.equals(bestBow) || getBowDamage(itemStack) > bestBowDmg;
    }

    public static double getBowDamage(ItemStack stack) {
        double damage = 0.0;
        if (stack.getItem() instanceof BowItem && stack.hasFoil()) {
            damage += getLevel(Enchantments.POWER_ARROWS, stack);
        }

        return damage;
    }

    public static boolean isBestProjecitle(AbstractContainerMenu handler,ItemStack stack) {
        if (stack.isEmpty()) return false;

        for (int i = 9; i < 45; i++) {
            ItemStack stack1 = handler.getSlot(i).getItem();

            if (stack1.isEmpty()) continue;

            if (stack1.getItem() == Items.EGG) {
                if (stack1.getCount() == stack.getCount()) return true;

                return stack.getCount() > stack1.getCount();
            }

            if (stack1.getItem() == Items.SNOWBALL) {
                if (stack1.getCount() == stack.getCount()) return true;

                return stack.getCount() > stack1.getCount();
            }
        }
        return true;
    }

    public static boolean isBestTool(AbstractContainerMenu handler, ItemStack itemStack) {
        int type = getToolType(itemStack);
        Tool bestTool = new Tool(-1, -1.0, ItemStack.EMPTY);

        for (int i = 9; i < 45; i++) {
            ItemStack stack = handler.getSlot(i).getItem();
            if (!stack.isEmpty() && stack.getItem() instanceof DiggerItem && type == getToolType(stack)) {
                double efficiency = getToolScore(stack);
                if (efficiency > (double)getToolScore(bestTool.getItem())) {
                    bestTool = new Tool(i, efficiency, stack);
                }
            }
        }

        return bestTool.getItem().equals(itemStack) || getToolScore(itemStack) > getToolScore(bestTool.getItem());
    }

    public static boolean isBestBlock(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem item && (
                item == Items.STONE || item == Items.OAK_PLANKS || item == Items.OAK_LOG || item == Items.WHITE_WOOL || item == Items.BLACK_WOOL ||
                        item == Items.RED_WOOL || item == Items.GREEN_WOOL || item == Items.BROWN_WOOL || item == Items.BLUE_WOOL ||
                        item == Items.PURPLE_WOOL || item == Items.CYAN_WOOL || item == Items.LIGHT_GRAY_WOOL || item == Items.GRAY_WOOL ||
                        item == Items.PINK_WOOL || item == Items.LIME_WOOL || item == Items.YELLOW_WOOL || item == Items.LIGHT_BLUE_WOOL ||
                        item == Items.MAGENTA_WOOL || item == Items.ORANGE_WOOL);
    }

    public static float getToolScore(ItemStack stack) {
        float score = 0.0F;
        Item item = stack.getItem();
        if (item instanceof DiggerItem tool) {
            if (item instanceof PickaxeItem) {
                score = tool.getDestroySpeed(stack, Blocks.STONE.defaultBlockState()) - 0.0F;
            } else {
                if (!(item instanceof AxeItem)) {
                    return 1.0F;
                }

                score = tool.getDestroySpeed(stack, Blocks.DARK_OAK_LOG.defaultBlockState());
            }

            score += (float)getLevel(Enchantments.BLOCK_EFFICIENCY, stack) * 0.0075F;
            score += (float)getLevel(Enchantments.BLOCK_EFFICIENCY, stack) / 100.0F;
            score += (float) getLevel(Enchantments.SHARPNESS, stack);
        }

        return score;
    }

    public static boolean isBestSword(AbstractContainerMenu c, ItemStack itemStack) {
        double damage = 0.0;
        ItemStack bestStack = ItemStack.EMPTY;

        for (int i = 9; i < 45; i++) {
            ItemStack stack = c.getSlot(i).getItem();
            if (!stack.isEmpty() && (stack.getItem() instanceof SwordItem || stack.getItem() instanceof TridentItem)) {
                double newDamage = getItemDamage(stack);
                if (newDamage > damage) {
                    damage = newDamage;
                    bestStack = stack;
                }
            }
        }

        return bestStack.equals(itemStack) || getItemDamage(itemStack) > damage;
    }


    public static boolean isBestArmor(AbstractContainerMenu c, ItemStack itemStack) {
        ArmorItem itemArmor = (ArmorItem)itemStack.getItem();
        double reduction = 0.0;
        ItemStack bestStack = ItemStack.EMPTY;

        for (int i = 5; i < 45; i++) {
            ItemStack stack = c.getSlot(i).getItem();
            if (!stack.isEmpty()) {
                Item newReduction = stack.getItem();
                if (newReduction instanceof ArmorItem stackArmor) {
                    if (stackArmor.getEquipmentSlot().getFilterFlag() - 1 == itemArmor.getEquipmentSlot().getFilterFlag() - 1) {
                        double newReductionx = getDamageReduction(stack);
                        if (newReductionx > reduction) {
                            reduction = newReductionx;
                            bestStack = stack;
                        }
                    }
                }
            }
        }

        return bestStack.equals(itemStack) || getDamageReduction(itemStack) > reduction;
    }

    public static double getDamageReduction(ItemStack stack) {
        double reduction = 0.0;
        if (!(stack.getItem() instanceof ArmorItem armor)) {
            return 0.0;
        } else {
            reduction += armor.getDefense();
            if (stack.hasFoil()) {
                reduction += (double)getLevel(Enchantments.ALL_DAMAGE_PROTECTION, stack) * 0.25;
            }

            return reduction;
        }
    }

    public static double getItemDamage(ItemStack stack) {
        double damage = 0.0;
        Multimap<Attribute, AttributeModifier> attributeModifierMap = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);

        for (Attribute attributeName : attributeModifierMap.keySet()) {
            if (attributeName.getDescriptionId().equals("attribute.name.generic.attack_damage")) {
                Iterator<AttributeModifier> attributeModifiers = attributeModifierMap.get(attributeName).iterator();
                if (attributeModifiers.hasNext()) {
                    damage += attributeModifiers.next().getAmount();
                }
                break;
            }
        }

        if (stack.hasFoil()) {
            damage += EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, stack);
            damage += (double) EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, stack) * 0.5;
        }

        return damage;
    }

    private static int getLevel(Enchantment registryKey, ItemStack itemStack) {
        return EnchantmentHelper.getItemEnchantmentLevel(registryKey, itemStack);
    }

    public static int getToolType(ItemStack stack) {
        DiggerItem tool = (DiggerItem)stack.getItem();
        if (tool instanceof PickaxeItem) {
            return 0;
        } else {
            return tool instanceof AxeItem ? 1 : -1;
        }
    }

    public static int getBlockSlot() {
        if (mc.player == null) return -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.containerMenu.getSlot(i + 36).getItem();

            if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) continue;

            return i;
        }
        return -1;
    }

    public static int getPotion_HealingSlot() {
        if (mc.player == null) return -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.containerMenu.getSlot(i + 36).getItem();

            if (stack.isEmpty() || !stack.getItem().getDescriptionId().equals("item.minecraft.splash_potion")) continue;

            return i;
        }
        return -1;
    }

    public static int getBlockIndex(AbstractContainerMenu handler) {
        int blockCount = 0;
        for (int slotIndex = 9; slotIndex < 45; slotIndex++) {
            ItemStack stack = handler.getSlot(slotIndex).getItem();

            if (stack.getItem() instanceof BlockItem) {
                blockCount += stack.getCount();
            }
        }
        return blockCount;
    }

    public static int getBlockIndex() {
        if (mc.player == null) return -1;

        int blockCount = 0;
        for (int slotIndex = 9; slotIndex < 45; slotIndex++) {
            ItemStack stack = mc.player.containerMenu.getSlot(slotIndex).getItem();

            if (stack.getItem() instanceof BlockItem) {
                blockCount += stack.getCount();
            }
        }
        return blockCount;
    }

    public static int getPailSlot() {
        if (mc.player == null) return -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.containerMenu.getSlot(i + 36).getItem();

            if (stack.isEmpty() || !stack.getItem().getDescriptionId().equals("item.minecraft.water_bucket")) continue;

            return i;
        }
        return -1;
    }

    public static void putItemInSlot(int slot, int slotIn) {
        if (mc.gameMode == null) return;

        mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, slotIn, slot == 45 ? 40 : slot - 36, ClickType.SWAP, mc.player);
    }

    public static void putItemInSlot(int slot) {
        if (mc.gameMode == null) return;

        mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, slot, 0, ClickType.QUICK_MOVE, mc.player);
    }

    public static void putItemInSlotOFF(int slot, int slotIn) {
        if (mc.gameMode == null) return;

        mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, slot, 0, ClickType.PICKUP, mc.player);
        mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, slotIn, 0, ClickType.PICKUP, mc.player);
    }

    private record Tool(int slot, double efficiency, ItemStack stack) {

        public ItemStack getItem() {
                return this.stack;
            }
        }
}