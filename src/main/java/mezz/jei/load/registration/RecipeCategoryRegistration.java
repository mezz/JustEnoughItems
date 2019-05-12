package mezz.jei.load.registration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.util.ErrorUtil;

public class RecipeCategoryRegistration implements IRecipeCategoryRegistration {
	private final List<IRecipeCategory> recipeCategories = new ArrayList<>();
	private final Map<ResourceLocation, IRecipeCategory> recipeCategoriesByUid = new HashMap<>();
	private final IJeiHelpers jeiHelpers;

	public RecipeCategoryRegistration(IJeiHelpers jeiHelpers) {
		this.jeiHelpers = jeiHelpers;
	}

	@Override
	public void addRecipeCategories(IRecipeCategory... recipeCategories) {
		ErrorUtil.checkNotEmpty(recipeCategories, "recipeCategories");

		for (IRecipeCategory recipeCategory : recipeCategories) {
			ResourceLocation uid = recipeCategory.getUid();
			Preconditions.checkNotNull(uid, "Recipe category UID cannot be null %s", recipeCategory);
			Class<?> recipeClass = recipeCategory.getRecipeClass();
			Preconditions.checkNotNull(recipeClass, "Recipe class cannot be null %s", recipeCategory);
			if (recipeCategoriesByUid.containsKey(uid)) {
				throw new IllegalArgumentException("A RecipeCategory with UID \"" + uid + "\" has already been registered.");
			} else {
				recipeCategoriesByUid.put(uid, recipeCategory);
			}
		}

		Collections.addAll(this.recipeCategories, recipeCategories);
	}

	@Override
	public IJeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}

	public ImmutableList<IRecipeCategory> getRecipeCategories() {
		return ImmutableList.copyOf(recipeCategories);
	}

	public ImmutableMap<ResourceLocation, IRecipeCategory> getRecipeCategoriesByUid() {
		return ImmutableMap.copyOf(recipeCategoriesByUid);
	}
}
