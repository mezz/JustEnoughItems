package mezz.jei.api;

import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.api.runtime.IJeiClientExecutor;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.config.IJeiConfigManager;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

/**
 * The main async class to implement to create a JEI plugin.
 * Everything communicated between a mod and JEI is through this class.
 * IAsyncModPlugin must have the {@link JeiPlugin} annotation to get loaded by JEI.
 *
 * In a Forge environment, IModPlugins must have the {@link JeiAsyncPlugin} annotation to get loaded by JEI.
 *
 * In a Fabric environment, IModPlugins must be declared under `entrypoints.jei_async_mod_plugin` in `fabric.mod.json`.
 * See <a href="https://fabricmc.net/wiki/documentation:entrypoint">the Fabric Wiki</a> for more information.
 *
 * @see IModPlugin for a simpler, synchronous version
 *
 * @since 13.2.0
 */
public interface IAsyncModPlugin {

	/**
	 * The unique ID for this mod plugin.
	 * The namespace should be your mod's modId.
	 */
	ResourceLocation getPluginUid();

	/**
	 * If your item has subtypes that depend on NBT or capabilities, use this to help JEI identify those subtypes correctly.
	 */
	default CompletableFuture<Void> registerItemSubtypes(ISubtypeRegistration registration, IJeiClientExecutor clientThreadExecutor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * If your fluid has subtypes that depend on NBT or capabilities,
	 * use this to help JEI identify those subtypes correctly.
	 *
	 * @since 10.1.0
	 */
	default <T> CompletableFuture<Void> registerFluidSubtypes(ISubtypeRegistration registration, IPlatformFluidHelper<T> platformFluidHelper, IJeiClientExecutor clientThreadExecutor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Register special ingredients, beyond the basic ItemStack and FluidStack.
	 */
	default CompletableFuture<Void> registerIngredients(IModIngredientRegistration registration, IJeiClientExecutor clientThreadExecutor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Register the categories handled by this plugin.
	 * These are registered before recipes so they can be checked for validity.
	 */
	default CompletableFuture<Void> registerCategories(IRecipeCategoryRegistration registration, IJeiClientExecutor clientThreadExecutor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Register modded extensions to the vanilla crafting recipe category.
	 * Custom crafting recipes for your mod should use this to tell JEI how they work.
	 */
	default CompletableFuture<Void> registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration, IJeiClientExecutor clientThreadExecutor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Register modded recipes.
	 */
	default CompletableFuture<Void> registerRecipes(IRecipeRegistration registration, IJeiClientExecutor clientThreadExecutor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Register recipe transfer handlers (move ingredients from the inventory into crafting GUIs).
	 */
	default CompletableFuture<Void> registerRecipeTransferHandlers(IRecipeTransferRegistration registration, IJeiClientExecutor clientThreadExecutor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Register recipe catalysts.
	 * Recipe Catalysts are ingredients that are needed in order to craft other things.
	 * Vanilla examples of Recipe Catalysts are the Crafting Table and Furnace.
	 */
	default CompletableFuture<Void> registerRecipeCatalysts(IRecipeCatalystRegistration registration, IJeiClientExecutor clientThreadExecutor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Register various GUI-related things for your mod.
	 * This includes adding clickable areas in your guis to open JEI,
	 * and adding areas on the screen that JEI should avoid drawing.
	 */
	default CompletableFuture<Void> registerGuiHandlers(IGuiHandlerRegistration registration, IJeiClientExecutor clientThreadExecutor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Register advanced features for your mod plugin.
	 */
	default CompletableFuture<Void> registerAdvanced(IAdvancedRegistration registration, IJeiClientExecutor clientThreadExecutor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Called when JEI's runtime features are available, after all mods have registered.
	 */
	default CompletableFuture<Void> onRuntimeAvailable(IJeiRuntime jeiRuntime, IJeiClientExecutor clientThreadExecutor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Called when JEI's runtime features are no longer available, after a user quits or logs out of a world.
	 * @since 11.5.0
	 */
	default CompletableFuture<Void> onRuntimeUnavailable(IJeiClientExecutor clientThreadExecutor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Called when JEI's configs are available.
	 * This is called early on, as soon as configs are available.
	 * @since 12.3.0
	 */
	default CompletableFuture<Void> onConfigManagerAvailable(IJeiConfigManager configManager, IJeiClientExecutor clientThreadExecutor) {
		return CompletableFuture.completedFuture(null);
	}
}
