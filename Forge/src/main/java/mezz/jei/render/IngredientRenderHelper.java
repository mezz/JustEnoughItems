package mezz.jei.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IngredientRenderHelper {
	private static final Logger LOGGER = LogManager.getLogger();

	public static <V> List<Component> getIngredientTooltipSafe(V ingredient, IIngredientRenderer<V> ingredientRenderer, IIngredientHelper<V> ingredientHelper, IModIdHelper modIdHelper) {
		try {
			Minecraft minecraft = Minecraft.getInstance();
			TooltipFlag.Default tooltipFlag = minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
			List<Component> tooltip = ingredientRenderer.getTooltip(ingredient, tooltipFlag);
			tooltip = modIdHelper.addModNameToIngredientTooltip(tooltip, ingredient, ingredientHelper);
			return tooltip;
		} catch (RuntimeException | LinkageError e) {
			LOGGER.error("Tooltip crashed.", e);
		}

		List<Component> tooltip = new ArrayList<>();
		TranslatableComponent translated = new TranslatableComponent("jei.tooltip.error.crash");
		tooltip.add(translated.withStyle(ChatFormatting.RED));
		return tooltip;
	}
}
