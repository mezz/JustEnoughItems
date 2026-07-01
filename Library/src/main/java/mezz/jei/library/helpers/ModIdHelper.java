package mezz.jei.library.helpers;

import com.google.common.collect.ImmutableSetMultimap;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.platform.IPlatformModHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.library.config.IModIdFormatConfig;
import mezz.jei.library.config.ModIdFormatConfig;
import mezz.jei.library.config.StyledTextHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public final class ModIdHelper implements IModIdHelper {
	private final IModIdFormatConfig modIdFormattingConfig;
	private final Function<ITypedIngredient<?>, String> getDisplayModId;
	private final ImmutableSetMultimap<String, String> modAliases;

	public ModIdHelper(IModIdFormatConfig modIdFormattingConfig, Function<ITypedIngredient<?>, String> getDisplayModId, ImmutableSetMultimap<String, String> modAliases) {
		this.modIdFormattingConfig = modIdFormattingConfig;
		this.getDisplayModId = getDisplayModId;
		this.modAliases = modAliases;
	}

	@Override
	public boolean isDisplayingModNameEnabled() {
		Component modNameFormat = modIdFormattingConfig.getModNameFormat();
		return !modNameFormat.getString().isEmpty();
	}

	@SuppressWarnings("removal")
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
		return addModNameToTooltip(tooltip, modId);
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

		String modId = getDisplayModId.apply(typedIngredient);
		return Optional.of(getFormattedModNameComponentForModId(modId));
	}

	@SuppressWarnings("removal")
	@Override
	public <T> List<Component> addModNameToIngredientTooltip(List<Component> tooltip, ITypedIngredient<T> typedIngredient) {
		IIngredientType<T> type = typedIngredient.getType();
		if (!isDisplayingModNameEnabled()) {
			return tooltip;
		}
		if (modIdFormattingConfig.isModNameFormatOverrideActive() && type == VanillaTypes.ITEM_STACK) {
			// we detected that another mod is adding the mod name already
			return tooltip;
		}
		String modId = getDisplayModId.apply(typedIngredient);
		return addModNameToTooltip(tooltip, modId);
	}

	@Override
	@Deprecated(since = "19.31.0", forRemoval = true)
	@SuppressWarnings("removal")
	public String getFormattedModNameForModId(String modId) {
		Component modName = getFormattedModNameComponentForModId(modId);
		return StyledTextHelper.toLegacyString(modName);
	}

	@Override
	public Component getFormattedModNameComponentForModId(String modId) {
		String modName = getModNameForModId(modId);
		modName = ChatFormatting.stripFormatting(modName); // some crazy mod has formatting in the name
		Component modNameFormat = modIdFormattingConfig.getModNameFormat();
		if (!modNameFormat.getString().isEmpty()) {
			return ModIdFormatConfig.replaceModNameFormatCode(modNameFormat, modName);
		}
		return Component.literal(modName);
	}

	private List<Component> addModNameToTooltip(List<Component> tooltip, String modId) {
		List<Component> tooltipCopy = new ArrayList<>(tooltip);
		tooltipCopy.add(getFormattedModNameComponentForModId(modId));
		return tooltipCopy;
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
