package mezz.jei.gui.recipes;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;

import mezz.jei.Internal;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.gui.HoverChecker;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

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

	public void draw(PoseStack poseStack, int recipeWidth) {
		int shapelessIconX = recipeWidth - (icon.getWidth() / scale);

		poseStack.pushPose();
		poseStack.translate(shapelessIconX, 0, 0);
		poseStack.scale(1F / scale, 1F / scale, 1);
		icon.draw(poseStack);
		poseStack.popPose();
	}

	@Nullable
	public List<Component> getTooltipStrings(int mouseX, int mouseY) {
		if (hoverChecker.checkHover(mouseX, mouseY)) {
			return Collections.singletonList(new TranslatableComponent("jei.tooltip.shapeless.recipe"));
		}
		return null;
	}
}
