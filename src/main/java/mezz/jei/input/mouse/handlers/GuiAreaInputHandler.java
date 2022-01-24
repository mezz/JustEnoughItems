package mezz.jei.input.mouse.handlers;

import mezz.jei.config.KeyBindings;
import mezz.jei.gui.Focus;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.util.Rectangle2dBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;

import java.util.Optional;

public class GuiAreaInputHandler implements IUserInputHandler {
	private final GuiScreenHelper guiScreenHelper;
	private final RecipesGui recipesGui;

	public GuiAreaInputHandler(GuiScreenHelper guiScreenHelper, RecipesGui recipesGui) {
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
							clickableArea.onClick(Focus::new, recipesGui);
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
