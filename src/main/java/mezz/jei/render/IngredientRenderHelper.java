package mezz.jei.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.util.Translator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IngredientRenderHelper {
	private static final Logger LOGGER = LogManager.getLogger();

	public static <V> List<String> getIngredientTooltipSafe(V ingredient, IIngredientRenderer<V> ingredientRenderer, IIngredientHelper<V> ingredientHelper, IModIdHelper modIdHelper) {
		try {
			Minecraft minecraft = Minecraft.getInstance();
			ITooltipFlag.TooltipFlags tooltipFlag = minecraft.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL;
			List<String> tooltip = ingredientRenderer.getTooltip(ingredient, tooltipFlag);
			tooltip = modIdHelper.addModNameToIngredientTooltip(tooltip, ingredient, ingredientHelper);
			return tooltip;
		} catch (RuntimeException | LinkageError e) {
			LOGGER.error("Tooltip crashed.", e);
		}

		List<String> tooltip = new ArrayList<>();
		tooltip.add(TextFormatting.RED + Translator.translateToLocal("jei.tooltip.error.crash"));
		return tooltip;
	}
}
