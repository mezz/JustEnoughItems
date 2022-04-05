package mezz.jei.common.gui.recipes;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.common.gui.HoverChecker;
import mezz.jei.common.gui.elements.HighResolutionDrawable;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShapelessIcon {
	private final HighResolutionDrawable icon;
	private final HoverChecker hoverChecker;
	private int posX;
	private int posY;

	public ShapelessIcon(Textures textures) {
		this.icon = textures.getShapelessIcon();
		this.hoverChecker = new HoverChecker();
		setPosition(0, 0);
	}

	public IDrawable getIcon() {
		return icon;
	}

	public void setPosition(int posX, int posY) {
		this.posX = posX;
		this.posY = posY;

		ImmutableRect2i area = new ImmutableRect2i(posX, posY, icon.getWidth(), icon.getHeight());
		this.hoverChecker.updateBounds(area);
	}

	public void draw(PoseStack poseStack) {
		this.icon.draw(poseStack, this.posX, this.posY);
	}

	@Nullable
	public List<Component> getTooltipStrings(int mouseX, int mouseY) {
		if (hoverChecker.checkHover(mouseX, mouseY)) {
			return List.of(new TranslatableComponent("jei.tooltip.shapeless.recipe"));
		}
		return null;
	}
}
