package mezz.jei.ingredients;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.config.IClientConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.config.ModIdFormattingConfig;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.commons.lang3.StringUtils;

public class ForgeModIdHelper extends AbstractModIdHelper {
	private final IClientConfig config;
	private final ModIdFormattingConfig modIdFormattingConfig;

	public ForgeModIdHelper(IClientConfig config, ModIdFormattingConfig modIdFormattingConfig) {
		this.config = config;
		this.modIdFormattingConfig = modIdFormattingConfig;
	}

	@Override
	public String getModNameForModId(String modId) {
		return ModList.get().getModContainerById(modId)
			.map(ModContainer::getModInfo)
			.map(IModInfo::getDisplayName)
			.orElse(StringUtils.capitalize(modId));
	}

	@Override
	public boolean isDisplayingModNameEnabled() {
		String modNameFormat = modIdFormattingConfig.getModNameFormat();
		return !modNameFormat.isEmpty();
	}

	@Override
	public String getFormattedModNameForModId(String modId) {
		String modName = getModNameForModId(modId);
		modName = removeChatFormatting(modName); // some crazy mod has formatting in the name
		String modNameFormat = modIdFormattingConfig.getModNameFormat();
		if (!modNameFormat.isEmpty()) {
			if (modNameFormat.contains(ModIdFormattingConfig.MOD_NAME_FORMAT_CODE)) {
				return StringUtils.replaceOnce(modNameFormat, ModIdFormattingConfig.MOD_NAME_FORMAT_CODE, modName);
			}
			return modNameFormat + modName;
		}
		return modName;
	}

	private static String removeChatFormatting(String string) {
		String withoutFormattingCodes = ChatFormatting.stripFormatting(string);
		return (withoutFormattingCodes == null) ? "" : withoutFormattingCodes;
	}

	@Override
	public <T> List<Component> addModNameToIngredientTooltip(List<Component> tooltip, T ingredient, IIngredientHelper<T> ingredientHelper) {
		if (config.isDebugModeEnabled() && Minecraft.getInstance().options.advancedItemTooltips) {
			tooltip = addDebugInfo(tooltip, ingredient, ingredientHelper);
		}
		if (modIdFormattingConfig.isModNameFormatOverrideActive() && (ingredient instanceof ItemStack)) {
			// we detected that another mod is adding the mod name already
			return tooltip;
		}
		String modNameFormat = modIdFormattingConfig.getModNameFormat();
		if (modNameFormat.isEmpty()) {
			return tooltip;
		}
		return super.addModNameToIngredientTooltip(tooltip, ingredient, ingredientHelper);
	}

	private static <T> List<Component> addDebugInfo(List<Component> tooltip, T ingredient, IIngredientHelper<T> ingredientHelper) {
		tooltip = new ArrayList<>(tooltip);
		TextComponent jeiDebug = new TextComponent("JEI Debug:");
		TextComponent info = new TextComponent("info: " + ingredientHelper.getErrorInfo(ingredient));
		TextComponent uid = new TextComponent("uid: " + ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient));
		tooltip.add(jeiDebug.withStyle(ChatFormatting.GRAY));
		tooltip.add(info.withStyle(ChatFormatting.GRAY));
		tooltip.add(uid.withStyle(ChatFormatting.GRAY));
		return tooltip;
	}
}
