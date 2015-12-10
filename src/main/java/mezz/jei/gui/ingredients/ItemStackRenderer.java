package mezz.jei.gui.ingredients;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.util.Translator;

public class ItemStackRenderer implements IIngredientRenderer<ItemStack> {
	@Override
	public void draw(@Nonnull Minecraft minecraft, int xPosition, int yPosition, @Nonnull ItemStack itemStack) {
		FontRenderer font = getFontRenderer(minecraft, itemStack);

		RenderHelper.enableGUIStandardItemLighting();

		minecraft.getRenderItem().renderItemAndEffectIntoGUI(itemStack, xPosition, yPosition);
		minecraft.getRenderItem().renderItemOverlayIntoGUI(font, itemStack, xPosition, yPosition, null);

		RenderHelper.disableStandardItemLighting();

		if (Config.editModeEnabled) {
			if (Config.isItemOnConfigBlacklist(itemStack, false)) {
				GuiScreen.drawRect(xPosition, yPosition, xPosition + 8, yPosition + 16, 0xFFFFFF00);
			}
			if (Config.isItemOnConfigBlacklist(itemStack, true)) {
				GuiScreen.drawRect(xPosition + 8, yPosition, xPosition + 16, yPosition + 16, 0xFFFF0000);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getTooltip(@Nonnull Minecraft minecraft, @Nonnull ItemStack itemStack) {
		List<String> list = itemStack.getTooltip(minecraft.thePlayer, minecraft.gameSettings.advancedItemTooltips);
		for (int k = 0; k < list.size(); ++k) {
			if (k == 0) {
				list.set(k, itemStack.getRarity().rarityColor + list.get(k));
			} else {
				list.set(k, EnumChatFormatting.GRAY + list.get(k));
			}
		}

		if (Config.editModeEnabled) {
			list.add("");
			list.add(EnumChatFormatting.ITALIC + Translator.translateToLocal("gui.jei.editMode.description"));
			if (Config.isItemOnConfigBlacklist(itemStack, false)) {
				String description = EnumChatFormatting.YELLOW + Translator.translateToLocal("gui.jei.editMode.description.show");
				list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(description, Constants.MAX_TOOLTIP_WIDTH));
			} else {
				String description = EnumChatFormatting.YELLOW + Translator.translateToLocal("gui.jei.editMode.description.hide");
				list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(description, Constants.MAX_TOOLTIP_WIDTH));
			}

			Item item = itemStack.getItem();
			if (item.getHasSubtypes()) {
				if (Config.isItemOnConfigBlacklist(itemStack, true)) {
					String description = EnumChatFormatting.RED + Translator.translateToLocal("gui.jei.editMode.description.show.wild");
					list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(description, Constants.MAX_TOOLTIP_WIDTH));
				} else {
					String description = EnumChatFormatting.RED + Translator.translateToLocal("gui.jei.editMode.description.hide.wild");
					list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(description, Constants.MAX_TOOLTIP_WIDTH));
				}
			}
		}

		return list;
	}

	@Override
	public FontRenderer getFontRenderer(@Nonnull Minecraft minecraft, @Nonnull ItemStack itemStack) {
		FontRenderer fontRenderer = itemStack.getItem().getFontRenderer(itemStack);
		if (fontRenderer == null) {
			fontRenderer = minecraft.fontRendererObj;
		}
		return fontRenderer;
	}
}
