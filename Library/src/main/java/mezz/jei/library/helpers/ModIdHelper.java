package mezz.jei.library.helpers;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.platform.IPlatformModHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.library.config.IModIdFormatConfig;
import mezz.jei.library.config.ModIdFormatConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class ModIdHelper implements IModIdHelper {
	private final IModIdFormatConfig modIdFormattingConfig;
	private final IIngredientManager ingredientManager;

	public ModIdHelper(IModIdFormatConfig modIdFormattingConfig, IIngredientManager ingredientManager) {
		this.modIdFormattingConfig = modIdFormattingConfig;
		this.ingredientManager = ingredientManager;
	}

	@Override
	public boolean isDisplayingModNameEnabled() {
		String modNameFormat = modIdFormattingConfig.getModNameFormat();
		return !modNameFormat.isEmpty();
	}

	@Override
	public <T> List<Component> addModNameToIngredientTooltip(List<Component> tooltip, T ingredient, IIngredientHelper<T> ingredientHelper) {
		if (DebugConfig.isDebugModeEnabled() && Minecraft.getInstance().options.advancedItemTooltips) {
			tooltip = addDebugInfo(tooltip, ingredient, ingredientHelper);
		}
		if (!isDisplayingModNameEnabled()) {
			return tooltip;
		}
		if (modIdFormattingConfig.isModNameFormatOverrideActive() && (ingredient instanceof ItemStack)) {
			// we detected that another mod is adding the mod name already
			return tooltip;
		}
		String modId = ingredientHelper.getDisplayModId(ingredient);
		String modName = getFormattedModNameForModId(modId);
		List<Component> tooltipCopy = new ArrayList<>(tooltip);
		tooltipCopy.add(Component.literal(modName));
		return tooltipCopy;
	}

	@Override
	public <T> List<Component> addModNameToIngredientTooltip(List<Component> tooltip, ITypedIngredient<T> typedIngredient) {
		IIngredientType<T> type = typedIngredient.getType();
		T ingredient = typedIngredient.getIngredient();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(type);
		return addModNameToIngredientTooltip(tooltip, ingredient, ingredientHelper);
	}

	private static String removeChatFormatting(String string) {
		String withoutFormattingCodes = ChatFormatting.stripFormatting(string);
		return (withoutFormattingCodes == null) ? "" : withoutFormattingCodes;
	}

	private static <T> List<Component> addDebugInfo(List<Component> tooltip, T ingredient, IIngredientHelper<T> ingredientHelper) {
		tooltip = new ArrayList<>(tooltip);
		MutableComponent jeiDebug = Component.literal("JEI Debug:");
		MutableComponent info = Component.literal("info: " + ingredientHelper.getErrorInfo(ingredient));
		MutableComponent uid = Component.literal("uid: " + ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient));
		tooltip.add(jeiDebug.withStyle(ChatFormatting.DARK_GRAY));
		tooltip.add(info.withStyle(ChatFormatting.DARK_GRAY));
		tooltip.add(uid.withStyle(ChatFormatting.DARK_GRAY));
		return tooltip;
	}

	@Override
	public String getFormattedModNameForModId(String modId) {
		String modName = getModNameForModId(modId);
		modName = removeChatFormatting(modName); // some crazy mod has formatting in the name
		String modNameFormat = modIdFormattingConfig.getModNameFormat();
		if (!modNameFormat.isEmpty()) {
			if (modNameFormat.contains(ModIdFormatConfig.MOD_NAME_FORMAT_CODE)) {
				return StringUtils.replaceOnce(modNameFormat, ModIdFormatConfig.MOD_NAME_FORMAT_CODE, modName);
			}
			return modNameFormat + modName;
		}
		return modName;
	}

	@Override
	public String getModNameForModId(String modId) {
		IPlatformModHelper modHelper = Services.PLATFORM.getModHelper();
		return modHelper.getModNameForModId(modId);
	}
}
