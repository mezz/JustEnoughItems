package mezz.jei.library.helpers;

import com.google.common.collect.ImmutableSetMultimap;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformModHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.library.config.IModIdFormatConfig;
import mezz.jei.library.config.ModIdFormatConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class ModIdHelper implements IModIdHelper {
	private final IModIdFormatConfig modIdFormattingConfig;
	private final IIngredientManager ingredientManager;
	private final ImmutableSetMultimap<String, String> modAliases;

	public ModIdHelper(IModIdFormatConfig modIdFormattingConfig, IIngredientManager ingredientManager, ImmutableSetMultimap<String, String> modAliases) {
		this.modIdFormattingConfig = modIdFormattingConfig;
		this.ingredientManager = ingredientManager;
		this.modAliases = modAliases;
	}

	@Override
	public boolean isDisplayingModNameEnabled() {
		String modNameFormat = modIdFormattingConfig.getModNameFormat();
		return !modNameFormat.isEmpty();
	}

	@Override
	public <T> List<Component> addModNameToIngredientTooltip(List<Component> tooltip, T ingredient, IIngredientHelper<T> ingredientHelper) {
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
	public <T> Optional<Component> getModNameForTooltip(ITypedIngredient<T> typedIngredient) {
		if (!isDisplayingModNameEnabled()) {
			return Optional.empty();
		}

		IIngredientType<T> type = typedIngredient.getType();

		if (modIdFormattingConfig.isModNameFormatOverrideActive() && type == VanillaTypes.ITEM_STACK) {
			// we detected that another mod is adding the mod name already
			return Optional.empty();
		}

		T ingredient = typedIngredient.getIngredient();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(type);
		String modId = ingredientHelper.getDisplayModId(ingredient);
		String modName = getFormattedModNameForModId(modId);
		return Optional.of(Component.literal(modName));
	}

	@Override
	public <T> List<Component> addModNameToIngredientTooltip(List<Component> tooltip, ITypedIngredient<T> typedIngredient) {
		IIngredientType<T> type = typedIngredient.getType();
		T ingredient = typedIngredient.getIngredient();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(type);
		return addModNameToIngredientTooltip(tooltip, ingredient, ingredientHelper);
	}

	@Override
	public String getFormattedModNameForModId(String modId) {
		String modName = getModNameForModId(modId);
		modName = ChatFormatting.stripFormatting(modName); // some crazy mod has formatting in the name
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
	public Set<String> getModAliases(String modId) {
		return modAliases.get(modId);
	}

	@Override
	public String getModNameForModId(String modId) {
		IPlatformModHelper modHelper = Services.PLATFORM.getModHelper();
		return modHelper.getModNameForModId(modId);
	}
}
