package mezz.jei.plugins.jei.info;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.textures.Textures;
import mezz.jei.util.Translator;

public class IngredientInfoRecipeCategory implements IRecipeCategory<IngredientInfoRecipe> {
	public static final int recipeWidth = 160;
	public static final int recipeHeight = 125;
	private static final int lineSpacing = 2;

	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawable slotBackground;
	private final String localizedName;

	public IngredientInfoRecipeCategory(IGuiHelper guiHelper, Textures textures) {
		background = guiHelper.createBlankDrawable(recipeWidth, recipeHeight);
		icon = textures.getInfoIcon();
		slotBackground = guiHelper.getSlotDrawable();
		localizedName = Translator.translateToLocal("gui.jei.category.itemInformation");
	}

	@Override
	public ResourceLocation getUid() {
		return VanillaRecipeCategoryUid.INFORMATION;
	}

	@Override
	public Class<? extends IngredientInfoRecipe> getRecipeClass() {
		return IngredientInfoRecipe.class;
	}

	@Override
	public String getTitle() {
		return localizedName;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void setIngredients(IngredientInfoRecipe recipe, IIngredients ingredients) {
		setIngredientsTyped((IngredientInfoRecipe<?>) recipe, ingredients);
	}

	private <T> void setIngredientsTyped(IngredientInfoRecipe<T> recipe, IIngredients ingredients) {
		IIngredientType<T> ingredientType = recipe.getIngredientType();
		List<List<T>> recipeIngredients = Collections.singletonList(recipe.getIngredients());
		ingredients.setInputLists(ingredientType, recipeIngredients);
		ingredients.setOutputLists(ingredientType, recipeIngredients);
	}

	@Override
	public void draw(IngredientInfoRecipe recipe, double mouseX, double mouseY) {
		drawTyped((IngredientInfoRecipe<?>) recipe);
	}

	private <T> void drawTyped(IngredientInfoRecipe<T> recipe) {
		int xPos = 0;
		int yPos = slotBackground.getHeight() + 4;

		Minecraft minecraft = Minecraft.getInstance();
		for (String descriptionLine : recipe.getDescription()) {
			minecraft.fontRenderer.drawString(descriptionLine, xPos, yPos, 0xFF000000);
			yPos += minecraft.fontRenderer.FONT_HEIGHT + lineSpacing;
		}
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IngredientInfoRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		int xPos = (recipeWidth - 18) / 2;
		guiItemStacks.init(0, true, xPos, 0);
		guiItemStacks.setBackground(0, slotBackground);
		guiItemStacks.set(ingredients);

		IGuiFluidStackGroup guiFluidStackGroup = recipeLayout.getFluidStacks();
		guiFluidStackGroup.init(0, true, xPos + 1, 1);
		guiFluidStackGroup.set(ingredients);
	}
}
