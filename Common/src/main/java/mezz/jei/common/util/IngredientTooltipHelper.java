package mezz.jei.common.util;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import mezz.jei.api.ingredients.IIngredientRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IngredientTooltipHelper {
	private static final Logger LOGGER = LogManager.getLogger();

	public static <V> void getIngredientTooltipSafe(ITooltipBuilder tooltip, V ingredient, IIngredientRenderer<V> ingredientRenderer) {
		try {
			Minecraft minecraft = Minecraft.getInstance();
			TooltipFlag.Default tooltipFlag = minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
			tooltip.addAll(ingredientRenderer.getTooltip(ingredient, tooltipFlag));
			return;
		} catch (RuntimeException | LinkageError e) {
			LOGGER.error("Tooltip crashed.", e);
		}

		MutableComponent translated = Component.translatable("jei.tooltip.error.crash");
		tooltip.add(translated.withStyle(ChatFormatting.RED));
	}
}
