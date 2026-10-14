package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface IRecipeLayoutWithButtons<R> {
	void draw(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks);

	void updateBounds(int recipeXOffset, int recipeYOffset);

	int totalWidth();

	IInputTarget createUserInputHandler();

	/**
	 * Starts an interaction for the category's deprecated {@link IRecipeCategory#handleInput} callback.
	 * Call only after controls and shortcuts decline the input. Keyboard input runs immediately.
	 * Mouse input waits for release inside the recipe because the callback cannot simulate a press.
	 *
	 * @return the interaction, or empty when unsupported or unhandled; empty by default
	 */
	default Optional<IInputInteraction> beginLegacyCategoryInput(UserInput input) {
		return Optional.empty();
	}

	void tick();

	IRecipeLayoutDrawable<R> getRecipeLayout();

	@Nullable
	RecipeBookmark<?, ?> getRecipeBookmark();

	void drawTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY);

	int getMissingCountHint();
}
