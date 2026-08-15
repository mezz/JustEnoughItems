package mezz.jei.api.runtime;

import org.jetbrains.annotations.ApiStatus;

/**
 * Gives access to key mappings used by JEI.
 * This can be used by mods that want to use the same keys that players bind for JEI.
 *
 * Get the instance from {@link IJeiRuntime}.
 *
 * @since 10.3.0
 */
@ApiStatus.NonExtendable
public interface IJeiKeyMappings {
	/**
	 * @return the key mapping to show recipes.
	 * The default bindings are 'Left Click' and 'R'.
	 *
	 * @since 10.3.0
	 */
	IJeiKeyMapping getShowRecipe();

	/**
	 * @return the key mapping to show recipes.
	 * The default bindings are 'Right Click' and 'U'.
	 *
	 * @since 10.3.0
	 */
	IJeiKeyMapping getShowUses();
}
