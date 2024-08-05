package mezz.jei.api.gui.builder;

import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;

/**
 * Helper for building tooltips.
 *
 * @since 11.7.0
 */
@ApiStatus.NonExtendable
public interface ITooltipBuilder {
	/**
	 * Add a {@link Component} line to this tooltip.
	 *
	 * @since 11.7.0
	 */
	void add(Component component);

	/**
	 * Add a {@link FormattedText} line to this tooltip.
	 * Note that {@link Component} is {@link FormattedText}.
	 *
	 * @since 11.30.1
	 */
	void add(FormattedText formattedText);

	/**
	 * Add multiple {@link Component} lines to this tooltip.
	 *
	 * @since 11.7.0
	 */
	void addAll(Collection<? extends Component> components);

	/**
	 * Add a {@link TooltipComponent} line to this tooltip,
	 * to add images and other rich content.
	 *
	 * @implNote Make sure that {@link ClientTooltipComponent#create(TooltipComponent)}
	 * works for your {@link TooltipComponent} on your platform (Fabric or Forge)
	 * or else it will crash.
	 *
	 * @since 11.30.1
	 */
	void add(TooltipComponent component);

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
