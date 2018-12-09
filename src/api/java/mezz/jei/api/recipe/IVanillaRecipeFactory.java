package mezz.jei.api.recipe;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Allows creation of vanilla recipes.
 * Get the instance from {@link IJeiHelpers#getStackHelper()}.
 * <p>
 * Use {@link IModRegistry#addRecipes(Collection, String)} to add the recipe.
 *
 * @since JEI 4.5.0
 */
public interface IVanillaRecipeFactory {

	/**
	 * Adds an anvil recipe for the given inputs and output.
	 *
	 * @param leftInput   The itemStack placed on the left slot.
	 * @param rightInputs The itemStack(s) placed on the right slot.
	 * @param outputs     The resulting itemStack(s).
	 * @return the {@link IRecipeWrapper} for this recipe.
	 */
	default IRecipeWrapper createAnvilRecipe(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs) {
		return createAnvilRecipe(Collections.singletonList(leftInput), rightInputs, outputs);
	}

	/**
	 * Adds an anvil recipe for the given inputs and output.
	 *
	 * @param leftInputs   The itemStack(s) placed on the left slot.
	 * @param rightInputs The itemStack(s) placed on the right slot.
	 * @param outputs     The resulting itemStack(s).
	 * @return the {@link IRecipeWrapper} for this recipe.
	 * @since JEI 4.14.1
	 */
	IRecipeWrapper createAnvilRecipe(List<ItemStack> leftInputs, List<ItemStack> rightInputs, List<ItemStack> outputs);

	/**
	 * Create a new smelting recipe.
	 * By default, all smelting recipes from {@link FurnaceRecipes#smeltingList} are already added by JEI.
	 *
	 * @param inputs the list of possible inputs to rotate through
	 * @param output the output
	 * @return the {@link IRecipeWrapper} for this recipe.
	 */
	IRecipeWrapper createSmeltingRecipe(List<ItemStack> inputs, ItemStack output);

	/**
	 * Create a new brewing recipe.
	 * By default, all brewing recipes are already detected and added by JEI.
	 *
	 * @param ingredients  the ingredients added to a potion to create a new one.
	 *                     Normally one ingredient, but a list will display several in rotation.
	 * @param potionInput  the input potion for the brewing recipe.
	 * @param potionOutput the output potion for the brewing recipe.
	 * @return the {@link IRecipeWrapper} for this recipe.
	 */
	IRecipeWrapper createBrewingRecipe(List<ItemStack> ingredients, ItemStack potionInput, ItemStack potionOutput);
}
