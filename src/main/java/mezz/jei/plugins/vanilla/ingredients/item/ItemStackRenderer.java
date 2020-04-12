package mezz.jei.plugins.vanilla.ingredients.item;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Translator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ItemStackRenderer implements IIngredientRenderer<ItemStack> {
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void render(int xPosition, int yPosition, @Nullable ItemStack ingredient) {
		if (ingredient != null) {
			RenderSystem.enableDepthTest();
			RenderHelper.enableStandardItemLighting();
			Minecraft minecraft = Minecraft.getInstance();
			FontRenderer font = getFontRenderer(minecraft, ingredient);
			ItemRenderer itemRenderer = minecraft.getItemRenderer();
			itemRenderer.renderItemAndEffectIntoGUI(null, ingredient, xPosition, yPosition);
			itemRenderer.renderItemOverlayIntoGUI(font, ingredient, xPosition, yPosition, null);
			RenderSystem.disableBlend();
			RenderHelper.disableStandardItemLighting();
		}
	}

	@Override
	public List<String> getTooltip(ItemStack ingredient, ITooltipFlag tooltipFlag) {
		Minecraft minecraft = Minecraft.getInstance();
		PlayerEntity player = minecraft.player;
		List<String> list;
		try {
			list = ingredient.getTooltip(player, tooltipFlag).stream()
				.map(ITextComponent::getFormattedText)
				.collect(Collectors.toList());
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(ingredient);
			LOGGER.error("Failed to get tooltip: {}", itemStackInfo, e);
			list = new ArrayList<>();
			list.add(TextFormatting.RED + Translator.translateToLocal("jei.tooltip.error.crash"));
			return list;
		}

		Rarity rarity;
		try {
			rarity = ingredient.getRarity();
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(ingredient);
			LOGGER.error("Failed to get rarity: {}", itemStackInfo, e);
			rarity = Rarity.COMMON;
		}

		for (int k = 0; k < list.size(); ++k) {
			if (k == 0) {
				list.set(k, rarity.color + list.get(k));
			} else {
				list.set(k, TextFormatting.GRAY + list.get(k));
			}
		}

		return list;
	}

	@Override
	public FontRenderer getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
		FontRenderer fontRenderer = ingredient.getItem().getFontRenderer(ingredient);
		if (fontRenderer == null) {
			fontRenderer = minecraft.fontRenderer;
		}
		return fontRenderer;
	}
}
