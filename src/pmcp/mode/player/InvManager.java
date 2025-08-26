package pmcp.mode.player;

import jnic.JNICInclude;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import pmcp.event.EventTarget;
import pmcp.event.mode.player.EventMotion;
import pmcp.mode.Mode;
import pmcp.utils.block.BlockUtils;
import pmcp.utils.category.Category;
import pmcp.utils.item.InventoryUtils;
import pmcp.utils.time.MSTimer;
import pmcp.value.values.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static pmcp.utils.client.OldNaming.C0APacketAnimation;
import static pmcp.utils.item.InventoryUtils.*;

public class InvManager extends Mode {

    public static InvManager Instance;

    private final BooleanValue<Boolean> InvOpen = new BooleanValue<>(this,"InvOpen", true);
    private final BooleanValue<Boolean> swing = new BooleanValue<>(this,"Swing", true);
    public final BooleanValue<Boolean> offHand = new BooleanValue<>(this,"Offhand Gapple", true);
    private final BooleanValue<Boolean> Savethrow = new BooleanValue<>(this,"Throw", false);
    private final BooleanValue<Boolean> NoMove = new BooleanValue<>(this,"NoMove", false);
    public final NumberValue<Number> delay = new NumberValue<>(this,"ItemDelay", 0, 1, 10, 1);
    public final NumberValue<Number> blocks = new NumberValue<>(this,"Blocks", 128, 16, 512, 1);
    public final NumberValue<Number> slotWeapon = new NumberValue<>(this,"Weapon Slot", 1, 1, 9, 1);
    public final NumberValue<Number> slotPick = new NumberValue<>(this,"Pickaxe Slot", 2, 1, 9, 1);
    public final NumberValue<Number> slotAxe = new NumberValue<>(this,"Axe Slot", 3, 1, 9, 1);
    public final NumberValue<Number> slotGapple = new NumberValue<>(this,"Gapple Slot", 4, 1, 9, 1);
    public final NumberValue<Number> slotWater = new NumberValue<>(this,"Water Slot", 5, 1, 9, 1);
    public final NumberValue<Number> slotBow = new NumberValue<>(this,"Bow Slot", 6, 1, 9, 1);
    public final NumberValue<Number> slotBlock = new NumberValue<>(this,"Block Slot", 7, 1, 9, 1);
    public final NumberValue<Number> slotPearl = new NumberValue<>(this,"Pearl Slot", 8, 1, 9, 1);

    public InvManager() {
        super("InvManager", "背包管理器", "自动整理好你的背包", Category.PLAYER);
        Instance = this;
    }

    public final String[] serverItems = new String[]{"选择游戏", "加入游戏", "职业选择菜单", "离开对局", "再来一局", "selector", "tracking compass", "(right click)", "tienda ", "perfil", "salir", "shop", "collectibles", "game", "profil", "lobby", "show all", "hub", "friends only", "cofre", "(click", "teleport", "play", "exit", "hide all", "jeux", "gadget", " (activ", "emote", "amis", "bountique", "choisir", "choose "};
    private final int[] bestArmorPieces = new int[6];
    private final List<Integer> trash = new ArrayList<>();
    private final int[] bestToolSlots = new int[2];
    private final List<Integer> gappleStackSlots = new ArrayList<>();
    private final MSTimer timer = new MSTimer();
    private int bestSwordSlot;
    private int bestPearlSlot;
    private int bestBowSlot;
    private int bestWaterSlot;
    private boolean nextTickCloseInventory;

    @Override
    public void onEnable() {
        timer.reset();
    }

