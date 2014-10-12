package mezz.jei.gui;

import cpw.mods.fml.client.FMLClientHandler;
import mezz.jei.util.Commands;
import mezz.jei.util.Permissions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;

public class GuiItemButton extends GuiButton {

	private static final RenderItem itemRender = new RenderItem();

	protected ItemStack itemStack;
	protected static final int padding = 1;
	public static final int width = 16 + (padding * 2);
	public static final int height = 16 + (padding * 2);

	public GuiItemButton(ItemStack itemStack, int x, int y) {
		super(0, x, y, width, height, null);
		setItemStack(itemStack);
	}

	public void setItemStack(ItemStack itemStack) {
		this.itemStack = itemStack;
		this.visible = this.enabled = (itemStack != null);
	}

	public ItemStack getItemStack() {
		return this.itemStack;
	}

	public void handleMouseClick(int mouseButton) {
		if (!enabled)
			return;

		EntityClientPlayerMP player = FMLClientHandler.instance().getClientPlayerEntity();
		if (Permissions.canPlayerSpawnItems(player) && player.inventory.getFirstEmptyStack() != -1) {
			if (mouseButton == 0) {
				Commands.giveFullStack(itemStack);
			} else if (mouseButton == 1) {
				Commands.giveOneFromStack(itemStack);
			}
		}

		//TODO: recipes
	}

	@Override
	public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
		if (!visible)
			return;

		FontRenderer font = itemStack.getItem().getFontRenderer(itemStack);
		if (font == null)
			font = minecraft.fontRenderer;

		itemRender.renderItemAndEffectIntoGUI(font, minecraft.getTextureManager(), itemStack, xPosition + padding, yPosition + padding);
	}
}
