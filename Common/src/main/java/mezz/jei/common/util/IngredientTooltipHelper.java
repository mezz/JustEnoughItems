package mezz.jei.common.util;

import java.util.ArrayList;
import java.util.List;

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

	public static <V> List<Component> getMutableIngredientTooltipSafe(V ingredient, IIngredientRenderer<V> ingredientRenderer) {
		try {
			Minecraft minecraft = Minecraft.getInstance();
			TooltipFlag.Default tooltipFlag = minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
			List<Component> tooltip = ingredientRenderer.getTooltip(ingredient, tooltipFlag);
			return new ArrayList<>(tooltip);
		} catch (RuntimeException | LinkageError e) {
			LOGGER.error("Tooltip crashed.", e);
		}

		List<Component> tooltip = new ArrayList<>();
		MutableComponent translated = Component.translatable("jei.tooltip.error.crash");
		tooltip.add(translated.withStyle(ChatFormatting.RED));
		return tooltip;
	}
}
