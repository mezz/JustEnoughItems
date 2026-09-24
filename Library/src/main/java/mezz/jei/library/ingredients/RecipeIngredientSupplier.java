package mezz.jei.library.ingredients;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.ingredients.IIngredientSupplier;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Ingredients collected from a recipe layout, including the slot display data needed when recipes are indexed.
 */
public class RecipeIngredientSupplier implements IIngredientSupplier {
	private final Map<RecipeIngredientRole, List<SlotIngredient<?>>> ingredientsByRole;
	private final List<FocusLink> focusLinks;

	public RecipeIngredientSupplier(Map<RecipeIngredientRole, List<SlotIngredient<?>>> ingredientsByRole) {
		this(ingredientsByRole, List.of());
	}

	public RecipeIngredientSupplier(
		Map<RecipeIngredientRole, List<SlotIngredient<?>>> ingredientsByRole,
		List<FocusLink> focusLinks
	) {
		EnumMap<RecipeIngredientRole, List<SlotIngredient<?>>> copiedIngredients = new EnumMap<>(RecipeIngredientRole.class);
		ingredientsByRole.forEach((role, ingredients) -> copiedIngredients.put(role, List.copyOf(ingredients)));
		this.ingredientsByRole = Collections.unmodifiableMap(copiedIngredients);
		this.focusLinks = List.copyOf(focusLinks);
	}

	@Override
	public List<ITypedIngredient<?>> getIngredients(RecipeIngredientRole role) {
		return getSlotIngredients(role).stream()
			.<ITypedIngredient<?>>map(SlotIngredient::typedIngredient)
			.toList();
	}

	public List<SlotIngredient<?>> getSlotIngredients(RecipeIngredientRole role) {
		return ingredientsByRole.getOrDefault(role, List.of());
	}

	@Unmodifiable
	public List<FocusLink> getFocusLinks() {
		return focusLinks;
	}

	public record FocusLink(List<Slot> slots) {
		public FocusLink {
			slots = List.copyOf(slots);
		}

		public @Nullable Set<Integer> getVisibleIngredientIndexes(
			IFocusGroup focuses,
			IIngredientManagerInternal ingredientManager,
			Predicate<ITypedIngredient<?>> isVisible
		) {
			if (slots.isEmpty()) {
				return Set.of();
			}

			IntSet focusMatches = new IntArraySet();
			for (Slot slot : slots) {
				focusMatches.addAll(DisplayIngredientAcceptor.getMatches(
					slot.ingredients(),
					focuses,
					slot.role(),
					ingredientManager
				));
			}

			Set<Integer> candidateIndexes = new LinkedHashSet<>();
			if (focusMatches.isEmpty()) {
				for (int i = 0; i < slots.getFirst().ingredients().size(); i++) {
					candidateIndexes.add(i);
				}
			} else {
				candidateIndexes.addAll(focusMatches);
			}

			Set<Integer> visibleIndexes = new LinkedHashSet<>();
			for (int index : candidateIndexes) {
				boolean visible = true;
				for (Slot slot : slots) {
					if (!isIngredientVisible(slot, index, focuses, ingredientManager, isVisible)) {
						visible = false;
						break;
					}
				}
				if (visible) {
					visibleIndexes.add(index);
				}
			}

			if (visibleIndexes.isEmpty() && !candidateIndexes.isEmpty()) {
				return null;
			}
			if (focusMatches.isEmpty() && visibleIndexes.equals(candidateIndexes)) {
				// Preserve the empty-set sentinel so that an unrestricted slot does not look focused.
				return Set.of();
			}
			return Collections.unmodifiableSet(visibleIndexes);
		}

		private static boolean isIngredientVisible(
			Slot slot,
			int index,
			IFocusGroup focuses,
			IIngredientManagerInternal ingredientManager,
			Predicate<ITypedIngredient<?>> isVisible
		) {
			if (index < 0 || index >= slot.ingredients().size()) {
				return false;
			}
			SlotIngredient<?> ingredient = slot.ingredients().get(index);
			if (ingredient == null) {
				return true;
			}
			return SlotDisplayIngredientExpander.streamForDisplay(
					ingredientManager,
					List.of(ingredient),
					focuses,
					slot.role()
				)
				.anyMatch(expanded -> expanded == null || isVisible.test(expanded.typedIngredient()));
		}

		public record Slot(
			RecipeIngredientRole role,
			List<@Nullable SlotIngredient<?>> ingredients
		) {
			public Slot {
				ingredients = Collections.unmodifiableList(new ArrayList<>(ingredients));
			}
		}
	}
}
