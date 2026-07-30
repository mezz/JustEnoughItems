package mezz.jei.gui.plugins;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.handlers.IScreenHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.chat.JeiChatItemLinkHover;
import mezz.jei.common.chat.JeiChatItemLinks;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class ChatScreenHandler implements IScreenHandler<ChatScreen> {
	private final IIngredientManager ingredientManager;

	public ChatScreenHandler(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}

	@Override
	@Nullable
	public IGuiProperties apply(ChatScreen chatScreen) {
		return null;
	}

	@Override
	public Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(
		IClickableIngredientFactory factory,
		ChatScreen chatScreen,
		double mouseX,
		double mouseY
	) {
		return JeiChatItemLinkHover.getHoveredText(chatScreen, mouseX, mouseY)
			.flatMap(hoveredText -> {
				return getIngredient(hoveredText.style())
					.flatMap(typedIngredient -> factory.createBuilder(typedIngredient)
						.buildWithArea(hoveredText.area()));
			});
	}

	private Optional<ITypedIngredient<?>> getIngredient(Style style) {
		return getJeiChatLinkIngredient(style)
			.or(() -> getVanillaChatItemIngredient(style));
	}

	private Optional<ITypedIngredient<?>> getJeiChatLinkIngredient(Style style) {
		return JeiChatItemLinkHover.getIngredientLink(style)
			.flatMap(link -> JeiChatItemLinks.resolveTypedIngredient(link, ingredientManager));
	}

	private Optional<ITypedIngredient<ItemStack>> getVanillaChatItemIngredient(Style style) {
		HoverEvent hoverEvent = style.getHoverEvent();
		if (hoverEvent instanceof HoverEvent.ShowItem(ItemStackTemplate item)) {
			ItemStack itemStack = item.create();
			return ingredientManager.createTypedIngredient(
				VanillaTypes.ITEM_STACK,
				itemStack,
				false
			);
		}
		return Optional.empty();
	}

}
