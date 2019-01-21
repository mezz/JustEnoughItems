package mezz.jei.api;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.ingredients.ISubtypeRegistry;
import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.category.extensions.ICraftingRecipeWrapper;

/**
 * The main class to implement to create a JEI plugin. Everything communicated between a mod and JEI is through this class.
 * IModPlugins must have the {@link JeiPlugin} annotation to get loaded by JEI.
 */
public interface IModPlugin {

	ResourceLocation getPluginUid();

	/**
	 * If your item has subtypes that depend on NBT or capabilities, use this to help JEI identify those subtypes correctly.
	 */
	default void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {

	}

	/**
	 * Register special ingredients, beyond the basic ItemStack and FluidStack.
	 */
	default void registerIngredients(IModIngredientRegistration registry) {

	}

	/**
	 * Register the categories handled by this plugin.
	 * These are registered before recipes so they can be checked for validity.
	 */
	default void registerCategories(IRecipeCategoryRegistration registry) {

	}

	default void registerVanillaCategoryExtensions(IExtendableRecipeCategory<IRecipe, ICraftingRecipeWrapper> craftingCategory) {

	}

	/**
	 * Register this mod plugin with the mod registry.
	 */
	default void register(IModRegistry registry) {

	}

	/**
	 * Called when jei's runtime features are available, after all mods have registered.
	 */
	default void onRuntimeAvailable(IJeiRuntime jeiRuntime) {

	}
}
