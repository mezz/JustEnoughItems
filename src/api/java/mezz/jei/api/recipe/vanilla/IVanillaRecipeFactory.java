package mezz.jei.api.recipe.vanilla;

import java.util.Collection;
import java.util.List;

import net.minecraft.item.ItemStack;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IRecipeRegistration;

/**
 * Allows creation of vanilla recipes.
 * Get the instance from {@link IJeiHelpers#getStackHelper()}.
 *
 * Use {@link IRecipeRegistration#addRecipes(Collection, net.minecraft.util.ResourceLocation)} to add the recipe.
 */
public interface IVanillaRecipeFactory {
	/**
	 * Create an anvil recipe for the given inputs and output.
	 *
	 * @param leftInput   The itemStack placed on the left slot.
	 * @param rightInputs The itemStack(s) placed on the right slot.
	 * @param outputs     The resulting itemStack(s).
	 */
	Object createAnvilRecipe(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs);

	/**
	 * Create an anvil recipe for the given inputs and output.
	 * The number of inputs in the left and right side must match.
	 *
	 * @param leftInputs  The itemStack(s) placed on the left slot.
	 * @param rightInputs The itemStack(s) placed on the right slot.
	 * @param outputs     The resulting itemStack(s).
	 */
	Object createAnvilRecipe(List<ItemStack> leftInputs, List<ItemStack> rightInputs, List<ItemStack> outputs);

	/**
	 * Create a new brewing recipe.
	 * By default, all brewing recipes are already detected and added by JEI.
	 *
	 * @param ingredients  the ingredients added to a potion to create a new one.
	 *                     Normally one ingredient, but a list will display several in rotation.
	 * @param potionInput  the input potion for the brewing recipe.
	 * @param potionOutput the output potion for the brewing recipe.
	 */
	IJeiBrewingRecipe createBrewingRecipe(List<ItemStack> ingredients, ItemStack potionInput, ItemStack potionOutput);

	/**
	 * Create a new brewing recipe.
	 * By default, all brewing recipes are already detected and added by JEI.
	 *
	 * @param ingredients  the ingredients added to a potion to create a new one.
	 *                     Normally one ingredient, but a list will display several in rotation.
	 * @param potionInputs the input potions for the brewing recipe.
	 * @param potionOutput the output potion for the brewing recipe.
	 */
	IJeiBrewingRecipe createBrewingRecipe(List<ItemStack> ingredients, List<ItemStack> potionInputs, ItemStack potionOutput);
}
