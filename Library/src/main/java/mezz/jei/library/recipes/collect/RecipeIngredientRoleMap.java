package mezz.jei.library.recipes.collect;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.ingredients.RecipeIngredientSupplier;
import mezz.jei.library.ingredients.SlotDisplayData;
import mezz.jei.library.ingredients.SlotIngredient;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A {@link RecipeIngredientRoleMap} contains recipes, IRecipeCategory, and Ingredients for one {@link RecipeIngredientRole}.
 */
public class RecipeIngredientRoleMap {
	private final RecipeIngredientTable recipeTable = new RecipeIngredientTable();
	private final IngredientUidIndex<Set<IRecipeType<?>>> ingredientUidToCategoryMap = new IngredientUidIndex<>();
	private final IngredientUidIndex<Set<IRecipeType<?>>> craftingStationUidToRecipeCategoryMap = new IngredientUidIndex<>();
	private final Comparator<IRecipeType<?>> recipeTypeComparator;
	private final IIngredientManager ingredientManager;
	private final RecipeIngredientRole role;

	public RecipeIngredientRoleMap(Comparator<IRecipeType<?>> recipeTypeComparator, IIngredientManager ingredientManager, RecipeIngredientRole role) {
		this.recipeTypeComparator = recipeTypeComparator;
		this.ingredientManager = ingredientManager;
		this.role = role;
	}

	public <T> Stream<IRecipeType<?>> getRecipeTypes(ITypedIngredient<T> ingredient) {
		IIngredientType<T> ingredientType = ingredient.getType();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		Object exactUid = ingredientHelper.getUid(ingredient, UidContext.Recipe);
		Object groupingUid = ingredientHelper.getGroupingUid(ingredient);
		MatchBuckets<Set<IRecipeType<?>>> recipeTypes = ingredientUidToCategoryMap.get(ingredientType, exactUid, groupingUid);
		Set<IRecipeType<?>> catalystRecipeTypes = craftingStationUidToRecipeCategoryMap.get(ingredientType, exactUid).exact();
		return Stream.of(recipeTypes.exact(), recipeTypes.grouping(), catalystRecipeTypes)
			.filter(Objects::nonNull)
			.flatMap(Collection::stream)
			.distinct()
			.sorted(recipeTypeComparator);
	}

	public <T> void addCraftingStationForCategory(IRecipeType<?> recipeType, ITypedIngredient<T> ingredient) {
		IIngredientType<T> ingredientType = ingredient.getType();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		Object exactUid = ingredientHelper.getUid(ingredient, UidContext.Recipe);
		craftingStationUidToRecipeCategoryMap.computeExactIfAbsent(ingredientType, exactUid, ObjectOpenHashSet::new)
			.add(recipeType);
	}

	@UnmodifiableView
	public <T> List<T> getRecipes(IRecipeType<T> recipeType, ITypedIngredient<?> ingredient) {
		return getRecipes(recipeType, ingredient.getType(), ingredient);
	}

	public <T> boolean isCraftingStationForRecipeCategory(IRecipeType<T> recipeType, ITypedIngredient<?> ingredient) {
		return isCraftingStationForRecipeCategory(recipeType, ingredient.getType(), ingredient);
	}

	private <T, I> List<T> getRecipes(IRecipeType<T> recipeType, IIngredientType<I> ingredientType, ITypedIngredient<?> ingredient) {
		ITypedIngredient<I> typedIngredient = ingredient.cast(ingredientType);
		if (typedIngredient == null) {
			return List.of();
		}
		IIngredientHelper<I> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		Object exactUid = ingredientHelper.getUid(typedIngredient, UidContext.Recipe);
		Object groupingUid = ingredientHelper.getGroupingUid(typedIngredient);
		return recipeTable.get(recipeType, ingredientType, exactUid, groupingUid);
	}

	private <T, I> boolean isCraftingStationForRecipeCategory(IRecipeType<T> recipeType, IIngredientType<I> ingredientType, ITypedIngredient<?> ingredient) {
		ITypedIngredient<I> typedIngredient = ingredient.cast(ingredientType);
		if (typedIngredient == null) {
			return false;
		}
		IIngredientHelper<I> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		Object exactUid = ingredientHelper.getUid(typedIngredient, UidContext.Recipe);
		Set<IRecipeType<?>> craftingStationTypes = craftingStationUidToRecipeCategoryMap.get(ingredientType, exactUid).exact();
		if (craftingStationTypes == null) {
			return false;
		}
		return craftingStationTypes.contains(recipeType);
	}

	public <T> void addRecipe(IRecipeType<T> recipeType, T recipe, RecipeIngredientSupplier ingredientSupplier) {
		IngredientUidIndex<Boolean> ingredientUids = new IngredientUidIndex<>();
		for (SlotIngredient<?> ingredient : ingredientSupplier.getSlotIngredients(this.role)) {
			addRecipeIngredient(ingredient, ingredientUids);
		}

		ingredientUids.forEach((ingredientType, uid, buckets) -> {
			if (buckets.exact() != null) {
				ingredientUidToCategoryMap.computeExactIfAbsent(ingredientType, uid, () -> new ObjectOpenHashSet<>(2))
					.add(recipeType);
				recipeTable.addExact(recipe, recipeType, ingredientType, uid);
			}
			if (buckets.grouping() != null) {
				ingredientUidToCategoryMap.computeGroupingIfAbsent(ingredientType, uid, () -> new ObjectOpenHashSet<>(2))
					.add(recipeType);
				recipeTable.addGrouping(recipe, recipeType, ingredientType, uid);
			}
		});
	}

	public void compact() {
		recipeTable.compact();
	}

	private <T> void addRecipeIngredient(SlotIngredient<T> slotIngredient, IngredientUidIndex<Boolean> ingredientUids) {
		SlotDisplayData<T> slotDisplayData = slotIngredient.slotDisplayData();
		ITypedIngredient<T> typedIngredient = slotIngredient.typedIngredient();
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		if (slotDisplayData != null && slotDisplayData.info().matchesAllSubtypes()) {
			Object groupingUid = ingredientHelper.getGroupingUid(typedIngredient);
			ingredientUids.computeGroupingIfAbsent(ingredientType, groupingUid, () -> Boolean.TRUE);
		} else {
			Object exactUid = ingredientHelper.getUid(typedIngredient, UidContext.Recipe);
			ingredientUids.computeExactIfAbsent(ingredientType, exactUid, () -> Boolean.TRUE);
		}
	}
}
