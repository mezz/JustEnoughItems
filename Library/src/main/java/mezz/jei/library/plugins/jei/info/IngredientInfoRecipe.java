package mezz.jei.library.plugins.jei.info;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.vanilla.IJeiIngredientInfoRecipe;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.util.StringUtil;
import mezz.jei.library.ingredients.TypedIngredient;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import java.util.Collections;
import java.util.List;

public class IngredientInfoRecipe implements IJeiIngredientInfoRecipe {
	private final List<FormattedText> description;
	private final List<ITypedIngredient<?>> ingredients;

	public static <T> IJeiIngredientInfoRecipe create(
		IIngredientManager ingredientManager,
		List<T> ingredients,
		IIngredientType<T> ingredientType,
		Component... descriptionComponents
	) {
		List<ITypedIngredient<T>> typedIngredients = TypedIngredient.createAndFilterInvalidNonnullList(ingredientManager, ingredientType, ingredients, true);
		List<FormattedText> descriptionLines = StringUtil.expandNewlines(descriptionComponents);
		return new IngredientInfoRecipe(typedIngredients, descriptionLines);
	}

	private IngredientInfoRecipe(List<? extends ITypedIngredient<?>> ingredients, List<FormattedText> description) {
		this.description = description;
		this.ingredients = Collections.unmodifiableList(ingredients);
	}

	@Override
	public List<FormattedText> getDescription() {
		return description;
	}

	@Override
	public List<ITypedIngredient<?>> getIngredients() {
		return ingredients;
	}
}
