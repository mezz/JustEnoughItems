package mezz.jei.library.render;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.rendering.BatchRenderElement;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.library.render.batch.IItemStackBatchRenderer;
import mezz.jei.library.render.batch.SimpleItemStackBatchRenderer;
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
	private final IItemStackBatchRenderer batchRenderer = new SimpleItemStackBatchRenderer();

	@Override
	public void render(GuiGraphicsExtractor guiGraphics, @Nullable ItemStack ingredient) {
		render(guiGraphics, ingredient, 0, 0);
	}

	@Override
	public void render(GuiGraphicsExtractor guiGraphics, @Nullable ItemStack ingredient, int posX, int posY) {
		if (ingredient != null) {
			Minecraft minecraft = Minecraft.getInstance();
			Font font = getFontRenderer(minecraft, ingredient);
			guiGraphics.fakeItem(ingredient, posX, posY);
			guiGraphics.itemDecorations(font, ingredient, posX, posY);
		}
	}

	@Override
	public void renderBatch(GuiGraphicsExtractor guiGraphics, List<BatchRenderElement<ItemStack>> batchRenderElements) {
		batchRenderer.renderBatch(guiGraphics, this, batchRenderElements);
	}

	@Override
	public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		Item.TooltipContext tooltipContext = Item.TooltipContext.of(minecraft.level);
		return ingredient.getTooltipLines(tooltipContext, player, tooltipFlag);
	}

	@Override
	public Font getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		return renderHelper.getFontRenderer(minecraft, ingredient);
	}

	@Override
	public int getWidth() {
		return 16;
	}

	@Override
	public int getHeight() {
		return 16;
	}
}
