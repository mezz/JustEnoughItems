package mezz.jei.plugins.jei.info;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.util.MathUtil;
import mezz.jei.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import java.util.ArrayList;
import java.util.List;

public class IngredientInfoRecipe<T> {
	private static final int lineSpacing = 2;
	private final List<FormattedText> description;
	private final List<T> ingredients;
	private final IIngredientType<T> ingredientType;

	public static <T> List<IngredientInfoRecipe<T>> create(List<T> ingredients, IIngredientType<T> ingredientType, Component... descriptionComponents) {
		List<IngredientInfoRecipe<T>> recipes = new ArrayList<>();
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
			IngredientInfoRecipe<T> recipe = new IngredientInfoRecipe<>(ingredients, ingredientType, description);
			recipes.add(recipe);
		}

		return recipes;
	}

	private IngredientInfoRecipe(List<T> ingredients, IIngredientType<T> ingredientType, List<FormattedText> description) {
		this.description = description;
		this.ingredients = ingredients;
		this.ingredientType = ingredientType;
	}

	public List<FormattedText> getDescription() {
		return description;
	}

	public IIngredientType<T> getIngredientType() {
		return ingredientType;
	}

	public List<T> getIngredients() {
		return ingredients;
	}
}
