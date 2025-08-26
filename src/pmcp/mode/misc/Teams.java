/**
 *
 * @author MecoX
 * @date 7/24/2023
 */
package pmcp.mode.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Team;
import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.value.values.BooleanValue;

public class Teams extends Mode {

	public static Teams Instance;
	private final BooleanValue<Boolean> scoreboard = new BooleanValue<>(this,"Scoreboard", false);
	private final BooleanValue<Boolean> color = new BooleanValue<>(this,"Color", true);
	private final BooleanValue<Boolean> armorColor = new BooleanValue<>(this,"ArmorColor", false);

	public Teams() {
		super("Teams", "队伍", "检测你当前的队伍并且杀戮不打你的队友",  Category.MISC);
		Instance = this;
	}
	
	public boolean isTeam(LivingEntity entity) {
		if (!this.isEnable()) {
			return false;
		}
		Player thePlayer = mc.player;

		if (scoreboard.getValue() && thePlayer.getTeam() != null && entity.getTeam() != null) {
			Team iTeam = thePlayer.getTeam();
			Team iTeam2 = entity.getTeam();
			if (iTeam.isAlliedTo(iTeam2)) {
				return true;
			}
		}

		Component displayName = thePlayer.getDisplayName();

		if(armorColor.getValue()){
			Player entityPlayer = (Player) entity;
			if (!entityPlayer.getInventory().getItem(3).isEmpty()) {
				ItemStack myHead;
				ItemStack iItemStack = myHead = entityPlayer.getInventory().getItem(3);
				Item team = iItemStack.getItem();
				ArmorItem myItemArmor = (ArmorItem) team;
				ItemStack entityHead = entityPlayer.getInventory().getItem(3);
				Item iItem2 = myHead.getItem();
				ArmorItem entityItemArmor = (ArmorItem) iItem2;
				int n = myItemArmor.getBarColor(myHead);
				if (n == entityItemArmor.getBarColor(entityHead)) {
					return true;
				}
			}
		}

		if (color.getValue() && displayName != null && entity.getDisplayName() != null) {
			String targetName = entity.getDisplayName().getVisualOrderText().toString().replace("§r", "");
			String clientName = displayName.getVisualOrderText().toString().replace("§r", "");
			return targetName.startsWith(String.valueOf('§') + clientName.charAt(1));
		}

		return false;
	}
}
