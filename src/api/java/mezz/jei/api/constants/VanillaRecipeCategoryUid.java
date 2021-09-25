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
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICustomCraftingCategoryExtension;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;

/**
 * List of built-in recipe category UIDs, so that plugins with their own recipe handlers can use them.
 */
public final class VanillaRecipeCategoryUid {
	/**
	 * The crafting recipe category.
	 *
	 * Automatically includes all vanilla and Forge recipes.
	 *
	 * To add a shaped recipe extension to this category, it must implement {@link ICraftingCategoryExtension#getSize()}.
	 *
	 * To override the normal behavior of the crafting recipe category, you can implement {@link ICustomCraftingCategoryExtension}
	 */
	public static final ResourceLocation CRAFTING = new ResourceLocation(ModIds.MINECRAFT_ID, "crafting");

	/**
	 * The stonecutting recipe category
	 *
	 * Automatically includes every {@link StonecutterRecipe}
	 */
	public static final ResourceLocation STONECUTTING = new ResourceLocation(ModIds.MINECRAFT_ID, "stonecutting");

	/**
	 * The furnace recipe category.
	 *
	 * Automatically includes every {@link SmeltingRecipe}
	 */
	public static final ResourceLocation FURNACE = new ResourceLocation(ModIds.MINECRAFT_ID, "furnace");

	/**
	 * The smoking recipe category.
	 *
	 * Automatically includes every {@link SmokingRecipe}
	 */
	public static final ResourceLocation SMOKING = new ResourceLocation(ModIds.MINECRAFT_ID, "smoking");

	/**
	 * The blasting recipe category.
	 *
	 * Automatically includes every {@link BlastingRecipe}
	 */
	public static final ResourceLocation BLASTING = new ResourceLocation(ModIds.MINECRAFT_ID, "blasting");

	/**
	 * The campfire furnace recipe category.
	 *
	 * Automatically includes every {@link CampfireCookingRecipe}
	 */
	public static final ResourceLocation CAMPFIRE = new ResourceLocation(ModIds.MINECRAFT_ID, "campfire");

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
	 *
	 * Also automatically adds modded potions from {@link BrewingRecipeRegistry#getRecipes()}.
	 * JEI can only understand modded potion recipes that are built into vanilla or Forge.
	 */
	public static final ResourceLocation BREWING = new ResourceLocation(ModIds.MINECRAFT_ID, "brewing");

	/**
	 * The anvil recipe category.
	 *
	 * This is a built-in category, you can create new recipes with {@link IVanillaRecipeFactory#createAnvilRecipe(ItemStack, List, List)}
	 */
	public static final ResourceLocation ANVIL = new ResourceLocation(ModIds.MINECRAFT_ID, "anvil");

	/**
	 * The smithing recipe category.
	 *
	 * Automatically includes every {@link UpgradeRecipe}.
	 * @since JEI 7.3.1
	 */
	public static final ResourceLocation SMITHING = new ResourceLocation(ModIds.MINECRAFT_ID, "smithing");

	/**
	 * The sompostable recipe category.
	 *
	 * Automatically includes every item added to {@link ComposterBlock#COMPOSTABLES}.
	 * @since JEI 8.1.0
	 */
	public static final ResourceLocation COMPOSTABLE = new ResourceLocation(ModIds.MINECRAFT_ID, "compostable");

	/**
	 * The JEI info recipe category shows extra information about ingredients.
	 *
	 * This is a built-in category, you can add new recipes with
	 * {@link IRecipeRegistration#addIngredientInfo(Object, IIngredientType, net.minecraft.network.chat.Component...)} or
	 * {@link IRecipeRegistration#addIngredientInfo(List, IIngredientType, net.minecraft.network.chat.Component...)}
	 */
	public static final ResourceLocation INFORMATION = new ResourceLocation(ModIds.JEI_ID, "information");

	private VanillaRecipeCategoryUid() {

	}
}
