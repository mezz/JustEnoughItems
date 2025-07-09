package mezz.jei.gui.input.handlers;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.Optional;

public class GuiAreaInputHandler implements IUserInputHandler {
	private final IFocusFactory focusFactory;
	private final IScreenHelper screenHelper;
	private final IRecipesGui recipesGui;

	public GuiAreaInputHandler(IScreenHelper screenHelper, IRecipesGui recipesGui, IFocusFactory focusFactory) {
		this.focusFactory = focusFactory;
		this.screenHelper = screenHelper;
		this.recipesGui = recipesGui;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keyBindings) {
		if (input.is(keyBindings.getLeftClick())) {
			if (screen instanceof AbstractContainerScreen<?> guiContainer) {
				final int guiLeft = guiProperties.guiLeft();
				final int guiTop = guiProperties.guiTop();
				final double guiMouseX = input.getMouseX() - guiLeft;
				final double guiMouseY = input.getMouseY() - guiTop;
				return this.screenHelper.getGuiClickableArea(guiContainer, guiMouseX, guiMouseY)
					.findFirst()
					.map(clickableArea -> {
						if (!input.isSimulate()) {
							clickableArea.onClick(focusFactory, recipesGui);
						}

						ImmutableRect2i screenArea = new ImmutableRect2i(clickableArea.getArea())
							.addOffset(guiLeft, guiTop);
						return new SameElementInputHandler(this, screenArea::contains);
					});
			}
		}

		return Optional.empty();
	}

}
