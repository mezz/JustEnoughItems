package mezz.jei.plugins.jei.description;

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
import net.minecraft.item.ItemStack;

public class ItemDescriptionRecipe extends BlankRecipeWrapper {
	private static final int lineSpacing = 2;
	private final List<String> description;
	private final List<ItemStack> itemStacks;
	private final IDrawable slotDrawable;

	public static List<ItemDescriptionRecipe> create(IGuiHelper guiHelper, List<ItemStack> itemStacks, String... descriptionKeys) {
		List<ItemDescriptionRecipe> recipes = new ArrayList<ItemDescriptionRecipe>();

		List<String> descriptionLines = translateDescriptionLines(descriptionKeys);
		descriptionLines = expandNewlines(descriptionLines);
		descriptionLines = wrapDescriptionLines(descriptionLines);
		final int lineCount = descriptionLines.size();

		Minecraft minecraft = Minecraft.getMinecraft();
		final int maxLinesPerPage = (ItemDescriptionRecipeCategory.recipeHeight - 20) / (minecraft.fontRendererObj.FONT_HEIGHT + lineSpacing);
		final int pageCount = MathUtil.divideCeil(lineCount, maxLinesPerPage);
		for (int i = 0; i < pageCount; i++) {
			int startLine = i * maxLinesPerPage;
			int endLine = Math.min((i + 1) * maxLinesPerPage, lineCount);
			List<String> description = descriptionLines.subList(startLine, endLine);
			ItemDescriptionRecipe recipe = new ItemDescriptionRecipe(guiHelper, itemStacks, description);
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
			List<String> textLines = minecraft.fontRendererObj.listFormattedStringToWidth(descriptionLine, ItemDescriptionRecipeCategory.recipeWidth);
			descriptionLinesWrapped.addAll(textLines);
		}
		return descriptionLinesWrapped;
	}

	private ItemDescriptionRecipe(IGuiHelper guiHelper, List<ItemStack> itemStacks, List<String> description) {
		this.description = description;
		this.itemStacks = itemStacks;
		this.slotDrawable = guiHelper.getSlotDrawable();
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputLists(ItemStack.class, Collections.singletonList(itemStacks));
		ingredients.setOutputs(ItemStack.class, itemStacks);
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		int xPos = (recipeWidth - slotDrawable.getWidth()) / 2;
		int yPos = 0;
		slotDrawable.draw(minecraft, xPos, yPos);
		xPos = 0;
		yPos += slotDrawable.getHeight() + 4;

		for (String descriptionLine : description) {
			minecraft.fontRendererObj.drawString(descriptionLine, xPos, yPos, Color.black.getRGB());
			yPos += minecraft.fontRendererObj.FONT_HEIGHT + lineSpacing;
		}
	}

	public List<String> getDescription() {
		return description;
	}
}
