package pmcp.utils.client;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import pmcp.utils.rotation.SilentRotation;

public class OldNaming extends MinecraftInstance {

    public static ServerboundInteractPacket C02PacketUseEntity(Entity target) {
        return ServerboundInteractPacket.createAttackPacket(target, false);
    }


    public static ServerboundMovePlayerPacket.StatusOnly C03PacketPlayer(boolean onGround) {
        return new ServerboundMovePlayerPacket.StatusOnly(onGround);
    }

    public static ServerboundMovePlayerPacket.StatusOnly C03PacketPlayer() {
        return new ServerboundMovePlayerPacket.StatusOnly(mc.player.onGround());
    }

    public static ServerboundMovePlayerPacket.Pos C04PacketPlayerPosition(double x, double y, double z, boolean onGround) {
        return new ServerboundMovePlayerPacket.Pos(x, y, z, onGround);
    }

    public static ServerboundMovePlayerPacket.Pos C04PacketPlayerPosition() {
        return new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.onGround());
    }

    public static ServerboundMovePlayerPacket.Rot C05PacketPlayerLook(float yaw, float pitch, boolean onGround) {
        return new ServerboundMovePlayerPacket.Rot(yaw, pitch, onGround);
    }

    public static ServerboundMovePlayerPacket.Rot C05PacketPlayerLook() {
        return new ServerboundMovePlayerPacket.Rot(SilentRotation.getRotations().getYaw(), SilentRotation.getRotations().getPitch(), mc.player.onGround());
    }

    public static ServerboundMovePlayerPacket.PosRot C06PacketPlayerPosLook(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        return new ServerboundMovePlayerPacket.PosRot(x, y, z, yaw, pitch, onGround);
    }

    public static ServerboundMovePlayerPacket.PosRot C06PacketPlayerPosLook() {
        return new ServerboundMovePlayerPacket.PosRot(mc.player.getX(), mc.player.getY(), mc.player.getZ(), SilentRotation.getRotations().getYaw(), SilentRotation.getRotations().getPitch(), mc.player.onGround());
    }

    public static ServerboundPlayerActionPacket C07PacketPlayerDigging(Action action, BlockPos pos, Direction direction) {
        return new ServerboundPlayerActionPacket(action, pos, direction);
    }

    public static ServerboundSetCarriedItemPacket C09PacketHeldItemChange(int slot) {
        return new ServerboundSetCarriedItemPacket(slot);
    }

    public static ServerboundSwingPacket C0APacketAnimation(InteractionHand hand) {
        return new ServerboundSwingPacket(hand);
    }

    public static ServerboundUseItemPacket C08UseItemPacket(InteractionHand pHand, int pSequence) {
        return new  ServerboundUseItemPacket(pHand, pSequence);
    }
    public static ServerboundUseItemOnPacket C08UseItemBlockPacket(InteractionHand pHand, BlockHitResult pBlockHit, int pSequence) {
        return new ServerboundUseItemOnPacket(pHand, pBlockHit, pSequence);
    }

    public static ServerboundContainerClickPacket C0EPacketClickWindow(int containerId, int stateId, int slotNum, int buttonNum, ClickType clickType, ItemStack item, Int2ObjectMap<ItemStack> changedSlots) {
        return new ServerboundContainerClickPacket(containerId, stateId, slotNum, buttonNum, clickType, item, changedSlots);
    }

    public static ServerboundContainerClosePacket C0DPacketCloseWindow(int containerId) {
        return new ServerboundContainerClosePacket(containerId);
    }

    public static ServerboundPongPacket C0FPacketConfirmTransaction(int id) {
        return new ServerboundPongPacket(id);
    }

    public static ServerboundPlayerCommandPacket C0BPacketEntityAction(Player player, ServerboundPlayerCommandPacket.Action action) {
        return new ServerboundPlayerCommandPacket(player, action);
    }

    public static ServerboundPlayerAbilitiesPacket C13PacketPlayerAbilities(Abilities abilities) {
        return new ServerboundPlayerAbilitiesPacket(abilities);
    }
    public static ServerboundCustomPayloadPacket C17CustomPayload(ResourceLocation pIdentifier, FriendlyByteBuf pData) {
        return new ServerboundCustomPayloadPacket(pIdentifier, pData);
    }

    public static ClientboundPingPacket S32PacketConfirmTransaction(int id) {
        return new ClientboundPingPacket(id);
    }

    public static ClientboundHorseScreenOpenPacket S0EPacketOpenWindow() {
        return null;
    }

    public static ClientboundContainerClosePacket S0DPacketCloseWindow() {
        return null;
    }
}