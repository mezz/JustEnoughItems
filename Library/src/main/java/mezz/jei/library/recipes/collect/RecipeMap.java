package mezz.jei.library.recipes.collect;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientSupplier;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A RecipeMap efficiently links recipes, IRecipeCategory, and Ingredients.
 */
public class RecipeMap {
	private final RecipeIngredientTable recipeTable = new RecipeIngredientTable();
	private final Multimap<Object, RecipeType<?>> ingredientUidToCategoryMap = Multimaps.newSetMultimap(new Object2ObjectOpenHashMap<>(), () -> new ObjectOpenHashSet<>(2));
	private final Multimap<Object, RecipeType<?>> categoryCatalystUidToRecipeCategoryMap = Multimaps.newSetMultimap(new Object2ObjectOpenHashMap<>(), ObjectOpenHashSet::new);
	private final Comparator<RecipeType<?>> recipeTypeComparator;
	private final IIngredientManager ingredientManager;
	private final RecipeIngredientRole role;

	public RecipeMap(Comparator<RecipeType<?>> recipeTypeComparator, IIngredientManager ingredientManager, RecipeIngredientRole role) {
		this.recipeTypeComparator = recipeTypeComparator;
		this.ingredientManager = ingredientManager;
		this.role = role;
	}

	public <T> Stream<RecipeType<?>> getRecipeTypes(ITypedIngredient<T> ingredient) {
		Object ingredientUid = getIngredientUid(ingredient);
		Collection<RecipeType<?>> recipeCategoryUids = ingredientUidToCategoryMap.get(ingredientUid);
		Collection<RecipeType<?>> catalystRecipeCategoryUids = categoryCatalystUidToRecipeCategoryMap.get(ingredientUid);
		return Stream.concat(recipeCategoryUids.stream(), catalystRecipeCategoryUids.stream())
			.sorted(recipeTypeComparator);
	}

	public <T> void addCatalystForCategory(RecipeType<?> recipeType, ITypedIngredient<T> ingredient) {
		Object ingredientUid = getIngredientUid(ingredient);
		categoryCatalystUidToRecipeCategoryMap.put(ingredientUid, recipeType);
	}

	@UnmodifiableView
	public <T> List<T> getRecipes(RecipeType<T> recipeType, ITypedIngredient<?> ingredient) {
		Object ingredientUid = getIngredientUid(ingredient);
		return recipeTable.get(recipeType, ingredientUid);
	}

	public <T> boolean isCatalystForRecipeCategory(RecipeType<T> recipeType, ITypedIngredient<?> ingredient) {
		Object ingredientUid = getIngredientUid(ingredient);
		Collection<RecipeType<?>> catalystCategories = categoryCatalystUidToRecipeCategoryMap.get(ingredientUid);
		return catalystCategories.contains(recipeType);
	}

	public <T> void addRecipe(RecipeType<T> recipeType, T recipe, IIngredientSupplier ingredientSupplier) {
		Set<Object> ingredientUids = new HashSet<>();
		Collection<ITypedIngredient<?>> ingredients = ingredientSupplier.getIngredients(this.role);
		for (ITypedIngredient<?> ingredient : ingredients) {
			Object ingredientUid = getIngredientUid(ingredient);
			ingredientUids.add(ingredientUid);
		}

		if (!ingredientUids.isEmpty()) {
			for (Object ingredientUid : ingredientUids) {
				ingredientUidToCategoryMap.put(ingredientUid, recipeType);
			}
			recipeTable.add(recipe, recipeType, ingredientUids);
		}
	}

	public void compact() {
		recipeTable.compact();
	}

	private <T> Object getIngredientUid(ITypedIngredient<T> typedIngredient) {
		IIngredientType<T> type = typedIngredient.getType();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(type);
		return ingredientHelper.getUid(typedIngredient.getIngredient(), UidContext.Recipe);
	}
}
