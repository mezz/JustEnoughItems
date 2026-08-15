package mezz.jei.neoforge.startup;

import mezz.jei.gui.events.GuiEventHandler;
import mezz.jei.neoforge.events.JeiScreenRenderForegroundEvent;
import mezz.jei.neoforge.events.RuntimeEventSubscriptions;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.joml.Matrix3x2fStack;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class EventRegistrationTest {
	@Test
	public void nonContainerScreenForegroundEventDrawsJeiForeground() throws ReflectiveOperationException {
		// Setup: capture the runtime event listeners registered for a plain screen like RecipesGui.
		Map<Class<?>, Consumer<?>> listeners = new HashMap<>();
		IEventBus eventBus = createRecordingEventBus(listeners);
		RuntimeEventSubscriptions subscriptions = new RuntimeEventSubscriptions(eventBus);
		TestGuiEventHandler guiEventHandler = new TestGuiEventHandler();
		EventRegistration.registerGuiHandler(subscriptions, guiEventHandler);

		Screen screen = allocateWithoutConstructor(TestScreen.class);
		GuiGraphicsExtractor guiGraphics = createGuiGraphics();
		int mouseX = 13;
		int mouseY = 17;

		// Operation: fire the event bridged between screen contents and deferred tooltips.
		Consumer<JeiScreenRenderForegroundEvent> listener = getListener(listeners, JeiScreenRenderForegroundEvent.class);
		listener.accept(new JeiScreenRenderForegroundEvent(screen, guiGraphics, mouseX, mouseY));

		// Assertions: JEI's foreground path receives the original render context.
		assertEquals(1, guiEventHandler.foregroundDrawCount);
		assertSame(screen, guiEventHandler.screen);
		assertSame(guiGraphics, guiEventHandler.guiGraphics);
		assertEquals(mouseX, guiEventHandler.mouseX);
		assertEquals(mouseY, guiEventHandler.mouseY);
		assertFalse(listeners.containsKey(ScreenEvent.Render.Post.class));
	}

	private static IEventBus createRecordingEventBus(Map<Class<?>, Consumer<?>> listeners) {
		return (IEventBus) Proxy.newProxyInstance(
			IEventBus.class.getClassLoader(),
			new Class<?>[]{IEventBus.class},
			(proxy, method, args) -> {
				if (method.getName().equals("addListener") && args != null && args.length == 4) {
					listeners.put((Class<?>) args[2], (Consumer<?>) args[3]);
				}
				return null;
			}
		);
	}

	private static <T> T allocateWithoutConstructor(Class<T> type) throws ReflectiveOperationException {
		Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
		Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
		unsafeField.setAccessible(true);
		Object unsafe = unsafeField.get(null);
		Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
		return type.cast(allocateInstance.invoke(unsafe, type));
	}

	private static GuiGraphicsExtractor createGuiGraphics() throws ReflectiveOperationException {
		GuiGraphicsExtractor guiGraphics = allocateWithoutConstructor(GuiGraphicsExtractor.class);
		setField(guiGraphics, "pose", new Matrix3x2fStack(16));
		setField(guiGraphics, "guiRenderState", new GuiRenderState());
		return guiGraphics;
	}

	private static void setField(Object instance, String name, Object value) throws ReflectiveOperationException {
		Field field = instance.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(instance, value);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Event> Consumer<T> getListener(Map<Class<?>, Consumer<?>> listeners, Class<T> eventType) {
		Consumer<?> listener = listeners.get(eventType);
		assertNotNull(listener, () -> "Missing listener for " + eventType.getName());
		return (Consumer<T>) listener;
	}

	private static class TestScreen extends Screen {
		private TestScreen() {
			super(Component.empty());
		}
	}

	private static class TestGuiEventHandler extends GuiEventHandler {
		private int foregroundDrawCount;
		private Screen screen;
		private GuiGraphicsExtractor guiGraphics;
		private int mouseX;
		private int mouseY;

		private TestGuiEventHandler() {
			super(null, null, null);
		}

		@Override
		public void drawForScreenForeground(Screen screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
			this.foregroundDrawCount++;
			this.screen = screen;
			this.guiGraphics = guiGraphics;
			this.mouseX = mouseX;
			this.mouseY = mouseY;
		}
	}
}
