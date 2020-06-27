package mezz.jei.gui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.Rectangle2d;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.config.Constants;
import mezz.jei.gui.HoverChecker;
import mezz.jei.gui.TooltipRenderer;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiIconToggleButton {
	private final IDrawable offIcon;
	private final IDrawable onIcon;
	private final GuiIconButton button;
	private final HoverChecker hoverChecker;

	public GuiIconToggleButton(IDrawable offIcon, IDrawable onIcon) {
		this.offIcon = offIcon;
		this.onIcon = onIcon;
		this.button = new GuiIconButton(new DrawableBlank(0, 0), b -> {});
		this.hoverChecker = new HoverChecker();
		this.hoverChecker.updateBounds(this.button);
	}

	public void updateBounds(Rectangle2d area) {
		this.button.func_230991_b_(area.getWidth());
		this.button.setHeight(area.getHeight());
		this.button.field_230690_l_ = area.getX();
		this.button.field_230691_m_ = area.getY();
		this.hoverChecker.updateBounds(this.button);
	}

	public void draw(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.button.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
		IDrawable icon = isIconToggledOn() ? this.onIcon : this.offIcon;
		icon.draw(matrixStack, this.button.field_230690_l_ + 2, this.button.field_230691_m_ + 2);
	}

	public final boolean isMouseOver(double mouseX, double mouseY) {
		return this.hoverChecker.checkHover(mouseX, mouseY);
	}

	public final boolean handleMouseClick(double mouseX, double mouseY, int mouseButton) {
		return button.func_231044_a_(mouseX, mouseY, mouseButton) &&
			onMouseClicked(mouseX, mouseY, mouseButton);
	}

	public final void drawTooltips(MatrixStack matrixStack, int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			List<ITextComponent> tooltip = new ArrayList<>();
			getTooltips(tooltip);
			TooltipRenderer.drawHoveringText(tooltip, mouseX, mouseY, Constants.MAX_TOOLTIP_WIDTH, matrixStack);
		}
	}

	protected abstract void getTooltips(List<ITextComponent> tooltip);

	protected abstract boolean isIconToggledOn();

	protected abstract boolean onMouseClicked(double mouseX, double mouseY, int mouseButton);
}
