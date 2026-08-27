package mezz.jei.library.gui.ingredients;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.Internal;
import mezz.jei.library.focus.FocusGroup;
import mezz.jei.library.ingredients.DisplayIngredientAcceptor;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import mezz.jei.library.ingredients.SlotDisplayData;
import mezz.jei.library.ingredients.SlotDisplayIngredientExpander;
import mezz.jei.library.ingredients.SlotIngredient;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextMap;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Owns ingredient grouping, focus, visibility, cycling, and display overrides for a recipe slot.
 * Derived ingredients are cached for the lifetime of the slot; rebuild the recipe layout to observe runtime ingredient changes.
 */
public final class RecipeSlotIngredients {
	private static final int MAX_DISPLAYED_INGREDIENTS = 100;

	private final IIngredientManagerInternal ingredientManager;
	private final ContextMap contextMap;
	private final RecipeIngredientRole role;
	private final IFocusGroup focusGroup;

	/**
	 * Canonical ingredients supplied by the recipe, before subtype expansion, focus, and visibility are applied.
	 * Null ingredients represent a blank drawn ingredient in the rotation.
	 */
	@Unmodifiable
	private final List<@Nullable SlotIngredient<?>> sourceSlotIngredients;

	/**
	 * Ingredients selected by the current focus, before subtype expansion and visibility are applied.
	 * Null means that no focus applies to this slot.
	 * Null ingredients represent a blank drawn ingredient in the rotation.
	 */
	@Unmodifiable
	private final @Nullable List<@Nullable SlotIngredient<?>> focusedSlotIngredients;

	/**
	 * All ingredients, ignoring focus and visibility.
	 * Null ingredients represent a blank drawn ingredient in the rotation.
	 * The list is null until it is calculated.
	 */
	@Unmodifiable
	private @Nullable List<@Nullable ITypedIngredient<?>> allIngredients;

	/**
	 * Displayed ingredients, taking focus and visibility into account.
	 * Null ingredients represent a blank drawn ingredient in the rotation.
	 * The list is null until it is calculated.
	 */
	@Unmodifiable
	private @Nullable List<@Nullable SlotIngredient<?>> displayIngredients;

	private @Nullable DisplayIngredientAcceptor displayOverrides;
	private final Runnable displayOverridesChangedListener;

	/**
	 * Displayed ingredients calculated from {@link #displayOverrides}.
	 * Null ingredients represent a blank drawn ingredient in the rotation.
	 * The list is null until it is calculated.
	 */
	@Unmodifiable
	private @Nullable List<@Nullable SlotIngredient<?>> displayOverrideIngredients;

	public RecipeSlotIngredients(
		IIngredientManagerInternal ingredientManager,
		ContextMap contextMap,
		RecipeIngredientRole role,
		List<? extends @Nullable SlotIngredient<?>> allIngredients,
		@Nullable List<? extends @Nullable SlotIngredient<?>> focusedIngredients,
		IFocusGroup focusGroup,
		Runnable displayOverridesChangedListener
	) {
		this.ingredientManager = ingredientManager;
		this.contextMap = contextMap;
		this.role = role;
		this.focusGroup = focusGroup;
		this.displayOverridesChangedListener = displayOverridesChangedListener;
		this.sourceSlotIngredients = Collections.unmodifiableList(new ArrayList<>(allIngredients));
		if (focusedIngredients == null) {
			this.focusedSlotIngredients = null;
		} else {
			this.focusedSlotIngredients = Collections.unmodifiableList(new ArrayList<>(focusedIngredients));
		}
	}

	public Stream<ITypedIngredient<?>> getAllIngredients() {
		return getAllIngredientsList().stream()
			.filter(Objects::nonNull);
	}

	public Optional<TagKey<?>> getSingleDisplayGroupTagKey(Supplier<Optional<TagKey<?>>> fallback) {
		return getSingleDisplayGroupTagKey(this.sourceSlotIngredients, fallback);
	}

