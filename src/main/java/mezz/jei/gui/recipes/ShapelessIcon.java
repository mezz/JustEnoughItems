package mezz.jei.gui.recipes;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import mezz.jei.Internal;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.config.HoverChecker;

public class ShapelessIcon {
	private static final IDrawable icon = Internal.getHelpers().getGuiHelper().getShapelessIcon();
	private final HoverChecker hoverChecker;

	public ShapelessIcon() {
		int iconBottom = icon.getHeight() / 2;
		int iconLeft = CraftingRecipeCategory.width - getWidth();
		int iconRight = iconLeft + getWidth();
		this.hoverChecker = new HoverChecker(0, iconBottom, iconLeft, iconRight, 0);
	}

	public static int getWidth() {
		return icon.getWidth() / 2;
	}

	public static int getHeight() {
		return icon.getHeight() / 2;
	}

	public void draw(Minecraft minecraft, int recipeWidth) {
		int shapelessIconX = recipeWidth - getWidth();

		GlStateManager.pushMatrix();
		GlStateManager.scale(0.5, 0.5, 1.0);
		GlStateManager.color(1f, 1f, 1f, 1f);
		icon.draw(minecraft, shapelessIconX * 2, 0);
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
