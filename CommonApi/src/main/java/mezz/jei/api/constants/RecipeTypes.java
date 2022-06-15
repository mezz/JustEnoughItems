package mezz.jei.api.constants;

import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IJeiCompostingRecipe;
import mezz.jei.api.recipe.vanilla.IJeiFuelingRecipe;
import mezz.jei.api.recipe.vanilla.IJeiIngredientInfoRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.block.ComposterBlock;

/**
 * List of all the built-in {@link RecipeType}s that are added by JEI.
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
	public static final RecipeType<CraftingRecipe> CRAFTING =
		RecipeType.create(ModIds.MINECRAFT_ID, "crafting", CraftingRecipe.class);

	/**
	 * The stonecutting recipe type.
	 *
	 * Automatically includes every {@link StonecutterRecipe}.
	 *
	 * @since 9.5.0
	 */
	public static final RecipeType<StonecutterRecipe> STONECUTTING =
		RecipeType.create(ModIds.MINECRAFT_ID, "stonecutting", StonecutterRecipe.class);

	/**
	 * The smelting recipe type.
	 *
	 * Automatically includes every {@link SmeltingRecipe}.
	 *
	 * @since 9.5.0
	 */
	public static final RecipeType<SmeltingRecipe> SMELTING =
		RecipeType.create(ModIds.MINECRAFT_ID, "furnace", SmeltingRecipe.class);

	/**
	 * The smoking recipe type.
	 *
	 * Automatically includes every {@link SmokingRecipe}.
	 *
	 * @since 9.5.0
	 */
	public static final RecipeType<SmokingRecipe> SMOKING =
		RecipeType.create(ModIds.MINECRAFT_ID, "smoking", SmokingRecipe.class);

	/**
	 * The blasting recipe type.
	 *
	 * Automatically includes every {@link BlastingRecipe}.
	 *
	 * @since 9.5.0
	 */
	public static final RecipeType<BlastingRecipe> BLASTING =
		RecipeType.create(ModIds.MINECRAFT_ID, "blasting", BlastingRecipe.class);

	/**
	 * The campfire cooking recipe type.
	 *
	 * Automatically includes every {@link CampfireCookingRecipe}.
	 *
	 * @since 9.5.0
	 */
	public static final RecipeType<CampfireCookingRecipe> CAMPFIRE_COOKING =
		RecipeType.create(ModIds.MINECRAFT_ID, "campfire", CampfireCookingRecipe.class);

	/**
	 * The fueling recipe type.
	 *
	 * JEI automatically creates a fuel recipe for anything that has a burn time.
	 * @see Item#getBurnTime
	 *
	 * @since 9.5.0
	 */
	public static final RecipeType<IJeiFuelingRecipe> FUELING =
		RecipeType.create(ModIds.MINECRAFT_ID, "fuel", IJeiFuelingRecipe.class);

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
	public static final RecipeType<IJeiBrewingRecipe> BREWING =
		RecipeType.create(ModIds.MINECRAFT_ID, "brewing", IJeiBrewingRecipe.class);

	/**
	 * The anvil recipe type.
	 *
	 * @see IVanillaRecipeFactory#createAnvilRecipe to create new anvil recipes in JEI.
	 *
	 * @since 9.5.0
	 */
	public static final RecipeType<IJeiAnvilRecipe> ANVIL =
		RecipeType.create(ModIds.MINECRAFT_ID, "anvil", IJeiAnvilRecipe.class);

	/**
	 * The smithing recipe type.
	 * Automatically includes every {@link UpgradeRecipe}.
	 *
	 * @since 9.5.0
	 */
	public static final RecipeType<UpgradeRecipe> SMITHING =
		RecipeType.create(ModIds.MINECRAFT_ID, "smithing", UpgradeRecipe.class);

	/**
	 * The composting recipe type.
	 * Automatically includes every item added to {@link ComposterBlock#COMPOSTABLES}.
	 *
	 * @since 9.5.0
	 */
	public static final RecipeType<IJeiCompostingRecipe> COMPOSTING =
		RecipeType.create(ModIds.MINECRAFT_ID, "compostable", IJeiCompostingRecipe.class);

	/**
	 * The JEI info recipe type.
	 *
	 * @see IRecipeRegistration#addIngredientInfo to create this type of recipe.
	 *
	 * @since 9.5.0
	 */
	public static final RecipeType<IJeiIngredientInfoRecipe> INFORMATION =
		RecipeType.create(ModIds.JEI_ID, "information", IJeiIngredientInfoRecipe.class);

	private RecipeTypes() {}
}
