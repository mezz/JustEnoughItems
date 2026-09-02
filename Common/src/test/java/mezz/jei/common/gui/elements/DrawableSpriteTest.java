package mezz.jei.common.gui.elements;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

public class DrawableSpriteTest {
	@Test
	public void usesSpriteSizeByDefault() {
		TextureAtlasSprite sprite = createSprite(36, 42);
		DrawableSprite drawable = new DrawableSprite(() -> sprite);

		Assertions.assertEquals(36, drawable.getWidth());
		Assertions.assertEquals(42, drawable.getHeight());
	}

	@Test
	public void usesLogicalSizeWithoutResolvingSprite() {
		DrawableSprite drawable = new DrawableSprite(
			() -> {
				throw new AssertionError("Sprite must not be resolved when the logical size is supplied");
			},
			18,
			21
		);

		Assertions.assertEquals(18, drawable.getWidth());
		Assertions.assertEquals(21, drawable.getHeight());
	}

	@Test
	public void refreshesSpriteFromSupplier() {
		AtomicReference<TextureAtlasSprite> sprite = new AtomicReference<>(createSprite(18, 21));
		DrawableSprite drawable = new DrawableSprite(sprite::get);

		Assertions.assertEquals(18, drawable.getWidth());
		Assertions.assertEquals(21, drawable.getHeight());

		sprite.set(createSprite(36, 42));

		Assertions.assertEquals(36, drawable.getWidth());
		Assertions.assertEquals(42, drawable.getHeight());
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
		ResourceLocation name = ResourceLocation.fromNamespaceAndPath("jei", "test_sprite");
		NativeImage image = new NativeImage(width, height, false);
		SpriteContents contents = new SpriteContents(name, new FrameSize(width, height), image, ResourceMetadata.EMPTY);
		return new TestTextureAtlasSprite(contents);
	}

	private static class TestTextureAtlasSprite extends TextureAtlasSprite {
		public TestTextureAtlasSprite(SpriteContents contents) {
			super(ResourceLocation.fromNamespaceAndPath("jei", "test_atlas"), contents, 256, 256, 0, 0);
		}
	}
}
