package mezz.jei.plugins.vanilla.cooking.fuel;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;

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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class FurnaceFuelCategory extends FurnaceVariantCategory<FuelRecipe> {
	private final IDrawableStatic background;
	private final IDrawableStatic flameTransparentBackground;
	private final Component localizedName;

	public FurnaceFuelCategory(IGuiHelper guiHelper, Textures textures) {
		super(guiHelper);

		// width of the recipe depends on the text, which is different in each language
		Minecraft minecraft = Minecraft.getInstance();
		Font fontRenderer = minecraft.font;
		Component smeltCountText = FuelRecipe.createSmeltCountText(100000);
		int stringWidth = fontRenderer.width(smeltCountText.getString());

		background = guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 0, 134, 18, 34)
			.addPadding(0, 0, 0, stringWidth + 20)
			.build();

		flameTransparentBackground = textures.getFlameIcon();
		localizedName = new TranslatableComponent("gui.jei.category.fuel");
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
	public Component getTitle() {
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
	public void draw(FuelRecipe recipe, PoseStack poseStack, double mouseX, double mouseY) {
		IDrawableAnimated flame = recipe.getFlame();
		flame.draw(poseStack, 1, 0);
		Minecraft minecraft = Minecraft.getInstance();
		Component smeltCountText = recipe.getSmeltCountText();
		minecraft.font.draw(poseStack, smeltCountText, 24, 13, 0xFF808080);
	}
}
