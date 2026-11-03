package mezz.jei.library.render;

import mezz.jei.api.ingredients.rendering.IIngredientRenderContext;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ItemStackRenderer implements IIngredientRenderer<ItemStack> {
	@Override
	@Deprecated(since = "31.18.0", forRemoval = true)
	public void render(GuiGraphicsExtractor guiGraphics, @Nullable ItemStack ingredient) {
		render(guiGraphics, ingredient, 0, 0, IIngredientRenderContext.INGREDIENT);
	}

	@Override
	@Deprecated(since = "31.18.0", forRemoval = true)
	public void render(GuiGraphicsExtractor guiGraphics, @Nullable ItemStack ingredient, int posX, int posY) {
		render(guiGraphics, ingredient, posX, posY, IIngredientRenderContext.INGREDIENT);
	}

	@Override
	public void render(GuiGraphicsExtractor guiGraphics, @Nullable ItemStack ingredient, int posX, int posY, IIngredientRenderContext context) {
		if (ingredient != null) {
			guiGraphics.fakeItem(ingredient, posX, posY);
		}
	}

	@Override
	@Deprecated(since = "30.26.0", forRemoval = true)
	@SuppressWarnings("removal")
	public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
		Minecraft minecraft = Minecraft.getInstance();
		Item.TooltipContext tooltipContext = Item.TooltipContext.of(minecraft.level);
		return getTooltip(ingredient, tooltipContext, minecraft.player, tooltipFlag);
	}

	@Override
	@Deprecated(since = "30.26.0", forRemoval = true)
	@SuppressWarnings("removal")
	public void getTooltip(ITooltipBuilder tooltip, ItemStack ingredient, TooltipFlag tooltipFlag) {
		Minecraft minecraft = Minecraft.getInstance();
		Item.TooltipContext tooltipContext = Item.TooltipContext.of(minecraft.level);
		getTooltip(tooltip, ingredient, tooltipContext, minecraft.player, tooltipFlag);
	}

	@Override
	public List<Component> getTooltip(ItemStack ingredient, Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
		return ingredient.getTooltipLines(tooltipContext, player, tooltipFlag);
	}

	@Override
	public Font getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		return renderHelper.getFontRenderer(minecraft, ingredient);
	}

}
