package mezz.jei.gui.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Rectangle2d;

import mezz.jei.Internal;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.config.Constants;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.TooltipRenderer;

/**
 * A gui button that has an {@link IDrawable} instead of a string label.
 */
public class GuiIconButton extends GuiButton {
	private final Consumer<List<String>> tooltipCallback;
	private final Supplier<IDrawable> iconSupplier;

	public GuiIconButton(int buttonId, IDrawable icon) {
		this(buttonId, (tooltip) -> {
		}, () -> icon);
	}

	public GuiIconButton(int buttonId, Consumer<List<String>> tooltipCallback, Supplier<IDrawable> iconSupplier) {
		super(buttonId, 0, 0, 0, 0, "");
		this.tooltipCallback = tooltipCallback;
		this.iconSupplier = iconSupplier;
	}

	public void updateBounds(Rectangle2d area) {
		this.x = area.getX();
		this.y = area.getY();
		this.width = area.getWidth();
		this.height = area.getHeight();
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			int i = this.getHoverState(this.hovered);
			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			GuiHelper guiHelper = Internal.getHelpers().getGuiHelper();
			Minecraft minecraft = Minecraft.getInstance();
			DrawableNineSliceTexture texture = guiHelper.getButtonForState(i);
			texture.draw(this.x, this.y, this.width, this.height);
			this.renderBg(minecraft, mouseX, mouseY);

			int color = 14737632;
			if (packedFGColor != 0) {
				color = packedFGColor;
			} else if (!this.enabled) {
				color = 10526880;
			} else if (this.hovered) {
				color = 16777120;
			}
			if ((color & -67108864) == 0) {
				color |= -16777216;
			}

			float red = (float) (color >> 16 & 255) / 255.0F;
			float blue = (float) (color >> 8 & 255) / 255.0F;
			float green = (float) (color & 255) / 255.0F;
			float alpha = (float) (color >> 24 & 255) / 255.0F;
			GlStateManager.color4f(red, blue, green, alpha);

			IDrawable icon = iconSupplier.get();
			double xOffset = x + (width - icon.getWidth()) / 2.0;
			double yOffset = y + (height - icon.getHeight()) / 2.0;
			GlStateManager.pushMatrix();
			GlStateManager.translated(xOffset, yOffset, 0);
			icon.draw();
			GlStateManager.popMatrix();
		}
	}

	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		if (isMouseOver()) {
			List<String> tooltip = new ArrayList<>();
			this.tooltipCallback.accept(tooltip);
			TooltipRenderer.drawHoveringText(tooltip, mouseX, mouseY, Constants.MAX_TOOLTIP_WIDTH);
		}
	}
}
