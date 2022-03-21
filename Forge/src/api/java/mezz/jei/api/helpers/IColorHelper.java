package mezz.jei.api.helpers;

import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

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
     * @return A list of the main colors for the given sprite when overlayed with a specific tint color.
     */
    List<Integer> getColors(TextureAtlasSprite textureAtlasSprite, int renderColor, int colorCount);
}
