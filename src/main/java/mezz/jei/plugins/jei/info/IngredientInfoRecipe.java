package mezz.jei.plugins.jei.info;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.util.MathUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;

public class IngredientInfoRecipe<T> {
	private static final int lineSpacing = 2;
	private final List<ITextProperties> description;
	private final List<T> ingredients;
	private final IIngredientType<T> ingredientType;

	public static <T> List<IngredientInfoRecipe<T>> create(List<T> ingredients, IIngredientType<T> ingredientType, ITextComponent... descriptionComponents) {
		List<IngredientInfoRecipe<T>> recipes = new ArrayList<>();
		List<ITextProperties> descriptionLines = expandNewlines(descriptionComponents);
		descriptionLines = wrapDescriptionLines(descriptionLines);
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

	private static List<ITextProperties> expandNewlines(ITextComponent... descriptionComponents) {
		List<ITextProperties> descriptionLinesExpanded = new ArrayList<>();
		for (ITextComponent descriptionLine : descriptionComponents) {
			ExpandNewLineTextAcceptor newLineTextAcceptor = new ExpandNewLineTextAcceptor();
			descriptionLine.visit(newLineTextAcceptor, Style.EMPTY);
			newLineTextAcceptor.addLinesTo(descriptionLinesExpanded);
		}
		return descriptionLinesExpanded;
	}

	private static List<ITextProperties> wrapDescriptionLines(List<ITextProperties> descriptionLines) {
		Minecraft minecraft = Minecraft.getInstance();
		List<ITextProperties> descriptionLinesWrapped = new ArrayList<>();
		for (ITextProperties descriptionLine : descriptionLines) {
			List<ITextProperties> textLines = minecraft.font.getSplitter().splitLines(descriptionLine, IngredientInfoRecipeCategory.recipeWidth, Style.EMPTY);
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

	private static class ExpandNewLineTextAcceptor implements ITextProperties.IStyledTextAcceptor<Void> {

		private final List<ITextProperties> lines = new ArrayList<>();
		@Nullable
		private IFormattableTextComponent lastComponent;

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
						lines.add(StringTextComponent.EMPTY);
					}
					continue;
				}
				StringTextComponent textComponent = new StringTextComponent(s);
				textComponent.setStyle(style);
				if (lastComponent != null) {
					//If we already have a component that we want to continue with
					if (i == 0) {
						// and we are the first line, add ourselves to the last component
						if (!lastComponent.getStyle().isEmpty() && !lastComponent.getStyle().equals(style)) {
							//If it has a style and the style is different from the style the text component
							// we are adding has add the last component as a sibling to an empty unstyled
							// component so that we don't cause the styling to leak into the component we are adding
							lastComponent = new StringTextComponent("").append(lastComponent);
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

		public void addLinesTo(List<ITextProperties> descriptionLinesExpanded) {
			descriptionLinesExpanded.addAll(lines);
			if (lastComponent != null) {
				descriptionLinesExpanded.add(lastComponent);
			}
		}
	}
}
