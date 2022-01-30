package mezz.jei.gui.ingredients.adapters;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.gui.ingredients.RecipeSlot;

import javax.annotation.Nullable;
import java.util.List;

public class RecipeSlotGuiIngredientAdapter<T> implements IGuiIngredient<T> {
	private final RecipeSlot recipeSlot;
	private final IIngredientType<T> ingredientType;

	public RecipeSlotGuiIngredientAdapter(
		RecipeSlot recipeSlot,
		IIngredientType<T> ingredientType
	) {
		this.recipeSlot = recipeSlot;
		this.ingredientType = ingredientType;
	}

	@Override
	public IIngredientType<T> getIngredientType() {
		return this.ingredientType;
	}

	@Nullable
	@Override
	public T getDisplayedIngredient() {
		return this.recipeSlot.getDisplayedIngredient(this.ingredientType);
	}

	@Override
	public List<T> getAllIngredients() {
		return this.recipeSlot.getAllIngredients(this.ingredientType)
			.toList();
	}

	@Override
	public int getSlotIndex() {
		return this.recipeSlot.getSlotIndex();
	}

	@Override
	public RecipeIngredientRole getRole() {
		return this.recipeSlot.getRole();
	}

	@Override
	public void drawHighlight(PoseStack stack, int color, int xOffset, int yOffset) {
		this.recipeSlot.drawHighlight(stack, color, xOffset, yOffset);
	}

	@Override
	public boolean isInput() {
		return switch (getRole()) {
			case INPUT, CATALYST -> true;
			case OUTPUT -> false;
		};
	}

	public RecipeSlot getRecipeSlot() {
		return recipeSlot;
	}
}
