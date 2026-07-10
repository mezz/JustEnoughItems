package mezz.jei.test;

import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.handlers.IScreenHandler;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.common.collect.ListMultiMap;
import mezz.jei.common.ingredients.ITypedIngredientFactory;
import mezz.jei.library.gui.GuiContainerHandlers;
import mezz.jei.library.gui.helpers.ScreenHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class ScreenHelperTest {
	private static final ITypedIngredientFactory TYPED_INGREDIENT_FACTORY = new ITypedIngredientFactory() {
		@Override
		public <T> Optional<ITypedIngredient<T>> createTypedIngredient(IIngredientType<T> ingredientType, T ingredient, boolean normalize) {
			return Optional.empty();
		}
	};

	@Test
	public void exactScreenHandlerReturnsGuiPropertiesBeforeAssignableFallback() {
		// Setup: both exact and assignable handlers match, and both return properties.
		TestGuiProperties baseProperties = new TestGuiProperties(BaseScreen.class);
		TestGuiProperties exactProperties = new TestGuiProperties(TestScreen.class);
		TestScreenHandler<BaseScreen> baseHandler = new TestScreenHandler<>(baseProperties, null);
		TestScreenHandler<TestScreen> exactHandler = new TestScreenHandler<>(exactProperties, null);
		Map<Class<?>, IScreenHandler<?>> screenHandlers = new LinkedHashMap<>();
		screenHandlers.put(BaseScreen.class, baseHandler);
		screenHandlers.put(TestScreen.class, exactHandler);
		ScreenHelper screenHelper = createScreenHelper(screenHandlers);

		// Operation: look up properties for a screen with an exact registered handler.
		Optional<IGuiProperties> result = screenHelper.getGuiProperties(new TestScreen());

		// Assertions: the exact handler is preferred over assignable handlers.
		Assertions.assertSame(exactProperties, result.orElseThrow());
		Assertions.assertEquals(1, exactHandler.getGuiPropertiesCalls());
		Assertions.assertEquals(0, baseHandler.getGuiPropertiesCalls());
	}

	@Test
	public void exactScreenHandlerReturningNullFallsBackToAssignableHandler() {
		// Setup: an exact handler returns null, and an assignable fallback would return properties.
		TestGuiProperties baseProperties = new TestGuiProperties(BaseScreen.class);
		TestScreenHandler<BaseScreen> baseHandler = new TestScreenHandler<>(baseProperties, null);
		TestScreenHandler<TestScreen> exactHandler = new TestScreenHandler<>(null, null);
		Map<Class<?>, IScreenHandler<?>> screenHandlers = new LinkedHashMap<>();
		screenHandlers.put(BaseScreen.class, baseHandler);
		screenHandlers.put(TestScreen.class, exactHandler);
		ScreenHelper screenHelper = createScreenHelper(screenHandlers);

		// Operation: look up properties for a screen with an exact registered handler.
		Optional<IGuiProperties> result = screenHelper.getGuiProperties(new TestScreen());

		// Assertions: the exact handler is tried first, then lookup falls back to the assignable handler.
		Assertions.assertSame(baseProperties, result.orElseThrow());
		Assertions.assertEquals(1, exactHandler.getGuiPropertiesCalls());
		Assertions.assertEquals(1, baseHandler.getGuiPropertiesCalls());
	}

	@Test
	public void closestAssignableScreenHandlerReturnsGuiProperties() {
		// Setup: two assignable handlers match, and both return properties.
		TestGuiProperties baseProperties = new TestGuiProperties(BaseScreen.class);
		TestGuiProperties intermediateProperties = new TestGuiProperties(IntermediateScreen.class);
		TestScreenHandler<BaseScreen> baseHandler = new TestScreenHandler<>(baseProperties, null);
		TestScreenHandler<IntermediateScreen> intermediateHandler = new TestScreenHandler<>(intermediateProperties, null);
		Map<Class<?>, IScreenHandler<?>> screenHandlers = new LinkedHashMap<>();
		screenHandlers.put(BaseScreen.class, baseHandler);
		screenHandlers.put(IntermediateScreen.class, intermediateHandler);
		ScreenHelper screenHelper = createScreenHelper(screenHandlers);

		// Operation: look up properties for a screen with no exact handler.
		Optional<IGuiProperties> result = screenHelper.getGuiProperties(new TestScreen());

		// Assertions: lookup uses the closest assignable handler, regardless of registration order.
		Assertions.assertSame(intermediateProperties, result.orElseThrow());
		Assertions.assertEquals(0, baseHandler.getGuiPropertiesCalls());
		Assertions.assertEquals(1, intermediateHandler.getGuiPropertiesCalls());
	}

	@Test
	public void closestAssignableScreenHandlerReturningNullFallsBackToLessSpecificHandler() {
		// Setup: two assignable handlers match, and the closest one returns null.
		TestGuiProperties baseProperties = new TestGuiProperties(BaseScreen.class);
		TestScreenHandler<BaseScreen> baseHandler = new TestScreenHandler<>(baseProperties, null);
		TestScreenHandler<IntermediateScreen> intermediateHandler = new TestScreenHandler<>(null, null);
		Map<Class<?>, IScreenHandler<?>> screenHandlers = new LinkedHashMap<>();
		screenHandlers.put(BaseScreen.class, baseHandler);
		screenHandlers.put(IntermediateScreen.class, intermediateHandler);
		ScreenHelper screenHelper = createScreenHelper(screenHandlers);

		// Operation: look up properties for a screen with no exact handler.
		Optional<IGuiProperties> result = screenHelper.getGuiProperties(new TestScreen());

		// Assertions: lookup falls through from the closest handler to the next-best handler.
		Assertions.assertSame(baseProperties, result.orElseThrow());
		Assertions.assertEquals(1, baseHandler.getGuiPropertiesCalls());
		Assertions.assertEquals(1, intermediateHandler.getGuiPropertiesCalls());
	}

	@Test
	public void unmatchedScreenHandlerReturnsEmptyGuiProperties() {
		// Setup: a registered handler does not match the queried screen class.
		TestGuiProperties unrelatedProperties = new TestGuiProperties(UnrelatedScreen.class);
		TestScreenHandler<UnrelatedScreen> unrelatedHandler = new TestScreenHandler<>(unrelatedProperties, null);
		Map<Class<?>, IScreenHandler<?>> screenHandlers = new LinkedHashMap<>();
		screenHandlers.put(UnrelatedScreen.class, unrelatedHandler);
		ScreenHelper screenHelper = createScreenHelper(screenHandlers);

		// Operation: look up properties for a screen with no matching handler.
		Optional<IGuiProperties> result = screenHelper.getGuiProperties(new TestScreen());

		// Assertions: no handler is called when none of the registered classes match.
		Assertions.assertTrue(result.isEmpty());
		Assertions.assertEquals(0, unrelatedHandler.getGuiPropertiesCalls());
	}

	@Test
	public void clickableIngredientLookupReturnsExactScreenHandlerIngredient() {
		// Setup: both exact and assignable handlers match, and both return clickable ingredients.
		TestClickableIngredient baseIngredient = new TestClickableIngredient();
		TestClickableIngredient exactIngredient = new TestClickableIngredient();
		TestScreenHandler<BaseScreen> baseHandler = new TestScreenHandler<>(null, baseIngredient);
		TestScreenHandler<TestScreen> exactHandler = new TestScreenHandler<>(null, exactIngredient);
		Map<Class<?>, IScreenHandler<?>> screenHandlers = new LinkedHashMap<>();
		screenHandlers.put(BaseScreen.class, baseHandler);
		screenHandlers.put(TestScreen.class, exactHandler);
		ScreenHelper screenHelper = createScreenHelper(screenHandlers);

		// Operation: look up clickable ingredients for a screen with an exact registered handler.
		List<IClickableIngredient<?>> result = screenHelper.getClickableIngredientUnderMouse(new TestScreen(), 0, 0)
			.toList();

		// Assertions: the exact handler is preferred over assignable handlers.
		Assertions.assertEquals(List.of(exactIngredient, baseIngredient), result);
		Assertions.assertEquals(1, exactHandler.getClickableIngredientCalls());
		Assertions.assertEquals(1, baseHandler.getClickableIngredientCalls());
	}

	@Test
	public void clickableIngredientLookupFallsBackWhenExactScreenHandlerReturnsEmpty() {
		// Setup: an exact handler returns no clickable ingredient, and an assignable fallback would return one.
		TestClickableIngredient baseIngredient = new TestClickableIngredient();
		TestScreenHandler<BaseScreen> baseHandler = new TestScreenHandler<>(null, baseIngredient);
		TestScreenHandler<TestScreen> exactHandler = new TestScreenHandler<>(null, null);
		Map<Class<?>, IScreenHandler<?>> screenHandlers = new LinkedHashMap<>();
		screenHandlers.put(BaseScreen.class, baseHandler);
		screenHandlers.put(TestScreen.class, exactHandler);
		ScreenHelper screenHelper = createScreenHelper(screenHandlers);

		// Operation: look up clickable ingredients for a screen with an exact registered handler.
		List<IClickableIngredient<?>> result = screenHelper.getClickableIngredientUnderMouse(new TestScreen(), 0, 0)
			.toList();

		// Assertions: the exact handler is tried first, then lookup falls back to the assignable handler.
		Assertions.assertEquals(List.of(baseIngredient), result);
		Assertions.assertEquals(1, exactHandler.getClickableIngredientCalls());
		Assertions.assertEquals(1, baseHandler.getClickableIngredientCalls());
	}

	@Test
	public void clickableIngredientLookupReturnsClosestAssignableScreenHandlerIngredient() {
		// Setup: two assignable handlers match, and both return clickable ingredients.
		TestClickableIngredient baseIngredient = new TestClickableIngredient();
		TestClickableIngredient intermediateIngredient = new TestClickableIngredient();
		TestScreenHandler<BaseScreen> baseHandler = new TestScreenHandler<>(null, baseIngredient);
		TestScreenHandler<IntermediateScreen> intermediateHandler = new TestScreenHandler<>(null, intermediateIngredient);
		Map<Class<?>, IScreenHandler<?>> screenHandlers = new LinkedHashMap<>();
		screenHandlers.put(BaseScreen.class, baseHandler);
		screenHandlers.put(IntermediateScreen.class, intermediateHandler);
		ScreenHelper screenHelper = createScreenHelper(screenHandlers);

		// Operation: look up clickable ingredients for a screen with no exact handler.
		List<IClickableIngredient<?>> result = screenHelper.getClickableIngredientUnderMouse(new TestScreen(), 0, 0)
			.toList();

		// Assertions: lookup uses the closest assignable handler, regardless of registration order.
		Assertions.assertEquals(List.of(intermediateIngredient, baseIngredient), result);
		Assertions.assertEquals(1, baseHandler.getClickableIngredientCalls());
		Assertions.assertEquals(1, intermediateHandler.getClickableIngredientCalls());
	}

	@Test
	public void clickableIngredientLookupFallsBackWhenClosestAssignableScreenHandlerReturnsEmpty() {
		// Setup: two assignable handlers match, and the closest one returns no clickable ingredient.
		TestClickableIngredient baseIngredient = new TestClickableIngredient();
		TestScreenHandler<BaseScreen> baseHandler = new TestScreenHandler<>(null, baseIngredient);
		TestScreenHandler<IntermediateScreen> intermediateHandler = new TestScreenHandler<>(null, null);
		Map<Class<?>, IScreenHandler<?>> screenHandlers = new LinkedHashMap<>();
		screenHandlers.put(BaseScreen.class, baseHandler);
		screenHandlers.put(IntermediateScreen.class, intermediateHandler);
		ScreenHelper screenHelper = createScreenHelper(screenHandlers);

		// Operation: look up clickable ingredients for a screen with no exact handler.
		List<IClickableIngredient<?>> result = screenHelper.getClickableIngredientUnderMouse(new TestScreen(), 0, 0)
			.toList();

		// Assertions: lookup falls through from the closest handler to the next-best handler.
		Assertions.assertEquals(List.of(baseIngredient), result);
		Assertions.assertEquals(1, baseHandler.getClickableIngredientCalls());
		Assertions.assertEquals(1, intermediateHandler.getClickableIngredientCalls());
	}

	@Test
	public void containerScreenUsesScreenHandlerForPropertiesAndGuiHandlerForExtraAreas() {
		// Setup: a container screen has both a screen handler and a GUI container handler registered.
		TestGuiProperties screenProperties = new TestGuiProperties(TestContainerScreen.class);
		Rect2i extraArea = new Rect2i(1, 2, 3, 4);
		TestScreenHandler<TestContainerScreen> screenHandler = new TestScreenHandler<>(screenProperties, null);
		TestGuiContainerHandler<TestContainerScreen> guiHandler = new TestGuiContainerHandler<>(extraArea);
		Map<Class<?>, IScreenHandler<?>> screenHandlers = new LinkedHashMap<>();
		screenHandlers.put(TestContainerScreen.class, screenHandler);
		GuiContainerHandlers guiContainerHandlers = createGuiContainerHandlers(TestContainerScreen.class, guiHandler);
		ScreenHelper screenHelper = createScreenHelper(screenHandlers, guiContainerHandlers);
		TestContainerScreen screen = allocateInstance(TestContainerScreen.class);

		// Operation: query the screen-specific and container-specific handler paths.
		Optional<IGuiProperties> properties = screenHelper.getGuiProperties(screen);
		List<Rect2i> extraAreas = screenHelper.getGuiExclusionAreas(screen)
			.toList();

		// Assertions: each public API uses the appropriate handler family for the same screen.
		Assertions.assertSame(screenProperties, properties.orElseThrow());
		Assertions.assertEquals(List.of(extraArea), extraAreas);
		Assertions.assertEquals(1, screenHandler.getGuiPropertiesCalls());
		Assertions.assertEquals(1, guiHandler.getGuiExtraAreasCalls());
	}

	@Test
	public void guiContainerHandlersReturnAllMatchingHandlersInRegistrationOrder() {
		// Setup: an exact container handler, an assignable container handler, and an unrelated handler are registered.
		Rect2i baseArea = new Rect2i(1, 2, 3, 4);
		Rect2i exactArea = new Rect2i(5, 6, 7, 8);
		Rect2i unrelatedArea = new Rect2i(9, 10, 11, 12);
		TestGuiContainerHandler<BaseContainerScreen> baseHandler = new TestGuiContainerHandler<>(baseArea);
		TestGuiContainerHandler<TestContainerScreen> exactHandler = new TestGuiContainerHandler<>(exactArea);
		TestGuiContainerHandler<UnrelatedContainerScreen> unrelatedHandler = new TestGuiContainerHandler<>(unrelatedArea);
		GuiContainerHandlers guiContainerHandlers = new GuiContainerHandlers();
		guiContainerHandlers.add(BaseContainerScreen.class, baseHandler);
		guiContainerHandlers.add(TestContainerScreen.class, exactHandler);
		guiContainerHandlers.add(UnrelatedContainerScreen.class, unrelatedHandler);
		ScreenHelper screenHelper = createScreenHelper(Map.of(), guiContainerHandlers);

		// Operation: get extra areas for a container screen.
		List<Rect2i> extraAreas = screenHelper.getGuiExclusionAreas(allocateInstance(TestContainerScreen.class))
			.toList();

		// Assertions: all assignable GUI handlers are used in registration order.
		Assertions.assertEquals(List.of(baseArea, exactArea), extraAreas);
		Assertions.assertEquals(1, baseHandler.getGuiExtraAreasCalls());
		Assertions.assertEquals(1, exactHandler.getGuiExtraAreasCalls());
		Assertions.assertEquals(0, unrelatedHandler.getGuiExtraAreasCalls());
	}

	@Test
	public void guiContainerHandlerWithNoExactMatchStillAppliesToSubclass() {
		// Setup: only a base container handler is registered.
		Rect2i baseArea = new Rect2i(1, 2, 3, 4);
		TestGuiContainerHandler<BaseContainerScreen> baseHandler = new TestGuiContainerHandler<>(baseArea);
		GuiContainerHandlers guiContainerHandlers = createGuiContainerHandlers(BaseContainerScreen.class, baseHandler);
		ScreenHelper screenHelper = createScreenHelper(Map.of(), guiContainerHandlers);

		// Operation: get extra areas for a subclass of the registered container screen.
		List<Rect2i> extraAreas = screenHelper.getGuiExclusionAreas(allocateInstance(TestContainerScreen.class))
			.toList();

		// Assertions: GUI container handlers use assignable matching when there is no exact match.
		Assertions.assertEquals(List.of(baseArea), extraAreas);
		Assertions.assertEquals(1, baseHandler.getGuiExtraAreasCalls());
	}

	private static ScreenHelper createScreenHelper(Map<Class<?>, IScreenHandler<?>> screenHandlers) {
		return createScreenHelper(screenHandlers, new GuiContainerHandlers());
	}

	private static ScreenHelper createScreenHelper(Map<Class<?>, IScreenHandler<?>> screenHandlers, GuiContainerHandlers guiContainerHandlers) {
		return new ScreenHelper(
			TYPED_INGREDIENT_FACTORY,
			List.of(),
			guiContainerHandlers,
			new ListMultiMap<>(),
			screenHandlers
		);
	}

	private static <T extends AbstractContainerScreen<?>> GuiContainerHandlers createGuiContainerHandlers(
		Class<? extends T> containerClass,
		IGuiContainerHandler<? super T> handler
	) {
		GuiContainerHandlers guiContainerHandlers = new GuiContainerHandlers();
		guiContainerHandlers.add(containerClass, handler);
		return guiContainerHandlers;
	}

	private static <T> T allocateInstance(Class<T> instanceClass) {
		try {
			Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
			Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			Object unsafe = theUnsafe.get(null);
			Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
			Object instance = allocateInstance.invoke(unsafe, instanceClass);
			return instanceClass.cast(instance);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Failed to allocate test instance for " + instanceClass.getName(), e);
		}
	}

	private static class BaseScreen extends Screen {
		protected BaseScreen() {
			super(Component.literal("test"));
		}
	}

	private static class IntermediateScreen extends BaseScreen {

	}

	private static class TestScreen extends IntermediateScreen {

	}

	private static class UnrelatedScreen extends Screen {
		protected UnrelatedScreen() {
			super(Component.literal("test"));
		}
	}

	private static class TestScreenHandler<T extends Screen> implements IScreenHandler<T> {
		@Nullable
		private final IGuiProperties guiProperties;
		private final @Nullable IClickableIngredient<?> clickableIngredient;
		private final AtomicInteger guiPropertiesCalls = new AtomicInteger();
		private final AtomicInteger clickableIngredientCalls = new AtomicInteger();

		private TestScreenHandler(@Nullable IGuiProperties guiProperties, @Nullable IClickableIngredient<?> clickableIngredient) {
			this.guiProperties = guiProperties;
			this.clickableIngredient = clickableIngredient;
		}

		@Override
		@Nullable
		public IGuiProperties apply(T guiScreen) {
			guiPropertiesCalls.incrementAndGet();
			return guiProperties;
		}

		@Override
		public Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(
			IClickableIngredientFactory factory,
			T screen,
			double mouseX,
			double mouseY
		) {
			clickableIngredientCalls.incrementAndGet();
			return Optional.ofNullable(clickableIngredient);
		}

		public int getGuiPropertiesCalls() {
			return guiPropertiesCalls.get();
		}

		public int getClickableIngredientCalls() {
			return clickableIngredientCalls.get();
		}
	}

	private static class TestMenu extends AbstractContainerMenu {
		private TestMenu() {
			super(null, 0);
		}

		@Override
		public ItemStack quickMoveStack(Player player, int slotIndex) {
			return ItemStack.EMPTY;
		}

		@Override
		public boolean stillValid(Player player) {
			return true;
		}
	}

	private static class BaseContainerScreen extends AbstractContainerScreen<TestMenu> {
		private BaseContainerScreen() {
			super(new TestMenu(), new Inventory(null), Component.literal("test"));
		}

		@Override
		protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {

		}
	}

	private static class TestContainerScreen extends BaseContainerScreen {

	}

	private static class UnrelatedContainerScreen extends AbstractContainerScreen<TestMenu> {
		private UnrelatedContainerScreen() {
			super(new TestMenu(), new Inventory(null), Component.literal("test"));
		}

		@Override
		protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {

		}
	}

	private record TestGuiProperties(Class<? extends Screen> screenClass) implements IGuiProperties {
		@Override
		public int guiLeft() {
			return 0;
		}

		@Override
		public int guiTop() {
			return 0;
		}

		@Override
		public int guiXSize() {
			return 0;
		}

		@Override
		public int guiYSize() {
			return 0;
		}

		@Override
		public int screenWidth() {
			return 0;
		}

		@Override
		public int screenHeight() {
			return 0;
		}
	}

	private static class TestClickableIngredient implements IClickableIngredient<Object> {
		@Override
		public ITypedIngredient<Object> getTypedIngredient() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Rect2i getArea() {
			return new Rect2i(0, 0, 1, 1);
		}
	}

	private static class TestGuiContainerHandler<T extends AbstractContainerScreen<?>> implements IGuiContainerHandler<T> {
		private final Rect2i extraArea;
		private final AtomicInteger guiExtraAreasCalls = new AtomicInteger();

		private TestGuiContainerHandler(Rect2i extraArea) {
			this.extraArea = extraArea;
		}

		@Override
		public List<Rect2i> getGuiExtraAreas(T containerScreen) {
			guiExtraAreasCalls.incrementAndGet();
			return List.of(extraArea);
		}

		public int getGuiExtraAreasCalls() {
			return guiExtraAreasCalls.get();
		}
	}
}
