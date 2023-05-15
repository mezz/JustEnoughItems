package mezz.jei.api;

import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.api.runtime.IJeiClientExecutor;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

/**
 * In a Forge environment, IRuntimePlugins must have the {@link JeiRuntimePlugin} annotation to get loaded by JEI.
 *
 * In a Fabric environment, IModPlugins must be declared under `entrypoints.jei_runtime_plugin` in `fabric.mod.json`.
 * See <a href="https://fabricmc.net/wiki/documentation:entrypoint">the Fabric Wiki</a> for more information.
 *
 * @since 13.2.0
 */
public interface IRuntimePlugin {

	/**
	 * The unique ID for this mod plugin.
	 * The namespace should be your mod's modId.
	 */
	ResourceLocation getPluginUid();

	/**
	 * Override the default JEI runtime.
	 */
	default CompletableFuture<Void> registerRuntime(IRuntimeRegistration registration, IJeiClientExecutor clientExecutor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Called when JEI's runtime features are available, after all mods have registered.
	 */
	default CompletableFuture<Void> onRuntimeAvailable(IJeiRuntime jeiRuntime, IJeiClientExecutor clientExecutor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Called when JEI's runtime features are no longer available, after a user quits or logs out of a world.
	 */
	default CompletableFuture<Void> onRuntimeUnavailable(IJeiClientExecutor clientExecutor) {
		return CompletableFuture.completedFuture(null);
	}
}
