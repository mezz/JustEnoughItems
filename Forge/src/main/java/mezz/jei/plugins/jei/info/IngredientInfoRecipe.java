package mezz.jei.plugins.jei.info;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.vanilla.IJeiIngredientInfoRecipe;
import mezz.jei.ingredients.RegisteredIngredients;
import mezz.jei.ingredients.TypedIngredient;
import mezz.jei.util.MathUtil;
import mezz.jei.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IngredientInfoRecipe implements IJeiIngredientInfoRecipe {
	private static final int lineSpacing = 2;
	private final List<FormattedText> description;
	private final List<ITypedIngredient<?>> ingredients;

	public static <T> List<IJeiIngredientInfoRecipe> create(
		RegisteredIngredients registeredIngredients,
		List<T> ingredients,
		IIngredientType<T> ingredientType,
		Component... descriptionComponents
	) {
		List<ITypedIngredient<?>> typedIngredients = ingredients.stream()
			.map(i -> TypedIngredient.create(registeredIngredients, ingredientType, i))
			.flatMap(Optional::stream)
			.toList();

		List<IJeiIngredientInfoRecipe> recipes = new ArrayList<>();
		List<FormattedText> descriptionLines = StringUtil.expandNewlines(descriptionComponents);
		descriptionLines = StringUtil.splitLines(descriptionLines, IngredientInfoRecipeCategory.recipeWidth);
		final int lineCount = descriptionLines.size();

		Minecraft minecraft = Minecraft.getInstance();
		final int maxLinesPerPage = (IngredientInfoRecipeCategory.recipeHeight - 20) / (minecraft.font.lineHeight + lineSpacing);
		final int pageCount = MathUtil.divideCeil(lineCount, maxLinesPerPage);
		for (int i = 0; i < pageCount; i++) {
			int startLine = i * maxLinesPerPage;
			int endLine = Math.min((i + 1) * maxLinesPerPage, lineCount);
			List<FormattedText> description = descriptionLines.subList(startLine, endLine);
			IngredientInfoRecipe recipe = new IngredientInfoRecipe(typedIngredients, description);
			recipes.add(recipe);
		}

		return recipes;
	}

	private IngredientInfoRecipe(List<ITypedIngredient<?>> ingredients, List<FormattedText> description) {
		this.description = description;
		this.ingredients = ingredients;
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
