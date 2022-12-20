package mezz.jei.library.gui;

import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.handlers.IScreenHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.input.ClickableIngredient;
import mezz.jei.common.input.ClickableIngredientFactory;
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.library.ingredients.TypedIngredient;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class ScreenHelper implements IScreenHelper {
	private final IIngredientManager ingredientManager;
	private final IClickableIngredientFactory clickableIngredientFactory;
	private final List<IGlobalGuiHandler> globalGuiHandlers;
	private final GuiContainerHandlers guiContainerHandlers;
	private final Map<Class<?>, IGhostIngredientHandler<?>> ghostIngredientHandlers;
	private final Map<Class<?>, IScreenHandler<?>> guiScreenHandlers;

	public ScreenHelper(
		IIngredientManager ingredientManager,
		List<IGlobalGuiHandler> globalGuiHandlers,
		GuiContainerHandlers guiContainerHandlers,
		Map<Class<?>, IGhostIngredientHandler<?>> ghostIngredientHandlers,
		Map<Class<?>, IScreenHandler<?>> guiScreenHandlers
	) {
		this.ingredientManager = ingredientManager;
		this.clickableIngredientFactory = new ClickableIngredientFactory(ingredientManager);
		this.globalGuiHandlers = globalGuiHandlers;
		this.guiContainerHandlers = guiContainerHandlers;
		this.ghostIngredientHandlers = ghostIngredientHandlers;
		this.guiScreenHandlers = guiScreenHandlers;
	}

	@Override
	public <T extends Screen> Optional<IGuiProperties> getGuiProperties(T screen) {
		{
			@SuppressWarnings("unchecked")
			IScreenHandler<T> handler = (IScreenHandler<T>) guiScreenHandlers.get(screen.getClass());
			if (handler != null) {
				IGuiProperties properties = handler.apply(screen);
				return Optional.ofNullable(properties);
			}
		}
		for (Map.Entry<Class<?>, IScreenHandler<?>> entry : guiScreenHandlers.entrySet()) {
			Class<?> guiScreenClass = entry.getKey();
			if (guiScreenClass.isInstance(screen)) {
				@SuppressWarnings("unchecked")
				IScreenHandler<T> handler = (IScreenHandler<T>) entry.getValue();
				if (handler != null) {
					IGuiProperties properties = handler.apply(screen);
					return Optional.ofNullable(properties);
				}
			}
		}
		return Optional.empty();
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
			getSlotIngredientUnderMouse(clickableIngredientFactory, screen).stream()
		);
	}

	private Optional<IClickableIngredient<?>> getSlotIngredientUnderMouse(IClickableIngredientFactory factory, Screen guiScreen) {
		if (!(guiScreen instanceof AbstractContainerScreen<?> guiContainer)) {
			return Optional.empty();
		}
		IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
		return screenHelper.getSlotUnderMouse(guiContainer)
			.flatMap(slot -> getClickedIngredient(factory, slot, guiContainer));
	}

	private Stream<IClickableIngredient<?>> getPluginsIngredientUnderMouse(IClickableIngredientFactory factory, Screen guiScreen, double mouseX, double mouseY) {
		Stream<IClickableIngredient<?>> globalIngredients = this.globalGuiHandlers.stream()
			.map(a -> castClickableIngredient(a.getClickableIngredientUnderMouse(factory, mouseX, mouseY)))
			.flatMap(Optional::stream);

		if (guiScreen instanceof AbstractContainerScreen<?> guiContainer) {
			Stream<IClickableIngredient<?>> containerIngredients = getGuiContainerHandlerIngredients(factory, guiContainer, mouseX, mouseY);
			return Stream.concat(
				containerIngredients,
				globalIngredients
			);
		}
		return globalIngredients;
	}

	private Optional<IClickableIngredient<?>> getClickedIngredient(IClickableIngredientFactory factory, Slot slot, AbstractContainerScreen<?> guiContainer) {
		ItemStack stack = slot.getItem();
		IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
		return factory.createBuilder(stack)
			.buildWithArea(
				screenHelper.getGuiLeft(guiContainer) + slot.x,
				screenHelper.getGuiTop(guiContainer) + slot.y,
				16,
				16
			)
			.map(i -> (IClickableIngredient<?>) i);
	}

	private <T extends AbstractContainerScreen<?>> Stream<IClickableIngredient<?>> getGuiContainerHandlerIngredients(IClickableIngredientFactory factory, T guiContainer, double mouseX, double mouseY) {
		return this.guiContainerHandlers.getActiveGuiHandlerStream(guiContainer)
			.map(a -> castClickableIngredient(a.getClickableIngredientUnderMouse(factory, guiContainer, mouseX, mouseY)))
			.flatMap(Optional::stream);
	}

	private static Optional<IClickableIngredient<?>> castClickableIngredient(Optional<? extends IClickableIngredient<?>> clickedIngredient) {
		return clickedIngredient.map(i -> (IClickableIngredient<?>) i);
	}

	@Override
	public <T extends Screen> Optional<IGhostIngredientHandler<T>> getGhostIngredientHandler(T guiScreen) {
		{
			@SuppressWarnings("unchecked")
			IGhostIngredientHandler<T> handler = (IGhostIngredientHandler<T>) ghostIngredientHandlers.get(guiScreen.getClass());
			if (handler != null) {
				return Optional.of(handler);
			}
		}
		for (Map.Entry<Class<?>, IGhostIngredientHandler<?>> entry : ghostIngredientHandlers.entrySet()) {
			Class<?> guiScreenClass = entry.getKey();
			if (guiScreenClass.isInstance(guiScreen)) {
				@SuppressWarnings("unchecked")
				IGhostIngredientHandler<T> handler = (IGhostIngredientHandler<T>) entry.getValue();
				if (handler != null) {
					return Optional.of(handler);
				}
			}
		}
		return Optional.empty();
	}

	private <T> Optional<IClickableIngredient<?>> createClickedIngredient(@Nullable T ingredient, Screen guiScreen) {
		if (ingredient == null) {
			return Optional.empty();
		}
		return TypedIngredient.createAndFilterInvalid(ingredientManager, ingredient)
			.map(typedIngredient -> {
				ImmutableRect2i area = getSlotArea(typedIngredient, guiScreen).orElse(ImmutableRect2i.EMPTY);
				return new ClickableIngredient<>(typedIngredient, area);
			});
	}

	@Override
	public Stream<IGuiClickableArea> getGuiClickableArea(AbstractContainerScreen<?> guiContainer, double guiMouseX, double guiMouseY) {
		return this.guiContainerHandlers.getGuiClickableArea(guiContainer, guiMouseX, guiMouseY);
	}

	public static <T> Optional<ImmutableRect2i> getSlotArea(ITypedIngredient<T> typedIngredient, Screen guiScreen) {
		if (!(guiScreen instanceof AbstractContainerScreen<?> guiContainer)) {
			return Optional.empty();
		}
		IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
		return screenHelper.getSlotUnderMouse(guiContainer)
			.flatMap(slotUnderMouse ->
				typedIngredient.getItemStack()
					.filter(i -> ItemStack.matches(slotUnderMouse.getItem(), i))
					.map(i ->
						new ImmutableRect2i(
							screenHelper.getGuiLeft(guiContainer) + slotUnderMouse.x,
							screenHelper.getGuiTop(guiContainer) + slotUnderMouse.y,
							16,
							16
						)
					)
			);
	}

}
