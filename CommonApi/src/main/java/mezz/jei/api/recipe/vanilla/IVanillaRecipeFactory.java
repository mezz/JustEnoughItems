package mezz.jei.api.recipe.vanilla;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The {@link IVanillaRecipeFactory} allows creation of vanilla recipes.
 * Get the instance from {@link IJeiHelpers#getStackHelper()} or {@link IRecipeRegistration#getVanillaRecipeFactory()}.
 * <p>
 * Use {@link IRecipeRegistration#addRecipes(IRecipeType, List)} to add the recipe.
 */
public interface IVanillaRecipeFactory {
	/**
	 * Create an anvil recipe for the given inputs and output.
	 *
	 * @param leftInput   The itemStack placed on the left slot.
	 * @param rightInputs The itemStack(s) placed on the right slot.
	 * @param outputs     The resulting itemStack(s).
	 * @param uid		  The unique ID for this recipe.
	 *
	 * @since 19.1.0
	 */
	IJeiAnvilRecipe createAnvilRecipe(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs, @Nullable Identifier uid);

	/**
	 * Create an anvil recipe for the given inputs and output.
	 * The number of inputs in the left and right side must match.
	 *
	 * @param leftInputs  The itemStack(s) placed on the left slot.
	 * @param rightInputs The itemStack(s) placed on the right slot.
	 * @param outputs     The resulting itemStack(s).
	 * @param uid		  The unique ID for this recipe.
	 *
	 * @since 19.1.0
	 */
	IJeiAnvilRecipe createAnvilRecipe(List<ItemStack> leftInputs, List<ItemStack> rightInputs, List<ItemStack> outputs, Identifier uid);

	/**
	 * Create a grindstone recipe for the given inputs and output.
	 * The number of inputs in the top and bottom must match.
	 *
	 * @param topInputs    The itemStack(s) placed on the top slot.
	 * @param bottomInputs The itemStack(s) placed on the bottom slot.
	 * @param outputs      The resulting itemStack(s).
	 * @param minXp        The minimum amount of XP that a player can receive.
	 * @param maxXp        The maximum amount of XP that a player can receive.
	 * @param uid		   The unique ID for this recipe.
	 *
	 * @since 23.1.0
	 */
	IJeiGrindstoneRecipe createGrindstoneRecipe(List<ItemStack> topInputs, List<ItemStack> bottomInputs, List<ItemStack> outputs, int minXp, int maxXp, Identifier uid);

	/**
	 * Create a new brewing recipe.
	 * By default, all brewing recipes are already detected and added by JEI.
	 *
	 * @param ingredients  the ingredients added to a potion to create a new one.
	 *                     Normally one ingredient, but a list will display several in rotation.
	 * @param potionInput  the input potion for the brewing recipe.
	 * @param potionOutput the output potion for the brewing recipe.
	 * @param uid		  The unique ID for this recipe.
	 *
	 * @since 19.1.0
	 */
	IJeiBrewingRecipe createBrewingRecipe(List<ItemStack> ingredients, ItemStack potionInput, ItemStack potionOutput, Identifier uid);

	/**
	 * Create a new brewing recipe.
	 * By default, all brewing recipes are already detected and added by JEI.
	 *
	 * @param ingredients  the ingredients added to a potion to create a new one.
	 *                     Normally one ingredient, but a list will display several in rotation.
	 * @param potionInputs the input potions for the brewing recipe.
	 * @param potionOutput the output potion for the brewing recipe.
	 * @param uid		  The unique ID for this recipe.
	 *
	 * @since 19.1.0
	 */
	IJeiBrewingRecipe createBrewingRecipe(List<ItemStack> ingredients, List<ItemStack> potionInputs, ItemStack potionOutput, Identifier uid);

	/**
	 * Builds a serializable ShapedRecipe that isn't registered with the vanilla game.
	 * Useful for generating crafting recipes from {@link IRecipeManagerPlugin}.
	 *
	 * @since 20.0.0
	 */
	IJeiShapedRecipeBuilder createShapedRecipeBuilder(CraftingBookCategory category, SlotDisplay results);
}
