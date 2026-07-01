package mezz.jei.api.helpers;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Helper class for getting mod names from their modIds.
 * Get an instance from {@link IJeiHelpers#getModIdHelper()}
 */
@ApiStatus.NonExtendable
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
	 *
	 * @deprecated use {@link #getFormattedModNameComponentForModId(String)}
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated(since = "10.68.0", forRemoval = true)
	String getFormattedModNameForModId(String modId);

	/**
	 * Returns the mod name with color formatting, as specified in JEI's config. (default is blue italic)
	 *
	 * @since 10.68.0
	 */
	default Component getFormattedModNameComponentForModId(String modId) {
		return new TextComponent(getFormattedModNameForModId(modId));
	}

	/**
	 * Returns alternative mod names, used for searching for a mod by a different name.
	 *
	 * @since 10.10.0
	 */
	Set<String> getModAliases(String modId);

	/**
	 * Adds the mod name to the tooltip with color formatting.
	 *
	 * If {@link #isDisplayingModNameEnabled()} is false,
	 * this will just return the tooltip without adding the mod name.
	 *
	 * @deprecated use {@link #getModNameForTooltip(ITypedIngredient)}
	 */
	@Deprecated(since = "10.29.0", forRemoval = true)
	<T> List<Component> addModNameToIngredientTooltip(List<Component> tooltip, T ingredient, IIngredientHelper<T> ingredientHelper);

	/**
	 * Adds the mod name to the tooltip with color formatting.
	 *
	 * If {@link #isDisplayingModNameEnabled()} is false,
	 * this will just return the tooltip without adding the mod name.
	 *
	 * @since 10.3.0
	 *
	 * @deprecated use {@link #getModNameForTooltip(ITypedIngredient)}
	 */
	@Deprecated(since = "10.29.0", forRemoval = true)
	default <T> List<Component> addModNameToIngredientTooltip(List<Component> tooltip, ITypedIngredient<T> typedIngredient) {
		return tooltip;
	}

	/**
	 * Gets the mod name for the tooltip with color formatting.
	 *
	 * If {@link #isDisplayingModNameEnabled()} is false,
	 * or another mod already adds the mod name, this will return {@link Optional#empty}.
	 *
	 * @since 10.5.0
	 */
	default <T> Optional<Component> getModNameForTooltip(ITypedIngredient<T> typedIngredient) {
		return Optional.empty();
	}
}
