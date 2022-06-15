package mezz.jei.common.deprecated.gui.ingredients.adapters;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.gui.ingredients.RecipeSlot;

import org.jetbrains.annotations.Nullable;
import java.util.List;

@SuppressWarnings({"removal"})
@Deprecated
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
		return this.recipeSlot.getDisplayedIngredient(this.ingredientType)
			.orElse(null);
	}

	@Override
	public List<T> getAllIngredients() {
		return this.recipeSlot.getIngredients(this.ingredientType)
			.toList();
	}

	@Override
	public void drawHighlight(PoseStack stack, int color, int xOffset, int yOffset) {
		stack.pushPose();
		{
			stack.translate(xOffset, yOffset, 0);
			this.recipeSlot.drawHighlight(stack, color);
		}
		stack.popPose();
	}

	@Override
	public boolean isInput() {
		RecipeIngredientRole role = this.recipeSlot.getRole();
		return switch (role) {
			case INPUT, CATALYST -> true;
			case OUTPUT, RENDER_ONLY -> false;
		};
	}

	public RecipeSlot getRecipeSlot() {
		return recipeSlot;
	}
}
