package mezz.jei.api.runtime;

/**
 * Gives access to key mappings used by JEI.
 * This can be used by mods that want to use the same keys that players bind for JEI.
 *
 * Get the instance from {@link IJeiRuntime}.
 *
 * @since 11.0.1
 */
public interface IJeiKeyMappings {
    /**
     * @return the key mapping to show recipes.
     * The default bindings are 'Left Click' and 'R'.
     *
     * @since 11.0.1
     */
    IJeiKeyMapping getShowRecipe();

    /**
     * @return the key mapping to show recipes.
     * The default bindings are 'Right Click' and 'U'.
     *
     * @since 11.0.1
     */
    IJeiKeyMapping getShowUses();
}