    private boolean dropItem(List<Integer> listOfSlots) {
        if (!listOfSlots.isEmpty()) {
            int slot = listOfSlots.removeFirst();
            mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, slot, 1, ClickType.THROW, mc.player);
            if (swing.getValue()) {
                mc.player.connection.send(C0APacketAnimation(InteractionHand.MAIN_HAND));
            }

            return true;
        } else {
            return false;
        }
    }

    @EventTarget
    private void onMotion(EventMotion event) {
        setSuffix("OpenInv");
        if (mc.player == null || mc.gameMode == null || event.getType() == EventMotion.Type.Post || mc.player.isSpectator() || NoMove.getValue() && isMoving()) {
            return;
        }

        if (InvOpen.getValue() && !(mc.screen instanceof InventoryScreen)) return;

        if (!InvOpen.getValue() && !(mc.screen == null)) return;

        clear();

        for (int slot = 5; slot < 45; slot++) {
            ItemStack stack = mc.player.containerMenu.getSlot(slot).getItem();
            AbstractContainerMenu handler = mc.player.containerMenu;
            if (!stack.isEmpty()) {
                if ((stack.getItem() instanceof SwordItem || stack.getItem() instanceof TridentItem) && InventoryUtils.isBestSword(handler, stack)) {
                    bestSwordSlot = slot;
                } else if (stack.getItem() instanceof DiggerItem && InventoryUtils.isBestTool(handler, stack)) {
                    int toolType = InventoryUtils.getToolType(stack);
                    if (toolType != -1 && slot != bestToolSlots[toolType]) {
                        bestToolSlots[toolType] = slot;
                    }
                } else {
                    Item armorSlot = stack.getItem();
                    if (armorSlot instanceof ArmorItem armor) {
                        if (InventoryUtils.isBestArmor(handler, stack)) {
                            EquipmentSlot armorSlotx = armor.getEquipmentSlot();
                            int index = armorSlotx.ordinal();
                            if (index >= 1 && index < bestArmorPieces.length + 2) {
                                int pieceSlot = bestArmorPieces[index];
                                if (pieceSlot == -1 || slot != pieceSlot) {
                                    bestArmorPieces[index] = slot;
                                }
                            }
                            continue;
                        }
                    }

                    if (!stack.isEmpty() && stack.getItem() instanceof BlockItem && (InventoryUtils.getBlockIndex() > blocks.getValue().intValue() || !isBestBlock(stack)) && !trash.contains(slot)) {
                        trash.add(slot);
                    }

                    if (!(stack.getItem() instanceof BowItem) || !InventoryUtils.isBestBow(handler, stack)) {
                        if (stack.getItem() == Items.GOLDEN_APPLE) {
                            gappleStackSlots.add(slot);
                        } else if (stack.getItem() == Items.ENDER_PEARL) {
                            bestPearlSlot = slot;
                        } else if (stack.getItem() == Items.WATER_BUCKET) {
                            if (slot != bestWaterSlot) {
                                bestWaterSlot = slot;
                            }
                        } else if (!trash.contains(slot) && !isValidStack(stack)) {
                            trash.add(slot);
                        }
                    } else if (slot != bestBowSlot) {
                        bestBowSlot = slot;
                    }
                }
            }
        }

        boolean busy = !trash.isEmpty() || equipArmor(false) || sortItems(false);
        if (!busy) {
            if (nextTickCloseInventory) {
                mc.player.connection.send(new ServerboundContainerClosePacket(mc.player.inventoryMenu.containerId));

                nextTickCloseInventory = false;
            } else {
                nextTickCloseInventory = true;
            }

            return;
        }

        if (nextTickCloseInventory) {
            nextTickCloseInventory = false;
        }

        if (timer.hasTimePassed(delay.getValue().intValue() * 10L)) {

            if (equipArmor(true)) {
                timer.reset();
                return;
            }

            if (dropItem(trash)) {
                timer.reset();
                return;
            }

            if (sortItems(true)) {
                timer.reset();

            }
        }
    }

    private boolean sortItems(boolean moveItems) {
        if (mc.player == null) return false;

        int goodSwordSlot = slotWeapon.getValue().intValue() + 35;
        if (bestSwordSlot != -1 && bestSwordSlot != goodSwordSlot) {
            if (moveItems) {
                putItemInSlot(goodSwordSlot, bestSwordSlot);
                bestSwordSlot = goodSwordSlot;
            }

            return true;
        } else {
            int goodBowSlot = slotBow.getValue().intValue() + 35;
            if (bestBowSlot != -1 && bestBowSlot != goodBowSlot) {
                if (moveItems) {
                    putItemInSlot(goodBowSlot, bestBowSlot);
                    bestBowSlot = goodBowSlot;
                }

                return true;
            } else {
                int goodWaterSlot = slotWater.getValue().intValue() + 35;
                if (bestWaterSlot != -1 && bestWaterSlot != goodWaterSlot) {
                    if (moveItems) {
                        putItemInSlot(goodWaterSlot, bestWaterSlot);
                        bestWaterSlot = goodWaterSlot;
                    }

                    return true;
                } else {
                    int goodGappleSlot = slotGapple.getValue().intValue() + 35;
                    if (offHand.getValue()) {
                        if (!gappleStackSlots.isEmpty()) {
                            gappleStackSlots.sort((slot1, slot2) -> {
                                int count1 = mc.player.containerMenu.getSlot(slot1).getItem().getCount();
                                int count2 = mc.player.containerMenu.getSlot(slot2).getItem().getCount();
                                return Integer.compare(count1, count2);
                            });
                            int bestGappleSlot = gappleStackSlots.getFirst();
                            if (bestGappleSlot != 45) {
                                if (moveItems) {
                                    if (mc.player.containerMenu.getSlot(45).getItem().isEmpty()) {
                                        putItemInSlot(45, bestGappleSlot);
                                        gappleStackSlots.set(0, 45);
                                    } else {
                                        putItemInSlotOFF(bestGappleSlot, 45);
                                        gappleStackSlots.set(0, 45);
                                    }
                                }

                                return true;
                            }
                        }
                    } else if (!gappleStackSlots.isEmpty()) {
                        gappleStackSlots.sort((slot1, slot2) -> {
                            int count1 = mc.player.containerMenu.getSlot(slot1).getItem().getCount();
                            int count2 = mc.player.containerMenu.getSlot(slot2).getItem().getCount();
                            return Integer.compare(count1, count2);
                        });
                        int bestGappleSlot = gappleStackSlots.getFirst();
                        if (bestGappleSlot != goodGappleSlot) {
                            if (moveItems) {
                                putItemInSlot(goodGappleSlot, bestGappleSlot);
                                gappleStackSlots.set(0, goodGappleSlot);
                            }

                            return true;
                        }
                    }

                    int[] toolSlots = new int[]{slotPick.getValue().intValue() + 35, slotAxe.getValue().intValue() + 35};

                    for (int toolSlot : bestToolSlots) {
                        if (toolSlot != -1) {
                            int type = InventoryUtils.getToolType(mc.player.containerMenu.getSlot(toolSlot).getItem());
                            if (type != -1 && toolSlot != toolSlots[type]) {
                                if (moveItems) {
                                    int toolSlotqwq = toolSlots[type];
                                    mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, bestToolSlots[type], toolSlotqwq - 36, ClickType.SWAP, mc.player);
                                    bestToolSlots[type] = toolSlotqwq;
                                }

                                return true;
                            }
                        }
                    }

                    int goodBlockSlot = slotBlock.getValue().intValue() + 35;
                    int mostBlocksSlot = getMostBlocks();
                    if (mostBlocksSlot != -1 && mostBlocksSlot != goodBlockSlot) {
                        Slot dss = mc.player.containerMenu.getSlot(goodBlockSlot);
                        ItemStack dsis = dss.getItem();
                        if (dsis.isEmpty()
                                || !(dsis.getItem() instanceof BlockItem)
                                || dsis.getCount() < mc.player.containerMenu.getSlot(mostBlocksSlot).getItem().getCount()) {
                            putItemInSlot(goodBlockSlot, mostBlocksSlot);
                        }
                    }

                    int goodPearlSlot = slotPearl.getValue().intValue() + 35;
                    if (bestPearlSlot != -1 && bestPearlSlot != goodPearlSlot) {
                        if (moveItems) {
                            putItemInSlot(goodPearlSlot, bestPearlSlot);
                            bestPearlSlot = goodPearlSlot;
                        }

                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
    }

    public int getMostBlocks() {
        int stack = 0;
        int biggestSlot = -1;

        for (int i = 9; i < 45; i++) {
            Slot slot = mc.player.containerMenu.getSlot(i);
            ItemStack is = slot.getItem();
            if (!is.isEmpty() && is.getItem() instanceof BlockItem && is.getCount() > stack) {
                boolean noneMatch = true;
                String itemName = is.getItem().getName(is).getString().toLowerCase();

                for (String serverItem : serverItems) {
                    if (itemName.contains(serverItem.toLowerCase())) {
                        noneMatch = false;
                        break;
                    }
                }

                if (noneMatch) {
                    stack = is.getCount();
                    biggestSlot = i;
                }
            }
        }

        return biggestSlot;
    }

    private boolean equipArmor(boolean moveItems) {
        for (int i = 0; i < bestArmorPieces.length; i++) {
            int piece = bestArmorPieces[i];
            if (piece != -1) {
                int armorPieceSlot = getArmorSlot(EquipmentSlot.values()[i]);
                if (armorPieceSlot >= 0 && armorPieceSlot < mc.player.containerMenu.slots.size()) {
                    ItemStack stack = mc.player.containerMenu.getSlot(armorPieceSlot).getItem();
                    if (stack.isEmpty()) {
                        if (moveItems) {
                            mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, piece, 0, ClickType.QUICK_MOVE, mc.player);
                        }

                        return true;
                    }
                }
            }
        }

        return false;
    }

    private int getArmorSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 5;
            case CHEST -> 6;
            case LEGS -> 7;
            case FEET -> 8;
            default -> -1;
        };
    }

    private boolean isValidStack(ItemStack stack) {
        Item item = stack.getItem();

        if (item == Items.TOTEM_OF_UNDYING ||
                item == Items.COMPASS ||
                item == Items.SLIME_BALL ||
                item == Items.END_CRYSTAL ||
                item == Items.FIRE_CHARGE ||
                item == Items.TNT ||
                item == Items.ARROW ||
                (item == Items.SNOWBALL || item == Items.EGG) && Savethrow.getValue()) {
            return true;
        }

        if (item instanceof BlockItem && BlockUtils.isValidBlock(((BlockItem) item).getBlock())) {
            return true;
        }

        if (item instanceof PotionItem) {
            return true;
        }

        return item.getFoodProperties() != null;
    }

    @Override
    public void onDisable() {
        clear();
    }

    private void clear() {
        trash.clear();
        bestBowSlot = -1;
        bestSwordSlot = -1;
        bestWaterSlot = -1;
        gappleStackSlots.clear();
        Arrays.fill(bestArmorPieces, -1);
        Arrays.fill(bestToolSlots, -1);
    }

    public boolean isMoving() {
        return mc.player != null && (mc.player.input.forwardImpulse != 0f || mc.player.input.leftImpulse != 0f);
    }
}
