package mezz.jei.test;

import mezz.jei.api.helpers.IColorHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public enum DummyColorHelper implements IColorHelper {
	INSTANCE;

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
