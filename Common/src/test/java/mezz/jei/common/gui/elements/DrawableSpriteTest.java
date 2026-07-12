package mezz.jei.common.gui.elements;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DrawableSpriteTest {
	@Test
	public void usesSpriteSizeByDefault() {
		TextureAtlasSprite sprite = createSprite(36, 42);
		DrawableSprite drawable = new DrawableSprite(() -> sprite);

		Assertions.assertEquals(36, drawable.getWidth());
		Assertions.assertEquals(42, drawable.getHeight());
	}

	@Test
	public void usesLogicalSizeWhenSupplied() {
		TextureAtlasSprite sprite = createSprite(36, 42);
		DrawableSprite drawable = new DrawableSprite(() -> sprite, 18, 21);

		Assertions.assertEquals(18, drawable.getWidth());
		Assertions.assertEquals(21, drawable.getHeight());
	}

	@Test
	public void rejectsIncompleteLogicalSize() {
		IllegalArgumentException exception = Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> new DrawableSprite(() -> createSprite(36, 42), 18, 0)
		);

		Assertions.assertTrue(exception.getMessage().contains("DrawableSprite size"));
	}

	private static TextureAtlasSprite createSprite(int width, int height) {
		ResourceLocation name = new ResourceLocation("jei", "test_sprite");
		return new TestTextureAtlasSprite(name, width, height);
	}

	private static class TestTextureAtlasSprite extends TextureAtlasSprite {
		public TestTextureAtlasSprite(ResourceLocation name, int width, int height) {
			super(
				null,
				new TextureAtlasSprite.Info(name, width, height, AnimationMetadataSection.EMPTY),
				0,
				256,
				256,
				0,
				0,
				new NativeImage(width, height, false)
			);
		}
	}
}
