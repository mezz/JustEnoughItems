package mezz.jei.gui.input;

import mezz.jei.common.input.IGuiInputLayer;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IngredientFocusSourceTest {
	@Test
	public void foregroundInputLayerBlocksObscuredFocusSources() {
		// Setup: an input area that supplies ingredients covers another input area under the mouse pointer.
		TestInputLayer foreground = new TestInputLayer(true);
		TestFocusSource obscured = new TestFocusSource();
		IngredientFocusSource combined = new IngredientFocusSource(new GuiInputScene(
			InputArea.builder("Foreground", foreground).blockUnhandledMouseInput().build(),
			InputArea.builder("Following", obscured).blockUnhandledMouseInput().build()
		));

		// Operation: query the ingredient sources under the front input area.
		combined.getIngredientUnderMouse(10, 10).count();

		// Assertions: only the foreground source participates in the query.
		assertEquals(1, foreground.getIngredientQueries());
		assertEquals(0, obscured.getIngredientQueries());
	}

	@Test
	public void inactiveForegroundInputLayerAllowsFollowingFocusSources() {
		// Setup: the front input area does not cover the mouse pointer.
		TestInputLayer foreground = new TestInputLayer(false);
		TestFocusSource following = new TestFocusSource();
		IngredientFocusSource combined = new IngredientFocusSource(new GuiInputScene(
			InputArea.builder("Foreground", foreground).blockUnhandledMouseInput().build(),
			InputArea.builder("Following", following).blockUnhandledMouseInput().build()
		));

		// Operation: query the ingredient sources outside the front input area.
		combined.getIngredientUnderMouse(10, 10).count();

		// Assertions: only the source under the mouse participates in the query.
		assertEquals(0, foreground.getIngredientQueries());
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
		public boolean isMouseOver(double mouseX, double mouseY) {
			return true;
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
		public void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {

		}

		@Override
		public Optional<IInputInteraction> beginInput(
			Screen screen,
			UserInput input,
			IInternalKeyMappings keyBindings
		) {
			return Optional.empty();
		}
	}
}
