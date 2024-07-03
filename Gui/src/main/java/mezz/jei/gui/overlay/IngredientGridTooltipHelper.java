package mezz.jei.gui.overlay;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.core.search.SearchMode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class IngredientGridTooltipHelper {
	private final IIngredientManager ingredientManager;
	private final IIngredientFilterConfig ingredientFilterConfig;
	private final IClientToggleState toggleState;
	private final IModIdHelper modIdHelper;
	private final IInternalKeyMappings keyBindings;
	private final IColorHelper colorHelper;

	public IngredientGridTooltipHelper(
		IIngredientManager ingredientManager,
		IIngredientFilterConfig ingredientFilterConfig,
		IClientToggleState toggleState,
		IModIdHelper modIdHelper,
		IInternalKeyMappings keyBindings,
		IColorHelper colorHelper
	) {
		this.ingredientManager = ingredientManager;
		this.ingredientFilterConfig = ingredientFilterConfig;
		this.toggleState = toggleState;
		this.modIdHelper = modIdHelper;
		this.keyBindings = keyBindings;
		this.colorHelper = colorHelper;
	}

	public <T> List<Component> getIngredientTooltip(
		ITypedIngredient<T> typedIngredient,
		IIngredientRenderer<T> ingredientRenderer,
		IIngredientHelper<T> ingredientHelper
	) {
		List<Component> tooltip = SafeIngredientUtil.getTooltip(ingredientManager, ingredientRenderer, typedIngredient);
		tooltip = modIdHelper.addModNameToIngredientTooltip(tooltip, typedIngredient.getIngredient(), ingredientHelper);

		if (ingredientFilterConfig.getColorSearchMode() != SearchMode.DISABLED) {
			addColorSearchInfoToTooltip(tooltip, typedIngredient, ingredientHelper);
		}

		if (toggleState.isEditModeEnabled()) {
			addEditModeInfoToTooltip(tooltip, keyBindings);
		}

		return tooltip;
	}

	public <T, R> List<Component> getRecipeTooltip(
		IRecipeCategory<T> recipeCategory,
		T recipe,
		ITypedIngredient<R> recipeOutput,
		IIngredientRenderer<R> ingredientRenderer,
		IIngredientHelper<R> ingredientHelper
	) {
		List<Component> tooltip = new ArrayList<>();
		tooltip.add(Component.translatable("jei.tooltip.bookmarks.recipe", recipeCategory.getTitle()));

		ResourceLocation recipeName = recipeCategory.getRegistryName(recipe);
		if (recipeName != null) {
			String recipeModId = recipeName.getNamespace();
			ResourceLocation ingredientName = ingredientHelper.getResourceLocation(recipeOutput.getIngredient());
			String ingredientModId = ingredientName.getNamespace();
			if (!recipeModId.equals(ingredientModId)) {
				String modName = modIdHelper.getFormattedModNameForModId(recipeModId);
				MutableComponent recipeBy = Component.translatable("jei.tooltip.recipe.by", modName);
				tooltip.add(recipeBy.withStyle(ChatFormatting.GRAY));
			}
		}

		tooltip.add(Component.empty());

		List<Component> outputTooltip = SafeIngredientUtil.getTooltip(ingredientManager, ingredientRenderer, recipeOutput);

		tooltip.addAll(outputTooltip);
		tooltip = modIdHelper.addModNameToIngredientTooltip(tooltip, recipeOutput.getIngredient(), ingredientHelper);

		return tooltip;
	}

	private <T> void addColorSearchInfoToTooltip(List<Component> tooltip, ITypedIngredient<T> typedIngredient, IIngredientHelper<T> ingredientHelper) {
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
