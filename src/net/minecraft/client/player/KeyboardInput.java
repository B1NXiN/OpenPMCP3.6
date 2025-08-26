package net.minecraft.client.player;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pmcp.event.EventManager;
import pmcp.event.mode.player.EventKeyMoveInput;

@OnlyIn(Dist.CLIENT)
public class KeyboardInput extends Input {
   private final Options options;

   public KeyboardInput(Options pOptions) {
      this.options = pOptions;
   }

   private static float calculateImpulse(boolean pInput, boolean pOtherInput) {
      if (pInput == pOtherInput) {
         return 0.0F;
      } else {
         return pInput ? 1.0F : -1.0F;
      }
   }

   public void tick(boolean pIsSneaking, float pSneakingSpeedMultiplier) {
      EventKeyMoveInput event = new EventKeyMoveInput(calculateImpulse(this.up, this.down), calculateImpulse(this.left, this.right), this.options.keyUp.isDown(), this.options.keyDown.isDown(), this.options.keyLeft.isDown(), this.options.keyRight.isDown(), this.options.keyJump.isDown(), this.options.keyShift.isDown());
      EventManager.instance.call(event);
      if (event.isCancelled()) return;

      this.up = event.isUp();
      this.down = event.isDown();
      this.left = event.isLeft();
      this.right = event.isRight();

      this.forwardImpulse = event.getMovementForward();
      this.leftImpulse = event.getMovementSideways();

      this.jumping = event.isJumping();
      this.shiftKeyDown = event.isShiftKeyDown();

      if (pIsSneaking) {
         this.leftImpulse *= pSneakingSpeedMultiplier;
         this.forwardImpulse *= pSneakingSpeedMultiplier;
      }
   }
}