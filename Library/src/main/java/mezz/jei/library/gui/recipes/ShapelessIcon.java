package mezz.jei.library.gui.recipes;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.common.Internal;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ShapelessIcon {
	private ImmutableRect2i area = ImmutableRect2i.EMPTY;

	public IDrawable getIcon() {
		return Internal.getTextures().getShapelessIcon();
	}

	public void setPosition(int posX, int posY) {
		IDrawable icon = getIcon();
		this.area = new ImmutableRect2i(posX, posY, icon.getWidth(), icon.getHeight());
	}

	public void draw(GuiGraphics guiGraphics) {
		IDrawable icon = this.getIcon();
		icon.draw(guiGraphics, area.getX(), area.getY());
	}

	public List<Component> getTooltipStrings(int mouseX, int mouseY) {
		if (this.area.contains(mouseX, mouseY)) {
			return List.of(Component.translatable("jei.tooltip.shapeless.recipe"));
		}
		return List.of();
	}
}
