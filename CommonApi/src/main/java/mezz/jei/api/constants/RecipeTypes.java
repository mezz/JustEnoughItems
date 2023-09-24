package mezz.jei.api.constants;

import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IJeiCompostingRecipe;
import mezz.jei.api.recipe.vanilla.IJeiFuelingRecipe;
import mezz.jei.api.recipe.vanilla.IJeiIngredientInfoRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
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
	public static final RecipeType<RecipeHolder<CraftingRecipe>> CRAFTING =
		RecipeType.createFromVanilla(net.minecraft.world.item.crafting.RecipeType.CRAFTING);

	/**
	 * The stonecutting recipe type.
	 *
	 * Automatically includes every {@link StonecutterRecipe}.
	 *
	 * @since 9.5.0
	 */
	public static final RecipeType<RecipeHolder<StonecutterRecipe>> STONECUTTING =
		RecipeType.createFromVanilla(net.minecraft.world.item.crafting.RecipeType.STONECUTTING);

	/**
	 * The smelting recipe type.
	 *
	 * Automatically includes every {@link SmeltingRecipe}.
	 *
	 * @since 9.5.0
	 */
	public static final RecipeType<RecipeHolder<SmeltingRecipe>> SMELTING =
		RecipeType.createFromVanilla(net.minecraft.world.item.crafting.RecipeType.SMELTING);

	/**
	 * The smoking recipe type.
	 *
	 * Automatically includes every {@link SmokingRecipe}.
	 *
	 * @since 9.5.0
	 */
	public static final RecipeType<RecipeHolder<SmokingRecipe>> SMOKING =
		RecipeType.createFromVanilla(net.minecraft.world.item.crafting.RecipeType.SMOKING);

	/**
	 * The blasting recipe type.
	 *
	 * Automatically includes every {@link BlastingRecipe}.
	 *
	 * @since 9.5.0
	 */
	public static final RecipeType<RecipeHolder<BlastingRecipe>> BLASTING =
		RecipeType.createFromVanilla(net.minecraft.world.item.crafting.RecipeType.BLASTING);

	/**
	 * The campfire cooking recipe type.
	 *
	 * Automatically includes every {@link CampfireCookingRecipe}.
	 *
	 * @since 9.5.0
	 */
	public static final RecipeType<RecipeHolder<CampfireCookingRecipe>> CAMPFIRE_COOKING =
		RecipeType.createFromVanilla(net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING);

	/**
	 * The fueling recipe type.
	 *
	 * JEI automatically creates a fuel recipe for anything that has a burn time.
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
	 * Automatically includes every
	 * {@link net.minecraft.world.item.crafting.SmithingTrimRecipe}
	 * {@link net.minecraft.world.item.crafting.SmithingTransformRecipe}
	 *
	 * @since 9.5.0
	 */
	public static final RecipeType<RecipeHolder<SmithingRecipe>> SMITHING =
		RecipeType.createFromVanilla(net.minecraft.world.item.crafting.RecipeType.SMITHING);

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
