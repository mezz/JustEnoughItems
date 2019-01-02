package mezz.jei.startup;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.ModList;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.config.ClientConfig;
import mezz.jei.config.ModIdFormattingConfig;
import mezz.jei.ingredients.AbstractModIdHelper;
import org.apache.commons.lang3.StringUtils;

public class ForgeModIdHelper extends AbstractModIdHelper {
	private final ClientConfig config;

	public ForgeModIdHelper(ClientConfig config) {
		this.config = config;
	}

	@Override
	public String getModNameForModId(String modId) {
		return ModList.get().getModContainerById(modId)
			.map(modContainer -> modContainer.getModInfo().getDisplayName())
			.orElse(modId);
	}

	@Override
	public boolean isDisplayingModNameEnabled() {
		String modNameFormat = config.getModNameFormat();
		return !modNameFormat.isEmpty();
	}

	@Override
	public String getFormattedModNameForModId(String modId) {
		String modName = getModNameForModId(modId);
		modName = removeChatFormatting(modName); // some crazy mod has formatting in the name
		String modNameFormat = config.getModNameFormat();
		if (!modNameFormat.isEmpty()) {
			if (modNameFormat.contains(ModIdFormattingConfig.MOD_NAME_FORMAT_CODE)) {
				return StringUtils.replaceOnce(modNameFormat, ModIdFormattingConfig.MOD_NAME_FORMAT_CODE, modName);
			}
			return modNameFormat + modName;
		}
		return modName;
	}

	private static String removeChatFormatting(String string) {
		String withoutFormattingCodes = TextFormatting.getTextWithoutFormattingCodes(string);
		return (withoutFormattingCodes == null) ? "" : withoutFormattingCodes;
	}

	@Override
	public <T> List<String> addModNameToIngredientTooltip(List<String> tooltip, T ingredient, IIngredientHelper<T> ingredientHelper) {
		if (config.isDebugModeEnabled() && Minecraft.getInstance().gameSettings.advancedItemTooltips) {
			tooltip = new ArrayList<>(tooltip);
			tooltip.add(TextFormatting.GRAY + "JEI Debug:");
			tooltip.add(TextFormatting.GRAY + "info: " + ingredientHelper.getErrorInfo(ingredient));
			tooltip.add(TextFormatting.GRAY + "uid: " + ingredientHelper.getUniqueId(ingredient));
		}
		if (config.isModNameFormatOverrideActive() && (ingredient instanceof ItemStack || ingredient instanceof EnchantmentData)) {
			// we detected that another mod is adding the mod name already
			return tooltip;
		}
		String modNameFormat = config.getModNameFormat();
		if (modNameFormat.isEmpty()) {
			return tooltip;
		}
		return super.addModNameToIngredientTooltip(tooltip, ingredient, ingredientHelper);
	}
}
