package mezz.jei.test.client;

import mezz.jei.api.gui.buttons.IButtonState;
import mezz.jei.api.gui.buttons.IIconButtonController;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.common.Internal;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.elements.IconButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

/** Checks the sprite vertices emitted by the deferred GUI renderer at each supported test scale. */
public final class SpriteRenderingTest {
	private static final int COLUMNS = 18;

	private SpriteRenderingTest() {
	}

	public static void run(Minecraft client) {
		int originalScale = (int) client.getWindow().getGuiScale();
		try {
			for (int scale : List.of(2, 3, 4)) {
				client.getWindow().setGuiScale(scale);
				GuiRenderState state = new GuiRenderState();
				GuiGraphics graphics = new GuiGraphics(client, state, -1, -1);
				drawButtons(graphics);
				List<BlitRenderState> arrows = new ArrayList<>();
				state.forEachElement(element -> {
					if (element instanceof BlitRenderState blit && blit.x1() - blit.x0() == 9 && blit.y1() - blit.y0() == 7) {
						arrows.add(blit);
					}
				}, GuiRenderState.TraverseRange.ALL);
				if (arrows.size() != 4 * COLUMNS) {
					throw new AssertionError("Expected both arrows in normal and pressed states at GUI scale " + scale + ", got " + arrows.size() + " sprites");
				}
				for (BlitRenderState arrow : arrows) {
					Vector2f topLeft = arrow.pose().transformPosition(arrow.x0(), arrow.y0(), new Vector2f());
					Vector2f bottomRight = arrow.pose().transformPosition(arrow.x1(), arrow.y1(), new Vector2f());
					assertPixelBoundary(topLeft.x * scale, scale);
					assertPixelBoundary(topLeft.y * scale, scale);
					assertPixelBoundary(bottomRight.x * scale, scale);
					assertPixelBoundary(bottomRight.y * scale, scale);
				}
			}
		} finally {
			client.getWindow().setGuiScale(originalScale);
		}
	}

	private static void drawButtons(GuiGraphics graphics) {
		int row = 0;
		var textures = Internal.getTextures();
		for (var icon : List.of(textures.getArrowPrevious(), textures.getArrowNext())) {
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
				for (int column = 0; column < COLUMNS; column++) {
					button.updateBounds(new ImmutableRect2i(8 + column * 24, 8 + row * 24, 20, 20));
					button.draw(graphics, -1, -1, 0);
				}
				row++;
			}
		}
	}

	private static void assertPixelBoundary(float coordinate, int scale) {
		// Allow the float conversion of GUI thirds while rejecting edges halfway through a physical pixel.
		if (Math.abs(coordinate - Math.round(coordinate)) > 2 * Math.ulp(coordinate)) {
			throw new AssertionError("Arrow edge is not on a physical pixel boundary at GUI scale " + scale + ": " + coordinate);
		}
	}
}
