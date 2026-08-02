package mezz.jei.library.gui.helpers;

import mezz.jei.api.constants.VanillaTypes;
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
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.core.collect.ListMultiMap;
import mezz.jei.library.gui.GuiContainerHandlers;
import mezz.jei.library.ingredients.TypedIngredient;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class ScreenHelper implements IScreenHelper {
	private final IIngredientManager ingredientManager;
	private final List<IGlobalGuiHandler> globalGuiHandlers;
	private final GuiContainerHandlers guiContainerHandlers;
	private final ListMultiMap<Class<?>, IGhostIngredientHandler<?>> ghostIngredientHandlers;
	private final ListMultiMap<Class<?>, IGhostIngredientHandler<?>> cachedGhostIngredientHandlers;
	private final Map<Class<?>, IScreenHandler<?>> guiScreenHandlers;

	public ScreenHelper(
		IIngredientManager ingredientManager,
		List<IGlobalGuiHandler> globalGuiHandlers,
		GuiContainerHandlers guiContainerHandlers,
		ListMultiMap<Class<?>, IGhostIngredientHandler<?>> ghostIngredientHandlers,
		Map<Class<?>, IScreenHandler<?>> guiScreenHandlers
	) {
		this.ingredientManager = ingredientManager;
		this.globalGuiHandlers = globalGuiHandlers;
		this.guiContainerHandlers = guiContainerHandlers;
		this.ghostIngredientHandlers = ghostIngredientHandlers;
		this.guiScreenHandlers = guiScreenHandlers;
		this.cachedGhostIngredientHandlers = new ListMultiMap<>();
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
			getPluginsIngredientUnderMouse(screen, mouseX, mouseY),
			getSlotIngredientUnderMouse(screen).stream()
		);
	}

	private Optional<IClickableIngredient<?>> getSlotIngredientUnderMouse(Screen guiScreen) {
		if (!(guiScreen instanceof AbstractContainerScreen<?> guiContainer)) {
			return Optional.empty();
		}
		IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
		return screenHelper.getSlotUnderMouse(guiContainer)
			.flatMap(slot -> getClickedIngredient(slot, guiContainer));
	}

	private Stream<IClickableIngredient<?>> getPluginsIngredientUnderMouse(Screen guiScreen, double mouseX, double mouseY) {
		Stream<IClickableIngredient<?>> globalIngredients = this.globalGuiHandlers.stream()
			.map(a -> a.getClickableIngredientUnderMouse(mouseX, mouseY))
			.flatMap(Optional::stream);

		if (guiScreen instanceof AbstractContainerScreen<?> guiContainer) {
			Stream<IClickableIngredient<?>> containerIngredients = getGuiContainerHandlerIngredients(guiContainer, mouseX, mouseY);
			return Stream.concat(
				containerIngredients,
				globalIngredients
			);
		}
		return globalIngredients;
	}

	private Optional<IClickableIngredient<?>> getClickedIngredient(Slot slot, AbstractContainerScreen<?> guiContainer) {
		ItemStack stack = slot.getItem();
		@Nullable ITypedIngredient<ItemStack> typedIngredient = TypedIngredient.createAndFilterInvalid(ingredientManager, VanillaTypes.ITEM_STACK, stack, false);
		if (typedIngredient == null) {
			return Optional.empty();
		}
		IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
		ImmutableRect2i slotArea = new ImmutableRect2i(
			screenHelper.getGuiLeft(guiContainer) + slot.x,
			screenHelper.getGuiTop(guiContainer) + slot.y,
			16,
			16
		);
		ClickableIngredient<ItemStack> clickableIngredient = new ClickableIngredient<>(typedIngredient, slotArea);
		return Optional.of(clickableIngredient);
	}

	private <T extends AbstractContainerScreen<?>> Stream<IClickableIngredient<?>> getGuiContainerHandlerIngredients(T guiContainer, double mouseX, double mouseY) {
		return this.guiContainerHandlers.getActiveGuiHandlerStream(guiContainer)
			.map(a ->
				a.getClickableIngredientUnderMouse(guiContainer, mouseX, mouseY)
			)
			.flatMap(Optional::stream);
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
