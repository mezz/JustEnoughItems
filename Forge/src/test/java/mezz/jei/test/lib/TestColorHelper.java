package mezz.jei.test.lib;

import mezz.jei.api.helpers.IColorHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class TestColorHelper implements IColorHelper {
    @Override
    public List<Integer> getColors(TextureAtlasSprite textureAtlasSprite, int renderColor, int colorCount) {
        return List.of();
    }

    @Override
    public List<Integer> getColors(ItemStack itemStack, int colorCount) {
        return List.of();
    }

    @Override
    public String getClosestColorName(int color) {
        return "";
    }
}
