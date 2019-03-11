package mezz.jei.gui.recipes;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import net.minecraftforge.fml.client.config.HoverChecker;
import net.minecraft.client.renderer.GlStateManager;

import mezz.jei.Internal;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.util.Translator;

public class ShapelessIcon {
	private static final int scale = 4;
	private final IDrawable icon;
	private final HoverChecker hoverChecker;

	public ShapelessIcon() {
		this.icon = Internal.getTextures().getShapelessIcon();
		int iconBottom = icon.getHeight() / scale;
		int iconLeft = CraftingRecipeCategory.width - (icon.getWidth() / scale);
		int iconRight = iconLeft + icon.getWidth() / scale;
		this.hoverChecker = new HoverChecker(0, iconBottom, iconLeft, iconRight, 0);
	}

	public void draw(int recipeWidth) {
		int shapelessIconX = recipeWidth - (icon.getWidth() / scale);

		GlStateManager.pushMatrix();
		GlStateManager.translatef(shapelessIconX, 0, 0);
		GlStateManager.scaled(1.0 / scale, 1.0 / scale, 1.0);
		icon.draw();
		GlStateManager.popMatrix();
	}

	@Nullable
	public List<String> getTooltipStrings(int mouseX, int mouseY) {
		if (hoverChecker.checkHover(mouseX, mouseY)) {
			return Collections.singletonList(Translator.translateToLocal("jei.tooltip.shapeless.recipe"));
		}
		return null;
	}
}
