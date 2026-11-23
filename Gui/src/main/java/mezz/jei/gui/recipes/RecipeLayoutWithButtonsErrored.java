package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.RecipeLayoutDrawableErrored;
import mezz.jei.common.gui.elements.ScalableDrawable;
import mezz.jei.common.input.IInputTarget;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.Nullable;

public final class RecipeLayoutWithButtonsErrored<R> implements IRecipeLayoutWithButtons<R> {
	private final RecipeLayoutDrawableErrored<R> errorLayout;

	public RecipeLayoutWithButtonsErrored(IRecipeLayoutDrawable<R> brokenRecipeLayout) {
		ScalableDrawable recipeBackground = Internal.getTextures().getRecipeBackground();
		this.errorLayout = new RecipeLayoutDrawableErrored<>(brokenRecipeLayout.getRecipeCategory(), brokenRecipeLayout.getRecipe(), recipeBackground, 4);
		Rect2i rect = brokenRecipeLayout.getRect();
		this.errorLayout.setPosition(rect.getX(), rect.getY());
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
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
	public IInputTarget createControlsInputHandler() {
		return new RecipeWidgetsInputHandler(errorLayout);
	}

	@Override
	public IInputTarget createApiInputHandler() {
		return new RecipeApiInputHandler(errorLayout);
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
	public void drawTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {

	}

	@Override
	public int getMissingCountHint() {
		return Integer.MAX_VALUE;
	}

}
