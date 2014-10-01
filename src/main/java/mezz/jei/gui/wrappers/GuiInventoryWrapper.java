package mezz.jei.gui.wrappers;

import cpw.mods.fml.client.FMLClientHandler;
import mezz.jei.JustEnoughItems;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class GuiInventoryWrapper extends GuiInventory {

	public GuiInventoryWrapper(GuiInventory gui) {
		super(FMLClientHandler.instance().getClientPlayerEntity());
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_)
	{
		RenderHelper.enableGUIStandardItemLighting();

		final int xInitial = xSize + 4;
		int x = xInitial;
		int y = - guiTop + 32;
		for (ItemStack stack : JustEnoughItems.itemRegistry.itemList) {
			drawItemStack(stack, x, y);
			x += 18;
			if (x + 16 + guiLeft > width) {
				x = xInitial;
				y += 18;
			}

			if (y + 16 + guiTop > height)
				break;
		}

		RenderHelper.disableStandardItemLighting();

		super.drawGuiContainerForegroundLayer(p_146979_1_, p_146979_2_);
	}

	private void drawItemStack(ItemStack stack, int xPos, int yPos) {
		FontRenderer font = stack.getItem().getFontRenderer(stack);
		if (font == null)
			font = fontRendererObj;
		itemRender.renderItemAndEffectIntoGUI(font, this.mc.getTextureManager(), stack, xPos, yPos);
		itemRender.renderItemOverlayIntoGUI(font, this.mc.getTextureManager(), stack, xPos, yPos);
	}

}