	public static Optional<TagKey<?>> getSingleDisplayGroupTagKey(
		List<? extends @Nullable SlotIngredient<?>> sourceSlotIngredients,
		Supplier<Optional<TagKey<?>>> fallback
	) {
		List<SlotIngredient<?>> sourceIngredients = sourceSlotIngredients.stream()
			.filter(Objects::nonNull)
			.<SlotIngredient<?>>map(ingredient -> ingredient)
			.toList();
		if (sourceIngredients.isEmpty()) {
			return Optional.empty();
		}
		SlotDisplayData<?> firstDisplayData = sourceIngredients.getFirst().slotDisplayData();
		boolean hasMultipleDisplayGroups = sourceIngredients.stream()
			.anyMatch(ingredient -> ingredient.slotDisplayData() != firstDisplayData);
		if (hasMultipleDisplayGroups) {
			return Optional.empty();
		}
		if (firstDisplayData == null) {
			return fallback.get();
		}
		return firstDisplayData.info().tagKeyOrElse(fallback);
	}

	@Unmodifiable
	public List<@Nullable ITypedIngredient<?>> getAllIngredientsList() {
		if (this.allIngredients == null) {
			this.allIngredients = SlotDisplayIngredientExpander.streamForDisplay(ingredientManager, sourceSlotIngredients)
				.<@Nullable ITypedIngredient<?>>map(RecipeSlotIngredients::getTypedIngredient)
				.toList();
		}
		return this.allIngredients;
	}

	public boolean isEmpty() {
		return this.sourceSlotIngredients.isEmpty() || this.sourceSlotIngredients.stream().allMatch(Objects::isNull);
	}

	public Optional<SlotIngredient<?>> getDisplayedIngredient(ICycler cycler) {
		return cycler.getCycled(getDisplayIngredients());
	}

	public Optional<SlotIngredient<?>> getFirstDisplayedIngredient() {
		List<@Nullable SlotIngredient<?>> displayIngredients = getDisplayIngredients();
		if (displayIngredients.isEmpty()) {
			return Optional.empty();
		}
		return Optional.ofNullable(displayIngredients.getFirst());
	}

	public Stream<SlotIngredient<?>> getVisibleSlotIngredientsInDisplayGroup(SlotIngredient<?> displayed) {
		IIngredientVisibility ingredientVisibility = Internal.getJeiRuntime().getJeiHelpers().getIngredientVisibility();
		return getCandidateSlotIngredientsInDisplayGroup(displayed)
			.filter(ingredient -> ingredientVisibility.isIngredientVisible(
				ingredient.typedIngredient(),
				UidContext.Recipe
			));
	}

	public Stream<ITypedIngredient<?>> getCandidateIngredientsInDisplayGroup(SlotIngredient<?> displayed) {
		return getCandidateSlotIngredientsInDisplayGroup(displayed)
			.map(SlotIngredient::typedIngredient);
	}

	private Stream<SlotIngredient<?>> getCandidateSlotIngredientsInDisplayGroup(SlotIngredient<?> displayed) {
		return SlotDisplayIngredientExpander.streamForDisplay(
				ingredientManager,
				getEffectiveDisplayGroupIngredients(displayed)
			)
			.filter(Objects::nonNull);
	}

	public Optional<TagKey<?>> getDisplayGroupTagKey(
		SlotIngredient<?> displayed,
		Supplier<Optional<TagKey<?>>> fallback
	) {
		return getSingleDisplayGroupTagKey(getEffectiveDisplayGroupIngredients(displayed), fallback);
	}

	public Stream<SlotIngredient<?>> getVisibleSlotIngredients() {
		IIngredientVisibility ingredientVisibility = Internal.getJeiRuntime().getJeiHelpers().getIngredientVisibility();
		return getVisibleSlotIngredients(
			getCandidateSource(),
			ingredientManager,
			ingredientVisibility::isIngredientVisible
		);
	}

