package mezz.jei.library.plugins.vanilla.anvil;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipe;

public class SmithingCategoryExtension<R extends SmithingRecipe> implements ISmithingCategoryExtension<R> {
	private final IPlatformRecipeHelper recipeHelper;

	public SmithingCategoryExtension(IPlatformRecipeHelper recipeHelper) {
		this.recipeHelper = recipeHelper;
	}

	@Override
	public <T extends IIngredientAcceptor<T>> void setBase(R recipe, T ingredientAcceptor) {
		Ingredient ingredient = recipeHelper.getBase(recipe);
		ingredientAcceptor.addIngredients(ingredient);
	}

	@Override
	public <T extends IIngredientAcceptor<T>> void setAddition(R recipe, T ingredientAcceptor) {
		Ingredient ingredient = recipeHelper.getAddition(recipe);
		ingredientAcceptor.addIngredients(ingredient);
	}

	@Override
	public <T extends IIngredientAcceptor<T>> void setTemplate(R recipe, T ingredientAcceptor) {
		Ingredient ingredient = recipeHelper.getTemplate(recipe);
		ingredientAcceptor.addIngredients(ingredient);
	}
}
