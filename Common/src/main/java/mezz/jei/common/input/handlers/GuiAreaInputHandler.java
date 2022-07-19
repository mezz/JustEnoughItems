package mezz.jei.common.input.handlers;

import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.gui.GuiScreenHelper;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.focus.FocusFactory;
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.Optional;

public class GuiAreaInputHandler implements IUserInputHandler {
	private final FocusFactory focusFactory;
	private final GuiScreenHelper guiScreenHelper;
	private final IRecipesGui recipesGui;

	public GuiAreaInputHandler(RegisteredIngredients registeredIngredients, GuiScreenHelper guiScreenHelper, IRecipesGui recipesGui) {
		this.focusFactory = new FocusFactory(registeredIngredients);
		this.guiScreenHelper = guiScreenHelper;
		this.recipesGui = recipesGui;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		if (input.is(keyBindings.getLeftClick())) {
			if (screen instanceof AbstractContainerScreen<?> guiContainer) {
				IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
				final int guiLeft = screenHelper.getGuiLeft(guiContainer);
				final int guiTop = screenHelper.getGuiTop(guiContainer);
				final double guiMouseX = input.getMouseX() - guiLeft;
				final double guiMouseY = input.getMouseY() - guiTop;
				return guiScreenHelper.getGuiClickableArea(guiContainer, guiMouseX, guiMouseY)
					.map(clickableArea -> {
						if (!input.isSimulate()) {
							clickableArea.onClick(focusFactory, recipesGui);
						}

						ImmutableRect2i screenArea = new ImmutableRect2i(clickableArea.getArea())
							.addOffset(guiLeft, guiTop);
						return LimitedAreaInputHandler.create(this, screenArea);
					});
			}
		}

		return Optional.empty();
	}

}
