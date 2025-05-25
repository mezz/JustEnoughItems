package mezz.jei.api.gui.builder;

import com.mojang.datafixers.util.Either;
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
	 * Remove the ingredient from this tooltip.
	 *
	 * @see #setIngredient(ITypedIngredient)
	 *
	 * @since 11.46.0
	 */
	void clearIngredient();

	/**
	 * Get the lines stored by this tooltip builder.
	 * These lines are directly modifiable.
	 *
	 * @since 11.46.0
	 */
	List<Either<FormattedText, TooltipComponent>> getLines();

	/**
	 * Get the underlying components for this tooltip.
	 *
	 * @since 11.7.0
	 */
	@Deprecated
	List<Component> getLegacyComponents();

	/**
	 * @deprecated this is only for legacy tooltip support and will be removed
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated(since = "11.34.2", forRemoval = true)
	List<Component> toLegacyToComponents();

	/**
	 * @deprecated this is only for legacy tooltip support and will be removed
	 */
	@Deprecated(since = "11.34.2", forRemoval = true)
	void removeAll(List<Component> components);
}
