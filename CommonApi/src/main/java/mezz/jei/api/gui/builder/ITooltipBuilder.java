package mezz.jei.api.gui.builder;

import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.List;

/**
 * Helper for building tooltips.
 *
 * @since 11.7.0
 */
public interface ITooltipBuilder {
	/**
	 * Add a {@link Component} line to this tooltip
	 *
	 * @since 11.7.0
	 */
	void add(Component component);

	/**
	 * Add multiple {@link Component} lines to this tooltip
	 *
	 * @since 11.7.0
	 */
	void addAll(Collection<? extends Component> components);

	/**
	 * Add an ingredient that is associated with this tooltip.
	 * Most platforms use this ingredient information in tooltip events in
	 * order to add extra info to the tooltip.
	 *
	 * @since 11.7.0
	 */
	void setIngredient(ITypedIngredient<?> typedIngredient);

	/**
	 * Remove all the lines and ingredients from this tooltip.
	 *
	 * @since 11.7.0
	 */
	void clear();

	/**
	 * Get the underlying components for this tooltip.
	 *
	 * @since 11.7.0
	 */
	@Deprecated
	List<Component> getLegacyComponents();
}
