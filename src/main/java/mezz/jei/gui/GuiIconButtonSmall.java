package mezz.jei.gui;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.config.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;

/**
 * A small gui button that has an {@link IDrawable} instead of a string label.
 */
public class GuiIconButtonSmall extends GuiButton {
	private static final ResourceLocation BUTTON_RESOURCE = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_RECIPE_BACKGROUND_PATH);
	private final IDrawable icon;

	public GuiIconButtonSmall(int buttonId, int x, int y, int widthIn, int heightIn, IDrawable icon) {
		super(buttonId, x, y, widthIn, heightIn, "");
		this.icon = icon;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY)
	{
		if (this.visible)
		{
			this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
			int k = this.getHoverState(this.hovered);
			GuiUtils.drawContinuousTexturedBox(BUTTON_RESOURCE, this.xPosition, this.yPosition, 0, 182 + k * 20, this.width, this.height, 95, 20, 2, 2, 2, 2, this.zLevel);
			this.mouseDragged(mc, mouseX, mouseY);

			int xOffset = xPosition + (height - this.icon.getWidth()) / 2;
			int yOffset = yPosition + (width - this.icon.getHeight()) / 2;
			this.icon.draw(mc, xOffset, yOffset);
		}
	}
}
