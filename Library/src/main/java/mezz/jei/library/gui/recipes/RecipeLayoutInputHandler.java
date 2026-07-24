package mezz.jei.library.gui.recipes;

import com.mojang.blaze3d.platform.InputConstants;

import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.minecraft.client.renderer.Rect2i;


public class RecipeLayoutInputHandler<T> implements IJeiInputHandler {
	private final RecipeLayout<T> recipeLayout;

	public RecipeLayoutInputHandler(
		RecipeLayout<T> recipeLayout
	) {
		this.recipeLayout = recipeLayout;
	}

	@Override
	public Rect2i getArea() {
		return recipeLayout.getRect();
	}

	@Override
	public boolean handleInput(double mouseX, double mouseY, IJeiUserInput userInput) {
		if (!recipeLayout.isMouseOver(mouseX, mouseY)) {
			return false;
		}

		Rect2i area = recipeLayout.getRect();
		final double recipeMouseX = mouseX - area.getX();
		final double recipeMouseY = mouseY - area.getY();

		if (userInput.isSimulate()) {
			return true;
		}
		IRecipeCategory<T> recipeCategory = recipeLayout.getRecipeCategory();
		T recipe = recipeLayout.getRecipe();
		return recipeCategory.handleInput(recipe, recipeMouseX, recipeMouseY, userInput.getKey());
	}

	@Override
	public boolean handleMouseDragged(double mouseX, double mouseY, InputConstants.Key mouseKey, double dragX, double dragY) {
		return false;
	}

	@Override
	public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaY) {
		return false;
	}

	@Override
	public void handleMouseMoved(double mouseX, double mouseY) {

	}
}
