package mezz.jei.api.gui.ingredient;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents all the drawn ingredients in slots that are part of a recipe.
 *
 * This view is meant as a source of information for drawing, positioning, and tooltips.
 *
 * @see IRecipeSlotsView for a view with less access to drawable properties of the slots.
 *
 * @since 15.20.0
 */
public interface IRecipeSlotDrawablesView {
	/**
	 * Get all slots for a recipe.
	 *
	 * @since 15.20.0
	 */
	@Unmodifiable
	List<IRecipeSlotDrawable> getSlots();

	/**
	 * Get the list of slots for the given {@link RecipeIngredientRole} for a recipe.
	 *
	 * @since 15.20.0
	 */
	default List<IRecipeSlotDrawable> getSlots(RecipeIngredientRole role) {
		List<IRecipeSlotDrawable> list = new ArrayList<>();
		for (IRecipeSlotDrawable slotView : getSlots()) {
			if (slotView.getRole() == role) {
				list.add(slotView);
			}
		}
		return list;
	}

	/**
	 * Get a recipe slot by its name set with {@link IRecipeSlotBuilder#setSlotName(String)}.
	 *
	 * @since 15.20.0
	 */
	default Optional<IRecipeSlotDrawable> findSlotByName(String slotName) {
		return getSlots().stream()
			.filter(slot ->
				slot.getSlotName()
					.map(slotName::equals)
					.orElse(false)
			)
			.findFirst();
	}
}
