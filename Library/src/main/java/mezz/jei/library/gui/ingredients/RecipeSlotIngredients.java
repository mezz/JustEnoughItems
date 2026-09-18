package mezz.jei.library.gui.ingredients;

import mezz.jei.api.gui.builder.IIngredientConsumer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
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
import java.util.function.Predicate;
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
	private final Runnable displayOverridesChangedListener;

	public RecipeSlotIngredients(
		List<@Nullable ITypedIngredient<?>> allIngredients,
		@Nullable List<@Nullable ITypedIngredient<?>> focusedIngredients,
		Runnable displayOverridesChangedListener
	) {
		this.allIngredients = Collections.unmodifiableList(new ArrayList<>(allIngredients));
		this.displayIngredients = focusedIngredients;
		this.displayOverridesChangedListener = displayOverridesChangedListener;
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
		return cycler.getCycled(getDisplayIngredients());
	}

	public Optional<ITypedIngredient<?>> getFirstDisplayedIngredient() {
		List<@Nullable ITypedIngredient<?>> displayIngredients = getDisplayIngredients();
		if (displayIngredients.isEmpty()) {
			return Optional.empty();
		}
		return Optional.ofNullable(displayIngredients.get(0));
	}

	public Stream<ITypedIngredient<?>> getDisplayedIngredients() {
		List<@Nullable ITypedIngredient<?>> candidates = allIngredients;
		if (displayOverrides != null) {
			candidates = displayOverrides.getAllIngredients();
		}
		IIngredientVisibility visibility = Internal.getJeiRuntime().getJeiHelpers().getIngredientVisibility();
		return candidates.stream()
			.filter(Objects::nonNull)
			.filter(ingredient -> visibility.isIngredientVisible(ingredient, UidContext.Recipe));
	}

	private List<@Nullable ITypedIngredient<?>> getDisplayIngredients() {
		if (this.displayOverrides != null) {
			return this.displayOverrides.getAllIngredients();
		}
		if (this.displayIngredients == null) {
			this.displayIngredients = calculateDisplayIngredients(this.allIngredients);
		}
		return this.displayIngredients;
	}

	public <T> List<T> getVisibleIngredients(IIngredientType<T> ingredientType) {
		IIngredientVisibility ingredientVisibility = Internal.getJeiRuntime().getJeiHelpers().getIngredientVisibility();
		return getAllIngredients()
			.filter(ingredient -> ingredientVisibility.isIngredientVisible(ingredient, UidContext.Recipe))
			.map(ingredient -> ingredient.getIngredient(ingredientType))
			.flatMap(Optional::stream)
			.toList();
	}

	public void clearDisplayOverrides() {
		boolean changed = this.displayOverrides != null;
		this.displayOverrides = null;
		if (changed) {
			displayOverridesChangedListener.run();
		}
	}

	public boolean hasDisplayOverrides() {
		return this.displayOverrides != null;
	}

	public IIngredientConsumer createDisplayOverrides() {
		if (this.displayOverrides == null) {
			IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
			this.displayOverrides = new DisplayIngredientAcceptor(ingredientManager, displayOverridesChangedListener);
			displayOverridesChangedListener.run();
		}
		return this.displayOverrides;
	}

	private static List<@Nullable ITypedIngredient<?>> calculateDisplayIngredients(List<@Nullable ITypedIngredient<?>> allIngredients) {
		IIngredientVisibility visibility = Internal.getJeiRuntime().getJeiHelpers().getIngredientVisibility();
		return filterVisibleIngredients(allIngredients, ingredient -> visibility.isIngredientVisible(ingredient, UidContext.Recipe), MAX_DISPLAYED_INGREDIENTS);
	}

	public static List<@Nullable ITypedIngredient<?>> filterVisibleIngredients(
		List<@Nullable ITypedIngredient<?>> ingredients,
		Predicate<ITypedIngredient<?>> isVisible,
		int limit
	) {
		List<@Nullable ITypedIngredient<?>> visible = ingredients.stream()
			.filter(ingredient -> ingredient == null || isVisible.test(ingredient))
			.limit(limit)
			.toList();
		if (!visible.isEmpty()) {
			return visible;
		}
		// If every ingredient is invisible, show them anyway so the recipe slot is not blank.
		return ingredients.stream().limit(limit).toList();
	}
}
