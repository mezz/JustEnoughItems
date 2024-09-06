package mezz.jei.library.render.batch;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import mezz.jei.api.ingredients.rendering.BatchRenderElement;
import mezz.jei.library.render.ItemStackRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ItemStackBatchRendererCache {
	private final LoadingCache<List<BatchRenderElement<ItemStack>>, ItemStackBatchRenderer> cache =
		CacheBuilder.newBuilder()
			.maximumSize(6)
			.build(new CacheLoader<>() {
				@Override
				public ItemStackBatchRenderer load(List<BatchRenderElement<ItemStack>> elements) {
					Minecraft minecraft = Minecraft.getInstance();
					return new ItemStackBatchRenderer(minecraft, elements);
				}
			});

	public void renderBatch(GuiGraphics guiGraphics, ItemStackRenderer itemStackRenderer, List<BatchRenderElement<ItemStack>> elements) {
		ItemStackBatchRenderer batchData = cache.getUnchecked(elements);

		Minecraft minecraft = Minecraft.getInstance();
		ItemRenderer itemRenderer = minecraft.getItemRenderer();
		batchData.render(guiGraphics, minecraft, itemRenderer, itemStackRenderer);
	}

}
