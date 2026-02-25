package mezz.jei.gui.ghost;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.gui.input.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
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
		List<IGhostIngredientHandler<T>> handlerList = new ArrayList<>();

		for (IGhostIngredientHandler<T> handler : screenHelper.getGhostIngredientHandlers(currentScreen)) {
			ITypedIngredient<V> ingredient = clicked.getTypedIngredient();
			List<IGhostIngredientHandler.Target<V>> targets = handler.getTargetsTyped(currentScreen, ingredient, false);
			if (!targets.isEmpty()) {
				handlerList.add(handler);
			}
		}

		if (handlerList.isEmpty()) {
			return false;
		}

		for (IGhostIngredientHandler<T> handler : handlerList) {
			ITypedIngredient<V> ingredient = clicked.getTypedIngredient();
			handler.quickMove(currentScreen, ingredient);
		}

		return true;
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
					if (mouseItem.isEmpty() &&
							quickMoveInternal(screen, clicked, input)) {
						return Optional.of(true);
					}
					return Optional.empty();
				})
				.isPresent();
	}

}
