package mezz.jei.gui.overlay;

import com.google.common.base.Joiner;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.Internal;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.color.ColorNamer;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.config.SearchMode;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.ingredients.IngredientInfo;
import mezz.jei.ingredients.RegisteredIngredients;
import mezz.jei.render.IngredientRenderHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public final class IngredientGridTooltipHelper {
	private final RegisteredIngredients registeredIngredients;
	private final IIngredientFilterConfig ingredientFilterConfig;
	private final IWorldConfig worldConfig;
	private final IModIdHelper modIdHelper;

	public IngredientGridTooltipHelper(
		RegisteredIngredients registeredIngredients,
		IIngredientFilterConfig ingredientFilterConfig,
		IWorldConfig worldConfig,
		IModIdHelper modIdHelper
	) {
		this.registeredIngredients = registeredIngredients;
		this.ingredientFilterConfig = ingredientFilterConfig;
		this.worldConfig = worldConfig;
		this.modIdHelper = modIdHelper;
	}

	public <T> void drawTooltip(PoseStack poseStack, int mouseX, int mouseY, ITypedIngredient<T> value) {
		IIngredientType<T> ingredientType = value.getType();
		T ingredient = value.getIngredient();
		IngredientInfo<T> ingredientInfo = registeredIngredients.getIngredientInfo(ingredientType);
		IIngredientRenderer<T> ingredientRenderer = ingredientInfo.getIngredientRenderer();

		List<Component> tooltip = getTooltip(ingredient, ingredientInfo);
		TooltipRenderer.drawHoveringText(poseStack, tooltip, mouseX, mouseY, ingredient, ingredientRenderer);
	}

	public <T> List<Component> getTooltip(T ingredient, IngredientInfo<T> ingredientInfo) {
		IIngredientRenderer<T> ingredientRenderer = ingredientInfo.getIngredientRenderer();
		IIngredientHelper<T> ingredientHelper = ingredientInfo.getIngredientHelper();
		List<Component> ingredientTooltipSafe = IngredientRenderHelper.getIngredientTooltipSafe(ingredient, ingredientRenderer, ingredientHelper, modIdHelper);
		List<Component> tooltip = new ArrayList<>(ingredientTooltipSafe);

		if (ingredientFilterConfig.getColorSearchMode() != SearchMode.DISABLED) {
			addColorSearchInfoToTooltip(tooltip, ingredient, ingredientInfo);
		}

		if (worldConfig.isEditModeEnabled()) {
			addEditModeInfoToTooltip(tooltip);
		}

		return tooltip;
	}

	private static <T> void addColorSearchInfoToTooltip(List<Component> tooltip, T ingredient, IngredientInfo<T> ingredientInfo) {
		ColorNamer colorNamer = Internal.getColorNamer();

		IIngredientHelper<T> ingredientHelper = ingredientInfo.getIngredientHelper();
		Iterable<Integer> colors = ingredientHelper.getColors(ingredient);
		Collection<String> colorNames = colorNamer.getColorNames(colors, false);
		if (!colorNames.isEmpty()) {
			String colorNamesString = Joiner.on(", ").join(colorNames);
			Component colorTranslation = new TranslatableComponent("jei.tooltip.item.colors", colorNamesString)
				.withStyle(ChatFormatting.GRAY);
			tooltip.add(colorTranslation);
		}
	}

	private static void addEditModeInfoToTooltip(List<Component> tooltip) {
		List<Component> lines = List.of(
			TextComponent.EMPTY,
			new TranslatableComponent("gui.jei.editMode.description")
				.withStyle(ChatFormatting.DARK_GREEN),
			new TranslatableComponent(
				"gui.jei.editMode.description.hide",
				KeyBindings.toggleHideIngredient.getTranslatedKeyMessage()
			).withStyle(ChatFormatting.GRAY),
			new TranslatableComponent(
				"gui.jei.editMode.description.hide.wild",
				KeyBindings.toggleWildcardHideIngredient.getTranslatedKeyMessage()
			).withStyle(ChatFormatting.GRAY)
		);
		tooltip.addAll(lines);
	}
}
