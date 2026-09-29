package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.RecipeLayoutDrawableErrored;
import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.ApiInputAdapter;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.common.input.interaction.ReleaseInsideBounds;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class RecipeLayoutWithButtonsErrored<R> implements IRecipeLayoutWithButtons<R> {
	private final RecipeLayoutDrawableErrored<R> errorLayout;

	public RecipeLayoutWithButtonsErrored(IRecipeLayoutDrawable<R> brokenRecipeLayout) {
		IScalableDrawable recipeBackground = Internal.getTextures().getRecipeBackground();
		this.errorLayout = new RecipeLayoutDrawableErrored<>(brokenRecipeLayout.getRecipeCategory(), brokenRecipeLayout.getRecipe(), recipeBackground, 4);
		Rect2i rect = brokenRecipeLayout.getRect();
		this.errorLayout.setPosition(rect.getX(), rect.getY());
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		errorLayout.drawRecipe(guiGraphics, mouseX, mouseY);
	}

	@Override
	public void updateBounds(int recipeXOffset, int recipeYOffset) {
		Rect2i rectWithBorder = errorLayout.getRectWithBorder();
		Rect2i rect = errorLayout.getRect();
		errorLayout.setPosition(
			recipeXOffset - rectWithBorder.getX() + rect.getX(),
			recipeYOffset - rectWithBorder.getY() + rect.getY()
		);
	}

	@Override
	public int totalWidth() {
		Rect2i area = errorLayout.getRect();
		Rect2i areaWithBorder = errorLayout.getRectWithBorder();
		int leftBorderWidth = area.getX() - areaWithBorder.getX();
		int rightAreaWidth = areaWithBorder.getWidth() - leftBorderWidth;
		return leftBorderWidth + rightAreaWidth;
	}

	@Override
	public IInputTarget createUserInputHandler() {
		return new ErroredRecipeInputTarget<>(errorLayout);
	}

	@Override
	public void tick() {
		errorLayout.tick();
	}

	@Override
	public IRecipeLayoutDrawable<R> getRecipeLayout() {
		return errorLayout;
	}

	@Override
	public @Nullable RecipeBookmark<?, ?> getRecipeBookmark() {
		return null;
	}

	@Override
	public void drawTooltips(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {

	}

	@Override
	public int getMissingCountHint() {
		return Integer.MAX_VALUE;
	}

	/**
	 * Lets the mod's recipe handler receive input when JEI could not build the recipe's normal controls.
	 *
	 * @param recipeLayout the recipe whose handler receives input and whose area is checked on release
	 * @param <R> recipe type displayed by the layout
	 */
	private record ErroredRecipeInputTarget<R>(IRecipeLayoutDrawable<R> recipeLayout) implements IInputTarget {
		@Override
		public Optional<IInputInteraction> beginInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
			if (!recipeLayout.isMouseOver(input.getMouseX(), input.getMouseY())) {
				return Optional.empty();
			}
			return ApiInputAdapter.beginInput(screen, input, keyBindings, recipeLayout.getInputHandler())
				.map(interaction -> new ReleaseInsideBounds(interaction, recipeLayout::isMouseOver));
		}

		@Override
		public boolean scroll(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
			return recipeLayout.isMouseOver(mouseX, mouseY) &&
				recipeLayout.getInputHandler().handleMouseScrolled(mouseX, mouseY, scrollDeltaX, scrollDeltaY);
		}
	}
}
