package mezz.jei.library.recipes.collect;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public class RecipeTypeData<T> {
	private final IRecipeCategory<T> recipeCategory;
	private final List<ITypedIngredient<?>> recipeCategoryCatalysts;
	private final List<T> recipes = new ArrayList<>();
	private final Set<T> hiddenRecipes = Collections.newSetFromMap(new IdentityHashMap<>());
	private final Map<ResourceLocation, T> recipeUidMap = new HashMap<>();

	public RecipeTypeData(IRecipeCategory<T> recipeCategory, List<ITypedIngredient<?>> recipeCategoryCatalysts) {
		this.recipeCategory = recipeCategory;
		this.recipeCategoryCatalysts = List.copyOf(recipeCategoryCatalysts);
	}

	public IRecipeCategory<T> getRecipeCategory() {
		return recipeCategory;
	}

	@Unmodifiable
	public List<ITypedIngredient<?>> getRecipeCategoryCatalysts() {
		return recipeCategoryCatalysts;
	}

	@UnmodifiableView
	public List<T> getRecipes() {
		return Collections.unmodifiableList(recipes);
	}

	public Optional<T> getRecipeById(ResourceLocation recipeUid) {
		return Optional.ofNullable(recipeUidMap.get(recipeUid));
	}

	public void addRecipes(Collection<T> recipes) {
		this.recipes.addAll(recipes);
		for (T recipe : recipes) {
			ResourceLocation recipeUid = recipeCategory.getUniqueId(recipe);
			if (recipeUid != null) {
				recipeUidMap.put(recipeUid, recipe);
			}
		}
	}

	public Set<T> getHiddenRecipes() {
		return hiddenRecipes;
	}
}
