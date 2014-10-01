package mezz.jei.gui;

import cpw.mods.fml.client.FMLClientHandler;
import mezz.jei.util.Commands;
import mezz.jei.util.Permissions;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;

public class GuiItemIcon {
	protected ItemStack itemStack;
	protected int x;
	protected int y;
	protected int width = 16;
	protected int height = 16;

	public GuiItemIcon(ItemStack itemStack, int x, int y) {
		this.itemStack = itemStack;
		this.x = x;
		this.y = y;
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + width;
	}

	public void mouseClicked(int xPos, int yPos, int mouseButton) {
		EntityClientPlayerMP player = FMLClientHandler.instance().getClientPlayerEntity();
		if (Permissions.canPlayerSpawnItems(player)) {
			if (mouseButton == 0)
				Commands.giveFullStack(itemStack);
			else if (mouseButton == 1)
				Commands.giveOneFromStack(itemStack);
		}
		//TODO: recipes
	}

	public void draw(RenderItem itemRender, FontRenderer fontRenderer, TextureManager textureManager) {
		FontRenderer font = itemStack.getItem().getFontRenderer(itemStack);
		if (font == null)
			font = fontRenderer;
		itemRender.renderItemAndEffectIntoGUI(font, textureManager, itemStack, x, y);
		itemRender.renderItemOverlayIntoGUI(font, textureManager, itemStack, x, y);
	}

}
