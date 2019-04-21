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
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.handlers.IGuiScreenHandler;
import mezz.jei.collect.ListMultiMap;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.util.MathUtil;

public class GuiScreenHelper {
	private final IngredientManager ingredientManager;
	private final List<IGlobalGuiHandler> globalGuiHandlers;
	private final ListMultiMap<Class<? extends GuiContainer>, IGuiContainerHandler<?>> guiHandlers;
	private final Map<Class, IGhostIngredientHandler> ghostIngredientHandlers;
	private final Map<Class, IGuiScreenHandler> guiScreenHandlers;
	private Set<Rectangle2d> guiExclusionAreas = Collections.emptySet();

	public GuiScreenHelper(IngredientManager ingredientManager, List<IGlobalGuiHandler> globalGuiHandlers, ListMultiMap<Class<? extends GuiContainer>, IGuiContainerHandler<?>> guiHandlers, Map<Class, IGhostIngredientHandler> ghostIngredientHandlers, Map<Class, IGuiScreenHandler> guiScreenHandlers) {
		this.ingredientManager = ingredientManager;
		this.globalGuiHandlers = globalGuiHandlers;
		this.guiHandlers = guiHandlers;
		this.ghostIngredientHandlers = ghostIngredientHandlers;
		this.guiScreenHandlers = guiScreenHandlers;
	}

	@Nullable
	public <T extends GuiScreen> IGuiProperties getGuiProperties(@Nullable T guiScreen) {
		if (guiScreen == null) {
			return null;
		}
		{
			@SuppressWarnings("unchecked")
			IGuiScreenHandler<T> handler = (IGuiScreenHandler<T>) guiScreenHandlers.get(guiScreen.getClass());
			if (handler != null) {
				return handler.apply(guiScreen);
			}
		}
		for (Map.Entry<Class, IGuiScreenHandler> entry : guiScreenHandlers.entrySet()) {
			Class guiScreenClass = entry.getKey();
			if (guiScreenClass.isInstance(guiScreen)) {
				@SuppressWarnings("unchecked")
				IGuiScreenHandler<T> handler = entry.getValue();
				if (handler != null) {
					return handler.apply(guiScreen);
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
				.map(Rectangle::new)
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
		GuiScreen guiScreen = Minecraft.getInstance().currentScreen;
		if (guiScreen == null) {
			return Collections.emptySet();
		}
		Set<Rectangle2d> allGuiExtraAreas = new HashSet<>();
		if (guiScreen instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) guiScreen;
			List<IGuiContainerHandler<GuiContainer>> activeAdvancedGuiHandlers = getActiveAdvancedGuiHandlers(guiContainer);
			for (IGuiContainerHandler<GuiContainer> advancedGuiHandler : activeAdvancedGuiHandlers) {
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
	public <T extends GuiContainer> IClickedIngredient<?> getPluginsIngredientUnderMouse(T guiContainer, double mouseX, double mouseY) {
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
	public <T extends GuiScreen> IGhostIngredientHandler<T> getGhostIngredientHandler(T guiScreen) {
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
	private <T> IClickedIngredient<T> createClickedIngredient(@Nullable T ingredient, GuiContainer guiContainer) {
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


	private <T extends GuiContainer> List<IGuiContainerHandler<T>> getActiveAdvancedGuiHandlers(T guiContainer) {
		List<IGuiContainerHandler<T>> activeAdvancedGuiHandler = new ArrayList<>();
		for (Map.Entry<Class<? extends GuiContainer>, List<IGuiContainerHandler<?>>> entry : guiHandlers.entrySet()) {
			Class<?> guiContainerClass = entry.getKey();
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
	public IGuiClickableArea getGuiClickableArea(GuiContainer guiContainer, double mouseX, double mouseY) {
		for (Map.Entry<Class<? extends GuiContainer>, List<IGuiContainerHandler<?>>> entry : guiHandlers.entrySet()) {
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
	private static <T extends GuiContainer> IGuiClickableArea getGuiClickableArea(Class<? extends T> handlerClass, IGuiContainerHandler<T> handler, GuiContainer guiContainer, double mouseX, double mouseY) {
		if (handlerClass.isInstance(guiContainer)) {
			T castContainer = handlerClass.cast(guiContainer);
			Collection<IGuiClickableArea> guiClickableAreas = handler.getGuiClickableAreas(castContainer);
			for (IGuiClickableArea guiClickableArea : guiClickableAreas) {
				if (MathUtil.contains(guiClickableArea.getArea(), mouseX, mouseY)) {
					return guiClickableArea;
				}
			}
		}
		return null;
	}
}
