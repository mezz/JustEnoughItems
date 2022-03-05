package mezz.jei.api.ingredients;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.Item;

/**
 * Put this interface on your {@link Item} to skip JEI's render optimizations.
 *
 * This is useful for baked models that use ASM and do not use {@link BakedModel#isCustomRenderer}.
 * If your model does not use ASM it should work fine, please report a bug instead of using this interface.
 *
 * @deprecated Rendering optimizations have been completely removed from JEI, so this is no longer necessary.
 */
@Deprecated(forRemoval = true, since = "9.4.3")
public interface ISlowRenderItem {
}
