package mezz.jei.gui;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

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
	protected void draw(@Nonnull Minecraft minecraft, int xPosition, int yPosition, @Nonnull ItemStack itemStack) {
		FontRenderer font = getFontRenderer(minecraft, itemStack);

		RenderHelper.enableGUIStandardItemLighting();

		minecraft.getRenderItem().renderItemAndEffectIntoGUI(itemStack, xPosition + padding, yPosition + padding);
		minecraft.getRenderItem().renderItemOverlayIntoGUI(font, itemStack, xPosition + padding, yPosition + padding, null);

		RenderHelper.disableStandardItemLighting();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List getTooltip(@Nonnull Minecraft minecraft, @Nonnull ItemStack itemStack) {
		List list = itemStack.getTooltip(minecraft.thePlayer, minecraft.gameSettings.advancedItemTooltips);
		for (int k = 0; k < list.size(); ++k) {
			if (k == 0) {
				list.set(k, itemStack.getRarity().rarityColor + (String) list.get(k));
			} else {
				list.set(k, EnumChatFormatting.GRAY + (String) list.get(k));
			}
		}
		return list;
	}

	@Override
	protected FontRenderer getFontRenderer(@Nonnull Minecraft minecraft, @Nonnull ItemStack itemStack) {
		FontRenderer fontRenderer = itemStack.getItem().getFontRenderer(itemStack);
		if (fontRenderer == null) {
			fontRenderer = minecraft.fontRendererObj;
		}
		return fontRenderer;
	}
}
