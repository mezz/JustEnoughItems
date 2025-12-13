package mezz.jei.gui.elements;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;

public interface IIconButtonDescriptor {
	IDrawable getIcon();

	boolean onPress(IJeiUserInput input);

	default void getTooltips(ITooltipBuilder tooltip) {

	}

	default boolean isActive() {
		return true;
	}

	default boolean isVisible() {
		return true;
	}

	default boolean isForcePressed() {
		return false;
	}

	default void tick() {

	}

	default void drawExtras(GuiGraphics guiGraphics, Rect2i buttonArea, int mouseX, int mouseY, float partialTicks) {

	}
}
