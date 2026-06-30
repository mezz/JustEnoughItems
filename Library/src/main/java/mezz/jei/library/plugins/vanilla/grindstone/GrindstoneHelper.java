package mezz.jei.library.plugins.vanilla.grindstone;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.GrindstoneMenu;
import org.jetbrains.annotations.Nullable;

final class GrindstoneHelper {
	private static @Nullable GrindstoneMenu GRINDSTONE_MENU;

	private GrindstoneHelper() {
	}

	@Nullable
	public static GrindstoneMenu getFakeGrindstoneMenu() {
		if (GRINDSTONE_MENU == null) {
			Player player = Minecraft.getInstance().player;
			if (player == null) {
				return null;
			}
			Inventory fakeInventory = new Inventory(player);
			GRINDSTONE_MENU = new GrindstoneMenu(0, fakeInventory);
		}
		return GRINDSTONE_MENU;
	}
}
