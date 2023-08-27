package mezz.jei.api;

import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * A runtime plugin is used to override the default JEI runtime.
 * Only one runtime plugin will be used, so if you create one then JEI's will be deactivated.
 * This is intended for mods that implement a GUI that completely replaces JEI's.
 *
 * In a Forge environment, IRuntimePlugins must have the {@link JeiPlugin} annotation to get loaded by JEI.
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
	 *
	 * @since 13.2.0
	 */
	ResourceLocation getPluginUid();

	/**
	 * Override the default JEI runtime.
	 *
	 * @since 13.2.0
	 */
	default CompletableFuture<Void> registerRuntime(IRuntimeRegistration registration, Executor clientExecutor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Called when JEI's runtime features are available, after all mods have registered.
	 *
	 * @since 13.2.0
	 */
	default CompletableFuture<Void> onRuntimeAvailable(IJeiRuntime jeiRuntime, Executor clientExecutor) {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Called when JEI's runtime features are no longer available, after a user quits or logs out of a world.
	 *
	 * @since 13.2.0
	 */
	default CompletableFuture<Void> onRuntimeUnavailable(Executor clientExecutor) {
		return CompletableFuture.completedFuture(null);
	}
}
