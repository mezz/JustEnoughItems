package mezz.jei.common.input.handlers;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.common.gui.TooltipRenderer;
import mezz.jei.common.gui.overlay.IIngredientGrid;
import mezz.jei.common.input.IKeyBindings;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.packets.PacketDeletePlayerItem;
import mezz.jei.common.network.packets.PacketJei;
import mezz.jei.common.platform.IPlatformItemStackHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.CheatUtil;
import mezz.jei.core.config.GiveMode;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.core.config.IWorldConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class DeleteItemInputHandler implements IUserInputHandler {
	private final IIngredientGrid ingredientGrid;
	private final IWorldConfig worldConfig;
	private final IClientConfig clientConfig;
	private final IConnectionToServer serverConnection;

	public DeleteItemInputHandler(
		IIngredientGrid ingredientGrid,
		IWorldConfig worldConfig,
		IClientConfig clientConfig,
		IConnectionToServer serverConnection
	) {
		this.ingredientGrid = ingredientGrid;
		this.worldConfig = worldConfig;
		this.clientConfig = clientConfig;
		this.serverConnection = serverConnection;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput userInput, IKeyBindings keyBindings) {
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
			PacketJei packet = new PacketDeletePlayerItem(itemStack);
			serverConnection.sendPacketToServer(packet);
		}
		return Optional.of(this);
	}

	@SuppressWarnings("MethodMayBeStatic")
	public void drawTooltips(PoseStack poseStack, int mouseX, int mouseY) {
		TranslatableComponent deleteItem = new TranslatableComponent("jei.tooltip.delete.item");
		TooltipRenderer.drawHoveringText(poseStack, List.of(deleteItem), mouseX, mouseY);
	}

	public boolean shouldDeleteItemOnClick(Minecraft minecraft, double mouseX, double mouseY) {
		if (!worldConfig.isDeleteItemsInCheatModeActive()) {
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
				.map(CheatUtil::getCheatItemStack)
				.map(i -> {
					IPlatformItemStackHelper itemStackHelper = Services.PLATFORM.getItemStackHelper();
					return !itemStackHelper.canStack(itemStack, i);
				})
				.orElse(true);
		}
		return true;
	}
}
