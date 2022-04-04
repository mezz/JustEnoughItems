package mezz.jei.api.constants;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.block.ComposterBlock;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;

/**
 * List of built-in recipe category UIDs, so that plugins with their own recipe handlers can use them.
 *
 * @deprecated This has been replaced by {@link RecipeTypes}.
 */
@Deprecated(forRemoval = true, since = "9.5.0")
public final class VanillaRecipeCategoryUid {
	/**
	 * The crafting recipe category.
	 *
	 * Automatically includes all vanilla and Forge recipes.
	 *
	 * To add a shaped recipe extension to this category, it must implement
	 * {@link ICraftingCategoryExtension#getWidth()} and {@link ICraftingCategoryExtension#getHeight()}.
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	public static final ResourceLocation CRAFTING = RecipeTypes.CRAFTING.getUid();

	/**
	 * The stonecutting recipe category
	 *
	 * Automatically includes every {@link StonecutterRecipe}
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	public static final ResourceLocation STONECUTTING = RecipeTypes.STONECUTTING.getUid();

	/**
	 * The furnace recipe category.
	 *
	 * Automatically includes every {@link SmeltingRecipe}
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	public static final ResourceLocation FURNACE = RecipeTypes.SMELTING.getUid();

	/**
	 * The smoking recipe category.
	 *
	 * Automatically includes every {@link SmokingRecipe}
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	public static final ResourceLocation SMOKING = RecipeTypes.SMOKING.getUid();

	/**
	 * The blasting recipe category.
	 *
	 * Automatically includes every {@link BlastingRecipe}
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	public static final ResourceLocation BLASTING = RecipeTypes.BLASTING.getUid();

	/**
	 * The campfire furnace recipe category.
	 *
	 * Automatically includes every {@link CampfireCookingRecipe}
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	public static final ResourceLocation CAMPFIRE = RecipeTypes.CAMPFIRE_COOKING.getUid();

	/**
	 * The fuel recipe category.
	 *
	 * Automatically includes everything that has a burn time.
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	public static final ResourceLocation FUEL = RecipeTypes.FUELING.getUid();

	/**
	 * The brewing recipe category.
	 *
	 * Automatically tries to generate all potion variations from the basic ingredients.
	 *
	 * Also automatically adds modded potions from the Forge BrewingRecipeRegistry.
	 * JEI can only understand modded potion recipes that are built into vanilla or Forge.
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	public static final ResourceLocation BREWING = RecipeTypes.BREWING.getUid();

	/**
	 * The anvil recipe category.
	 *
	 * This is a built-in category, you can create new recipes with {@link IVanillaRecipeFactory#createAnvilRecipe(ItemStack, List, List)}
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	public static final ResourceLocation ANVIL = RecipeTypes.ANVIL.getUid();

	/**
	 * The smithing recipe category.
	 *
	 * Automatically includes every {@link UpgradeRecipe}.
	 * @since 7.3.1
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	public static final ResourceLocation SMITHING = RecipeTypes.SMITHING.getUid();

	/**
	 * The compostable recipe category.
	 *
	 * Automatically includes every item added to {@link ComposterBlock#COMPOSTABLES}.
	 * @since 8.1.0
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	public static final ResourceLocation COMPOSTABLE = RecipeTypes.COMPOSTING.getUid();

	/**
	 * The JEI info recipe category shows extra information about ingredients.
	 *
	 * This is a built-in category, you can add new recipes with
	 * {@link IRecipeRegistration#addIngredientInfo(Object, IIngredientType, net.minecraft.network.chat.Component...)} or
	 * {@link IRecipeRegistration#addIngredientInfo(List, IIngredientType, net.minecraft.network.chat.Component...)}
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	public static final ResourceLocation INFORMATION = RecipeTypes.INFORMATION.getUid();

	private VanillaRecipeCategoryUid() {

	}
}
