package mezz.jei.api.recipe;

import java.util.List;

import mezz.jei.api.IModRegistry;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.wrapper.ICustomCraftingRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.potion.PotionHelper;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.brewing.BrewingOreRecipe;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

/**
 * List of built-in recipe category UIDs, so that plugins with their own recipe handlers can use them.
 */
public class VanillaRecipeCategoryUid {
	/**
	 * The crafting recipe category.
	 * <p>
	 * Automatically includes all {@link ShapedRecipes}, {@link ShapelessRecipes}, {@link ShapedOreRecipe}, and {@link ShapelessOreRecipe}.
	 * <p>
	 * To add a shaped recipe wrapper to this category, it must implement {@link IShapedCraftingRecipeWrapper}.
	 * <p>
	 * To override the normal behavior of the crafting recipe category, you can implement {@link ICustomCraftingRecipeWrapper}
	 */
	public static final String CRAFTING = "minecraft.crafting";

	/**
	 * The smelting recipe category.
	 * <p>
	 * Automatically includes everything from {@link FurnaceRecipes#getSmeltingList()}.
	 */
	public static final String SMELTING = "minecraft.smelting";

	/**
	 * The fuel recipe category.
	 * <p>
	 * Automatically includes everything that returns a value from {@link TileEntityFurnace#getItemBurnTime(ItemStack)}.
	 */
	public static final String FUEL = "minecraft.fuel";

	/**
	 * The brewing recipe category.
	 * <p>
	 * Automatically tries to generate all potion variations from the basic ingredients, determined by {@link PotionHelper#isReagent(ItemStack)}.
	 * You can get the list of known potion reagents from {@link IIngredientRegistry#getPotionIngredients()}.
	 * <p>
	 * Also automatically adds modded potions from {@link BrewingRecipeRegistry#getRecipes()}.
	 * JEI can only understand modded potion recipes that are {@link BrewingRecipe} or {@link BrewingOreRecipe}.
	 */
	public static final String BREWING = "minecraft.brewing";

	/**
	 * The anvil recipe category.
	 * <p>
	 * This is a built-in category, you can add new recipes with {@link IModRegistry#addAnvilRecipe(ItemStack, List, List)}}
	 * @since JEI 4.2.6
	 */
	public static final String ANVIL = "minecraft.anvil";

	/**
	 * The JEI description recipe category.
	 * <p>
	 * This is a built-in category, you can add new recipes with
	 * {@link IModRegistry#addDescription(ItemStack, String...)} or {@link IModRegistry#addDescription(List, String...)}
	 */
	public static final String DESCRIPTION = "jei.description";

	private VanillaRecipeCategoryUid() {

	}
}
