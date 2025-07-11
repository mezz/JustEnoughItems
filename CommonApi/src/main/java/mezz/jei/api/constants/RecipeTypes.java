package mezz.jei.api.constants;

import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IJeiCompostingRecipe;
import mezz.jei.api.recipe.vanilla.IJeiFuelingRecipe;
import mezz.jei.api.recipe.vanilla.IJeiGrindstoneRecipe;
import mezz.jei.api.recipe.vanilla.IJeiIngredientInfoRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.block.ComposterBlock;

/**
 * List of all the built-in {@link IRecipeType}s that are added by JEI.
 *
 * @since 9.5.0
 */
public final class RecipeTypes {
	/**
	 * The crafting recipe type.
	 *
	 * Automatically includes all recipes in the {@link net.minecraft.world.item.crafting.RecipeManager}.
	 *
	 * @since 9.5.0
	 */
	public static final IRecipeHolderType<CraftingRecipe> CRAFTING =
		IRecipeHolderType.create(RecipeType.CRAFTING);

	/**
	 * The stonecutting recipe type.
	 *
	 * Automatically includes every {@link StonecutterRecipe}.
	 *
	 * @since 9.5.0
	 */
	public static final IRecipeHolderType<StonecutterRecipe> STONECUTTING =
		IRecipeHolderType.create(RecipeType.STONECUTTING);

	/**
	 * The smelting recipe type.
	 *
	 * Automatically includes every {@link SmeltingRecipe}.
	 *
	 * @since 9.5.0
	 */
	public static final IRecipeHolderType<SmeltingRecipe> SMELTING =
		IRecipeHolderType.create(RecipeType.SMELTING);

	/**
	 * The smoking recipe type.
	 *
	 * Automatically includes every {@link SmokingRecipe}.
	 *
	 * @since 9.5.0
	 */
	public static final IRecipeHolderType<SmokingRecipe> SMOKING =
		IRecipeHolderType.create(RecipeType.SMOKING);

	/**
	 * The blasting recipe type.
	 *
	 * Automatically includes every {@link BlastingRecipe}.
	 *
	 * @since 9.5.0
	 */
	public static final IRecipeHolderType<BlastingRecipe> BLASTING =
		IRecipeHolderType.create(RecipeType.BLASTING);

	/**
	 * The campfire cooking recipe type.
	 *
	 * Automatically includes every {@link CampfireCookingRecipe}.
	 *
	 * @since 9.5.0
	 */
	public static final IRecipeHolderType<CampfireCookingRecipe> CAMPFIRE_COOKING =
		IRecipeHolderType.create(RecipeType.CAMPFIRE_COOKING);

	/**
	 * The furnace fuel recipe type.
	 *
	 * JEI automatically creates a fuel recipe for anything that has a burn time.
	 *
	 * @since 20.0.0
	 */
	public static final IRecipeType<IJeiFuelingRecipe> SMELTING_FUEL =
		IRecipeType.create(ModIds.MINECRAFT_ID, "smelting_fuel", IJeiFuelingRecipe.class);

	/**
	 * The blast furnace fuel recipe type.
	 *
	 * JEI automatically creates a fuel recipe for anything that has a burn time.
	 *
	 * @since 20.0.0
	 */
	public static final IRecipeType<IJeiFuelingRecipe> BLASTING_FUEL =
		IRecipeType.create(ModIds.MINECRAFT_ID, "blasting_fuel", IJeiFuelingRecipe.class);

	/**
	 * The smoker fuel recipe type.
	 *
	 * JEI automatically creates a fuel recipe for anything that has a burn time.
	 *
	 * @since 20.0.0
	 */
	public static final IRecipeType<IJeiFuelingRecipe> SMOKING_FUEL =
		IRecipeType.create(ModIds.MINECRAFT_ID, "smoking_fuel", IJeiFuelingRecipe.class);

	/**
	 * The brewing recipe type.
	 *
	 * JEI automatically tries to generate all potion variations from the basic ingredients,
	 * and also automatically adds modded potions from the Forge BrewingRecipeRegistry
	 *
	 * @see IVanillaRecipeFactory#createBrewingRecipe to create new brewing recipes in JEI.
	 *
	 * @since 9.5.0
	 */
	public static final IRecipeType<IJeiBrewingRecipe> BREWING =
		IRecipeType.create(ModIds.MINECRAFT_ID, "brewing", IJeiBrewingRecipe.class);

	/**
	 * The anvil recipe type.
	 *
	 * @see IVanillaRecipeFactory#createAnvilRecipe to create new anvil recipes in JEI.
	 *
	 * @since 9.5.0
	 */
	public static final IRecipeType<IJeiAnvilRecipe> ANVIL =
		IRecipeType.create(ModIds.MINECRAFT_ID, "anvil", IJeiAnvilRecipe.class);

	/**
	 * The grindstone recipe type.
	 *
	 * @see IVanillaRecipeFactory#createGrindstoneRecipe to create new grindstone recipes in JEI.
	 *
	 * @since 23.1.0
	 */
	public static final IRecipeType<IJeiGrindstoneRecipe> GRINDSTONE =
			IRecipeType.create(ModIds.MINECRAFT_ID, "grindstone", IJeiGrindstoneRecipe.class);

	/**
	 * The smithing recipe type.
	 * Automatically includes every
	 * {@link net.minecraft.world.item.crafting.SmithingTrimRecipe}
	 * {@link net.minecraft.world.item.crafting.SmithingTransformRecipe}
	 *
	 * @since 9.5.0
	 */
	public static final IRecipeHolderType<SmithingRecipe> SMITHING = IRecipeHolderType.create(RecipeType.SMITHING);

	/**
	 * The composting recipe type.
	 * Automatically includes every item added to {@link ComposterBlock#COMPOSTABLES}.
	 *
	 * @since 9.5.0
	 */
	public static final IRecipeType<IJeiCompostingRecipe> COMPOSTING =
		IRecipeType.create(ModIds.MINECRAFT_ID, "compostable", IJeiCompostingRecipe.class);

	/**
	 * The JEI info recipe type.
	 *
	 * @see IRecipeRegistration#addIngredientInfo to create this type of recipe.
	 *
	 * @since 9.5.0
	 */
	public static final IRecipeType<IJeiIngredientInfoRecipe> INFORMATION =
		IRecipeType.create(ModIds.JEI_ID, "information", IJeiIngredientInfoRecipe.class);

	private RecipeTypes() {}
}
