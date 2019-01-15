package mezz.jei.api.ingredients;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.Item;

/**
 * Put this interface on your {@link Item} to skip JEI's render optimizations.
 *
 * This is useful for baked models that use ASM and do not use {@link IBakedModel#isBuiltInRenderer}.
 * If your model does not use ASM it should work fine, please report a bug instead of using this interface.
 *
 * @since JEI 4.7.11
 */
public interface ISlowRenderItem {
}
