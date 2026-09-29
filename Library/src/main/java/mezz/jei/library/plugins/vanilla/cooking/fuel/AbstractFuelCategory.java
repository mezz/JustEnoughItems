package mezz.jei.library.plugins.vanilla.cooking.fuel;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.vanilla.IJeiFuelingRecipe;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.common.gui.textures.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.text.NumberFormat;

public abstract class AbstractFuelCategory extends AbstractRecipeCategory<IJeiFuelingRecipe> {
	protected AbstractFuelCategory(
		Textures textures,
		IRecipeType<IJeiFuelingRecipe> recipeType,
		Component title,
		IDrawable icon
	) {
		super(
			recipeType,
			title,
			new IconWithFlameOverlay(textures, icon),
			getMaxWidth(),
			34
		);
	}

	private static int getMaxWidth() {
		// width of the recipe depends on the text, which is different in each language
		Minecraft minecraft = Minecraft.getInstance();
		Font fontRenderer = minecraft.font;
		Component maxSmeltCountText = createSmeltCountText(10000000);
		int maxStringWidth = fontRenderer.width(maxSmeltCountText.getString());
		int textPadding = 20;
		return 18 + textPadding + maxStringWidth;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IJeiFuelingRecipe recipe, IFocusGroup focuses) {
		builder.addInputSlot(1, 17)
			.setStandardSlotBackground()
			.addItemStacks(recipe.getInputs());
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, IJeiFuelingRecipe recipe, IFocusGroup focuses) {
		builder.addAnimatedRecipeFlameWidget(recipe.getBurnTime())
			.setPosition(1, 0);

		Component smeltCountText = createSmeltCountText(recipe.getSmeltCount());
		builder.addText(smeltCountText, getWidth() - 20, getHeight())
			.setPosition(20, 0)
			.setTextAlignment(HorizontalAlignment.CENTER)
			.setTextAlignment(VerticalAlignment.CENTER)
			.setColor(JeiGuiColors.getColor(GuiColor.RECIPE_FUEL_SMELT_COUNT_TEXT));
	}

	public static Component createSmeltCountText(float smeltCount) {
		if (smeltCount == 1) {
			return Component.translatable("gui.jei.category.fuel.smeltCount.single");
		} else {
			NumberFormat numberInstance = NumberFormat.getNumberInstance();
			numberInstance.setMaximumFractionDigits(2);
			String formattedSmeltCount = numberInstance.format(smeltCount);
			return Component.translatable("gui.jei.category.fuel.smeltCount", formattedSmeltCount);
		}
	}

	@Override
	public @Nullable Identifier getIdentifier(IJeiFuelingRecipe recipe) {
		return null;
	}

	private static class IconWithFlameOverlay implements IDrawable {
		private final IDrawable icon;
		private final IDrawable flameIcon;

		public IconWithFlameOverlay(Textures textures, IDrawable icon) {
			this.icon = icon;
			this.flameIcon = textures.getFlameIcon();
		}

		@Override
		public int getWidth() {
			return 16;
		}

		@Override
		public int getHeight() {
			return 16;
		}

		@Override
		public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
			icon.draw(guiGraphics, xOffset, yOffset);

			var poseStack = guiGraphics.pose();
			poseStack.pushMatrix();
			{
				poseStack.translate(8 + xOffset, 8 + yOffset);
				poseStack.scale(0.5f, 0.5f);
				flameIcon.draw(guiGraphics);
			}
			poseStack.popMatrix();
		}
	}
}
