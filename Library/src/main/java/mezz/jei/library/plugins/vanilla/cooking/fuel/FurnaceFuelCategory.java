package mezz.jei.library.plugins.vanilla.cooking.fuel;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.vanilla.IJeiFuelingRecipe;
import mezz.jei.common.Constants;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.library.plugins.vanilla.cooking.FurnaceVariantCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;

public class FurnaceFuelCategory extends FurnaceVariantCategory<IJeiFuelingRecipe> {
	private final IDrawableStatic background;
	private final IDrawableStatic flameTransparentBackground;
	private final Component localizedName;
	private final ImmutableRect2i textArea;
	private final IGuiHelper guiHelper;

	public FurnaceFuelCategory(IGuiHelper guiHelper, Textures textures) {
		super(guiHelper);
		this.guiHelper = guiHelper;

		// width of the recipe depends on the text, which is different in each language
		Minecraft minecraft = Minecraft.getInstance();
		Font fontRenderer = minecraft.font;
		Component maxSmeltCountText = RecipeWidget.createSmeltCountText(10000000 * 200);
		int maxStringWidth = fontRenderer.width(maxSmeltCountText.getString());
		int backgroundHeight = 34;
		int textPadding = 20;

		background = guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 0, 134, 18, backgroundHeight)
			.addPadding(0, 0, 0, textPadding + maxStringWidth)
			.build();

		textArea = new ImmutableRect2i(20, 0, textPadding + maxStringWidth, backgroundHeight);

		flameTransparentBackground = textures.getFlameIcon();
		localizedName = Component.translatable("gui.jei.category.fuel");
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public RecipeType<IJeiFuelingRecipe> getRecipeType() {
		return RecipeTypes.FUELING;
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
	public void setRecipe(IRecipeLayoutBuilder builder, IJeiFuelingRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 1, 17)
			.addItemStacks(recipe.getInputs());
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder acceptor, IJeiFuelingRecipe recipe, IFocusGroup focuses) {
		acceptor.addWidget(new RecipeWidget(guiHelper, recipe.getBurnTime(), textArea));
	}

	@Override
	public @Nullable ResourceLocation getRegistryName(IJeiFuelingRecipe recipe) {
		return null;
	}

	private static class RecipeWidget implements IRecipeWidget {
		private final IDrawableAnimated flame;
		private final Component smeltCountText;
		private final ImmutableRect2i textArea;
		private final ScreenPosition screenPosition;

		public RecipeWidget(IGuiHelper guiHelper, int burnTime, ImmutableRect2i textArea) {
			this.flame = guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 82, 114, 14, 14)
				.buildAnimated(burnTime, IDrawableAnimated.StartDirection.TOP, true);
			this.smeltCountText = createSmeltCountText(burnTime);
			this.textArea = textArea;
			this.screenPosition = new ScreenPosition(0, 0);
		}

		@Override
		public ScreenPosition getPosition() {
			return screenPosition;
		}

		@Override
		public void draw(GuiGraphics guiGraphics, double mouseX, double mouseY) {
			flame.draw(guiGraphics, 1, 0);

			Minecraft minecraft = Minecraft.getInstance();
			Font font = minecraft.font;
			ImmutableRect2i centerArea = MathUtil.centerTextArea(this.textArea, font, smeltCountText);
			guiGraphics.drawString(font, smeltCountText, centerArea.getX(), centerArea.getY(), 0xFF808080, false);
		}

		public static Component createSmeltCountText(int burnTime) {
			if (burnTime == 200) {
				return Component.translatable("gui.jei.category.fuel.smeltCount.single");
			} else {
				NumberFormat numberInstance = NumberFormat.getNumberInstance();
				numberInstance.setMaximumFractionDigits(2);
				String smeltCount = numberInstance.format(burnTime / 200f);
				return Component.translatable("gui.jei.category.fuel.smeltCount", smeltCount);
			}
		}
	}
}
