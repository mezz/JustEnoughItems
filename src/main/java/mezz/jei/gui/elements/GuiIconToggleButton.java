package mezz.jei.gui.elements;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.input.IMouseHandler;
import mezz.jei.input.click.MouseClickState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.gui.HoverChecker;
import mezz.jei.gui.TooltipRenderer;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public abstract class GuiIconToggleButton {
	private final IDrawable offIcon;
	private final IDrawable onIcon;
	private final GuiIconButton button;
	private final HoverChecker hoverChecker;
	private final IMouseHandler mouseHandler;

	public GuiIconToggleButton(IDrawable offIcon, IDrawable onIcon) {
		this.offIcon = offIcon;
		this.onIcon = onIcon;
		this.button = new GuiIconButton(new DrawableBlank(0, 0), b -> {
		});
		this.hoverChecker = new HoverChecker();
		this.hoverChecker.updateBounds(this.button);
		this.mouseHandler = new MouseHandler();
	}

	public void updateBounds(Rect2i area) {
		this.button.setWidth(area.getWidth());
		this.button.setHeight(area.getHeight());
		this.button.x = area.getX();
		this.button.y = area.getY();
		this.hoverChecker.updateBounds(this.button);
	}

	public void draw(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		this.button.render(poseStack, mouseX, mouseY, partialTicks);
		IDrawable icon = isIconToggledOn() ? this.onIcon : this.offIcon;
		icon.draw(poseStack, this.button.x + 2, this.button.y + 2);
	}

	public final boolean isMouseOver(double mouseX, double mouseY) {
		return this.hoverChecker.checkHover(mouseX, mouseY);
	}

	public IMouseHandler getMouseHandler() {
		return this.mouseHandler;
	}

	public final void drawTooltips(PoseStack poseStack, int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			List<Component> tooltip = new ArrayList<>();
			getTooltips(tooltip);
			TooltipRenderer.drawHoveringText(poseStack, tooltip, mouseX, mouseY);
		}
	}

	protected abstract void getTooltips(List<Component> tooltip);

	protected abstract boolean isIconToggledOn();

	protected abstract boolean onMouseClicked(MouseClickState clickState);

	private class MouseHandler implements IMouseHandler {
		@Override
		@Nullable
		public final IMouseHandler handleClick(Screen screen, double mouseX, double mouseY, int mouseButton, MouseClickState clickState) {
			if (isMouseOver(mouseX, mouseY)) {
				IMouseHandler mouseHandler = button.getMouseHandler();
				IMouseHandler handled = mouseHandler.handleClick(screen, mouseX, mouseY, mouseButton, clickState);
				if (handled != null) {
					if (onMouseClicked(clickState)) {
						return this;
					}
				}
			}
			return null;
		}
	}
}
