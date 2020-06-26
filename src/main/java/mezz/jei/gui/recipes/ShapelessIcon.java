package mezz.jei.gui.recipes;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import mezz.jei.Internal;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.gui.HoverChecker;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ShapelessIcon {
	private static final int scale = 4;
	private final IDrawable icon;
	private final HoverChecker hoverChecker;

	public ShapelessIcon() {
		this.icon = Internal.getTextures().getShapelessIcon();
		int iconBottom = icon.getHeight() / scale;
		int iconLeft = CraftingRecipeCategory.width - (icon.getWidth() / scale);
		int iconRight = iconLeft + icon.getWidth() / scale;
		this.hoverChecker = new HoverChecker(0, iconBottom, iconLeft, iconRight);
	}

	public void draw(MatrixStack matrixStack, int recipeWidth) {
		int shapelessIconX = recipeWidth - (icon.getWidth() / scale);

		matrixStack.push();
		matrixStack.translate(shapelessIconX, 0, 0);
		matrixStack.scale(1F / scale, 1F / scale, 1);
		icon.draw(matrixStack);
		matrixStack.pop();
	}

	@Nullable
	public List<ITextComponent> getTooltipStrings(int mouseX, int mouseY) {
		if (hoverChecker.checkHover(mouseX, mouseY)) {
			return Collections.singletonList(new TranslationTextComponent("jei.tooltip.shapeless.recipe"));
		}
		return null;
	}
}
