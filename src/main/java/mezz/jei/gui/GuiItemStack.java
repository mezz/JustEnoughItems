package mezz.jei.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import mezz.jei.util.Log;
import mezz.jei.util.StackUtil;

public class GuiItemStack {

	private static final int baseWidth = 16;
	private static final int baseHeight = 16;

	private final int width;
	private final int height;
	private final int padding;
	private final int xPosition;
	private final int yPosition;

	/* the amount of time in ms to display one itemStack before cycling to the next one */
	private static final int cycleTime = 1000;
	private long drawTime = 0;

	private boolean enabled;
	private boolean visible;

	@Nonnull
	private final List<ItemStack> itemStacks = new ArrayList<ItemStack>();

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
		this.itemStacks.clear();
		Collection<ItemStack> itemStacks = StackUtil.getAllSubtypes(itemStacksIn);
		ItemStack matchingItemStack = StackUtil.containsStack(itemStacks, focusStack);
		if (matchingItemStack != null) {
			this.itemStacks.add(matchingItemStack);
		} else {
			this.itemStacks.addAll(itemStacks);
		}
		visible = enabled = !this.itemStacks.isEmpty();
	}

	public void setItemStack(@Nonnull ItemStack itemStack, @Nullable ItemStack focusStack) {
		setItemStacks(Collections.singletonList(itemStack), focusStack);
	}

	public void clearItemStacks() {
		itemStacks.clear();
		visible = enabled = false;
	}

	@Nullable
	public ItemStack getItemStack() {
		if (itemStacks.isEmpty()) {
			return null;
		}

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
		if (itemStack == null) {
			return;
		}
		draw(minecraft, false);
		try {
			minecraft.currentScreen.renderToolTip(itemStack, mouseX, mouseY);
			RenderHelper.disableStandardItemLighting();
		} catch (RuntimeException e) {
			Log.error("Exception when rendering tooltip on {}.\n{}", itemStack, e);
		}
	}

	private void draw(Minecraft minecraft, boolean cycleIcons) {
		if (!visible) {
			return;
		}

		if (cycleIcons) {
			drawTime = System.currentTimeMillis();
		}

		ItemStack itemStack = getItemStack();
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
