package mezz.jei.plugins.jei.info;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.util.MathUtil;
import mezz.jei.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;

public class IngredientInfoRecipe<T> {
	private static final int lineSpacing = 2;
	private final List<ITextProperties> description;
	private final List<T> ingredients;
	private final IIngredientType<T> ingredientType;

	@Deprecated
	public static <T> List<IngredientInfoRecipe<T>> create(List<T> ingredients, IIngredientType<T> ingredientType, String... descriptionKeys) {
		ITextComponent[] descriptionComponents = new ITextComponent[descriptionKeys.length];
		for (int i = 0; i < descriptionKeys.length; i++) {
			String descriptionKey = descriptionKeys[i];
			descriptionComponents[i] = new TranslationTextComponent(descriptionKey);
		}
		return create(ingredients, ingredientType, descriptionComponents);
	}

	public static <T> List<IngredientInfoRecipe<T>> create(List<T> ingredients, IIngredientType<T> ingredientType, ITextComponent... descriptionComponents) {
		List<IngredientInfoRecipe<T>> recipes = new ArrayList<>();
		List<ITextProperties> descriptionLines = StringUtil.expandNewlines(descriptionComponents);
		descriptionLines = StringUtil.splitLines(descriptionLines, IngredientInfoRecipeCategory.recipeWidth);
		final int lineCount = descriptionLines.size();

		Minecraft minecraft = Minecraft.getInstance();
		final int maxLinesPerPage = (IngredientInfoRecipeCategory.recipeHeight - 20) / (minecraft.font.lineHeight + lineSpacing);
		final int pageCount = MathUtil.divideCeil(lineCount, maxLinesPerPage);
		for (int i = 0; i < pageCount; i++) {
			int startLine = i * maxLinesPerPage;
			int endLine = Math.min((i + 1) * maxLinesPerPage, lineCount);
			List<ITextProperties> description = descriptionLines.subList(startLine, endLine);
			IngredientInfoRecipe<T> recipe = new IngredientInfoRecipe<>(ingredients, ingredientType, description);
			recipes.add(recipe);
		}

		return recipes;
	}

	private IngredientInfoRecipe(List<T> ingredients, IIngredientType<T> ingredientType, List<ITextProperties> description) {
		this.description = description;
		this.ingredients = ingredients;
		this.ingredientType = ingredientType;
	}

	public List<ITextProperties> getDescription() {
		return description;
	}

	public IIngredientType<T> getIngredientType() {
		return ingredientType;
	}

	public List<T> getIngredients() {
		return ingredients;
	}
}
