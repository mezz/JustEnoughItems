package mezz.jei.test.client;

import net.minecraft.client.gui.components.EditBox;
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
	private @Nullable EditBox textInput;

	public PreeditBlockingContainerScreen(LocalPlayer player) {
		super(player.inventoryMenu, player.getInventory(), Component.empty());
	}

	@Override
	protected void init() {
		super.init();
		this.textInput = this.addRenderableWidget(
			new EditBox(this.font, 8, 8, 100, 20, Component.literal("test input"))
		);
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(getTextInput());
	}

	public EditBox getTextInput() {
		if (this.textInput == null) {
			throw new IllegalStateException("The screen has not been initialized.");
		}
		return this.textInput;
	}

	@Override
	public boolean preeditUpdated(@Nullable PreeditEvent event) {
		throw new AssertionError("Expected preedit to be routed directly to JEI's focused search field.");
	}
}
