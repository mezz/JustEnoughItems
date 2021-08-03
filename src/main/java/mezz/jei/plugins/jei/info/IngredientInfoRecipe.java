package mezz.jei.plugins.jei.info;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.util.MathUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.Style;

public class IngredientInfoRecipe<T> {
	private static final int lineSpacing = 2;
	private final List<FormattedText> description;
	private final List<T> ingredients;
	private final IIngredientType<T> ingredientType;

	public static <T> List<IngredientInfoRecipe<T>> create(List<T> ingredients, IIngredientType<T> ingredientType, Component... descriptionComponents) {
		List<IngredientInfoRecipe<T>> recipes = new ArrayList<>();
		List<FormattedText> descriptionLines = expandNewlines(descriptionComponents);
		descriptionLines = wrapDescriptionLines(descriptionLines);
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

	private static List<FormattedText> expandNewlines(Component... descriptionComponents) {
		List<FormattedText> descriptionLinesExpanded = new ArrayList<>();
		for (Component descriptionLine : descriptionComponents) {
			ExpandNewLineTextAcceptor newLineTextAcceptor = new ExpandNewLineTextAcceptor();
			descriptionLine.visit(newLineTextAcceptor, Style.EMPTY);
			newLineTextAcceptor.addLinesTo(descriptionLinesExpanded);
		}
		return descriptionLinesExpanded;
	}

	private static List<FormattedText> wrapDescriptionLines(List<FormattedText> descriptionLines) {
		Minecraft minecraft = Minecraft.getInstance();
		List<FormattedText> descriptionLinesWrapped = new ArrayList<>();
		for (FormattedText descriptionLine : descriptionLines) {
			List<FormattedText> textLines = minecraft.font.getSplitter().splitLines(descriptionLine, IngredientInfoRecipeCategory.recipeWidth, Style.EMPTY);
			descriptionLinesWrapped.addAll(textLines);
		}
		return descriptionLinesWrapped;
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

	private static class ExpandNewLineTextAcceptor implements FormattedText.StyledContentConsumer<Void> {

		private final List<FormattedText> lines = new ArrayList<>();
		@Nullable
		private MutableComponent lastComponent;

		@Override
		public Optional<Void> accept(Style style, String line) {
			String[] descriptionLineExpanded = line.split("\\\\n");
			for (int i = 0; i < descriptionLineExpanded.length; i++) {
				String s = descriptionLineExpanded[i];
				if (s.isEmpty()) {
					//If the string is empty
					if (i == 0 && lastComponent != null) {
						// and we are the first string (for example from a string \nTest)
						// and we had a last component (we are a variable in a translation string)
						// add our last component as is and reset it
						lines.add(lastComponent);
						lastComponent = null;
					} else {
						//Otherwise just add the empty line
						lines.add(TextComponent.EMPTY);
					}
					continue;
				}
				TextComponent textComponent = new TextComponent(s);
				textComponent.setStyle(style);
				if (lastComponent != null) {
					//If we already have a component that we want to continue with
					if (i == 0) {
						// and we are the first line, add ourselves to the last component
						if (!lastComponent.getStyle().isEmpty() && !lastComponent.getStyle().equals(style)) {
							//If it has a style and the style is different from the style the text component
							// we are adding has add the last component as a sibling to an empty unstyled
							// component so that we don't cause the styling to leak into the component we are adding
							lastComponent = new TextComponent("").append(lastComponent);
						}
						lastComponent.append(textComponent);
						continue;
					} else {
						// otherwise if we aren't the first line, add the old component to our list of lines
						lines.add(lastComponent);
						lastComponent = null;
					}
				}
				if (i == descriptionLineExpanded.length - 1) {
					//If we are the last line we are adding, persist the text component
					lastComponent = textComponent;
				} else {
					//Otherwise add it to our list of lines
					lines.add(textComponent);
				}
			}
			return Optional.empty();
		}

		public void addLinesTo(List<FormattedText> descriptionLinesExpanded) {
			descriptionLinesExpanded.addAll(lines);
			if (lastComponent != null) {
				descriptionLinesExpanded.add(lastComponent);
			}
		}
	}
}
