package mezz.jei.gui;

import mezz.jei.util.StackUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiItemStack {

	private static final int baseWidth = 16;
	private static final int baseHeight = 16;
	private static final RenderItem itemRender = new RenderItem();

	private final int width;
	private final int height;
	private final int padding;
	/* the amount of time in ms to display one itemStack before cycling to the next one */
	private final int cycleTime = 1000;

	private int xPosition;
	private int yPosition;
	private boolean enabled;
	private boolean visible;

	@Nonnull
	private List<ItemStack> itemStacks = new ArrayList<ItemStack>();
	private long drawTime = 0;

	public GuiItemStack(int xPosition, int yPosition, int padding) {
		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.padding = padding;
		this.width = getWidth(padding);
		this.height = getHeight(padding);
	}

	public static int getWidth(int padding) {
		return baseWidth + (2 * padding);
	}

	public static int getHeight(int padding) {
		return baseHeight + (2 * padding);
	}

	public void setItemStacks(@Nonnull Iterable<ItemStack> itemStacksIn, @Nullable ItemStack focusStack) {
		List<ItemStack> itemStacks = StackUtil.getAllSubtypes(itemStacksIn);
		ItemStack matchingItemStack = StackUtil.containsStack(itemStacks, focusStack);
		if (matchingItemStack != null) {
			setItemStack(matchingItemStack);
		} else {
			setItemStacks(itemStacks);
		}
	}

	public void setItemStack(@Nonnull ItemStack itemStack) {
		setItemStacks(Collections.singletonList(itemStack));
	}

	private void setItemStacks(@Nonnull Iterable<ItemStack> itemStacks) {
		this.itemStacks = StackUtil.getAllSubtypes(itemStacks);
		visible = enabled = !this.itemStacks.isEmpty();
	}

	public void clearItemStacks() {
		itemStacks.clear();
		visible = enabled = false;
	}

	@Nullable
	public ItemStack getItemStack() {
		if (itemStacks.isEmpty())
			return null;

		Long stackIndex = (drawTime / cycleTime) % itemStacks.size();
		return itemStacks.get(stackIndex.intValue());
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return enabled && visible && (mouseX >= xPosition) && (mouseY >= yPosition) && (mouseX < xPosition + width) && (mouseY < yPosition + height);
	}

	public void draw(@Nonnull Minecraft minecraft) {
		draw(minecraft, true);
	}

	public void drawHovered(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		ItemStack itemStack = getItemStack();
		if (itemStack == null)
			return;
		draw(minecraft, false);
		minecraft.currentScreen.renderToolTip(itemStack, mouseX, mouseY);
		RenderHelper.disableStandardItemLighting();
	}

	private void draw(Minecraft minecraft, boolean cycleIcons) {
		if (!visible)
			return;

		if (cycleIcons)
			drawTime = System.currentTimeMillis();

		ItemStack itemStack = getItemStack();
		if (itemStack == null)
			return;

		FontRenderer font = itemStack.getItem().getFontRenderer(itemStack);
		if (font == null)
			font = minecraft.fontRenderer;

		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		{
			RenderHelper.enableGUIStandardItemLighting();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			GL11.glEnable(GL11.GL_LIGHTING);
			itemRender.zLevel = 100.0F;

			itemRender.renderItemAndEffectIntoGUI(font, minecraft.getTextureManager(), itemStack, xPosition + padding, yPosition + padding);
			itemRender.renderItemOverlayIntoGUI(font, minecraft.getTextureManager(), itemStack, xPosition + padding, yPosition + padding);

			itemRender.zLevel = 0.0F;
		}
		GL11.glPopAttrib();
	}
}
