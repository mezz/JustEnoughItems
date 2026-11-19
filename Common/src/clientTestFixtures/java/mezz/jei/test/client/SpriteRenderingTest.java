package mezz.jei.test.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import mezz.jei.api.gui.buttons.IButtonState;
import mezz.jei.api.gui.buttons.IIconButtonController;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.common.gui.elements.DrawableSprite;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.elements.IconButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/** Checks arrow pixels against neighboring atlas colors through the real GUI renderer. */
public final class SpriteRenderingTest {
	private static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath("jei", "test/arrow_atlas");
	private static final List<ResourceLocation> ARROWS = List.of(
		ResourceLocation.fromNamespaceAndPath("jei", "icons/arrow_previous_v2"),
		ResourceLocation.fromNamespaceAndPath("jei", "icons/arrow_next_v2")
	);
	private static final int WIDTH = 1920;
	private static final int HEIGHT = 1080;
	private static final int NEIGHBOR_COLOR = 0xFFFF00FF;
	private static final int COLUMNS = 18;
	private static final double[] SUBPIXEL_OFFSETS = {-1.0 / 16384, 0, 1.0 / 16384};

	private SpriteRenderingTest() {
	}

	public static void run(Minecraft client) {
		RenderSystem.assertOnRenderThread();
		Matrix4f projection = new Matrix4f(RenderSystem.getProjectionMatrix());
		VertexSorting sorting = RenderSystem.getVertexSorting();
		double guiScale = client.getWindow().getGuiScale();
		RenderTarget target = new TextureTarget(WIDTH, HEIGHT, true, Minecraft.ON_OSX);
		var modelView = RenderSystem.getModelViewStack();
		modelView.pushMatrix();
		try {
			modelView.identity();
			modelView.translate(0, 0, -2000);
			RenderSystem.applyModelViewMatrix();
			for (int layout = 0; layout < 2; layout++) {
				TextureAtlas atlas = createAtlas(client, layout);
				try {
					for (int scale : List.of(2, 3, 4)) {
						client.getWindow().setGuiScale(scale);
						RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(
							0, WIDTH / (float) scale, HEIGHT / (float) scale, 0, 1000, 3000
						), VertexSorting.ORTHOGRAPHIC_Z);
						target.setClearColor(0, 0, 0, 1);
						target.clear(Minecraft.ON_OSX);
						target.bindWrite(true);
						GuiGraphics graphics = new GuiGraphics(client, client.renderBuffers().bufferSource());
						drawButtons(graphics, atlas);
						graphics.flush();
						try (NativeImage image = Screenshot.takeScreenshot(target)) {
							saveScreenshot(client, image, scale, layout);
							assertPixels(image, scale, layout);
						}
					}
				} finally {
					atlas.clearTextureData();
					client.getTextureManager().release(ATLAS);
				}
			}
		} finally {
			client.getWindow().setGuiScale(guiScale);
			modelView.popMatrix();
			RenderSystem.applyModelViewMatrix();
			RenderSystem.setProjectionMatrix(projection, sorting);
			target.destroyBuffers();
			client.getMainRenderTarget().bindWrite(true);
		}
	}

	private static TextureAtlas createAtlas(Minecraft client, int layout) {
		TextureAtlas atlas = new TextureAtlas(ATLAS);
		client.getTextureManager().register(ATLAS, atlas);
		List<SpriteContents> contents = new ArrayList<>();
		contents.add(MissingTextureAtlasSprite.create());
		for (ResourceLocation arrow : ARROWS) {
			ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(arrow.getNamespace(), "textures/gui/sprites/" + arrow.getPath() + ".png");
			try (var stream = client.getResourceManager().open(texture)) {
				NativeImage image = NativeImage.read(stream);
				contents.add(new SpriteContents(arrow, new FrameSize(image.getWidth(), image.getHeight()), image, ResourceMetadata.EMPTY));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		int fillerSize;
		int atlasSize;
		if (layout == 0) {
			fillerSize = 128;
			atlasSize = 256;
		} else {
			fillerSize = 97;
			atlasSize = 512;
		}
		NativeImage filler = new NativeImage(fillerSize, fillerSize, false);
		filler.fillRect(0, 0, fillerSize, fillerSize, NEIGHBOR_COLOR);
		contents.add(new SpriteContents(ResourceLocation.fromNamespaceAndPath("jei", "test/neighbor"), new FrameSize(fillerSize, fillerSize), filler, ResourceMetadata.EMPTY));
		var preparations = new SpriteLoader(ATLAS, 1024, atlasSize, atlasSize).stitch(contents, 0, Runnable::run);
		atlas.upload(preparations);
		atlas.setFilter(false, false);
		// Fill unused texels too so every exposed arrow edge has a detectable neighbor.
		try (NativeImage neighbors = new NativeImage(preparations.width(), preparations.height(), false)) {
			neighbors.fillRect(0, 0, neighbors.getWidth(), neighbors.getHeight(), NEIGHBOR_COLOR);
			neighbors.upload(0, 0, 0, false);
		}
		preparations.regions().values().forEach(sprite -> sprite.uploadFirstFrame());
		return atlas;
	}

	private static void drawButtons(GuiGraphics graphics, TextureAtlas atlas) {
		int row = 0;
		for (ResourceLocation arrow : ARROWS) {
			DrawableSprite icon = new DrawableSprite(atlas, arrow, 9, 7);
			IconButton button = new IconButton(new IIconButtonController() {
				@Override
				public boolean onPress(IJeiUserInput input) {
					return false;
				}

				@Override
				public void initState(IButtonState state) {
					state.setIcon(icon);
				}
			});
			for (boolean pressed : List.of(false, true)) {
				button.setForcePressed(pressed);
				for (double offset : SUBPIXEL_OFFSETS) {
					for (int column = 0; column < COLUMNS; column++) {
						button.updateBounds(new ImmutableRect2i(8 + column * 24, 8 + row * 20, 20, 20));
						graphics.pose().pushPose();
						// Cover rounding on either side of the half-pixel position of a centered arrow.
						graphics.pose().translate(offset, offset, 0);
						button.draw(graphics, -1, -1, 0);
						graphics.pose().popPose();
					}
					row++;
				}
			}
		}
	}

	private static void assertPixels(NativeImage image, int scale, int layout) {
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int color = image.getPixelRGBA(x, y);
				int red = color & 0xFF;
				int green = color >> 8 & 0xFF;
				int blue = color >> 16 & 0xFF;
				if (red > 128 && blue > 128 && green < 64) {
					throw new AssertionError("Arrow sampled a neighboring sprite at GUI scale " + scale + ", atlas layout " + layout + ", pixel " + x + ", " + y);
				}
			}
		}
		for (int row = 0; row < 12; row++) {
			for (int column = 0; column < COLUMNS; column++) {
				int color = image.getPixelRGBA((18 + column * 24) * scale, (18 + row * 20) * scale);
				if ((color & 0xFF) < 160 || (color >> 8 & 0xFF) < 160 || (color >> 16 & 0xFF) < 160) {
					throw new AssertionError("Missing arrow at GUI scale " + scale + ", atlas layout " + layout + ", button " + column + ", " + row);
				}
			}
		}
	}

	private static void saveScreenshot(Minecraft client, NativeImage image, int scale, int layout) {
		var path = client.gameDirectory.toPath().resolve("screenshots/jei-arrows-scale-" + scale + "-layout-" + layout + ".png");
		try {
			Files.createDirectories(path.getParent());
			image.writeToFile(path);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
