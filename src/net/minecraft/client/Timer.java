package net.minecraft.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Timer {
   public float partialTick;
   public float tickDelta;
   public long lastMs;
   private final float msPerTick;

   public float timerSpeed = 1.0f;

   public Timer(float pTicksPerSecond, long pLastMs) {
      this.msPerTick = 1000.0F / pTicksPerSecond;
      this.lastMs = pLastMs;
   }

   public int advanceTime(long pGameTime) {
      this.tickDelta = (float)(pGameTime - this.lastMs) / this.msPerTick;
      this.lastMs = pGameTime;
      this.partialTick += (this.tickDelta * timerSpeed);
      int i = (int)this.partialTick;
      this.partialTick -= (float)i;
      return i;
   }
}