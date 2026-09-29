package mezz.jei.gui.ghost;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.InputAction;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IRecipeFocusSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;

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

	private <T extends Screen, V> Optional<InputAction> prepare(T screen, IDraggableIngredientInternal<V> clicked) {
		var handlers = screenHelper.getGhostIngredientHandlers(screen);
		if (handlers.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(InputAction.run(() -> {
				for (IGhostIngredientHandler<T> handler : handlers) {
					if (handler.quickMove(screen, clicked.getTypedIngredient())) {
						break;
					}
				}
			})
			.within(clicked.getArea()::contains).when(clicked.getElement()::isVisible));
	}

	public <T extends Screen> Optional<InputAction> prepareQuickMove(T screen, UserInput input) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || !player.containerMenu.getCarried().isEmpty()) {
			return Optional.empty();
		}
		return source.getDraggableIngredientUnderMouse(input.getMouseX(), input.getMouseY())
			.findFirst()
			.flatMap(clicked -> prepare(screen, clicked))
			.map(action -> action.when(() -> player.containerMenu.getCarried().isEmpty()));
	}
}
