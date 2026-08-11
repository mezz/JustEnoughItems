package mezz.jei.library.gui.ingredients;

import mezz.jei.api.gui.builder.IIngredientConsumer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.Internal;
import mezz.jei.library.ingredients.DisplayIngredientAcceptor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Owns focus, visibility, cycling, and display overrides for a recipe slot.
 */
public final class RecipeSlotIngredients {
	private static final int MAX_DISPLAYED_INGREDIENTS = 100;

	/**
	 * All ingredients, ignoring focus and visibility.
	 * Null ingredients represent a blank drawn ingredient in the rotation.
	 */
	@Unmodifiable
	private final List<@Nullable ITypedIngredient<?>> allIngredients;

	/**
	 * Displayed ingredients, taking focus and visibility into account.
	 * Null ingredients represent a blank drawn ingredient in the rotation.
	 */
	@Unmodifiable
	private @Nullable List<@Nullable ITypedIngredient<?>> displayIngredients;

	private @Nullable DisplayIngredientAcceptor displayOverrides;

	public RecipeSlotIngredients(
		List<@Nullable ITypedIngredient<?>> allIngredients,
		@Nullable List<@Nullable ITypedIngredient<?>> focusedIngredients
	) {
		this.allIngredients = Collections.unmodifiableList(new ArrayList<>(allIngredients));
		this.displayIngredients = focusedIngredients;
	}

	public Stream<ITypedIngredient<?>> getAllIngredients() {
		return this.allIngredients.stream()
			.filter(Objects::nonNull);
	}

	@Unmodifiable
	public List<@Nullable ITypedIngredient<?>> getAllIngredientsList() {
		return this.allIngredients;
	}

	public boolean isEmpty() {
		return this.allIngredients.isEmpty() || this.allIngredients.stream().allMatch(Objects::isNull);
	}

	public Optional<ITypedIngredient<?>> getDisplayedIngredient(ICycler cycler) {
		if (this.displayOverrides != null) {
			List<@Nullable ITypedIngredient<?>> overrides = this.displayOverrides.getAllIngredients();
			return cycler.getCycled(overrides);
		}
		if (this.displayIngredients == null) {
			this.displayIngredients = calculateDisplayIngredients(this.allIngredients);
		}
		return cycler.getCycled(this.displayIngredients);
	}

	public <T> List<T> getVisibleIngredients(IIngredientType<T> ingredientType) {
		IIngredientVisibility ingredientVisibility = Internal.getJeiRuntime().getJeiHelpers().getIngredientVisibility();
		return getAllIngredients()
			.filter(ingredientVisibility::isIngredientVisible)
			.map(ingredient -> ingredient.getIngredient(ingredientType))
			.flatMap(Optional::stream)
			.toList();
	}

	public void clearDisplayOverrides() {
		this.displayOverrides = null;
	}

	public IIngredientConsumer createDisplayOverrides() {
		if (this.displayOverrides == null) {
			IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
			this.displayOverrides = new DisplayIngredientAcceptor(ingredientManager);
		}
		return this.displayOverrides;
	}

	private static List<@Nullable ITypedIngredient<?>> calculateDisplayIngredients(List<@Nullable ITypedIngredient<?>> allIngredients) {
		if (allIngredients.isEmpty()) {
			return List.of();
		}

		List<@Nullable ITypedIngredient<?>> visibleIngredients = List.of();
		boolean hasInvisibleIngredients = false;

		// Hide invisible ingredients if there are any.
		// Try scanning through all the ingredients without building the list of visible ingredients.
		// If an invisible ingredient is found, start building the list of visible ingredients.
		IIngredientVisibility ingredientVisibility = Internal.getJeiRuntime().getJeiHelpers().getIngredientVisibility();
		for (int i = 0; i < allIngredients.size() && visibleIngredients.size() < MAX_DISPLAYED_INGREDIENTS; i++) {
			ITypedIngredient<?> ingredient = allIngredients.get(i);
			boolean visible = ingredient == null || ingredientVisibility.isIngredientVisible(ingredient);
			if (visible) {
				if (hasInvisibleIngredients) {
					visibleIngredients.add(ingredient);
				}
			} else if (!hasInvisibleIngredients) {
				hasInvisibleIngredients = true;
				// `i` is the first invisible ingredient, start putting visible ingredients into visibleIngredients.
				visibleIngredients = new ArrayList<>(allIngredients.subList(0, i));
			}
		}

		if (!visibleIngredients.isEmpty()) {
			// Some ingredients have been successfully hidden, and some are still visible.
			return visibleIngredients;
		}

		// Either everything is visible or everything is invisible.
		// If everything is invisible, show them all anyway so that the recipe slot is not blank.
		if (allIngredients.size() < MAX_DISPLAYED_INGREDIENTS) {
			// Reuse allIngredients to save some memory.
			return allIngredients;
		}
		return allIngredients.subList(0, MAX_DISPLAYED_INGREDIENTS);
	}
}
