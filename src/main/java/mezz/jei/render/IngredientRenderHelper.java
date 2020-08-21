package mezz.jei.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IngredientRenderHelper {
	private static final Logger LOGGER = LogManager.getLogger();

	public static <V> List<ITextComponent> getIngredientTooltipSafe(V ingredient, IIngredientRenderer<V> ingredientRenderer, IIngredientHelper<V> ingredientHelper, IModIdHelper modIdHelper) {
		try {
			Minecraft minecraft = Minecraft.getInstance();
			ITooltipFlag.TooltipFlags tooltipFlag = minecraft.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL;
			List<ITextComponent> tooltip = ingredientRenderer.getTooltip(ingredient, tooltipFlag);
			tooltip = modIdHelper.addModNameToIngredientTooltip(tooltip, ingredient, ingredientHelper);
			return tooltip;
		} catch (RuntimeException | LinkageError e) {
			LOGGER.error("Tooltip crashed.", e);
		}

		List<ITextComponent> tooltip = new ArrayList<>();
		TranslationTextComponent translated = new TranslationTextComponent("jei.tooltip.error.crash");
		tooltip.add(translated.mergeStyle(TextFormatting.RED));
		return tooltip;
	}
}
