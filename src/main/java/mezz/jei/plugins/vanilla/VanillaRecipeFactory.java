package mezz.jei.plugins.vanilla;

import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientManager;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IJeiBrewingRecipe;
import mezz.jei.api.recipe.IVanillaRecipeFactory;
import mezz.jei.plugins.vanilla.anvil.AnvilRecipe;
import mezz.jei.plugins.vanilla.brewing.BrewingRecipe;
import mezz.jei.plugins.vanilla.brewing.BrewingRecipeUtil;
import mezz.jei.util.ErrorUtil;

public class VanillaRecipeFactory implements IVanillaRecipeFactory {
	private final BrewingRecipeUtil brewingRecipeUtil;

	public VanillaRecipeFactory(IIngredientManager ingredientManager) {
		IIngredientHelper<ItemStack> ingredientHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM);
		this.brewingRecipeUtil = new BrewingRecipeUtil(ingredientHelper);
	}

	@Override
	public AnvilRecipe createAnvilRecipe(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs) {
		ErrorUtil.checkNotEmpty(leftInput, "leftInput");
		ErrorUtil.checkNotEmpty(rightInputs, "rightInputs");
		ErrorUtil.checkNotEmpty(outputs, "outputs");

		return new AnvilRecipe(Collections.singletonList(leftInput), rightInputs, outputs);
	}

	@Override
	public AnvilRecipe createAnvilRecipe(List<ItemStack> leftInputs, List<ItemStack> rightInputs, List<ItemStack> outputs) {
		ErrorUtil.checkNotEmpty(leftInputs, "leftInput");
		ErrorUtil.checkNotEmpty(rightInputs, "rightInputs");
		ErrorUtil.checkNotEmpty(outputs, "outputs");

		return new AnvilRecipe(leftInputs, rightInputs, outputs);
	}

	@Override
	public IJeiBrewingRecipe createBrewingRecipe(List<ItemStack> ingredients, ItemStack potionInput, ItemStack potionOutput) {
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");
		ErrorUtil.checkNotEmpty(potionInput, "potionInput");
		ErrorUtil.checkNotEmpty(potionOutput, "potionOutput");

		return new BrewingRecipe(ingredients, potionInput, potionOutput, brewingRecipeUtil);
	}
}
