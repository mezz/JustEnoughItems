package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Expands slot display ingredients that represent all subtypes into concrete ingredients for visual rotation.
 * The original ingredient list remains unchanged for recipe lookup and focus matching.
 */
public final class SlotDisplayIngredientExpander {
	private SlotDisplayIngredientExpander() {
	}

	public static List<@Nullable SlotIngredient<?>> expandForDisplay(
		IIngredientManagerInternal ingredientManager,
		List<? extends @Nullable SlotIngredient<?>> ingredients
	) {
		return streamForDisplay(ingredientManager, ingredients)
			.toList();
	}

	public static List<@Nullable SlotIngredient<?>> expandForDisplay(
		IIngredientManagerInternal ingredientManager,
		List<? extends @Nullable SlotIngredient<?>> ingredients,
		IFocusGroup focusGroup,
		RecipeIngredientRole role
	) {
		return streamForDisplay(ingredientManager, ingredients, focusGroup, role)
			.toList();
	}

	public static Stream<@Nullable SlotIngredient<?>> streamForDisplay(
		IIngredientManagerInternal ingredientManager,
		List<? extends @Nullable SlotIngredient<?>> ingredients
	) {
		return streamForDisplay(ingredientManager, ingredients, List.of());
	}

	public static Stream<@Nullable SlotIngredient<?>> streamForDisplay(
		IIngredientManagerInternal ingredientManager,
		List<? extends @Nullable SlotIngredient<?>> ingredients,
		IFocusGroup focusGroup,
		RecipeIngredientRole role
	) {
		List<IFocus<?>> focuses = focusGroup.getFocuses(role).toList();
		return streamForDisplay(ingredientManager, ingredients, focuses);
	}

	private static Stream<@Nullable SlotIngredient<?>> streamForDisplay(
		IIngredientManagerInternal ingredientManager,
		List<? extends @Nullable SlotIngredient<?>> ingredients,
		List<IFocus<?>> focuses
	) {
		Map<SlotDisplayData<?>, Set<ExpansionKey>> expandedGroups = new IdentityHashMap<>();
		return ingredients.stream()
			.flatMap(ingredient -> {
				if (ingredient == null) {
					return Stream.of((SlotIngredient<?>) null);
				}
				return streamForDisplay(ingredientManager, ingredient, focuses, expandedGroups);
			});
	}

	private static <T> Stream<SlotIngredient<?>> streamForDisplay(
		IIngredientManagerInternal ingredientManager,
		SlotIngredient<T> ingredient,
		List<IFocus<?>> focuses,
		Map<SlotDisplayData<?>, Set<ExpansionKey>> expandedGroups
	) {
		SlotDisplayData<T> slotDisplayData = ingredient.slotDisplayData();
		if (slotDisplayData == null || !slotDisplayData.info().matchesAllSubtypes()) {
			return Stream.of(ingredient);
		}

		ITypedIngredient<T> typedIngredient = ingredient.typedIngredient();
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		Object groupingUid = ingredientHelper.getGroupingUid(typedIngredient);
		List<ITypedIngredient<T>> focusedIngredients = getFocusedIngredients(
			ingredientType,
			ingredientHelper,
			groupingUid,
			focuses
		);
		List<ITypedIngredient<T>> replacements;
		if (focusedIngredients.isEmpty()) {
			replacements = ingredientManager.getGroupedIngredients(typedIngredient);
		} else {
			replacements = focusedIngredients;
		}
		if (replacements.isEmpty()) {
			return Stream.of(ingredient);
		}

		ExpansionKey expansionKey = new ExpansionKey(ingredientType, groupingUid);
		Set<ExpansionKey> displayGroups = expandedGroups.computeIfAbsent(slotDisplayData, key -> new HashSet<>());
		if (!displayGroups.add(expansionKey)) {
			return Stream.empty();
		}

		return replacements.stream()
			.<SlotIngredient<?>>map(replacement -> new SlotIngredient<>(replacement, slotDisplayData));
	}

	private static <T> List<ITypedIngredient<T>> getFocusedIngredients(
		IIngredientType<T> ingredientType,
		IIngredientHelper<T> ingredientHelper,
		Object groupingUid,
		List<IFocus<?>> focuses
	) {
		if (focuses.isEmpty()) {
			return List.of();
		}
		return focuses.stream()
			.map(IFocus::getTypedValue)
			.map(focus -> focus.cast(ingredientType))
			.filter(Objects::nonNull)
			.filter(focus -> groupingUid.equals(ingredientHelper.getGroupingUid(focus)))
			.toList();
	}

	private record ExpansionKey(IIngredientType<?> ingredientType, Object groupingUid) {
	}
}