	private List<@Nullable SlotIngredient<?>> getCandidateSource() {
		if (this.displayOverrides == null) {
			return this.sourceSlotIngredients;
		}
		return this.displayOverrides.getAllSlotIngredients();
	}

	private List<SlotIngredient<?>> getEffectiveDisplayGroupIngredients(SlotIngredient<?> displayed) {
		if (this.displayOverrides != null && this.displayOverrides.getAllSlotIngredients().size() == 1) {
			Optional<SlotIngredient<?>> matchingSourceIngredient = findMatchingSourceIngredient(displayed.typedIngredient());
			if (matchingSourceIngredient.isPresent()) {
				return getDisplayGroupIngredients(this.sourceSlotIngredients, matchingSourceIngredient.get());
			}
		}
		return getDisplayGroupIngredients(getCandidateSource(), displayed);
	}

	private <T> Optional<SlotIngredient<?>> findMatchingSourceIngredient(ITypedIngredient<T> displayed) {
		IIngredientType<T> ingredientType = displayed.getType();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		Object displayedUid = ingredientHelper.getUid(displayed, UidContext.Ingredient);
		return SlotDisplayIngredientExpander.streamForDisplay(ingredientManager, sourceSlotIngredients)
			.filter(Objects::nonNull)
			.filter(candidate -> {
				ITypedIngredient<T> typedCandidate = candidate.typedIngredient().cast(ingredientType);
				return typedCandidate != null && displayedUid.equals(
					ingredientHelper.getUid(typedCandidate, UidContext.Ingredient)
				);
			})
			.findFirst();
	}

	public static Stream<SlotIngredient<?>> getVisibleSlotIngredients(
		List<? extends @Nullable SlotIngredient<?>> ingredients,
		IIngredientManagerInternal ingredientManager,
		Predicate<ITypedIngredient<?>> isVisible
	) {
		return SlotDisplayIngredientExpander.streamForDisplay(ingredientManager, ingredients)
			.filter(Objects::nonNull)
			.filter(ingredient -> isVisible.test(ingredient.typedIngredient()));
	}

	public Stream<ITypedIngredient<?>> getVisibleTypedIngredients() {
		return getVisibleSlotIngredients()
			.map(SlotIngredient::typedIngredient);
	}

	public Stream<ITypedIngredient<?>> getVisibleTypedIngredientsInDisplayGroup(SlotIngredient<?> displayed) {
		return getVisibleSlotIngredientsInDisplayGroup(displayed)
			.map(SlotIngredient::typedIngredient);
	}

	public void clearDisplayOverrides() {
		boolean changed = this.displayOverrides != null;
		this.displayOverrides = null;
		this.displayOverrideIngredients = null;
		if (changed) {
			displayOverridesChangedListener.run();
		}
	}

	public boolean hasDisplayOverrides() {
		return this.displayOverrides != null;
	}

	public IIngredientAcceptor<?> createDisplayOverrides() {
		this.displayOverrideIngredients = null;
		if (displayOverrides == null) {
			displayOverrides = new DisplayIngredientAcceptor(
				ingredientManager,
				contextMap,
				role,
				this::onDisplayOverridesChanged
			);
			onDisplayOverridesChanged();
		}
		return displayOverrides;
	}

	private void onDisplayOverridesChanged() {
		this.displayOverrideIngredients = null;
		displayOverridesChangedListener.run();
	}

	private List<@Nullable SlotIngredient<?>> getDisplayIngredients() {
		if (this.displayOverrides != null) {
			if (this.displayOverrideIngredients == null) {
				this.displayOverrideIngredients = calculateDisplayIngredients(
					this.displayOverrides.getAllSlotIngredients(),
					ingredientManager,
					FocusGroup.EMPTY,
					role
				);
			}
			return this.displayOverrideIngredients;
		}
		if (this.displayIngredients == null) {
			if (this.focusedSlotIngredients == null) {
				this.displayIngredients = calculateDisplayIngredients(
					this.sourceSlotIngredients,
					ingredientManager,
					FocusGroup.EMPTY,
					role
				);
			} else {
				this.displayIngredients = calculateDisplayIngredients(
					this.focusedSlotIngredients,
					ingredientManager,
					this.focusGroup,
					role
				);
			}
		}
		return this.displayIngredients;
	}

