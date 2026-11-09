package mezz.jei.api.gui;

import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * Text and ingredients in a JEI tooltip component.
 *
 * @since 31.21.0
 */
@ApiStatus.NonExtendable
public interface IJeiTooltipComponent extends TooltipComponent {
	/**
	 * Returns the component's text in display order.
	 * Do not modify the returned list.
	 * @since 31.21.0
	 */
	List<String> getText();

	/**
	 * Returns the component's ingredients, including those scrolled out of view.
	 * Do not modify the returned list or ingredients.
	 * @since 31.21.0
	 */
	List<ITypedIngredient<?>> getIngredients();
}
