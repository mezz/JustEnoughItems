package mezz.jei.gui.ghost;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.gui.input.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class GhostIngredientQuickMoveManager {
	private final IRecipeFocusSource source;
	private final IScreenHelper screenHelper;

	public GhostIngredientQuickMoveManager(
		IRecipeFocusSource source,
		IScreenHelper screenHelper
	) {
		this.source = source;
		this.screenHelper = screenHelper;
	}

	private <T extends Screen, V> boolean quickMoveInternal(T currentScreen, IDraggableIngredientInternal<V> clicked, UserInput input) {
		for (IGhostIngredientHandler<T> handler : screenHelper.getGhostIngredientHandlers(currentScreen)) {
			ITypedIngredient<V> ingredient = clicked.getTypedIngredient();
			if (handler.quickMove(currentScreen, ingredient))
				return true;
		}

		return false;
	}

	public <T extends Screen> boolean quickMove(T screen, UserInput input) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return false;
		}

		return source.getDraggableIngredientUnderMouse(input.getMouseX(), input.getMouseY())
				.findFirst()
				.flatMap(clicked -> {
					ItemStack mouseItem = player.containerMenu.getCarried();
					if (mouseItem.isEmpty() && (input.isSimulate() ||
							quickMoveInternal(screen, clicked, input))) {
						return Optional.of(true);
					}
					return Optional.empty();
				})
				.isPresent();
	}

}
