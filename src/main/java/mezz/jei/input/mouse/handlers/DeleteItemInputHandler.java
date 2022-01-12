package mezz.jei.input.mouse.handlers;

import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.network.Network;
import mezz.jei.network.packets.PacketDeletePlayerItem;
import mezz.jei.network.packets.PacketJei;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class DeleteItemInputHandler implements IUserInputHandler {
	private final IngredientGrid ingredientGrid;

	public DeleteItemInputHandler(IngredientGrid ingredientGrid) {
		this.ingredientGrid = ingredientGrid;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput userInput) {
		double mouseX = userInput.getMouseX();
		double mouseY = userInput.getMouseY();
		if (!this.ingredientGrid.isMouseOver(mouseX, mouseY)) {
			return Optional.empty();
		}
		Minecraft minecraft = Minecraft.getInstance();
		if (!this.ingredientGrid.shouldDeleteItemOnClick(minecraft, mouseX, mouseY)) {
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
			Network.sendPacketToServer(packet);
		}
		return Optional.of(this);
	}
}
