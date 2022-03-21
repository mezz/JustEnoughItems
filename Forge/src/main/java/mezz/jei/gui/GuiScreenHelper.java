package mezz.jei.gui;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.handlers.IScreenHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.ingredients.RegisteredIngredients;
import mezz.jei.ingredients.TypedIngredient;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.util.ImmutableRect2i;
import mezz.jei.util.MathUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GuiScreenHelper {
	private final RegisteredIngredients registeredIngredients;
	private final List<IGlobalGuiHandler> globalGuiHandlers;
	private final GuiContainerHandlers guiContainerHandlers;
	private final Map<Class<?>, IGhostIngredientHandler<?>> ghostIngredientHandlers;
	private final Map<Class<?>, IScreenHandler<?>> guiScreenHandlers;
	private Set<ImmutableRect2i> guiExclusionAreas = Collections.emptySet();

	public GuiScreenHelper(
		RegisteredIngredients registeredIngredients,
		List<IGlobalGuiHandler> globalGuiHandlers,
		GuiContainerHandlers guiContainerHandlers,
		Map<Class<?>, IGhostIngredientHandler<?>> ghostIngredientHandlers,
		Map<Class<?>, IScreenHandler<?>> guiScreenHandlers
	) {
		this.registeredIngredients = registeredIngredients;
		this.globalGuiHandlers = globalGuiHandlers;
		this.guiContainerHandlers = guiContainerHandlers;
		this.ghostIngredientHandlers = ghostIngredientHandlers;
		this.guiScreenHandlers = guiScreenHandlers;
	}

	@Nullable
	public <T extends Screen> IGuiProperties getGuiProperties(@Nullable T screen) {
		if (screen == null) {
			return null;
		}
		{
			@SuppressWarnings("unchecked")
			IScreenHandler<T> handler = (IScreenHandler<T>) guiScreenHandlers.get(screen.getClass());
			if (handler != null) {
				return handler.apply(screen);
			}
		}
		for (Map.Entry<Class<?>, IScreenHandler<?>> entry : guiScreenHandlers.entrySet()) {
			Class<?> guiScreenClass = entry.getKey();
			if (guiScreenClass.isInstance(screen)) {
				@SuppressWarnings("unchecked")
				IScreenHandler<T> handler = (IScreenHandler<T>) entry.getValue();
				if (handler != null) {
					return handler.apply(screen);
				}
			}
		}
		return null;
	}

	public boolean updateGuiExclusionAreas(Screen screen) {
		Set<Rect2i> guiAreas = getPluginsExclusionAreas(screen);
		if (!MathUtil.equalRects(guiAreas, this.guiExclusionAreas)) {
			// make a defensive copy because Rect2i is mutable
			this.guiExclusionAreas = guiAreas.stream()
				.map(ImmutableRect2i::new)
				.collect(Collectors.toUnmodifiableSet());
			return true;
		}
		return false;
	}

	public Set<ImmutableRect2i> getGuiExclusionAreas() {
		return guiExclusionAreas;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean isInGuiExclusionArea(double mouseX, double mouseY) {
		return MathUtil.contains(guiExclusionAreas, mouseX, mouseY);
	}

	private Set<Rect2i> getPluginsExclusionAreas(Screen screen) {
		Set<Rect2i> globalGuiHandlerExclusionAreas = globalGuiHandlers.stream()
			.map(IGlobalGuiHandler::getGuiExtraAreas)
			.flatMap(Collection::stream)
			.collect(Collectors.toSet());

		if (screen instanceof AbstractContainerScreen<?> guiContainer) {
			Collection<Rect2i> guiExtraAreas = this.guiContainerHandlers.getGuiExtraAreas(guiContainer);
			Set<Rect2i> areas = new HashSet<>(guiExtraAreas);
			areas.addAll(globalGuiHandlerExclusionAreas);
			return areas;
		} else {
			return globalGuiHandlerExclusionAreas;
		}
	}

	public <T extends AbstractContainerScreen<?>> Stream<IClickedIngredient<?>> getPluginsIngredientUnderMouse(T guiContainer, double mouseX, double mouseY) {
		return Stream.concat(
				this.guiContainerHandlers.getActiveGuiHandlerStream(guiContainer)
					.map(a -> a.getIngredientUnderMouse(guiContainer, mouseX, mouseY)),
				this.globalGuiHandlers.stream()
					.map(a -> a.getIngredientUnderMouse(mouseX, mouseY))
			)
			.map(i -> createClickedIngredient(i, guiContainer))
			.flatMap(Optional::stream);
	}

	@Nullable
	public <T extends Screen> IGhostIngredientHandler<T> getGhostIngredientHandler(T guiScreen) {
		{
			@SuppressWarnings("unchecked")
			IGhostIngredientHandler<T> handler = (IGhostIngredientHandler<T>) ghostIngredientHandlers.get(guiScreen.getClass());
			if (handler != null) {
				return handler;
			}
		}
		for (Map.Entry<Class<?>, IGhostIngredientHandler<?>> entry : ghostIngredientHandlers.entrySet()) {
			Class<?> guiScreenClass = entry.getKey();
			if (guiScreenClass.isInstance(guiScreen)) {
				@SuppressWarnings("unchecked")
				IGhostIngredientHandler<T> handler = (IGhostIngredientHandler<T>) entry.getValue();
				if (handler != null) {
					return handler;
				}
			}
		}
		return null;
	}

	private <T> Optional<IClickedIngredient<?>> createClickedIngredient(@Nullable T ingredient, AbstractContainerScreen<?> guiContainer) {
		if (ingredient == null) {
			return Optional.empty();
		}
		return TypedIngredient.create(registeredIngredients, ingredient)
			.map(typedIngredient -> {
				ImmutableRect2i area = getSlotArea(typedIngredient, guiContainer);
				return new ClickedIngredient<>(typedIngredient, area, false, false);
			});
	}

	public Optional<IGuiClickableArea> getGuiClickableArea(AbstractContainerScreen<?> guiContainer, double guiMouseX, double guiMouseY) {
		return this.guiContainerHandlers.getGuiClickableArea(guiContainer, guiMouseX, guiMouseY);
	}

	@Nullable
	public static <T> ImmutableRect2i getSlotArea(ITypedIngredient<T> typedIngredient, AbstractContainerScreen<?> guiContainer) {
		Slot slotUnderMouse = guiContainer.getSlotUnderMouse();
		if (slotUnderMouse == null) {
			return null;
		}
		return typedIngredient.getIngredient(VanillaTypes.ITEM)
			.filter(i -> ItemStack.matches(slotUnderMouse.getItem(), i))
			.map(i ->
				new ImmutableRect2i(
					guiContainer.getGuiLeft() + slotUnderMouse.x,
					guiContainer.getGuiTop() + slotUnderMouse.y,
					16,
					16
				)
			)
			.orElse(null);
	}

}
