package mezz.jei.input.mouse.handlers;

import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.recipes.FocusFactory;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.util.Rectangle2dBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;

import java.util.Optional;

public class GuiAreaInputHandler implements IUserInputHandler {
	private final FocusFactory focusFactory;
	private final GuiScreenHelper guiScreenHelper;
	private final RecipesGui recipesGui;

	public GuiAreaInputHandler(IIngredientManager ingredientManager, GuiScreenHelper guiScreenHelper, RecipesGui recipesGui) {
		this.focusFactory = new FocusFactory(ingredientManager);
		this.guiScreenHelper = guiScreenHelper;
		this.recipesGui = recipesGui;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input) {
		if (input.is(KeyBindings.leftClick)) {
			if (screen instanceof AbstractContainerScreen<?> guiContainer) {
				double guiMouseX = input.getMouseX() - guiContainer.getGuiLeft();
				double guiMouseY = input.getMouseY() - guiContainer.getGuiTop();
				return guiScreenHelper.getGuiClickableArea(guiContainer, guiMouseX, guiMouseY)
					.map(clickableArea -> {
						if (!input.isSimulate()) {
							clickableArea.onClick(focusFactory, recipesGui);
						}

						Rect2i screenArea = new Rectangle2dBuilder(clickableArea.getArea())
							.addX(guiContainer.getGuiLeft())
							.addY(guiContainer.getGuiTop())
							.build();
						return LimitedAreaInputHandler.create(this, screenArea);
					});
			}
		}

		return Optional.empty();
	}

}
