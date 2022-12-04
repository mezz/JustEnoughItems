package mezz.jei.library.color;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.library.config.ColorNameConfig;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ColorHelper implements IColorHelper {
    private final ColorGetter colorGetter;
    private final ColorNameConfig colorNameConfig;

    public ColorHelper(ColorNameConfig colorNameConfig) {
        this.colorGetter = new ColorGetter();
        this.colorNameConfig = colorNameConfig;
    }

    @Override
    public List<Integer> getColors(TextureAtlasSprite textureAtlasSprite, int renderColor, int colorCount) {
        return colorGetter.getColors(textureAtlasSprite, renderColor, colorCount);
    }

    @Override
    public List<Integer> getColors(ItemStack itemStack, int colorCount) {
        return colorGetter.getColors(itemStack, colorCount);
    }

    @Override
    public String getClosestColorName(int color) {
        return colorNameConfig.getClosestColorName(color);
    }
}
