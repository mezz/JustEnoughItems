package mezz.jei.api.gui.builder;

import com.mojang.datafixers.util.Either;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IJeiKeyMapping;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.Collection;
import java.util.List;

/**
 * Helper for building tooltips.
 *
 * @since 19.5.4
 */
public interface ITooltipBuilder {
	/**
	 * Add a {@link FormattedText} line to this tooltip
	 * Note that {@link Component} is {@link FormattedText}.
	 *
	 * @since 19.5.4
	 */
	void add(FormattedText component);

	/**
	 * Add multiple {@link FormattedText} lines to this tooltip
	 * Note that {@link Component} is {@link FormattedText}.
	 *
	 * @since 19.5.4
	 */
	void addAll(Collection<? extends FormattedText> components);

	/**
	 * Add a {@link TooltipComponent} line to this tooltip,
	 * to add images and other rich content.
	 *
	 * @implNote Make sure that {@link ClientTooltipComponent#create(TooltipComponent)}
	 * works for your {@link TooltipComponent} on your mod loader platform or else it will crash.
	 *
	 * @since 19.5.4
	 */
	void add(TooltipComponent component);

	/**
	 * Add text describing the given keyMapping.
	 * The translationKey must take a string formatting key (%s) to accept the translated key mapping string.
	 *
	 * For example, a translation key could be defined this way:
	 * "jei.tooltip.bookmarks.tooltips.usage": "[Press \"%s\" to show details]"
	 *
	 * @since 27.1.0
	 */
	void addKeyUsageComponent(String translationKey, IJeiKeyMapping keyMapping);

	/**
	 * Add an ingredient that is associated with this tooltip.
	 * Most platforms use this ingredient information in tooltip events in
	 * order to add extra info to the tooltip.
	 *
	 * @since 19.5.4
	 */
	void setIngredient(ITypedIngredient<?> typedIngredient);

	/**
	 * Remove all the lines and ingredients from this tooltip.
	 *
	 * @since 19.16.4
	 */
	default void clear() {
		clearIngredient();
		getLines().clear();
	}

	/**
	 * Remove the ingredient from this tooltip.
	 *
	 * @see #setIngredient(ITypedIngredient)
	 *
	 * @since 21.1.0
	 */
	void clearIngredient();

	/**
	 * Get the lines stored by this tooltip builder.
	 * These lines are directly modifiable.
	 *
	 * @since 21.1.0
	 */
	List<Either<FormattedText, TooltipComponent>> getLines();
}
