package mezz.jei.api.ingredients;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.Item;

/**
 * Put this interface on your {@link Item} to skip JEI's render optimizations.
 *
 * This is useful for baked models that use ASM and do not use {@link BakedModel#isCustomRenderer}.
 * If your model does not use ASM it should work fine, please report a bug instead of using this interface.
 */
public interface ISlowRenderItem {
}
