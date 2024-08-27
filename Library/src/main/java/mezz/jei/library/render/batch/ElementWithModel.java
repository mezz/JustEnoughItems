package mezz.jei.library.render.batch;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;

public record ElementWithModel(BakedModel model, ItemStack stack, int x, int y) {
}
