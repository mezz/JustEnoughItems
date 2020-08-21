package mezz.jei.plugins.jei.info;

import java.util.ArrayList;
import java.util.List;

import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.util.MathUtil;

public class IngredientInfoRecipe<T> {
	private static final int lineSpacing = 2;
	private final List<ITextProperties> description;
	private final List<T> ingredients;
	private final IIngredientType<T> ingredientType;

	public static <T> List<IngredientInfoRecipe<T>> create(List<T> ingredients, IIngredientType<T> ingredientType, String... descriptionKeys) {
		List<IngredientInfoRecipe<T>> recipes = new ArrayList<>();

		List<ITextProperties> descriptionLines = translateDescriptionLines(descriptionKeys);
		descriptionLines = expandNewlines(descriptionLines);
		descriptionLines = wrapDescriptionLines(descriptionLines);
		final int lineCount = descriptionLines.size();

		Minecraft minecraft = Minecraft.getInstance();
		final int maxLinesPerPage = (IngredientInfoRecipeCategory.recipeHeight - 20) / (minecraft.fontRenderer.FONT_HEIGHT + lineSpacing);
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

	private static List<ITextProperties> translateDescriptionLines(String... descriptionKeys) {
		List<ITextProperties> descriptionLines = new ArrayList<>();
		for (String descriptionKey : descriptionKeys) {
			TranslationTextComponent translatedLine = new TranslationTextComponent(descriptionKey);
			descriptionLines.add(translatedLine);
		}
		return descriptionLines;
	}

	private static List<ITextProperties> expandNewlines(List<ITextProperties> descriptionLines) {
		List<ITextProperties> descriptionLinesExpanded = new ArrayList<>();
		for (ITextProperties descriptionLine : descriptionLines) {
			Optional<String[]> optionalExpandedLines = descriptionLine.func_230438_a_(line -> Optional.of(line.split("\\\\n")));
			optionalExpandedLines.ifPresent(descriptionLineExpanded -> {
				for (String s : descriptionLineExpanded) {
					descriptionLinesExpanded.add(new StringTextComponent(s));
				}
			});
		}
		return descriptionLinesExpanded;
	}

	private static List<ITextProperties> wrapDescriptionLines(List<ITextProperties> descriptionLines) {
		Minecraft minecraft = Minecraft.getInstance();
		List<ITextProperties> descriptionLinesWrapped = new ArrayList<>();
		for (ITextProperties descriptionLine : descriptionLines) {
			List<ITextProperties> textLines = minecraft.fontRenderer.func_238420_b_().func_238362_b_(descriptionLine, IngredientInfoRecipeCategory.recipeWidth, Style.EMPTY);
			descriptionLinesWrapped.addAll(textLines);
		}
		return descriptionLinesWrapped;
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
