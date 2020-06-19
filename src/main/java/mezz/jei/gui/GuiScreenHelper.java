package mezz.jei.gui;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.handlers.IScreenHandler;
import mezz.jei.collect.ListMultiMap;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.util.MathUtil;

public class GuiScreenHelper {
	private final IngredientManager ingredientManager;
	private final List<IGlobalGuiHandler> globalGuiHandlers;
	private final ListMultiMap<Class<? extends ContainerScreen>, IGuiContainerHandler<?>> guiHandlers;
	private final Map<Class, IGhostIngredientHandler> ghostIngredientHandlers;
	private final Map<Class, IScreenHandler> guiScreenHandlers;
	private Set<Rectangle2d> guiExclusionAreas = Collections.emptySet();

	public GuiScreenHelper(
		IngredientManager ingredientManager,
		List<IGlobalGuiHandler> globalGuiHandlers,
		ListMultiMap<Class<? extends ContainerScreen>, IGuiContainerHandler<?>> guiHandlers,
		Map<Class, IGhostIngredientHandler> ghostIngredientHandlers,
		Map<Class, IScreenHandler> guiScreenHandlers
	) {
		this.ingredientManager = ingredientManager;
		this.globalGuiHandlers = globalGuiHandlers;
		this.guiHandlers = guiHandlers;
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
		for (Map.Entry<Class, IScreenHandler> entry : guiScreenHandlers.entrySet()) {
			Class guiScreenClass = entry.getKey();
			if (guiScreenClass.isInstance(screen)) {
				@SuppressWarnings("unchecked")
				IScreenHandler<T> handler = entry.getValue();
				if (handler != null) {
					return handler.apply(screen);
				}
			}
		}
		return null;
	}

	public boolean updateGuiExclusionAreas() {
		Set<Rectangle2d> guiAreas = getPluginsExclusionAreas();
		if (!guiAreas.equals(this.guiExclusionAreas)) {
			// make a defensive copy because Rectangle is mutable
			this.guiExclusionAreas = guiAreas.stream()
				.map(r -> new Rectangle2d(r.getX(), r.getY(), r.getWidth(), r.getHeight()))
				.collect(Collectors.toSet());
			return true;
		}
		return false;
	}

	public Set<Rectangle2d> getGuiExclusionAreas() {
		return guiExclusionAreas;
	}

	public boolean isInGuiExclusionArea(double mouseX, double mouseY) {
		return MathUtil.contains(guiExclusionAreas, mouseX, mouseY);
	}

	private Set<Rectangle2d> getPluginsExclusionAreas() {
		Screen screen = Minecraft.getInstance().currentScreen;
		if (screen == null) {
			return Collections.emptySet();
		}
		Set<Rectangle2d> allGuiExtraAreas = new HashSet<>();
		if (screen instanceof ContainerScreen) {
			ContainerScreen guiContainer = (ContainerScreen) screen;
			List<IGuiContainerHandler<ContainerScreen>> activeAdvancedGuiHandlers = getActiveAdvancedGuiHandlers(guiContainer);
			for (IGuiContainerHandler<ContainerScreen> advancedGuiHandler : activeAdvancedGuiHandlers) {
				List<Rectangle2d> guiExtraAreas = advancedGuiHandler.getGuiExtraAreas(guiContainer);
				allGuiExtraAreas.addAll(guiExtraAreas);
			}
		}
		for (IGlobalGuiHandler globalGuiHandler : globalGuiHandlers) {
			Collection<Rectangle2d> guiExtraAreas = globalGuiHandler.getGuiExtraAreas();
			allGuiExtraAreas.addAll(guiExtraAreas);
		}
		return allGuiExtraAreas;
	}


