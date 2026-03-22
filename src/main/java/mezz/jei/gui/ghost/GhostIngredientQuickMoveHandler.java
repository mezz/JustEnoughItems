package mezz.jei.gui.ghost;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.click.MouseClickState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class GhostIngredientQuickMoveHandler implements IMouseHandler {
	private final IGhostIngredientDragSource source;
	private final GuiScreenHelper guiScreenHelper;

	public GhostIngredientQuickMoveHandler(IGhostIngredientDragSource source, GuiScreenHelper guiScreenHelper) {
		this.source = source;
		this.guiScreenHelper = guiScreenHelper;
	}

	@Nullable
	@Override
	public IMouseHandler handleClick(Screen screen, double mouseX, double mouseY, int mouseButton, MouseClickState clickState) {
		InputMappings.Input input = InputMappings.Type.MOUSE.getOrCreate(mouseButton);
		if (!KeyBindings.quickMove.isActiveAndMatches(input)) {
			return null;
		}

		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player == null) {
			return null;
		}
		ItemStack mouseItem = player.inventory.getCarried();
		if (!mouseItem.isEmpty()) {
			return null;
		}

		IClickedIngredient<?> clicked = source.getIngredientUnderMouse(mouseX, mouseY);
		if (clicked == null) {
			return null;
		}

		return quickMove(screen, clicked, clickState) ? this : null;
	}

	private <T extends Screen, V> boolean quickMove(T screen, IClickedIngredient<V> clicked, MouseClickState clickState) {
		List<IGhostIngredientHandler<T>> handlers = guiScreenHelper.getGhostIngredientHandlers(screen);
		if (handlers.isEmpty()) {
			return false;
		}
		if (clickState.isSimulate()) {
			return true;
		}

		V ingredient = clicked.getValue();
		for (IGhostIngredientHandler<T> handler : handlers) {
			if (handler.quickMove(screen, ingredient)) {
				return true;
			}
		}
		return false;
	}
}
