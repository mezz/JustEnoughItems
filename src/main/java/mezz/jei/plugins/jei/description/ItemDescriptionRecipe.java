package mezz.jei.plugins.jei.description;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import mezz.jei.Internal;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.util.MathUtil;
import mezz.jei.util.Translator;

public class ItemDescriptionRecipe extends BlankRecipeWrapper {
	private static final int lineSpacing = 2;
	@Nonnull
	private final List<String> description;
	@Nonnull
	private final List<List<ItemStack>> outputs;
	@Nonnull
	private final IDrawable slotDrawable;

	public static List<ItemDescriptionRecipe> create(@Nonnull List<ItemStack> itemStacks, String... descriptionKeys) {
		List<ItemDescriptionRecipe> recipes = new ArrayList<>();

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
			ItemDescriptionRecipe recipe = new ItemDescriptionRecipe(itemStacks, description);
			recipes.add(recipe);
		}

		return recipes;
	}

	@Nonnull
	private static List<String> translateDescriptionLines(String... descriptionKeys) {
		List<String> descriptionLines = new ArrayList<>();
		for (String descriptionKey : descriptionKeys) {
			String translatedLine = Translator.translateToLocal(descriptionKey);
			descriptionLines.add(translatedLine);
		}
		return descriptionLines;
	}

	@Nonnull
	private static List<String> expandNewlines(@Nonnull List<String> descriptionLines) {
		List<String> descriptionLinesExpanded = new ArrayList<>();
		for (String descriptionLine : descriptionLines) {
			String[] descriptionLineExpanded = descriptionLine.split("\\\\n");
			Collections.addAll(descriptionLinesExpanded, descriptionLineExpanded);
		}
		return descriptionLinesExpanded;
	}

	@Nonnull
	private static List<String> wrapDescriptionLines(@Nonnull List<String> descriptionLines) {
		Minecraft minecraft = Minecraft.getMinecraft();
		List<String> descriptionLinesWrapped = new ArrayList<>();
		for (String descriptionLine : descriptionLines) {
			List<String> textLines = minecraft.fontRendererObj.listFormattedStringToWidth(descriptionLine, ItemDescriptionRecipeCategory.recipeWidth);
			descriptionLinesWrapped.addAll(textLines);
		}
		return descriptionLinesWrapped;
	}

	private ItemDescriptionRecipe(@Nonnull List<ItemStack> itemStacks, @Nonnull List<String> description) {
		this.description = description;
		this.outputs = Collections.singletonList(itemStacks);
		this.slotDrawable = Internal.getHelpers().getGuiHelper().getSlotDrawable();
	}

	@Nonnull
	@Override
	public List<List<ItemStack>> getOutputs() {
		return outputs;
	}

	@Override
	public List<FluidStack> getFluidInputs() {
		return super.getFluidInputs();
	}

	@Override
	public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
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

	@Nonnull
	public List<String> getDescription() {
		return description;
	}
}
