package mezz.jei.library.plugins.vanilla;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.plugins.vanilla.anvil.AnvilRecipe;
import mezz.jei.library.plugins.vanilla.brewing.BrewingRecipeUtil;
import mezz.jei.library.plugins.vanilla.brewing.JeiBrewingRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class VanillaRecipeFactory implements IVanillaRecipeFactory {
	private final BrewingRecipeUtil brewingRecipeUtil;

	public VanillaRecipeFactory(IIngredientManager ingredientManager) {
		IIngredientHelper<ItemStack> ingredientHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);
		this.brewingRecipeUtil = new BrewingRecipeUtil(ingredientHelper);
	}

	@Override
	public AnvilRecipe createAnvilRecipe(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs) {
		ErrorUtil.checkNotEmpty(leftInput, "leftInput");
		ErrorUtil.checkNotNull(rightInputs, "rightInputs");
		ErrorUtil.checkNotEmpty(outputs, "outputs");

		return new AnvilRecipe(List.of(leftInput), rightInputs, outputs);
	}

	@Override
	public AnvilRecipe createAnvilRecipe(List<ItemStack> leftInputs, List<ItemStack> rightInputs, List<ItemStack> outputs) {
		ErrorUtil.checkNotEmpty(leftInputs, "leftInput");
		ErrorUtil.checkNotNull(rightInputs, "rightInputs");
		ErrorUtil.checkNotEmpty(outputs, "outputs");

		return new AnvilRecipe(leftInputs, rightInputs, outputs);
	}

	@Override
	public IJeiBrewingRecipe createBrewingRecipe(List<ItemStack> ingredients, ItemStack potionInput, ItemStack potionOutput) {
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");
		ErrorUtil.checkNotEmpty(potionInput, "potionInput");
		ErrorUtil.checkNotEmpty(potionOutput, "potionOutput");

		List<ItemStack> potionInputs = List.of(potionInput);
		return new JeiBrewingRecipe(ingredients, potionInputs, potionOutput, brewingRecipeUtil);
	}

	@Override
	public IJeiBrewingRecipe createBrewingRecipe(List<ItemStack> ingredients, List<ItemStack> potionInputs, ItemStack potionOutput) {
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");
		ErrorUtil.checkNotEmpty(potionInputs, "potionInputs");
		ErrorUtil.checkNotEmpty(potionOutput, "potionOutput");

		return new JeiBrewingRecipe(ingredients, potionInputs, potionOutput, brewingRecipeUtil);
	}
}
