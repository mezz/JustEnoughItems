package mezz.jei.test.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.PreeditEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import org.jspecify.annotations.Nullable;

/**
 * Simulates a modded screen that handles preedit itself without forwarding to its focused child.
 */
public final class PreeditBlockingContainerScreen extends AbstractContainerScreen<InventoryMenu> {
	public PreeditBlockingContainerScreen(LocalPlayer player) {
		super(player.inventoryMenu, player.getInventory(), Component.empty());
	}

	@Override
	public boolean preeditUpdated(@Nullable PreeditEvent event) {
		throw new AssertionError("Expected preedit to be routed directly to JEI's focused search field.");
	}
}
