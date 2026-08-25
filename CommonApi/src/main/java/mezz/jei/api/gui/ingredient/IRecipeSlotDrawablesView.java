package mezz.jei.api.gui.ingredient;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus;

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
 * @since 19.19.3
 */
@ApiStatus.NonExtendable
public interface IRecipeSlotDrawablesView {
	/**
	 * Get all slots for a recipe.
	 *
	 * @since 19.19.3
	 */
	@Unmodifiable
	List<IRecipeSlotDrawable> getSlots();

	/**
	 * Get all the ingredient sources for a recipe, as the unified {@link IRecipeIngredientsSource} view.
	 * Equivalent to {@link #getSlots()} unless slot-specific features like positions or drawing are needed.
	 *
	 * @since 30.26.0
	 */
	@Unmodifiable
	default List<IRecipeIngredientsSource> getIngredientSources() {
		return List.copyOf(getSlots());
	}

	/**
	 * Get the ingredient sources for the given {@link RecipeIngredientRole} for a recipe.
	 *
	 * @since 30.26.0
	 */
	default List<IRecipeIngredientsSource> getIngredientSources(RecipeIngredientRole role) {
		List<IRecipeIngredientsSource> list = new ArrayList<>();
		for (IRecipeIngredientsSource source : getIngredientSources()) {
			if (source.getRole() == role) {
				list.add(source);
			}
		}
		return list;
	}

	/**
	 * Get the list of slots for the given {@link RecipeIngredientRole} for a recipe.
	 *
	 * @since 19.19.3
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
	 * @since 19.19.3
	 */
	default Optional<IRecipeSlotDrawable> findSlotByName(String slotName) {
		return getSlots().stream()
			.filter(slot -> slot.getSlotName().map(slotName::equals).orElse(false))
			.findFirst();
	}
}
