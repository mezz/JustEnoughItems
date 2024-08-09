package mezz.jei.library.plugins.vanilla.anvil;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.library.util.RecipeUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;

public class SmithingCategoryExtension<R extends SmithingRecipe> implements ISmithingCategoryExtension<R> {
	private final IPlatformRecipeHelper recipeHelper;

	public SmithingCategoryExtension(IPlatformRecipeHelper recipeHelper) {
		this.recipeHelper = recipeHelper;
	}

	@Override
	public <T extends IIngredientAcceptor<T>> void setTemplate(R recipe, T ingredientAcceptor) {
		Ingredient ingredient = recipeHelper.getTemplate(recipe);
		ingredientAcceptor.addIngredients(ingredient);
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
	public <T extends IIngredientAcceptor<T>> void setOutput(R recipe, T ingredientAcceptor) {
		Ingredient templateIngredient = recipeHelper.getTemplate(recipe);
		Ingredient baseIngredient = recipeHelper.getBase(recipe);
		Ingredient additionIngredient = recipeHelper.getAddition(recipe);

		ItemStack[] additions = additionIngredient.getItems();
		if (additions.length == 0) {
			return;
		}
		ItemStack addition = additions[0];

		for (ItemStack template : templateIngredient.getItems()) {
			for (ItemStack base : baseIngredient.getItems()) {
				SmithingRecipeInput recipeInput = new SmithingRecipeInput(template, base, addition);
				ItemStack output = RecipeUtil.assembleResultItem(recipeInput, recipe);
				ingredientAcceptor.addItemStack(output);
			}
		}
	}
}
