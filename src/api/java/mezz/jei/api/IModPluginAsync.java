package mezz.jei.api;

import java.util.concurrent.CompletableFuture;

import net.minecraft.util.ResourceLocation;

import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.api.runtime.IJeiRuntime;

/**
 * The main class to implement to create an async-loaded JEI plugin.
 * Everything communicated between a mod and JEI is through this class or the sync version {@link IModPlugin}.
 * {@link IModPluginAsync}s must have the {@link JeiPlugin} annotation to get loaded by JEI.
 */
public interface IModPluginAsync {
	CompletableFuture<Void> COMPLETED = CompletableFuture.completedFuture(null);

	/**
	 * The unique ID for this mod plugin.
	 * The namespace should be your mod's modId.
	 */
	ResourceLocation getPluginUid();

	/**
	 * If your item has subtypes that depend on NBT or capabilities, use this to help JEI identify those subtypes correctly.
	 */
	default CompletableFuture<Void> registerItemSubtypes(ISubtypeRegistration registration) {
		return COMPLETED;
	}

	/**
	 * Register special ingredients, beyond the basic ItemStack and FluidStack.
	 */
	default CompletableFuture<Void> registerIngredients(IModIngredientRegistration registration) {
		return COMPLETED;
	}

	/**
	 * Register the categories handled by this plugin.
	 * These are registered before recipes so they can be checked for validity.
	 */
	default CompletableFuture<Void> registerCategories(IRecipeCategoryRegistration registration) {
		return COMPLETED;
	}

	/**
	 * Register modded extensions to the vanilla crafting recipe category.
	 * Custom crafting recipes for your mod should use this to tell JEI how they work.
	 */
	default CompletableFuture<Void> registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
		return COMPLETED;
	}

	/**
	 * Register modded recipes.
	 */
	default CompletableFuture<Void> registerRecipes(IRecipeRegistration registration) {
		return COMPLETED;
	}

	/**
	 * Register recipe transfer handlers (move ingredients from the inventory into crafting GUIs).
	 */
	default CompletableFuture<Void> registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		return COMPLETED;
	}

	/**
	 * Register recipe catalysts.
	 * Recipe Catalysts are ingredients that are needed in order to craft other things.
	 * Vanilla examples of Recipe Catalysts are the Crafting Table and Furnace.
	 */
	default CompletableFuture<Void> registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		return COMPLETED;
	}

	/**
	 * Register various GUI-related things for your mod.
	 * This includes adding clickable areas in your guis to open JEI,
	 * and adding areas on the screen that JEI should avoid drawing.
	 */
	default CompletableFuture<Void> registerGuiHandlers(IGuiHandlerRegistration registration) {
		return COMPLETED;
	}

	/**
	 * Register advanced features for your mod plugin.
	 */
	default CompletableFuture<Void> registerAdvanced(IAdvancedRegistration registration) {
		return COMPLETED;
	}

	/**
	 * Called when jei's runtime features are available, after all mods have registered.
	 */
	default CompletableFuture<Void> onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		return COMPLETED;
	}
}
