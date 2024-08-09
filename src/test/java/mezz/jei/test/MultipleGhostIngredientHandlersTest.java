package mezz.jei.test;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.load.registration.GuiHandlerRegistration;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class MultipleGhostIngredientHandlersTest {
	@Test
	@SuppressWarnings("unchecked")
	public void keepsMultipleHandlersRegisteredForOneScreen() {
		IGhostIngredientHandler<TestScreen> first = mock(IGhostIngredientHandler.class);
		IGhostIngredientHandler<TestScreen> second = mock(IGhostIngredientHandler.class);
		GuiHandlerRegistration registration = new GuiHandlerRegistration();
		registration.addGhostIngredientHandler(TestScreen.class, first);
		registration.addGhostIngredientHandler(TestScreen.class, second);

		GuiScreenHelper screenHelper = registration.createGuiScreenHelper(mock(IngredientManager.class));

		assertEquals(Arrays.asList(first, second), screenHelper.getGhostIngredientHandlers(new TestScreen()));
	}

	private static class TestScreen extends Screen {
		private TestScreen() {
			super(new StringTextComponent("test"));
		}
	}
}
