package mezz.jei.plugins.jei.info;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.util.MathUtil;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;

public class IngredientInfoRecipe<T> extends BlankRecipeWrapper {
	private static final int lineSpacing = 2;
	private final List<String> description;
	private final List<T> ingredients;
	private final Class<? extends T> ingredientClass;
	private final IDrawable slotDrawable;

	public static <T> List<IngredientInfoRecipe<T>> create(IGuiHelper guiHelper, List<T> ingredients, Class<? extends T> ingredientClass, String... descriptionKeys) {
		List<IngredientInfoRecipe<T>> recipes = new ArrayList<IngredientInfoRecipe<T>>();

		List<String> descriptionLines = translateDescriptionLines(descriptionKeys);
		descriptionLines = expandNewlines(descriptionLines);
		descriptionLines = wrapDescriptionLines(descriptionLines);
		final int lineCount = descriptionLines.size();

		Minecraft minecraft = Minecraft.getMinecraft();
		final int maxLinesPerPage = (IngredientInfoRecipeCategory.recipeHeight - 20) / (minecraft.fontRenderer.FONT_HEIGHT + lineSpacing);
		final int pageCount = MathUtil.divideCeil(lineCount, maxLinesPerPage);
		for (int i = 0; i < pageCount; i++) {
			int startLine = i * maxLinesPerPage;
			int endLine = Math.min((i + 1) * maxLinesPerPage, lineCount);
			List<String> description = descriptionLines.subList(startLine, endLine);
			IngredientInfoRecipe<T> recipe = new IngredientInfoRecipe<T>(guiHelper, ingredients, ingredientClass, description);
			recipes.add(recipe);
		}

		return recipes;
	}

	private static List<String> translateDescriptionLines(String... descriptionKeys) {
		List<String> descriptionLines = new ArrayList<String>();
		for (String descriptionKey : descriptionKeys) {
			String translatedLine = Translator.translateToLocal(descriptionKey);
			descriptionLines.add(translatedLine);
		}
		return descriptionLines;
	}

	private static List<String> expandNewlines(List<String> descriptionLines) {
		List<String> descriptionLinesExpanded = new ArrayList<String>();
		for (String descriptionLine : descriptionLines) {
			String[] descriptionLineExpanded = descriptionLine.split("\\\\n");
			Collections.addAll(descriptionLinesExpanded, descriptionLineExpanded);
		}
		return descriptionLinesExpanded;
	}

	private static List<String> wrapDescriptionLines(List<String> descriptionLines) {
		Minecraft minecraft = Minecraft.getMinecraft();
		List<String> descriptionLinesWrapped = new ArrayList<String>();
		for (String descriptionLine : descriptionLines) {
			List<String> textLines = minecraft.fontRenderer.listFormattedStringToWidth(descriptionLine, IngredientInfoRecipeCategory.recipeWidth);
			descriptionLinesWrapped.addAll(textLines);
		}
		return descriptionLinesWrapped;
	}

	private IngredientInfoRecipe(IGuiHelper guiHelper, List<T> ingredients, Class<? extends T> ingredientClass, List<String> description) {
		this.description = description;
		this.ingredients = ingredients;
		this.ingredientClass = ingredientClass;
		this.slotDrawable = guiHelper.getSlotDrawable();
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputLists(this.ingredientClass, Collections.singletonList(this.ingredients));
		ingredients.setOutputs(this.ingredientClass, this.ingredients);
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		int xPos = 0;
		int yPos = slotDrawable.getHeight() + 4;

		for (String descriptionLine : description) {
			minecraft.fontRenderer.drawString(descriptionLine, xPos, yPos, Color.black.getRGB());
			yPos += minecraft.fontRenderer.FONT_HEIGHT + lineSpacing;
		}
	}

	public List<String> getDescription() {
		return description;
	}
}
