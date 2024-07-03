package mezz.jei.gui.input.handlers;

import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.GiveMode;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.gui.TooltipRenderer;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.packets.PacketDeletePlayerItem;
import mezz.jei.common.util.ServerCommandUtil;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.overlay.IIngredientGrid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class DeleteItemInputHandler implements IUserInputHandler {
	private final IIngredientGrid ingredientGrid;
	private final IClientToggleState toggleState;
	private final IClientConfig clientConfig;
	private final IConnectionToServer serverConnection;
	private final IIngredientManager ingredientManager;

	public DeleteItemInputHandler(
		IIngredientGrid ingredientGrid,
		IClientToggleState toggleState,
		IClientConfig clientConfig,
		IConnectionToServer serverConnection,
		IIngredientManager ingredientManager
	) {
		this.ingredientGrid = ingredientGrid;
		this.toggleState = toggleState;
		this.clientConfig = clientConfig;
		this.serverConnection = serverConnection;
		this.ingredientManager = ingredientManager;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput userInput, IInternalKeyMappings keyBindings) {
		if (!userInput.is(keyBindings.getLeftClick())) {
			return Optional.empty();
		}
		double mouseX = userInput.getMouseX();
		double mouseY = userInput.getMouseY();
		if (!this.ingredientGrid.isMouseOver(mouseX, mouseY)) {
			return Optional.empty();
		}
		Minecraft minecraft = Minecraft.getInstance();
		if (!shouldDeleteItemOnClick(minecraft, mouseX, mouseY)) {
			return Optional.empty();
		}
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return Optional.empty();
		}
		ItemStack itemStack = player.containerMenu.getCarried();
		if (itemStack.isEmpty()) {
			return Optional.empty();
		}
		if (!userInput.isSimulate()) {
			player.containerMenu.setCarried(ItemStack.EMPTY);
			var packet = new PacketDeletePlayerItem(itemStack);
			serverConnection.sendPacketToServer(packet);
		}
		return Optional.of(this);
	}

	@SuppressWarnings("MethodMayBeStatic")
	public void drawTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		Component deleteItem = Component.translatable("jei.tooltip.delete.item");
		TooltipRenderer.drawHoveringText(guiGraphics, List.of(deleteItem), mouseX, mouseY);
	}

	public boolean shouldDeleteItemOnClick(Minecraft minecraft, double mouseX, double mouseY) {
		if (!toggleState.isCheatItemsEnabled() || !serverConnection.isJeiOnServer()) {
			return false;
		}
		Player player = minecraft.player;
		if (player == null) {
			return false;
		}
		ItemStack itemStack = player.containerMenu.getCarried();
		if (itemStack.isEmpty()) {
			return false;
		}
		GiveMode giveMode = this.clientConfig.getGiveMode();
		if (giveMode == GiveMode.MOUSE_PICKUP) {
			return this.ingredientGrid.getIngredientUnderMouse(mouseX, mouseY)
				.findFirst()
				.map(c -> c.getCheatItemStack(ingredientManager))
				.map(i -> !ServerCommandUtil.canStack(itemStack, i))
				.orElse(true);
		}
		return true;
	}
}
