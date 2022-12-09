package mezz.jei.api.helpers;

import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;

/**
 * Helper class for getting colors for sprites for purposes of implementing {@link mezz.jei.api.ingredients.IIngredientHelper#getColors(Object)}.
 * Get an instance from {@link mezz.jei.api.registration.IModIngredientRegistration#getColorHelper()}
 *
 * @since 7.6.3
 */
public interface IColorHelper {

    /**
     * Gets the "main" colors of a given sprite when overlayed with a specific tint color.
     * @param textureAtlasSprite Sprite to extract main colors from.
     * @param renderColor        Overlay/tint color that is applied to the sprite.
     * @param colorCount         Number of "main" colors to get.
     * @return A list of the main ARGB colors for the given sprite when overlayed with a specific tint color.
     */
    List<Integer> getColors(TextureAtlasSprite textureAtlasSprite, int renderColor, int colorCount);

    /**
     * Gets the "main" colors of a given ItemStack.
     * @param itemStack ItemStack to extract main colors from.
     * @param colorCount Number of "main" colors to get.
     * @return A list of the main ARGB colors for the given ItemStack
     *
     * @since 11.5.0
     */
    List<Integer> getColors(ItemStack itemStack, int colorCount);

    /**
     * @param color a color in ARGB format (0xAARRGGBB) Alpha, Red, Green, Blue
     * @return the color name that is closest to the given color, using JEI's color name config file
     *
     * @since 11.5.0
     */
    String getClosestColorName(int color);
}
