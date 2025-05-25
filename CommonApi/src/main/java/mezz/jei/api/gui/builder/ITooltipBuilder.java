package mezz.jei.api.gui.builder;

import com.mojang.datafixers.util.Either;
import mezz.jei.api.ingredients.ITypedIngredient;
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

	/**
	 * @deprecated this is only for legacy tooltip support and will be removed
	 */
	@Deprecated(since = "19.8.4", forRemoval = true)
	List<Component> toLegacyToComponents();

	/**
	 * @deprecated this is only for legacy tooltip support and will be removed
	 */
	@Deprecated(since = "19.8.4", forRemoval = true)
	void removeAll(List<Component> components);
}
