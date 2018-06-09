package mezz.jei.gui.elements;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.config.Constants;
import mezz.jei.gui.TooltipRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A gui button that has an {@link IDrawable} instead of a string label.
 */
public class GuiIconButton extends GuiButton {
	private final Consumer<List<String>> tooltipCallback;
	private final Supplier<IDrawable> iconSupplier;
	private final IMouseClickedButtonCallback mouseClickCallback;

	public GuiIconButton(int buttonId, IDrawable icon, IMouseClickedButtonCallback mouseClickCallback) {
		this(buttonId, (tooltip) -> {}, () -> icon, mouseClickCallback);
	}

	public GuiIconButton(int buttonId, Consumer<List<String>> tooltipCallback, Supplier<IDrawable> iconSupplier, IMouseClickedButtonCallback mouseClickCallback) {
		super(buttonId, 0, 0, 0, 0, "");
		this.tooltipCallback = tooltipCallback;
		this.iconSupplier = iconSupplier;
		this.mouseClickCallback = mouseClickCallback;
	}

	public void updateBounds(Rectangle area) {
		this.x = area.x;
		this.y = area.y;
		this.width = area.width;
		this.height = area.height;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			int i = this.getHoverState(this.hovered);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			int firstHalfWidth = this.width / 2;
			int secondHalfWidth = (int) Math.ceil(this.width / 2.0f);
			this.drawTexturedModalRect(this.x, this.y, 0, 46 + i * 20, firstHalfWidth, this.height);
			this.drawTexturedModalRect(this.x + firstHalfWidth, this.y, 200 - secondHalfWidth, 46 + i * 20, secondHalfWidth, this.height);
			this.mouseDragged(mc, mouseX, mouseY);

			IDrawable icon = iconSupplier.get();
			int xOffset = x + (width - icon.getWidth()) / 2;
			int yOffset = y + (height - icon.getHeight()) / 2;
			GlStateManager.pushMatrix();
			if (width % 2 == 1) {
				GlStateManager.translate(0.5, 0, 0);
			}
			if (height % 2 == 1) {
				GlStateManager.translate(0, 0.5, 0);
			}
			icon.draw(mc, xOffset, yOffset);
			GlStateManager.popMatrix();
		}
	}

	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		if (isMouseOver()) {
			List<String> tooltip = new ArrayList<>();
			this.tooltipCallback.accept(tooltip);
			TooltipRenderer.drawHoveringText(minecraft, tooltip, mouseX, mouseY, Constants.MAX_TOOLTIP_WIDTH);
		}
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (super.mousePressed(mc, mouseX, mouseY)) {
			if (mouseClickCallback.mousePressed(mc, mouseX, mouseY)) {
				playPressSound(mc.getSoundHandler());
				return true;
			}
		}
		return false;
	}
}
