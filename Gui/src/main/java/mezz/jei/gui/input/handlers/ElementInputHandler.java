package mezz.jei.gui.input.handlers;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.CombinedRecipeFocusSource;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.handlers.SameElementInputHandler;
import mezz.jei.gui.overlay.elements.IElement;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Optional;

public class ElementInputHandler implements IUserInputHandler {
	private final CombinedRecipeFocusSource focusSource;

	public ElementInputHandler(CombinedRecipeFocusSource focusSource) {
		this.focusSource = focusSource;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keyBindings) {
		List<IClickableIngredientInternal<?>> ingredientUnderMouse = focusSource.getIngredientUnderMouse(input, keyBindings)
			.toList();

		for (IClickableIngredientInternal<?> clicked : ingredientUnderMouse) {
			IElement<?> element = clicked.getElement();
			if (element.handleClick(input, keyBindings)) {
				IUserInputHandler result = new SameElementInputHandler(this, clicked::isMouseOver);
				return Optional.of(result);
			}
		}
		return Optional.empty();
	}
}
