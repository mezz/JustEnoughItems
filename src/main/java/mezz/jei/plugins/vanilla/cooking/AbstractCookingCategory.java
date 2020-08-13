package mezz.jei.plugins.vanilla.cooking;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.AbstractCookingRecipe;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.config.Constants;
import mezz.jei.util.Translator;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class AbstractCookingCategory<T extends AbstractCookingRecipe> extends FurnaceVariantCategory<T> {
	private final IDrawable background;
	private final IDrawable icon;
	private final String localizedName;
	protected final IDrawableAnimated arrow;

	public AbstractCookingCategory(IGuiHelper guiHelper, Block icon, String translationKey, int regularCookTime) {
		super(guiHelper);
		background = guiHelper.createDrawable(Constants.RECIPE_GUI_VANILLA, 0, 114, 82, 54);
		this.icon = guiHelper.createDrawableIngredient(new ItemStack(icon));
		localizedName = Translator.translateToLocal(translationKey);
		arrow = guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 82, 128, 24, 17)
			.buildAnimated(regularCookTime, IDrawableAnimated.StartDirection.LEFT, false);
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setIngredients(T recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
	}

	@Override
	public void draw(T recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		animatedFlame.draw(matrixStack, 1, 20);
		arrow.draw(matrixStack, 24, 18);

		float experience = recipe.getExperience();
		if (experience > 0) {
			TranslationTextComponent experienceString = new TranslationTextComponent("gui.jei.category.smelting.experience", experience);
			Minecraft minecraft = Minecraft.getInstance();
			FontRenderer fontRenderer = minecraft.fontRenderer;
			int stringWidth = fontRenderer.func_238414_a_(experienceString);
			fontRenderer.func_243248_b(matrixStack, experienceString, background.getWidth() - stringWidth, 0, 0xFF808080);
		}
	}

	@Override
	public String getTitle() {
		return localizedName;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, T recipe, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(inputSlot, true, 0, 0);
		guiItemStacks.init(outputSlot, false, 60, 18);

		guiItemStacks.set(ingredients);
	}
}
