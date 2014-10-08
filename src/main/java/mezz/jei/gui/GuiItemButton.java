package mezz.jei.gui;

import cpw.mods.fml.client.FMLClientHandler;
import mezz.jei.util.Commands;
import mezz.jei.util.Permissions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiItemButton extends GuiButton {

	private static final RenderItem itemRender = new RenderItem();

	protected ItemStack itemStack;
	public static final int width = 16;
	public static final int height = 16;

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

	public void actionPerformed() {
		if (!enabled)
			return;

		EntityClientPlayerMP player = FMLClientHandler.instance().getClientPlayerEntity();
		if (Permissions.canPlayerSpawnItems(player) && player.inventory.getFirstEmptyStack() != -1)
			Commands.giveFullStack(itemStack);

		//TODO: recipes
	}

	@Override
	public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
		if (!visible)
			return;

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.enableGUIStandardItemLighting();

		FontRenderer font = itemStack.getItem().getFontRenderer(itemStack);
		if (font == null)
			font = minecraft.fontRenderer;

		itemRender.renderItemAndEffectIntoGUI(font, minecraft.getTextureManager(), itemStack, xPosition, yPosition);

		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
