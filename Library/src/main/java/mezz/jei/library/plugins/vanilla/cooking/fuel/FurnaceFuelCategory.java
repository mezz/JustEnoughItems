package mezz.jei.library.plugins.vanilla.cooking.fuel;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.vanilla.IJeiFuelingRecipe;
import mezz.jei.common.gui.textures.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;

public class FurnaceFuelCategory extends AbstractRecipeCategory<IJeiFuelingRecipe> {
	public FurnaceFuelCategory(Textures textures) {
		super(
			RecipeTypes.FUELING,
			Component.translatable("gui.jei.category.fuel"),
			textures.getFlameIcon(),
			getMaxWidth(),
			34
		);
	}

	private static int getMaxWidth() {
		// width of the recipe depends on the text, which is different in each language
		Minecraft minecraft = Minecraft.getInstance();
		Font fontRenderer = minecraft.font;
		Component maxSmeltCountText = createSmeltCountText(10000000 * 200);
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
	public void createRecipeExtras(IRecipeExtrasBuilder builder, IJeiFuelingRecipe recipe, IRecipeSlotsView recipeSlotsView, IFocusGroup focuses) {
		int burnTime = recipe.getBurnTime();
		builder.addAnimatedRecipeFlame(burnTime, 1, 0);

		Component smeltCountText = createSmeltCountText(burnTime);
		builder.addText(smeltCountText, 20, 0, getWidth() - 20, getHeight())
			.alignHorizontalCenter()
			.alignVerticalCenter()
			.setColor(0xFF808080);
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

	@Override
	public @Nullable ResourceLocation getRegistryName(IJeiFuelingRecipe recipe) {
		return null;
	}
}
