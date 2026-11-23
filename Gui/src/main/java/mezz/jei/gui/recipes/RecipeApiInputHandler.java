package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.ApiInputAdapter;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.common.input.interaction.ReleaseInsideBounds;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

/** Offers unhandled input to the recipe's combined public API handler. */
public record RecipeApiInputHandler(IRecipeLayoutDrawable<?> recipeLayout) implements IInputTarget {
	@Override
	public Optional<IInputInteraction> beginInput(Screen screen, UserInput input, IInternalKeyMappings keys) {
		if (!recipeLayout.isMouseOver(input.getMouseX(), input.getMouseY())) {
			return Optional.empty();
		}
		return ApiInputAdapter.beginInput(input, recipeLayout.getInputHandler())
			.map(interaction -> new ReleaseInsideBounds(interaction, recipeLayout::isMouseOver));
	}

	@Override
	public boolean scroll(double mouseX, double mouseY, double scrollX, double scrollY) {
		return recipeLayout.isMouseOver(mouseX, mouseY) &&
			recipeLayout.getInputHandler().handleMouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}
}
