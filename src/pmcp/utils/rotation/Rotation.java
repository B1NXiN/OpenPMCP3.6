package pmcp.utils.rotation;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.player.Player;
import pmcp.utils.client.MinecraftInstance;

/**
 * Rotations
 */
@Setter
@Getter
public class Rotation extends MinecraftInstance {
    private float yaw;
    private float pitch;

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Set rotations to player
     */
    public void toPlayer(Player player) {
        if (Float.isNaN(yaw) || Float.isNaN(pitch))
            return;

        player.setYRot(yaw);
        player.setXRot(pitch);
    }
}