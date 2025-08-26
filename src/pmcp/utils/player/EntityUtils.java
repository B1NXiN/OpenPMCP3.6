package pmcp.utils.player;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import pmcp.PMCP;
import pmcp.mode.client.IRCSetting;
import pmcp.mode.misc.Teams;

public class EntityUtils {
    public static boolean isPlayer(Entity entity) {
        return entity instanceof Player;
    }

    public static boolean isAnimal(Entity entity) {
        return entity instanceof Animal ||
                entity instanceof Villager ||
                entity instanceof AbstractGolem ||
                entity instanceof Bat ||
                entity instanceof Fox ||
                entity instanceof Dolphin ||
                entity instanceof Panda ||
                entity instanceof Parrot ||
                entity instanceof Rabbit ||
                entity instanceof Axolotl ||
                entity instanceof Goat ||
                entity instanceof Ocelot ||
                entity instanceof Chicken ||
                entity instanceof Cod ||
                entity instanceof Cow ||
                entity instanceof Donkey ||
                entity instanceof Horse ||
                entity instanceof Mule ||
                entity instanceof Pig ||
                entity instanceof Pufferfish ||
                entity instanceof Salmon ||
                entity instanceof Sheep ||
                entity instanceof Squid ||
                entity instanceof Strider ||
                entity instanceof TropicalFish ||
                entity instanceof Turtle;
    }

    public static boolean isValidTarget(LivingEntity livingEntity, Minecraft mc) {
        Teams teams = (Teams) PMCP.Instance.getMode(Teams.class);

        return (livingEntity.getId() != mc.player.getId()
                && !livingEntity.getName().getString().isEmpty()
                && (livingEntity.getYRot() != 0.0 && livingEntity.getXRot() != 0.0)
                && !livingEntity.isDeadOrDying()
                && !(((IRCSetting.Instance == null || IRCSetting.Instance.ircFiend.getValue()) && livingEntity instanceof Player player) && player.ircUser != null)
                && !FriendManager.isFriend(livingEntity.getName().getString())
                && (!teams.isEnable() || !teams.isTeam(livingEntity)) && !(livingEntity.getName().getString().contains("@cet_npc_") || livingEntity.getName().getString().contains("CIT-") || livingEntity.getName().getString().contains("@cet_")));
    }

    public static boolean isValidTarget(Player livingEntity, Minecraft mc) {
        Teams teams = (Teams) PMCP.Instance.getMode(Teams.class);

        return (livingEntity.getId() != mc.player.getId()
                && (livingEntity.getYRot() != 0.0 && livingEntity.getXRot() != 0.0)
                && !livingEntity.getName().getString().isEmpty()
                && !livingEntity.isDeadOrDying()
                && !FriendManager.isFriend(livingEntity.getName().getString())
                && !((IRCSetting.Instance == null || IRCSetting.Instance.ircFiend.getValue()) && livingEntity.ircUser != null)
                && (!teams.isEnable() || !teams.isTeam(livingEntity))
                && !(livingEntity.getName().getString().contains("@cet_npc_") || livingEntity.getName().getString().contains("CIT-") || livingEntity.getName().getString().contains("@cet_")));
    }

    public static boolean isTeams(LivingEntity entity) {
        Teams teams = (Teams) PMCP.Instance.getMode(Teams.class);

        return teams.isEnable() && teams.isTeam(entity);
    }

    public static boolean isMob(Entity entity) {
        return entity instanceof Monster ||
                entity instanceof Slime ||
                entity instanceof Ghast ||
                entity instanceof Shulker ||
                entity instanceof Blaze ||
                entity instanceof Creeper ||
                entity instanceof Drowned ||
                entity instanceof ElderGuardian ||
                entity instanceof EnderDragon ||
                entity instanceof Endermite ||
                entity instanceof Evoker ||
                entity instanceof Guardian ||
                entity instanceof Hoglin ||
                entity instanceof Husk ||
                entity instanceof Illusioner ||
                entity instanceof IronGolem ||
                entity instanceof MagmaCube ||
                entity instanceof Phantom ||
                entity instanceof Pillager ||
                entity instanceof Ravager ||
                entity instanceof Silverfish ||
                entity instanceof Skeleton ||
                entity instanceof Spider ||
                entity instanceof Stray ||
                entity instanceof Vex ||
                entity instanceof Vindicator ||
                entity instanceof Witch ||
                entity instanceof WitherSkeleton ||
                entity instanceof Zoglin ||
                entity instanceof Zombie ||
                entity instanceof ZombieVillager;
    }

    public static boolean isInvisible(Entity entity) {
        if (!(entity instanceof Player)) return false;
        return entity.isInvisible();
    }



    // 死亡实体
    public static boolean isCorpse(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity.isDeadOrDying();
        }
        return false;
    }

    // 被攻击的实体
    public static boolean isBeingAttacked(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity.hurtTime > 0;
        }
        return false;
    }
}
