package mezz.jei.gui.chat;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import mezz.jei.common.chat.JeiChatItemLinkHover;
import mezz.jei.common.chat.JeiChatItemLinks;
import mezz.jei.common.chat.JeiChatItemLinks.IngredientLink;
import mezz.jei.common.gui.IngredientTooltipComponent;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.util.SafeIngredientUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class ChatIngredientTooltip {
	private ChatIngredientTooltip() {
	}

	public record IngredientTooltipData<T>(
		ITypedIngredient<T> typedIngredient,
		IIngredientRenderer<T> ingredientRenderer,
		IIngredientManager ingredientManager,
		JeiTooltip tooltip
	) {
		public void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
			tooltip.draw(guiGraphics, mouseX, mouseY, typedIngredient, ingredientRenderer, ingredientManager);
		}
	}

	public static boolean setTooltipForHoveredText(
		GuiGraphicsExtractor guiGraphics,
		@Nullable Style hoveredStyle,
		int mouseX,
		int mouseY
	) {
		Optional<IngredientTooltipData<?>> optionalTooltipData = getTooltipForHoveredText(hoveredStyle);
		if (optionalTooltipData.isEmpty()) {
			return false;
		}

		IngredientTooltipData<?> tooltipData = optionalTooltipData.get();
		tooltipData.draw(guiGraphics, mouseX, mouseY);
		return true;
	}

	public static Optional<IngredientTooltipData<?>> getTooltipForHoveredChatLink(@Nullable Screen screen, double mouseX, double mouseY) {
		if (screen == null) {
			return Optional.empty();
		}
		return JeiChatItemLinkHover.getHoveredStyle(screen, mouseX, mouseY)
			.flatMap(ChatIngredientTooltip::getTooltipForHoveredText);
	}

	public static Optional<IngredientTooltipData<?>> getTooltipForHoveredText(@Nullable Style hoveredStyle) {
		Optional<IngredientLink> optionalLink = JeiChatItemLinkHover.getIngredientLink(hoveredStyle);
		if (optionalLink.isEmpty()) {
			return Optional.empty();
		}

		Optional<IJeiRuntime> optionalRuntime = Internal.getOptionalJeiRuntime();
		if (optionalRuntime.isEmpty()) {
			return Optional.empty();
		}

		IJeiRuntime jeiRuntime = optionalRuntime.get();
		IIngredientManager ingredientManager = jeiRuntime.getIngredientManager();
		IngredientLink link = optionalLink.get();
		Optional<ITypedIngredient<?>> optionalTypedIngredient = JeiChatItemLinks.resolveTypedIngredient(link, ingredientManager);
		if (optionalTypedIngredient.isEmpty()) {
			return Optional.empty();
		}

		ITypedIngredient<?> typedIngredient = optionalTypedIngredient.get();
		IngredientTooltipData<?> tooltipData = createTooltipData(typedIngredient, ingredientManager);
		return Optional.of(tooltipData);
	}

	private static <T> IngredientTooltipData<T> createTooltipData(
		ITypedIngredient<T> typedIngredient,
		IIngredientManager ingredientManager
	) {
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(typedIngredient.getType());
		JeiTooltip tooltip = new JeiTooltip();
		tooltip.add(new IngredientTooltipComponent<>(typedIngredient, ingredientRenderer));
		SafeIngredientUtil.getRichTooltip(tooltip, ingredientManager, ingredientRenderer, typedIngredient);
		return new IngredientTooltipData<>(typedIngredient, ingredientRenderer, ingredientManager, tooltip);
	}
}
