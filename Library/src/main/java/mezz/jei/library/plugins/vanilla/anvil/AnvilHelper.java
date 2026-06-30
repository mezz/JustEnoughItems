package mezz.jei.library.plugins.vanilla.anvil;

import mezz.jei.common.util.ErrorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class AnvilHelper {
	private static final Logger LOGGER = LogManager.getLogger();
	private static @Nullable AnvilMenu ANVIL_MENU = null;

	public static int findLevelsCost(ItemStack leftStack, ItemStack rightStack) {
		AnvilMenu anvilMenu = getFakeAnvilMenu();
		AnvilMenu result = setAnvilMenu(anvilMenu, leftStack, rightStack);
		if (result == null) {
			return -1;
		}
		return result.getCost();
	}

	public static AnvilMenu getFakeAnvilMenu() {
		if (ANVIL_MENU == null) {
			Minecraft minecraft = Minecraft.getInstance();
			Player player = Objects.requireNonNull(minecraft.player);
			Inventory fakeInventory = new Inventory(player, new EntityEquipment());
			ANVIL_MENU = new AnvilMenu(0, fakeInventory);
		}
		return ANVIL_MENU;
	}

	@Nullable
	public static AnvilMenu setAnvilMenu(AnvilMenu anvilMenu, ItemStack leftStack, ItemStack rightStack) {
		try {
			Slot leftSlot = anvilMenu.slots.get(0);
			Slot rightSlot = anvilMenu.slots.get(1);

			// setting the stack triggers a recalculation of the recipe, so avoid it when possible
			if (leftSlot.getItem() != leftStack) {
				leftSlot.set(leftStack);
			}
			if (rightSlot.getItem() != rightStack) {
				rightSlot.set(rightStack);
			}
			return anvilMenu;
		} catch (RuntimeException e) {
			String left = ErrorUtil.getItemStackInfo(leftStack);
			String right = ErrorUtil.getItemStackInfo(rightStack);
			LOGGER.error("Could not set anvil recipe for: ({} and {}).", left, right, e);
			return null;
		}
	}
}
