package mezz.jei.gui;

import javax.annotation.Nonnull;
import java.util.Collection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import mezz.jei.util.Log;
import mezz.jei.util.StackUtil;

public class GuiItemStack extends GuiWidget<ItemStack> {

	private static final int baseWidth = 16;
	private static final int baseHeight = 16;

	public static int getWidth(int padding) {
		return baseWidth + (2 * padding);
	}

	public static int getHeight(int padding) {
		return baseHeight + (2 * padding);
	}

	private final int padding;

	public GuiItemStack(int xPosition, int yPosition, int padding) {
		super(xPosition, yPosition, getWidth(padding), getHeight(padding));
		this.padding = padding;
	}

	@Override
	protected Collection<ItemStack> expandSubtypes(Collection<ItemStack> contained) {
		return StackUtil.getAllSubtypes(contained);
	}

	@Override
	protected ItemStack getMatch(Iterable<ItemStack> contained, @Nonnull Focus toMatch) {
		return StackUtil.containsStack(contained, toMatch.getStack());
	}

	@Override
	public void draw(@Nonnull Minecraft minecraft) {
		draw(minecraft, true);
	}

	@Override
	public void drawHovered(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		ItemStack itemStack = get();
		if (itemStack == null) {
			return;
		}
		draw(minecraft, false);
		try {
			GlStateManager.disableDepth();
			this.zLevel = 0;
			RenderHelper.disableStandardItemLighting();
			GL11.glEnable(GL11.GL_BLEND);
			drawRect(xPosition, yPosition, xPosition + width, yPosition + width, 0x7FFFFFFF);
			this.zLevel = 0;
			minecraft.currentScreen.renderToolTip(itemStack, mouseX, mouseY);
			GlStateManager.enableDepth();
		} catch (RuntimeException e) {
			Log.error("Exception when rendering tooltip on {}.\n{}", itemStack, e);
		}
	}

	private void draw(Minecraft minecraft, boolean cycleIcons) {
		if (!visible) {
			return;
		}

		cycleTimer.onDraw(cycleIcons);

		ItemStack itemStack = get();
		if (itemStack == null) {
			return;
		}

		FontRenderer font = itemStack.getItem().getFontRenderer(itemStack);
		if (font == null) {
			font = minecraft.fontRendererObj;
		}

		RenderHelper.enableGUIStandardItemLighting();

		minecraft.getRenderItem().renderItemAndEffectIntoGUI(itemStack, xPosition + padding, yPosition + padding);
		minecraft.getRenderItem().renderItemOverlayIntoGUI(font, itemStack, xPosition + padding, yPosition + padding, null);

		RenderHelper.disableStandardItemLighting();
	}
}
