package mezz.jei.gui.input;

import mezz.jei.common.input.IInternalKeyMappings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CombinedRecipeFocusSourceTest {
	@Test
	public void foregroundInputLayerBlocksObscuredFocusSources() {
		// Setup: a foreground focus source covers another focus source at the mouse position.
		TestInputLayer foreground = new TestInputLayer(true);
		TestFocusSource obscured = new TestFocusSource();
		CombinedRecipeFocusSource combined = new CombinedRecipeFocusSource(foreground, obscured);

		// Operation: query the focus sources under the foreground layer.
		combined.getIngredientUnderMouse(10, 10).count();

		// Assertions: only the foreground source participates in the query.
		assertEquals(1, foreground.getIngredientQueries());
		assertEquals(0, obscured.getIngredientQueries());
	}

	@Test
	public void inactiveForegroundInputLayerAllowsFollowingFocusSources() {
		// Setup: a foreground focus source does not cover the mouse position.
		TestInputLayer foreground = new TestInputLayer(false);
		TestFocusSource following = new TestFocusSource();
		CombinedRecipeFocusSource combined = new CombinedRecipeFocusSource(foreground, following);

		// Operation: query the focus sources outside the foreground layer.
		combined.getIngredientUnderMouse(10, 10).count();

		// Assertions: the query continues through both sources.
		assertEquals(1, foreground.getIngredientQueries());
		assertEquals(1, following.getIngredientQueries());
	}

	private static class TestFocusSource implements IRecipeFocusSource {
		private int ingredientQueries;

		@Override
		public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
			ingredientQueries++;
			return Stream.empty();
		}

		@Override
		public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
			return Stream.empty();
		}

		public int getIngredientQueries() {
			return ingredientQueries;
		}
	}

	private static class TestInputLayer extends TestFocusSource implements IGuiInputLayer {
		private final boolean mouseOver;

		private TestInputLayer(boolean mouseOver) {
			this.mouseOver = mouseOver;
		}

		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			return mouseOver;
		}

		@Override
		public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY) {

		}

		@Override
		public Optional<IUserInputHandler> handleUserInput(
			Screen screen,
			UserInput input,
			IInternalKeyMappings keyBindings
		) {
			return Optional.empty();
		}
	}
}
