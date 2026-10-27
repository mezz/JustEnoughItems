package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.rendering.BatchRenderElement;
import mezz.jei.library.render.ItemStackRenderer;
import net.minecraft.client.Screenshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.function.IntPredicate;

/** Compares count and durability pixels from single and batched ingredient rendering. */
final class ItemBatchRenderingClientGameTest {
	static FabricClientTestRunner.ClientTestCase getTestCase() {
		return new FabricClientTestRunner.ClientTestCase("fabric-client-rendering", "itemDecorations", () -> {
			try (FabricClientTestWorld ignored = FabricClientTestWorld.create()) {
				run();
			}
		});
	}

	private static void run() {
		FabricClientTestContext context = new FabricClientTestContext();
		try {
			context.setScreen(DecorationScreen::new);
			context.takeScreenshot("jei-item-batch-decorations");
			context.runOnClient(client -> {
				try (NativeImage image = Screenshot.takeScreenshot(client.getMainRenderTarget())) {
					int scale = (int) client.getWindow().getGuiScale();
					// Compare decoration pixels in both lighting groups and the custom-rendered chest path.
					assertMatchingPixels(image, scale, 24, 18, 10, ItemBatchRenderingClientGameTest::isWhite, "stack count");
					assertMatchingPixels(image, scale, 61, 16, 2, ItemBatchRenderingClientGameTest::isGreen, "durability bar");
					assertMatchingPixels(image, scale, 88, 18, 10, ItemBatchRenderingClientGameTest::isWhite, "custom-rendered item count");
				}
			});
		} finally {
			context.runOnClient(client -> client.setScreen(null));
		}
	}

	private static void assertMatchingPixels(NativeImage image, int scale, int top, int width, int height, IntPredicate matches, String decoration) {
		int matchingPixels = 0;
		for (int y = top * scale; y < (top + height) * scale; y++) {
			for (int x = 0; x < width * scale; x++) {
				boolean reference = matches.test(image.getPixelRGBA(16 * scale + x, y));
				boolean batch = matches.test(image.getPixelRGBA(80 * scale + x, y));
				if (reference != batch) {
					throw new AssertionError("Batched " + decoration + " differs from single-item rendering at " + x + ", " + y);
				}
				if (reference) {
					matchingPixels++;
				}
			}
		}
		if (matchingPixels < 5) {
			throw new AssertionError("The reference renderer did not draw the expected " + decoration);
		}
	}

	private static boolean isWhite(int pixel) {
		return (pixel & 0xff) > 235 && ((pixel >>> 8) & 0xff) > 235 && ((pixel >>> 16) & 0xff) > 235;
	}

	private static boolean isGreen(int pixel) {
		int green = (pixel >>> 8) & 0xff;
		return green > (pixel & 0xff) + 30 && green > ((pixel >>> 16) & 0xff) + 30;
	}

	private static final class DecorationScreen extends Screen {
		private final ItemStackRenderer renderer = new ItemStackRenderer();
		private final ItemStack blocks = new ItemStack(Items.OAK_PLANKS, 64);
		private final ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
		private final ItemStack chest = new ItemStack(Items.CHEST, 16);

		private DecorationScreen() {
			super(Component.literal("Item decorations"));
			sword.setDamageValue(100);
		}

		@Override
		public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
			fill(poseStack, 0, 0, width, height, 0xff202020);
			renderer.render(poseStack, blocks, 16, 16);
			renderer.render(poseStack, sword, 16, 48);
			renderer.render(poseStack, chest, 16, 80);
			renderer.renderBatch(poseStack, List.of(
				new BatchRenderElement<>(blocks, 80, 16),
				new BatchRenderElement<>(sword, 80, 48),
				new BatchRenderElement<>(chest, 80, 80)
			));
			Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
		}
	}
}
