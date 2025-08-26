package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import pmcp.mode.move.NoWeb;

public class WebBlock extends Block {
   public WebBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {

      if (NoWeb.Instance != null && NoWeb.Instance.isEnable()) {
         switch (NoWeb.Instance.modeValue.getValue()) {
            case GrimSlow -> pEntity.makeStuckInBlock(pState, new Vec3(1.45D, (double)0.05F, 1.45D));
            case Packet -> pEntity.makeStuckInBlock(pState, new Vec3(1D, 1D, 1D));
            default -> pEntity.makeStuckInBlock(pState, new Vec3(0.25D, (double)0.05F, 0.25D));
         }
      } else {
         pEntity.makeStuckInBlock(pState, new Vec3(0.25D, (double)0.05F, 0.25D));
      }
   }
}