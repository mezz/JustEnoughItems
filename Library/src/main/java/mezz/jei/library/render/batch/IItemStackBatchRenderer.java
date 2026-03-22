package mezz.jei.library.render.batch;

import mezz.jei.api.ingredients.rendering.BatchRenderElement;
import mezz.jei.library.render.ItemStackRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IItemStackBatchRenderer {
	void renderBatch(GuiGraphicsExtractor guiGraphics, ItemStackRenderer itemStackRenderer, List<BatchRenderElement<ItemStack>> elements);
}
