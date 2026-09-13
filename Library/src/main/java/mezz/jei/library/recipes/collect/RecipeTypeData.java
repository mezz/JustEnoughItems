package mezz.jei.library.recipes.collect;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.IRecipeCategory;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class RecipeTypeData<T> {
	private final IRecipeCategory<T> recipeCategory;
	private final List<Consumer<IIngredientAcceptor<?>>> craftingStations;
	private final List<T> recipes = new ArrayList<>();
	private final Set<T> hiddenRecipes = Collections.newSetFromMap(new IdentityHashMap<>());

	public RecipeTypeData(
		IRecipeCategory<T> recipeCategory,
		List<Consumer<IIngredientAcceptor<?>>> craftingStations
	) {
		this.recipeCategory = recipeCategory;
		this.craftingStations = List.copyOf(craftingStations);
	}

	public IRecipeCategory<T> getRecipeCategory() {
		return recipeCategory;
	}

	@Unmodifiable
	public List<Consumer<IIngredientAcceptor<?>>> getCraftingStations() {
		return craftingStations;
	}

	@UnmodifiableView
	public List<T> getRecipes() {
		return Collections.unmodifiableList(recipes);
	}

	public void addRecipes(Collection<T> recipes) {
		this.recipes.addAll(recipes);
	}

	public Set<T> getHiddenRecipes() {
		return hiddenRecipes;
	}
}
