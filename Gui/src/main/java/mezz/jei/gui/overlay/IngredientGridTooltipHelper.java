package mezz.jei.gui.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.IngredientTooltipHelper;
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.core.config.IWorldConfig;
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
	private final IWorldConfig worldConfig;
	private final IInternalKeyMappings keyBindings;
	private final IColorHelper colorHelper;

	public IngredientGridTooltipHelper(
		IIngredientManager ingredientManager,
		IIngredientFilterConfig ingredientFilterConfig,
		IWorldConfig worldConfig,
		IInternalKeyMappings keyBindings,
		IColorHelper colorHelper
	) {
		this.ingredientManager = ingredientManager;
		this.ingredientFilterConfig = ingredientFilterConfig;
		this.worldConfig = worldConfig;
		this.keyBindings = keyBindings;
		this.colorHelper = colorHelper;
	}

	public <T> void drawTooltip(PoseStack poseStack, int mouseX, int mouseY, ITypedIngredient<T> typedIngredient) {
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);

		JeiTooltip tooltip = new JeiTooltip();
		getTooltip(tooltip, typedIngredient, ingredientRenderer, ingredientHelper);
		tooltip.draw(poseStack, mouseX, mouseY);
	}

	public <T> void getTooltip(JeiTooltip tooltip, ITypedIngredient<T> typedIngredient, IIngredientRenderer<T> ingredientRenderer, IIngredientHelper<T> ingredientHelper) {
		T ingredient = typedIngredient.getIngredient();
		IngredientTooltipHelper.getIngredientTooltipSafe(tooltip, ingredient, ingredientRenderer);
		IModIdHelper modIdHelper = Internal.getJeiRuntime().getJeiHelpers().getModIdHelper();
		modIdHelper.getModNameForTooltip(typedIngredient)
				.ifPresent(tooltip::add);

		if (ingredientFilterConfig.getColorSearchMode() != SearchMode.DISABLED) {
			addColorSearchInfoToTooltip(tooltip, ingredient, ingredientHelper);
		}

		if (worldConfig.isEditModeEnabled()) {
			addEditModeInfoToTooltip(tooltip, keyBindings);
		}
	}

	private <T> void addColorSearchInfoToTooltip(JeiTooltip tooltip, T ingredient, IIngredientHelper<T> ingredientHelper) {
		Iterable<Integer> colors = ingredientHelper.getColors(ingredient);
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
		List<Component> lines = List.of(
			CommonComponents.EMPTY,
			Component.translatable("gui.jei.editMode.description")
				.withStyle(ChatFormatting.DARK_GREEN),
			Component.translatable(
				"gui.jei.editMode.description.hide",
				keyBindings.getToggleHideIngredient().getTranslatedKeyMessage()
			).withStyle(ChatFormatting.GRAY),
			Component.translatable(
				"gui.jei.editMode.description.hide.wild",
				keyBindings.getToggleWildcardHideIngredient().getTranslatedKeyMessage()
			).withStyle(ChatFormatting.GRAY)
		);
		tooltip.addAll(lines);
	}

	public <T> void getIngredientTooltip(
		JeiTooltip tooltip,
		ITypedIngredient<T> typedIngredient,
		IIngredientRenderer<T> ingredientRenderer,
		IIngredientHelper<T> ingredientHelper
	) {
		SafeIngredientUtil.getTooltip(tooltip, ingredientManager, ingredientRenderer, typedIngredient);

		if (ingredientFilterConfig.getColorSearchMode() != SearchMode.DISABLED) {
			addColorSearchInfoToTooltip(tooltip, typedIngredient.getIngredient(), ingredientHelper);
		}

		if (worldConfig.isEditModeEnabled()) {
			addEditModeInfoToTooltip(tooltip, keyBindings);
		}
	}
}
