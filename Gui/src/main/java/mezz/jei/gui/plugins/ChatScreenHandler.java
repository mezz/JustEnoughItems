package mezz.jei.gui.plugins;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.chat.JeiChatItemLinkHover;
import mezz.jei.common.chat.JeiChatItemLinks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class ChatScreenHandler implements IGlobalGuiHandler {
	private final IIngredientManager ingredientManager;

	public ChatScreenHandler(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}

	@Override
	public Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(
		IClickableIngredientFactory factory,
		double mouseX,
		double mouseY
	) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!(minecraft.screen instanceof ChatScreen chatScreen)) {
			return Optional.empty();
		}
		return JeiChatItemLinkHover.getHoveredText(chatScreen, mouseX, mouseY)
			.flatMap(hoveredText -> getIngredient(hoveredText.style())
				.flatMap(typedIngredient -> factory.createBuilder(typedIngredient)
					.buildWithArea(hoveredText.area())));
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
		if (hoverEvent == null) {
			return Optional.empty();
		}
		HoverEvent.ItemStackInfo itemStackInfo = hoverEvent.getValue(HoverEvent.Action.SHOW_ITEM);
		if (itemStackInfo == null) {
			return Optional.empty();
		}
		ItemStack itemStack = itemStackInfo.getItemStack();
		return ingredientManager.createTypedIngredient(
			VanillaTypes.ITEM_STACK,
			itemStack
		);
	}

}
