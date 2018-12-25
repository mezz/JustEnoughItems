package mezz.jei.gui.elements;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.config.Constants;
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

	public void updateBounds(Rectangle area) {
		this.x = area.x;
		this.y = area.y;
		this.width = area.width;
		this.height = area.height;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			Minecraft minecraft = Minecraft.getInstance();
			minecraft.getTextureManager().bindTexture(BUTTON_TEXTURES);
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			int i = this.getHoverState(this.hovered);
			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			int firstHalfWidth = this.width / 2;
			int secondHalfWidth = (int) Math.ceil(this.width / 2.0f);
			this.drawTexturedModalRect(this.x, this.y, 0, 46 + i * 20, firstHalfWidth, this.height);
			this.drawTexturedModalRect(this.x + firstHalfWidth, this.y, 200 - secondHalfWidth, 46 + i * 20, secondHalfWidth, this.height);
			this.renderBg(minecraft, mouseX, mouseY);

			IDrawable icon = iconSupplier.get();
			int xOffset = x + (width - icon.getWidth()) / 2;
			int yOffset = y + (height - icon.getHeight()) / 2;
			GlStateManager.pushMatrix();
			if (width % 2 == 1) {
				GlStateManager.translated(0.5, 0, 0);
			}
			if (height % 2 == 1) {
				GlStateManager.translated(0, 0.5, 0);
			}
			icon.draw(xOffset, yOffset);
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
