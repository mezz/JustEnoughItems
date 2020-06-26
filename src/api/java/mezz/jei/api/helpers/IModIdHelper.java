package mezz.jei.api.helpers;

import java.util.List;

import mezz.jei.api.ingredients.IIngredientHelper;
import net.minecraft.util.text.ITextComponent;

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
	 */
	<T> List<ITextComponent> addModNameToIngredientTooltip(List<ITextComponent> tooltip, T ingredient, IIngredientHelper<T> ingredientHelper);
}
