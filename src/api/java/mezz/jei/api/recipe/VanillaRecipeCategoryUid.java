package mezz.jei.api.recipe;

import java.util.List;

import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.IModRegistry;
import mezz.jei.api.ModIds;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.wrapper.ICustomCraftingRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;

/**
 * List of built-in recipe category UIDs, so that plugins with their own recipe handlers can use them.
 */
public final class VanillaRecipeCategoryUid {
	/**
	 * The crafting recipe category.
	 *
	 * Automatically includes all vanilla and Forge recipes.
	 *
	 * To add a shaped recipe wrapper to this category, it must implement {@link IShapedCraftingRecipeWrapper}.
	 *
	 * To override the normal behavior of the crafting recipe category, you can implement {@link ICustomCraftingRecipeWrapper}
	 */
	public static final ResourceLocation CRAFTING = new ResourceLocation(ModIds.MINECRAFT_ID, "crafting");

	/**
	 * The furnace recipe category.
	 *
	 * Automatically includes every {@link FurnaceRecipe}
	 */
	public static final ResourceLocation FURNACE = new ResourceLocation(ModIds.MINECRAFT_ID, "furnace");

	/**
	 * The fuel recipe category.
	 *
	 * Automatically includes everything that has a burn time.
	 */
	public static final ResourceLocation FUEL = new ResourceLocation(ModIds.MINECRAFT_ID, "fuel");

	/**
	 * The brewing recipe category.
	 *
	 * Automatically tries to generate all potion variations from the basic ingredients.
	 * You can get the list of known potion reagents from {@link IIngredientRegistry#getPotionIngredients()}.
	 *
	 * Also automatically adds modded potions from {@link BrewingRecipeRegistry#getRecipes()}.
	 * JEI can only understand modded potion recipes that are built into vanilla or Forge.
	 */
	public static final ResourceLocation BREWING = new ResourceLocation(ModIds.MINECRAFT_ID, "brewing");

	/**
	 * The anvil recipe category.
	 *
	 * This is a built-in category, you can create new recipes with {@link IVanillaRecipeFactory#createAnvilRecipe(ItemStack, List, List)}
	 *
	 * @since JEI 4.2.6
	 */
	public static final ResourceLocation ANVIL = new ResourceLocation(ModIds.MINECRAFT_ID, "anvil");

	/**
	 * The JEI info recipe category shows extra information about ingredients.
	 *
	 * This is a built-in category, you can add new recipes with
	 * {@link IModRegistry#addIngredientInfo(Object, IIngredientType, String...)}   or {@link IModRegistry#addIngredientInfo(List, IIngredientType, String...)}
	 *
	 * @since JEI 4.5.0
	 */
	public static final ResourceLocation INFORMATION = new ResourceLocation(ModIds.JEI_ID, "information");

	private VanillaRecipeCategoryUid() {

	}
}
