package mezz.jei.input.mouse.handlers;

import mezz.jei.common.input.IKeyBindings;
import mezz.jei.common.gui.GuiScreenHelper;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.recipes.FocusFactory;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.Optional;

public class GuiAreaInputHandler implements IUserInputHandler {
	private final FocusFactory focusFactory;
	private final GuiScreenHelper guiScreenHelper;
	private final RecipesGui recipesGui;

	public GuiAreaInputHandler(RegisteredIngredients registeredIngredients, GuiScreenHelper guiScreenHelper, RecipesGui recipesGui) {
		this.focusFactory = new FocusFactory(registeredIngredients);
		this.guiScreenHelper = guiScreenHelper;
		this.recipesGui = recipesGui;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IKeyBindings keyBindings) {
		if (input.is(keyBindings.getLeftClick())) {
			if (screen instanceof AbstractContainerScreen<?> guiContainer) {
				double guiMouseX = input.getMouseX() - guiContainer.getGuiLeft();
				double guiMouseY = input.getMouseY() - guiContainer.getGuiTop();
				return guiScreenHelper.getGuiClickableArea(guiContainer, guiMouseX, guiMouseY)
					.map(clickableArea -> {
						if (!input.isSimulate()) {
							clickableArea.onClick(focusFactory, recipesGui);
						}

						ImmutableRect2i screenArea = new ImmutableRect2i(clickableArea.getArea())
							.addOffset(guiContainer.getGuiLeft(), guiContainer.getGuiTop());
						return LimitedAreaInputHandler.create(this, screenArea);
					});
			}
		}

		return Optional.empty();
	}

}
