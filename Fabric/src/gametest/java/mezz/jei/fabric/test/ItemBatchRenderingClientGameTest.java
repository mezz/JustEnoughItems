package mezz.jei.fabric.test;

import mezz.jei.api.ingredients.rendering.BatchRenderElement;
import mezz.jei.library.render.ItemStackRenderer;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.ColoredRectangleRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/** Verifies that batch-rendered ingredients retain their count and durability decorations. */
@SuppressWarnings("UnstableApiUsage")
public class ItemBatchRenderingClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		JUnitXmlTestReporter.runAndReport("fabric-client-gametest", getClass().getSimpleName(), () -> {
			try (TestSingleplayerContext ignored = context.worldBuilder().create()) {
				context.runOnClient(client -> {
					ItemStack blocks = new ItemStack(Items.OAK_PLANKS, 64);
					ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
					sword.setDamageValue(100);
					GuiRenderState state = new GuiRenderState();
					GuiGraphics graphics = new GuiGraphics(client, state, 0, 0);

					new ItemStackRenderer().renderBatch(graphics, List.of(
						new BatchRenderElement<>(blocks, 16, 16),
						new BatchRenderElement<>(sword, 40, 16)
					));

					// Inspect the actual render output, including the decorations missing from the old batch path.
					List<String> labels = new ArrayList<>();
					state.forEachText(text -> {
						StringBuilder label = new StringBuilder();
						text.text.accept((index, style, codePoint) -> {
							label.appendCodePoint(codePoint);
							return true;
						});
						labels.add(label.toString());
					});
					if (!labels.equals(List.of("64"))) {
						throw new AssertionError("Expected the stacked ingredient to render its count, got " + labels);
					}
					List<ColoredRectangleRenderState> bars = new ArrayList<>();
					state.forEachElement(element -> {
						if (element instanceof ColoredRectangleRenderState rectangle && Math.min(rectangle.x0(), rectangle.x1()) == 42 && Math.min(rectangle.y0(), rectangle.y1()) == 29) {
							bars.add(rectangle);
						}
					}, GuiRenderState.TraverseRange.ALL);
					if (bars.size() != 2 || bars.stream().noneMatch(bar -> Math.abs(bar.x1() - bar.x0()) == sword.getBarWidth() && Math.abs(bar.y1() - bar.y0()) == 1)) {
						throw new AssertionError("Expected the damaged ingredient to render its durability bar and background.");
					}
				});
			}
		});
	}
}
