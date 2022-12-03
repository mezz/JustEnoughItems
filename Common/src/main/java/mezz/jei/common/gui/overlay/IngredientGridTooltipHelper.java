package mezz.jei.common.gui.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientInfo;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.gui.TooltipRenderer;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.render.IngredientRenderHelper;
import mezz.jei.core.config.IWorldConfig;
import mezz.jei.core.search.SearchMode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class IngredientGridTooltipHelper {
	private final IRegisteredIngredients registeredIngredients;
	private final IIngredientFilterConfig ingredientFilterConfig;
	private final IWorldConfig worldConfig;
	private final IModIdHelper modIdHelper;
	private final IInternalKeyMappings keyBindings;
	private final IColorHelper colorHelper;

	public IngredientGridTooltipHelper(
		IRegisteredIngredients registeredIngredients,
		IIngredientFilterConfig ingredientFilterConfig,
		IWorldConfig worldConfig,
		IModIdHelper modIdHelper,
		IInternalKeyMappings keyBindings,
		IColorHelper colorHelper
	) {
		this.registeredIngredients = registeredIngredients;
		this.ingredientFilterConfig = ingredientFilterConfig;
		this.worldConfig = worldConfig;
		this.modIdHelper = modIdHelper;
		this.keyBindings = keyBindings;
		this.colorHelper = colorHelper;
	}

	public <T> void drawTooltip(PoseStack poseStack, int mouseX, int mouseY, ITypedIngredient<T> value) {
		IIngredientType<T> ingredientType = value.getType();
		T ingredient = value.getIngredient();
		IIngredientInfo<T> ingredientInfo = registeredIngredients.getIngredientInfo(ingredientType);
		IIngredientRenderer<T> ingredientRenderer = ingredientInfo.getIngredientRenderer();

		List<Component> tooltip = getTooltip(ingredient, ingredientInfo);
		TooltipRenderer.drawHoveringText(poseStack, tooltip, mouseX, mouseY, ingredient, ingredientRenderer);
	}

	public <T> List<Component> getTooltip(T ingredient, IIngredientInfo<T> ingredientInfo) {
		IIngredientRenderer<T> ingredientRenderer = ingredientInfo.getIngredientRenderer();
		IIngredientHelper<T> ingredientHelper = ingredientInfo.getIngredientHelper();
		List<Component> ingredientTooltipSafe = IngredientRenderHelper.getIngredientTooltipSafe(ingredient, ingredientRenderer, ingredientHelper, modIdHelper);
		List<Component> tooltip = new ArrayList<>(ingredientTooltipSafe);

		if (ingredientFilterConfig.getColorSearchMode() != SearchMode.DISABLED) {
			addColorSearchInfoToTooltip(tooltip, ingredient, ingredientInfo);
		}

		if (worldConfig.isEditModeEnabled()) {
			addEditModeInfoToTooltip(tooltip, keyBindings);
		}

		return tooltip;
	}

	private <T> void addColorSearchInfoToTooltip(List<Component> tooltip, T ingredient, IIngredientInfo<T> ingredientInfo) {
		IIngredientHelper<T> ingredientHelper = ingredientInfo.getIngredientHelper();
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

	private static void addEditModeInfoToTooltip(List<Component> tooltip, IInternalKeyMappings keyBindings) {
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
}
