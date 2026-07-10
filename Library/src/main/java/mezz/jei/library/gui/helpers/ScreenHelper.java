package mezz.jei.library.gui.helpers;

import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.handlers.IScreenHandler;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.collect.ListMultiMap;
import mezz.jei.common.ingredients.ITypedIngredientFactory;
import mezz.jei.common.input.ClickableIngredientFactory;
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.library.gui.GuiContainerHandlers;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ScreenHelper implements IScreenHelper {
	private final IClickableIngredientFactory clickableIngredientFactory;
	private final List<IGlobalGuiHandler> globalGuiHandlers;
	private final GuiContainerHandlers guiContainerHandlers;
	private final ListMultiMap<Class<?>, IGhostIngredientHandler<?>> ghostIngredientHandlers;
	private final ListMultiMap<Class<?>, IGhostIngredientHandler<?>> cachedGhostIngredientHandlers;
	private final Map<Class<?>, IScreenHandler<?>> guiScreenHandlers;

	public ScreenHelper(
		ITypedIngredientFactory typedIngredientFactory,
		List<IGlobalGuiHandler> globalGuiHandlers,
		GuiContainerHandlers guiContainerHandlers,
		ListMultiMap<Class<?>, IGhostIngredientHandler<?>> ghostIngredientHandlers,
		Map<Class<?>, IScreenHandler<?>> guiScreenHandlers
	) {
		this.clickableIngredientFactory = new ClickableIngredientFactory(typedIngredientFactory);
		this.globalGuiHandlers = globalGuiHandlers;
		this.guiContainerHandlers = guiContainerHandlers;
		this.ghostIngredientHandlers = ghostIngredientHandlers;
		this.guiScreenHandlers = guiScreenHandlers;
		this.cachedGhostIngredientHandlers = new ListMultiMap<>();
	}

	@Override
	public <T extends Screen> Optional<IGuiProperties> getGuiProperties(T screen) {
		return getActiveScreenHandlerStream(screen)
			.map(handler -> handler.apply(screen))
			.filter(Objects::nonNull)
			.findFirst();
	}

	@Override
	public Stream<Rect2i> getGuiExclusionAreas(Screen screen) {
		Stream<Rect2i> globalGuiHandlerExclusionAreas = globalGuiHandlers.stream()
			.map(IGlobalGuiHandler::getGuiExtraAreas)
			.flatMap(Collection::stream);

		if (screen instanceof AbstractContainerScreen<?> guiContainer) {
			Stream<Rect2i> guiExtraAreas = this.guiContainerHandlers.getGuiExtraAreas(guiContainer);
			return Stream.concat(globalGuiHandlerExclusionAreas, guiExtraAreas);
		} else {
			return globalGuiHandlerExclusionAreas;
		}
	}

	@Override
	public Stream<IClickableIngredient<?>> getClickableIngredientUnderMouse(Screen screen, double mouseX, double mouseY) {
		return Stream.concat(
			getPluginsIngredientUnderMouse(clickableIngredientFactory, screen, mouseX, mouseY),
			getHoveredSlotIngredient(clickableIngredientFactory, screen).stream()
		);
	}

	private Optional<IClickableIngredient<?>> getHoveredSlotIngredient(IClickableIngredientFactory factory, Screen guiScreen) {
		if (!(guiScreen instanceof AbstractContainerScreen<?> guiContainer)) {
			return Optional.empty();
		}
		IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
		return screenHelper.getSlotUnderMouse(guiContainer)
			.flatMap(slot -> getClickedIngredient(factory, slot, guiContainer));
	}

	private Stream<IClickableIngredient<?>> getPluginsIngredientUnderMouse(IClickableIngredientFactory factory, Screen guiScreen, double mouseX, double mouseY) {
		Stream<IClickableIngredient<?>> screenIngredients = getScreenHandlerIngredients(factory, guiScreen, mouseX, mouseY);
		Stream<IClickableIngredient<?>> globalIngredients = this.globalGuiHandlers.stream()
			.map(a -> a.getClickableIngredientUnderMouse(factory, mouseX, mouseY))
			.flatMap(Optional::stream);

		if (guiScreen instanceof AbstractContainerScreen<?> guiContainer) {
			Stream<IClickableIngredient<?>> containerIngredients = getGuiContainerHandlerIngredients(factory, guiContainer, mouseX, mouseY);
			return Stream.concat(
				containerIngredients,
				Stream.concat(screenIngredients, globalIngredients)
			);
		}
		return Stream.concat(screenIngredients, globalIngredients);
	}

	private Optional<IClickableIngredient<ItemStack>> getClickedIngredient(IClickableIngredientFactory factory, Slot slot, AbstractContainerScreen<?> guiContainer) {
		ItemStack stack = slot.getItem();
		return getGuiProperties(guiContainer)
			.flatMap(guiProperties -> {
				return factory.createBuilder(stack)
					.buildWithArea(
						guiProperties.guiLeft() + slot.x,
						guiProperties.guiTop() + slot.y,
						16,
						16
					);
			});
	}

	private <T extends AbstractContainerScreen<?>> Stream<IClickableIngredient<?>> getGuiContainerHandlerIngredients(IClickableIngredientFactory factory, T guiContainer, double mouseX, double mouseY) {
		return this.guiContainerHandlers.getActiveGuiHandlerStream(guiContainer)
			.map(a -> a.getClickableIngredientUnderMouse(factory, guiContainer, mouseX, mouseY))
			.flatMap(Optional::stream);
	}

	private <T extends Screen> Stream<IClickableIngredient<?>> getScreenHandlerIngredients(IClickableIngredientFactory factory, T guiScreen, double mouseX, double mouseY) {
		return getActiveScreenHandlerStream(guiScreen)
			.map(handler -> handler.getClickableIngredientUnderMouse(factory, guiScreen, mouseX, mouseY))
			.flatMap(Optional::stream);
	}

	private <T extends Screen> Stream<IScreenHandler<T>> getActiveScreenHandlerStream(T guiScreen) {
		Class<? extends Screen> guiScreenClass = guiScreen.getClass();
		return guiScreenHandlers.entrySet()
			.stream()
			.filter(entry -> entry.getKey().isInstance(guiScreen))
			.sorted(Comparator.comparingInt(entry -> getClassDistance(guiScreenClass, entry.getKey())))
			.map(Map.Entry::getValue)
			.map(ScreenHelper::castScreenHandler);
	}

	private static int getClassDistance(Class<?> childClass, Class<?> parentClass) {
		int distance = 0;
		for (Class<?> currentClass = childClass; currentClass != null; currentClass = currentClass.getSuperclass()) {
			if (currentClass == parentClass) {
				return distance;
			}
			distance++;
		}
		return Integer.MAX_VALUE;
	}

	@SuppressWarnings("unchecked")
	private static <T extends Screen> IScreenHandler<T> castScreenHandler(IScreenHandler<?> handler) {
		return (IScreenHandler<T>) handler;
	}

	@Override
	public <T extends Screen> List<IGhostIngredientHandler<T>> getGhostIngredientHandlers(T guiScreen) {
		{
			Class<? extends Screen> guiScreenClass = guiScreen.getClass();
			if (cachedGhostIngredientHandlers.containsKey(guiScreenClass)) {
				@SuppressWarnings("unchecked")
				List<IGhostIngredientHandler<T>> cached = (List<IGhostIngredientHandler<T>>) (Object) cachedGhostIngredientHandlers.get(guiScreenClass);
				return cached;
			}
		}

		List<IGhostIngredientHandler<?>> results = new ArrayList<>();
		{
			List<IGhostIngredientHandler<?>> handlers = ghostIngredientHandlers.get(guiScreen.getClass());
			if (!handlers.isEmpty()) {
				results.addAll(handlers);
			}
		}
		for (Map.Entry<Class<?>, List<IGhostIngredientHandler<?>>> entry : ghostIngredientHandlers.entrySet()) {
			Class<?> handledClass = entry.getKey();
			if (handledClass.isInstance(guiScreen)) {
				List<IGhostIngredientHandler<?>> handlers = entry.getValue();
				if (!handlers.isEmpty()) {
					results.addAll(handlers);
				}
			}
		}
		cachedGhostIngredientHandlers.putAll(guiScreen.getClass(), results);
		@SuppressWarnings("unchecked")
		List<IGhostIngredientHandler<T>> castResults = (List<IGhostIngredientHandler<T>>) (Object) results;
		return castResults;
	}

	@Override
	public Stream<IGuiClickableArea> getGuiClickableArea(AbstractContainerScreen<?> guiContainer, double guiMouseX, double guiMouseY) {
		return this.guiContainerHandlers.getGuiClickableArea(guiContainer, guiMouseX, guiMouseY);
	}

}
