package mezz.jei.api.gui.ingredient;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents all the drawn ingredients in slots that are part of a recipe.
 * Useful for implementing {@link IRecipeTransferHandler} and some other advanced cases.
 *
 * This view is meant as a source of information for recipe transfer, drawing, and tooltips.
 *
 * @since 9.3.0
 */
@ApiStatus.NonExtendable
public interface IRecipeSlotsView {
	/**
	 * Get all slots for a recipe.
	 *
	 * @since 9.3.0
	 */
	@Unmodifiable
	List<IRecipeSlotView> getSlotViews();

	/**
	 * Get all the ingredient sources for a recipe, as the unified {@link IRecipeIngredientsSource} view.
	 * Equivalent to {@link #getSlotViews()} unless slot-specific features like positions or drawing are needed.
	 *
	 * @since 30.26.0
	 */
	@Unmodifiable
	default List<IRecipeIngredientsSource> getIngredientSources() {
		return List.copyOf(getSlotViews());
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
	 * @since 9.3.0
	 */
	default List<IRecipeSlotView> getSlotViews(RecipeIngredientRole role) {
		List<IRecipeSlotView> list = new ArrayList<>();
		for (IRecipeSlotView slotView : getSlotViews()) {
			if (slotView.getRole() == role) {
				list.add(slotView);
			}
		}
		return list;
	}

	/**
	 * Get a recipe slot by its name set with {@link IRecipeSlotBuilder#setSlotName(String)}.
	 *
	 * @since 9.3.0
	 */
	default Optional<IRecipeSlotView> findSlotByName(String slotName) {
		return getSlotViews().stream()
			.filter(slot -> slot.getSlotName().map(slotName::equals).orElse(false))
			.findFirst();
	}
}
