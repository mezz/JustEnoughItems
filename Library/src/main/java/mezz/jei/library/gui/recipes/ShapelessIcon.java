package mezz.jei.library.gui.recipes;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.common.gui.elements.HighResolutionDrawable;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.network.chat.Component;


import java.util.List;

public class ShapelessIcon {
	private final HighResolutionDrawable icon;
	private ImmutableRect2i area;

	public ShapelessIcon(Textures textures) {
		this.icon = textures.getShapelessIcon();
		this.area = ImmutableRect2i.EMPTY;
		setPosition(0, 0);
	}

	public IDrawable getIcon() {
		return icon;
	}

	public void setPosition(int posX, int posY) {
		this.area = new ImmutableRect2i(posX, posY, icon.getWidth(), icon.getHeight());
	}

	public void draw(PoseStack poseStack) {
		this.icon.draw(poseStack, area.getX(), area.getY());
	}

	public List<Component> getTooltipStrings(int mouseX, int mouseY) {
		if (this.area.contains(mouseX, mouseY)) {
			return List.of(Component.translatable("jei.tooltip.shapeless.recipe"));
		}
		return List.of();
	}
}
