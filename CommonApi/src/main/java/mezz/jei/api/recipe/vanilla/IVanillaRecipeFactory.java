package mezz.jei.api.recipe.vanilla;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The {@link IVanillaRecipeFactory} allows creation of vanilla recipes.
 * Get the instance from {@link IJeiHelpers#getVanillaRecipeFactory()} or {@link IRecipeRegistration#getVanillaRecipeFactory()}.
 * <p>
 * Use {@link IRecipeRegistration#addRecipes(RecipeType, List)} to add the recipe.
 */
@ApiStatus.NonExtendable
public interface IVanillaRecipeFactory {
	/**
	 * Create an anvil recipe for the given inputs and output.
	 *
	 * @param leftInput   The itemStack placed on the left slot.
	 * @param rightInputs The itemStack(s) placed on the right slot.
	 * @param outputs     The resulting itemStack(s).
	 * @param uid		  The unique ID for this recipe.
	 *
	 * @since 11.26.0
	 */
	IJeiAnvilRecipe createAnvilRecipe(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs, @Nullable ResourceLocation uid);

	/**
	 * Create an anvil recipe for the given inputs and output.
	 * The number of inputs in the left and right side must match.
	 *
	 * @param leftInputs  The itemStack(s) placed on the left slot.
	 * @param rightInputs The itemStack(s) placed on the right slot.
	 * @param outputs     The resulting itemStack(s).
	 * @param uid		  The unique ID for this recipe.
	 *
	 * @since 11.26.0
	 */
	IJeiAnvilRecipe createAnvilRecipe(List<ItemStack> leftInputs, List<ItemStack> rightInputs, List<ItemStack> outputs, ResourceLocation uid);

	/**
	 * Create a smelting recipe that accepts any normal furnace fuel.
	 *
	 * @param input       The ingredient placed in the furnace input slot.
	 * @param output      The resulting item stack placed in the furnace result slot.
	 * @param cookingTime The cooking time in ticks. Must be greater than 0.
	 * @param experience  The experience granted by the recipe. Must be greater than or equal to 0.
	 * @param uid         The unique ID for this recipe.
	 *
	 * @since 11.56.0
	 */
	default SmeltingRecipe createSmeltingRecipe(
		Ingredient input,
		ItemStack output,
		int cookingTime,
		float experience,
		ResourceLocation uid
	) {
		return createSmeltingRecipe(input, Ingredient.EMPTY, ItemStack.EMPTY, output, cookingTime, experience, uid);
	}

	/**
	 * Create a smelting recipe with a custom fuel and optional fuel output.
	 * <p>
	 * Use {@link Ingredient#EMPTY} for the fuel and {@link ItemStack#EMPTY} for the fuel output
	 * to accept all normal furnace fuels without a fuel transformation.
	 *
	 * @param input       The ingredient placed in the furnace input slot.
	 * @param fuel        The ingredient placed in the furnace fuel slot, or empty to accept any normal furnace fuel.
	 * @param fuelOutput  The item left by the fuel when cooking completes, or empty for no fuel output.
	 * @param output      The resulting item stack placed in the furnace result slot.
	 * @param cookingTime The cooking time in ticks. Must be greater than 0.
	 * @param experience  The experience granted by the recipe. Must be greater than or equal to 0.
	 * @param uid         The unique ID for this recipe.
	 *
	 * @since 11.56.0
	 */
	SmeltingRecipe createSmeltingRecipe(
		Ingredient input,
		Ingredient fuel,
		ItemStack fuelOutput,
		ItemStack output,
		int cookingTime,
		float experience,
		ResourceLocation uid
	);

	/**
	 * Create a new brewing recipe.
	 * JEI automatically detects supported brewing mixtures.
	 * Custom platform brewing recipe types must register an extension with
	 * {@link IVanillaCategoryExtensionRegistration#getBrewingCategory()}.
	 *
	 * @param ingredients  the ingredients added to a potion to create a new one.
	 *                     Normally one ingredient, but a list will display several in rotation.
	 * @param potionInput  the input potion for the brewing recipe.
	 * @param potionOutput the output potion for the brewing recipe.
	 * @param uid		  The unique ID for this recipe.
	 *
	 * @since 11.26.0
	 */
	IJeiBrewingRecipe createBrewingRecipe(List<ItemStack> ingredients, ItemStack potionInput, ItemStack potionOutput, ResourceLocation uid);

	/**
	 * Create a new brewing recipe.
	 * JEI automatically detects supported brewing mixtures.
	 * Custom platform brewing recipe types must register an extension with
	 * {@link IVanillaCategoryExtensionRegistration#getBrewingCategory()}.
	 *
	 * @param ingredients  the ingredients added to a potion to create a new one.
	 *                     Normally one ingredient, but a list will display several in rotation.
	 * @param potionInputs the input potions for the brewing recipe.
	 * @param potionOutput the output potion for the brewing recipe.
	 * @param uid		  The unique ID for this recipe.
	 *
	 * @since 11.26.0
	 */
	IJeiBrewingRecipe createBrewingRecipe(List<ItemStack> ingredients, List<ItemStack> potionInputs, ItemStack potionOutput, ResourceLocation uid);

	/**
	 * Builds a serializable ShapedRecipe that isn't registered with the vanilla game.
	 * Useful for generating crafting recipes from {@link IRecipeManagerPlugin}.
	 *
	 * @since 11.47.0
	 */
	IJeiShapedRecipeBuilder createShapedRecipeBuilder(List<ItemStack> results);

	/**
	 * Create an anvil recipe for the given inputs and output.
	 *
	 * @param leftInput   The itemStack placed on the left slot.
	 * @param rightInputs The itemStack(s) placed on the right slot.
	 * @param outputs     The resulting itemStack(s).
	 *
	 * @deprecated use {@link #createAnvilRecipe(ItemStack, List, List, ResourceLocation)}
	 */
	@Deprecated(since = "11.26.0")
	IJeiAnvilRecipe createAnvilRecipe(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs);

	/**
	 * Create an anvil recipe for the given inputs and output.
	 * The number of inputs in the left and right side must match.
	 *
	 * @param leftInputs  The itemStack(s) placed on the left slot.
	 * @param rightInputs The itemStack(s) placed on the right slot.
	 * @param outputs     The resulting itemStack(s).
	 *
	 * @deprecated use {@link #createAnvilRecipe(List, List, List, ResourceLocation)}
	 */
	@Deprecated(since = "11.26.0")
	IJeiAnvilRecipe createAnvilRecipe(List<ItemStack> leftInputs, List<ItemStack> rightInputs, List<ItemStack> outputs);

	/**
	 * Create a grindstone recipe for the given inputs and output.
	 * The number of inputs in the top and bottom must match.
	 *
	 * @param topInputs    The itemStack(s) placed on the top slot.
	 * @param bottomInputs The itemStack(s) placed on the bottom slot.
	 * @param outputs      The resulting itemStack(s).
	 * @param minXp        The minimum amount of XP that a player can receive.
	 * @param maxXp        The maximum amount of XP that a player can receive.
	 * @param uid          The unique ID for this recipe.
	 *
	 * @since 11.18.0
	 */
	IJeiGrindstoneRecipe createGrindstoneRecipe(List<ItemStack> topInputs, List<ItemStack> bottomInputs, List<ItemStack> outputs, int minXp, int maxXp, ResourceLocation uid);

	/**
	 * Create a new brewing recipe.
	 * JEI automatically detects supported brewing mixtures.
	 * Custom platform brewing recipe types must register an extension with
	 * {@link IVanillaCategoryExtensionRegistration#getBrewingCategory()}.
	 *
	 * @param ingredients  the ingredients added to a potion to create a new one.
	 *                     Normally one ingredient, but a list will display several in rotation.
	 * @param potionInput  the input potion for the brewing recipe.
	 * @param potionOutput the output potion for the brewing recipe.
	 *
	 * @deprecated use {@link #createBrewingRecipe(List, ItemStack, ItemStack, ResourceLocation)}
	 */
	@Deprecated(since = "11.26.0")
	IJeiBrewingRecipe createBrewingRecipe(List<ItemStack> ingredients, ItemStack potionInput, ItemStack potionOutput);

	/**
	 * Create a new brewing recipe.
	 * JEI automatically detects supported brewing mixtures.
	 * Custom platform brewing recipe types must register an extension with
	 * {@link IVanillaCategoryExtensionRegistration#getBrewingCategory()}.
	 *
	 * @param ingredients  the ingredients added to a potion to create a new one.
	 *                     Normally one ingredient, but a list will display several in rotation.
	 * @param potionInputs the input potions for the brewing recipe.
	 * @param potionOutput the output potion for the brewing recipe.
	 *
	 * @deprecated use {@link #createBrewingRecipe(List, List, ItemStack, ResourceLocation)}
	 */
	@Deprecated(since = "11.26.0")
	IJeiBrewingRecipe createBrewingRecipe(List<ItemStack> ingredients, List<ItemStack> potionInputs, ItemStack potionOutput);
}
