package pmcp.mode.render;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.value.values.BooleanValue;

public class Glow extends Mode {

    public static Glow Instance;

    public final BooleanValue<Boolean> players = new BooleanValue<>(this,"Players", true);
    public final BooleanValue<Boolean> items = new BooleanValue<>(this,"Items", true);
    public final BooleanValue<Boolean> mobs = new BooleanValue<>(this,"Mobs", true);
    public final BooleanValue<Boolean> animals = new BooleanValue<>(this,"Animals", true);
    public final BooleanValue<Boolean> arrows = new BooleanValue<>(this,"Arrows", true);

    public Glow() {
        super("Glow", "光晕", "实体光晕渲染", Category.RENDER);
        Instance = this;
    }

    public static boolean shouldGlow(Entity entity) {
        if (!Instance.isEnable()) {
            return false;
        } else if (entity instanceof Player && Instance.players.getValue()) {
            return true;
        } else if (entity instanceof ItemEntity && Instance.items.getValue()) {
            return true;
        } else if (entity instanceof Mob && Instance.mobs.getValue()) {
            return true;
        } else {
            return entity instanceof Animal && Instance.animals.getValue() ? true : entity instanceof Arrow && Instance.arrows.getValue();
        }
    }
}
