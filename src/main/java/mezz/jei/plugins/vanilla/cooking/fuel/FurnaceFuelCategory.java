package mezz.jei.plugins.vanilla.cooking.fuel;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.config.Constants;
import mezz.jei.gui.textures.Textures;
import mezz.jei.plugins.vanilla.cooking.FurnaceVariantCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

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
	public void setRecipe(IRecipeLayoutBuilder builder, FuelRecipe recipe, List<? extends IFocus<?>> focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 1, 17)
			.addItemStacks(recipe.getInputs());
	}

	@Override
	public void draw(FuelRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY) {
		IDrawableAnimated flame = recipe.getFlame();
		flame.draw(poseStack, 1, 0);
		Minecraft minecraft = Minecraft.getInstance();
		Component smeltCountText = recipe.getSmeltCountText();
		minecraft.font.draw(poseStack, smeltCountText, 24, 13, 0xFF808080);
	}
}