	@Nullable
	public <T extends ContainerScreen> IClickedIngredient<?> getPluginsIngredientUnderMouse(T guiContainer, double mouseX, double mouseY) {
		List<IGuiContainerHandler<T>> activeAdvancedGuiHandlers = getActiveAdvancedGuiHandlers(guiContainer);
		for (IGuiContainerHandler<T> advancedGuiHandler : activeAdvancedGuiHandlers) {
			Object clicked = advancedGuiHandler.getIngredientUnderMouse(guiContainer, mouseX, mouseY);
			IClickedIngredient<?> clickedIngredient = createClickedIngredient(clicked, guiContainer);
			if (clickedIngredient != null) {
				return clickedIngredient;
			}
		}
		for (IGlobalGuiHandler globalGuiHandler : globalGuiHandlers) {
			Object clicked = globalGuiHandler.getIngredientUnderMouse(mouseX, mouseY);
			IClickedIngredient<?> clickedIngredient = createClickedIngredient(clicked, guiContainer);
			if (clickedIngredient != null) {
				return clickedIngredient;
			}
		}
		return null;
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
		for (Map.Entry<Class, IGhostIngredientHandler> entry : ghostIngredientHandlers.entrySet()) {
			Class guiScreenClass = entry.getKey();
			if (guiScreenClass.isInstance(guiScreen)) {
				@SuppressWarnings("unchecked")
				IGhostIngredientHandler<T> handler = entry.getValue();
				if (handler != null) {
					return handler;
				}
			}
		}
		return null;
	}

	@Nullable
	private <T> IClickedIngredient<T> createClickedIngredient(@Nullable T ingredient, ContainerScreen guiContainer) {
		if (ingredient != null && ingredientManager.isValidIngredient(ingredient)) {
			Rectangle2d area = null;
			Slot slotUnderMouse = guiContainer.getSlotUnderMouse();
			if (ingredient instanceof ItemStack && slotUnderMouse != null && ItemStack.areItemStacksEqual(slotUnderMouse.getStack(), (ItemStack) ingredient)) {
				area = new Rectangle2d(slotUnderMouse.xPos, slotUnderMouse.yPos, 16, 16);
			}
			return ClickedIngredient.create(ingredient, area);
		}
		return null;
	}


	private <T extends ContainerScreen> List<IGuiContainerHandler<T>> getActiveAdvancedGuiHandlers(T guiContainer) {
		List<IGuiContainerHandler<T>> activeAdvancedGuiHandler = new ArrayList<>();
		for (Map.Entry<Class<? extends ContainerScreen>, List<IGuiContainerHandler<?>>> entry : guiHandlers.entrySet()) {
			Class<? extends ContainerScreen> guiContainerClass = entry.getKey();
			if (guiContainerClass.isInstance(guiContainer)) {
				for (IGuiContainerHandler<?> guiContainerHandler : entry.getValue()) {
					@SuppressWarnings("unchecked")
					IGuiContainerHandler<T> guiContainerHandlerCast = (IGuiContainerHandler<T>) guiContainerHandler;
					activeAdvancedGuiHandler.add(guiContainerHandlerCast);
				}
			}
		}
		return activeAdvancedGuiHandler;
	}

	@Nullable
	public IGuiClickableArea getGuiClickableArea(ContainerScreen guiContainer, double mouseX, double mouseY) {
		for (Map.Entry<Class<? extends ContainerScreen>, List<IGuiContainerHandler<?>>> entry : guiHandlers.entrySet()) {
			Class guiHandlerClass = entry.getKey();
			List<IGuiContainerHandler<?>> guiHandlers = entry.getValue();
			for (IGuiContainerHandler<?> guiHandler : guiHandlers) {
				@SuppressWarnings("unchecked")
				IGuiClickableArea guiClickableArea = getGuiClickableArea(guiHandlerClass, guiHandler, guiContainer, mouseX, mouseY);
				if (guiClickableArea != null) {
					return guiClickableArea;
				}
			}
		}
		return null;
	}

	@Nullable
	private static <T extends ContainerScreen> IGuiClickableArea getGuiClickableArea(Class<? extends T> handlerClass, IGuiContainerHandler<T> handler, ContainerScreen containerScreen, double mouseX, double mouseY) {
		if (handlerClass.isInstance(containerScreen)) {
			T castContainer = handlerClass.cast(containerScreen);
			Collection<IGuiClickableArea> guiClickableAreas = handler.getGuiClickableAreas(castContainer, mouseX, mouseY);
			for (IGuiClickableArea guiClickableArea : guiClickableAreas) {
				if (MathUtil.contains(guiClickableArea.getArea(), mouseX, mouseY)) {
					return guiClickableArea;
				}
			}
		}
		return null;
	}
}
