package mezz.jei.library.helpers;

import com.google.common.collect.ImmutableSetMultimap;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.platform.IPlatformModHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.library.config.IModIdFormatConfig;
import mezz.jei.library.config.ModIdFormatConfig;
import mezz.jei.library.config.StyledTextHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

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

	@Override
	@Deprecated(since = "29.10.0", forRemoval = true)
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
