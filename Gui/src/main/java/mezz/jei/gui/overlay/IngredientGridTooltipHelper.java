package mezz.jei.gui.overlay;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.core.search.SearchMode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class IngredientGridTooltipHelper {
	private final IIngredientManager ingredientManager;
	private final IIngredientFilterConfig ingredientFilterConfig;
	private final IClientToggleState toggleState;
	private final IInternalKeyMappings keyBindings;
	private final IColorHelper colorHelper;

	public IngredientGridTooltipHelper(
		IIngredientManager ingredientManager,
		IIngredientFilterConfig ingredientFilterConfig,
		IClientToggleState toggleState,
		IInternalKeyMappings keyBindings,
		IColorHelper colorHelper
	) {
		this.ingredientManager = ingredientManager;
		this.ingredientFilterConfig = ingredientFilterConfig;
		this.toggleState = toggleState;
		this.keyBindings = keyBindings;
		this.colorHelper = colorHelper;
	}

	public <T> JeiTooltip getIngredientTooltip(
		ITypedIngredient<T> typedIngredient,
		IIngredientRenderer<T> ingredientRenderer,
		IIngredientHelper<T> ingredientHelper
	) {
		List<Component> ingredientTooltip = SafeIngredientUtil.getTooltip(ingredientManager, ingredientRenderer, typedIngredient);

		JeiTooltip tooltip = new JeiTooltip();
		tooltip.addAll(ingredientTooltip);

		if (ingredientFilterConfig.getColorSearchMode() != SearchMode.DISABLED) {
			addColorSearchInfoToTooltip(tooltip, typedIngredient, ingredientHelper);
		}

		if (toggleState.isEditModeEnabled()) {
			addEditModeInfoToTooltip(tooltip, keyBindings);
		}

		return tooltip;
	}

	private <T> void addColorSearchInfoToTooltip(JeiTooltip tooltip, ITypedIngredient<T> typedIngredient, IIngredientHelper<T> ingredientHelper) {
		Iterable<Integer> colors = ingredientHelper.getColors(typedIngredient.getIngredient());
		String colorNamesString = StreamSupport.stream(colors.spliterator(), false)
			.map(colorHelper::getClosestColorName)
			.collect(Collectors.joining(", "));
		if (!colorNamesString.isEmpty()) {
			Component colorTranslation = Component.translatable("jei.tooltip.item.colors", colorNamesString)
				.withStyle(ChatFormatting.GRAY);
			tooltip.add(colorTranslation);
		}
	}

	private static void addEditModeInfoToTooltip(JeiTooltip tooltip, IInternalKeyMappings keyBindings) {
		tooltip.add(CommonComponents.EMPTY);
		tooltip.add(
			Component.translatable("gui.jei.editMode.description")
				.withStyle(ChatFormatting.DARK_GREEN)
		);
		tooltip.addKeyUsageComponent(
			"gui.jei.editMode.description.hide",
			keyBindings.getToggleHideIngredient()
		);
		tooltip.addKeyUsageComponent(
			"gui.jei.editMode.description.hide.wild",
			keyBindings.getToggleWildcardHideIngredient()
		);
	}
}
