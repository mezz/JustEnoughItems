package mezz.jei.api.helpers;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

/**
 * Helper class for getting mod names from their modIds.
 * Get an instance from {@link IJeiHelpers#getModIdHelper()}
 */
public interface IModIdHelper {
	/**
	 * Get the mod name for its modId
	 */
	String getModNameForModId(String modId);

	/**
	 * Returns true if JEI is configured to display mod names.
	 */
	boolean isDisplayingModNameEnabled();

	/**
	 * Returns the mod name with color formatting, as specified in JEI's config. (default is blue italic)
	 */
	String getFormattedModNameForModId(String modId);

	/**
	 * Adds the mod name to the tooltip with color formatting.
	 *
	 * If {@link #isDisplayingModNameEnabled()} is false,
	 * this will just return the tooltip without adding the mod name.
	 *
	 * @deprecated use {@link #getModNameForTooltip(ITypedIngredient)}
	 */
	@Deprecated(since = "15.8.4", forRemoval = true)
	<T> List<Component> addModNameToIngredientTooltip(List<Component> tooltip, T ingredient, IIngredientHelper<T> ingredientHelper);

	/**
	 * Adds the mod name to the tooltip with color formatting.
	 *
	 * If {@link #isDisplayingModNameEnabled()} is false,
	 * this will just return the tooltip without adding the mod name.
	 *
	 * @since 11.5.0
	 *
	 * @deprecated use {@link #getModNameForTooltip(ITypedIngredient)}
	 */
	@Deprecated(since = "15.8.4", forRemoval = true)
	<T> List<Component> addModNameToIngredientTooltip(List<Component> tooltip, ITypedIngredient<T> typedIngredient);

	/**
	 * Gets the mod name for the tooltip with color formatting.
	 *
	 * If {@link #isDisplayingModNameEnabled()} is false,
	 * or another mod already adds the mod name, this will return {@link Optional#empty}.
	 *
	 * @since 15.8.1
	 */
	<T> Optional<Component> getModNameForTooltip(ITypedIngredient<T> typedIngredient);
}
