package mezz.jei.api;

import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.config.IJeiConfigManager;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * The main class to implement to create a JEI plugin. Everything communicated between a mod and JEI is through this class.
 * IModPlugins must have the {@link JeiPlugin} annotation to get loaded by JEI.
 */
public interface IModPlugin {

	/**
	 * The unique ID for this mod plugin.
	 * The namespace should be your mod's modId.
	 */
	ResourceLocation getPluginUid();

	/**
	 * If your item has subtypes that depend on NBT or capabilities, use this to help JEI identify those subtypes correctly.
	 */
	default CompletableFuture<Void> registerItemSubtypes(ISubtypeRegistration registration, Executor executor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * If your fluid has subtypes that depend on NBT or capabilities,
	 * use this to help JEI identify those subtypes correctly.
	 *
	 * @since 10.1.0
	 */
	default <T> CompletableFuture<Void> registerFluidSubtypes(ISubtypeRegistration registration, IPlatformFluidHelper<T> platformFluidHelper, Executor executor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Register special ingredients, beyond the basic ItemStack and FluidStack.
	 */
	default CompletableFuture<Void> registerIngredients(IModIngredientRegistration registration, Executor executor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Register extra ItemStacks that are not in the creative menu,
	 * or FluidStacks that are different from the default ones available via the fluid registry.
	 *
	 * @since 15.19.0
	 */
	default CompletableFuture<Void> registerExtraIngredients(IExtraIngredientRegistration registration, Executor executor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Register search aliases for ingredients.
	 *
	 * @implNote If the player has disabled search aliases in the config, this will not be called.
	 *
	 * @since 15.15.0
	 */
	default CompletableFuture<Void> registerIngredientAliases(IIngredientAliasRegistration registration, Executor executor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Register the categories handled by this plugin.
	 * These are registered before recipes so they can be checked for validity.
	 */
	default CompletableFuture<Void> registerCategories(IRecipeCategoryRegistration registration, Executor executor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Register modded extensions to the vanilla crafting recipe category.
	 * Custom crafting recipes for your mod should use this to tell JEI how they work.
	 */
	default CompletableFuture<Void> registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration, Executor executor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Register modded recipes.
	 */
	default CompletableFuture<Void> registerRecipes(IRecipeRegistration registration, Executor executor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Register recipe transfer handlers (move ingredients from the inventory into crafting GUIs).
	 */
	default CompletableFuture<Void> registerRecipeTransferHandlers(IRecipeTransferRegistration registration, Executor executor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Register recipe catalysts.
	 * Recipe Catalysts are ingredients that are needed in order to craft other things.
	 * Vanilla examples of Recipe Catalysts are the Crafting Table and Furnace.
	 */
	default CompletableFuture<Void> registerRecipeCatalysts(IRecipeCatalystRegistration registration, Executor executor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Register various GUI-related things for your mod.
	 * This includes adding clickable areas in your guis to open JEI,
	 * and adding areas on the screen that JEI should avoid drawing.
	 */
	default CompletableFuture<Void> registerGuiHandlers(IGuiHandlerRegistration registration, Executor executor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Register advanced features for your mod plugin.
	 */
	default CompletableFuture<Void> registerAdvanced(IAdvancedRegistration registration, Executor executor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Override the default JEI runtime.
	 *
	 * @return
	 */
	default CompletableFuture<Void> registerRuntime(IRuntimeRegistration registration, Executor executor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Called when JEI's runtime features are available, after all mods have registered.
	 *
	 * @return
	 */
	default void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
	}

	/**
	 * Called when JEI's runtime features are no longer available, after a user quits or logs out of a world.
	 * @since 11.5.0
	 */
	default void onRuntimeUnavailable() {

	}

	/**
	 * Called when JEI's configs are available.
	 * This is called early on, as soon as configs are available.
	 * @since 12.3.0
	 */
	default void onConfigManagerAvailable(IJeiConfigManager configManager) {

	}
}