package mezz.jei.library.plugins.vanilla.cooking.fuel;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.vanilla.IJeiFuelingRecipe;
import mezz.jei.common.Constants;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.library.plugins.vanilla.cooking.FurnaceVariantCategory;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.text.NumberFormat;

public class FurnaceFuelCategory extends FurnaceVariantCategory<IJeiFuelingRecipe> {
	private final IDrawableStatic background;
	private final IDrawableStatic flameTransparentBackground;
	private final Component localizedName;
	private final LoadingCache<Integer, IDrawableAnimated> cachedFlames;
	private final ImmutableRect2i textArea;

	public FurnaceFuelCategory(IGuiHelper guiHelper, Textures textures) {
		super(guiHelper);

		// width of the recipe depends on the text, which is different in each language
		Minecraft minecraft = Minecraft.getInstance();
		Font fontRenderer = minecraft.font;
		Component maxSmeltCountText = createSmeltCountText(10000000 * 200);
		int maxStringWidth = fontRenderer.width(maxSmeltCountText.getString());
		int backgroundHeight = 34;
		int textPadding = 20;

		background = guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 0, 134, 18, backgroundHeight)
			.addPadding(0, 0, 0, textPadding + maxStringWidth)
			.build();

		textArea = new ImmutableRect2i(20, 0, textPadding + maxStringWidth, backgroundHeight);

		flameTransparentBackground = textures.getFlameIcon();
		localizedName = Component.translatable("gui.jei.category.fuel");

		this.cachedFlames = CacheBuilder.newBuilder()
			.maximumSize(25)
			.build(new CacheLoader<>() {
				@Override
				public IDrawableAnimated load(Integer burnTime) {
					return guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 82, 114, 14, 14)
						.buildAnimated(burnTime, IDrawableAnimated.StartDirection.TOP, true);
				}
			});
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
	public void draw(IJeiFuelingRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY) {
		int burnTime = recipe.getBurnTime();
		IDrawableAnimated flame = cachedFlames.getUnchecked(burnTime);
		flame.draw(poseStack, 1, 0);
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		Component smeltCountText = createSmeltCountText(burnTime);
		ImmutableRect2i centerArea = MathUtil.centerTextArea(this.textArea, font, smeltCountText);
		font.draw(poseStack, smeltCountText, centerArea.getX(), centerArea.getY(), 0xFF808080);
	}

	private static Component createSmeltCountText(int burnTime) {
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