	private static List<@Nullable SlotIngredient<?>> calculateDisplayIngredients(
		List<@Nullable SlotIngredient<?>> allIngredients,
		IIngredientManagerInternal ingredientManager,
		IFocusGroup focusGroup,
		RecipeIngredientRole role
	) {
		IIngredientVisibility ingredientVisibility = Internal.getJeiRuntime().getJeiHelpers().getIngredientVisibility();
		return calculateDisplayIngredients(
			allIngredients,
			ingredientManager,
			focusGroup,
			role,
			ingredientVisibility::isIngredientVisible
		);
	}

	public static List<@Nullable SlotIngredient<?>> calculateDisplayIngredients(
		List<? extends @Nullable SlotIngredient<?>> allIngredients,
		IIngredientManagerInternal ingredientManager,
		IFocusGroup focusGroup,
		RecipeIngredientRole role,
		Predicate<ITypedIngredient<?>> isVisible
	) {
		if (allIngredients.isEmpty()) {
			return List.of();
		}
		Supplier<Stream<@Nullable SlotIngredient<?>>> expandedIngredients = () -> SlotDisplayIngredientExpander.streamForDisplay(ingredientManager, allIngredients, focusGroup, role);
		return filterVisibleIngredients(expandedIngredients, isVisible);
	}

	public static List<@Nullable SlotIngredient<?>> filterVisibleIngredients(
		List<? extends @Nullable SlotIngredient<?>> ingredients,
		Predicate<ITypedIngredient<?>> isVisible
	) {
		return filterVisibleIngredients(ingredients::stream, isVisible);
	}

	private static List<@Nullable SlotIngredient<?>> filterVisibleIngredients(
		Supplier<? extends Stream<? extends @Nullable SlotIngredient<?>>> ingredients,
		Predicate<ITypedIngredient<?>> isVisible
	) {
		List<@Nullable SlotIngredient<?>> visibleIngredients = ingredients.get()
			.filter(ingredient -> ingredient == null || isVisible.test(ingredient.typedIngredient()))
			.<@Nullable SlotIngredient<?>>map(ingredient -> ingredient)
			.limit(MAX_DISPLAYED_INGREDIENTS)
			.toList();
		if (!visibleIngredients.isEmpty()) {
			return visibleIngredients;
		}
		// If every ingredient is invisible, show them anyway so that the recipe slot is not blank.
		return ingredients.get()
			.<@Nullable SlotIngredient<?>>map(ingredient -> ingredient)
			.limit(MAX_DISPLAYED_INGREDIENTS)
			.toList();
	}

	public static <T> List<SlotIngredient<?>> getDisplayGroupIngredients(
		List<? extends @Nullable SlotIngredient<?>> ingredients,
		SlotIngredient<T> displayed
	) {
		SlotDisplayData<T> slotDisplayData = displayed.slotDisplayData();
		if (slotDisplayData != null) {
			return slotDisplayData.ingredients()
				.stream()
				.<SlotIngredient<?>>map(ingredient -> new SlotIngredient<>(ingredient, slotDisplayData))
				.toList();
		}
		return ingredients.stream()
			.filter(Objects::nonNull)
			.filter(ingredient -> ingredient.slotDisplayData() == null)
			.<SlotIngredient<?>>map(ingredient -> ingredient)
			.toList();
	}

	private static @Nullable ITypedIngredient<?> getTypedIngredient(@Nullable SlotIngredient<?> ingredient) {
		if (ingredient == null) {
			return null;
		}
		return ingredient.typedIngredient();
	}
}
