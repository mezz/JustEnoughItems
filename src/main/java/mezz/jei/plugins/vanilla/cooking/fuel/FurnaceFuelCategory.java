package mezz.jei.plugins.vanilla.cooking.fuel;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.config.Constants;
import mezz.jei.gui.textures.Textures;
import mezz.jei.plugins.vanilla.cooking.FurnaceVariantCategory;
import mezz.jei.util.Translator;

public class FurnaceFuelCategory extends FurnaceVariantCategory<FuelRecipe> {
	private final IDrawableStatic background;
	private final IDrawableStatic flameTransparentBackground;
	private final String localizedName;

	public FurnaceFuelCategory(IGuiHelper guiHelper, Textures textures) {
		super(guiHelper);
		background = guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 0, 134, 18, 34)
			.addPadding(0, 0, 0, 88)
			.build();

		flameTransparentBackground = textures.getFlameIcon();
		localizedName = Translator.translateToLocal("gui.jei.category.fuel");
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public ResourceLocation getUid() {
		return VanillaRecipeCategoryUid.FUEL;
	}

	@Override
	public Class<? extends FuelRecipe> getRecipeClass() {
		return FuelRecipe.class;
	}

	@Override
	public String getTitle() {
		return localizedName;
	}

	@Override
	public IDrawable getIcon() {
		return flameTransparentBackground;
	}

	@Override
	public void setIngredients(FuelRecipe recipe, IIngredients ingredients) {
		ingredients.setInputs(VanillaTypes.ITEM, recipe.getInputs());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, FuelRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(fuelSlot, true, 0, 16);
		guiItemStacks.set(ingredients);
	}

	@Override
	public void draw(FuelRecipe recipe, double mouseX, double mouseY) {
		IDrawableAnimated flame = recipe.getFlame();
		flame.draw(1, 0);
		Minecraft minecraft = Minecraft.getInstance();
		String smeltCountString = recipe.getSmeltCountString();
		minecraft.fontRenderer.drawString(smeltCountString, 24, 13, 0xFF808080);
	}
}
