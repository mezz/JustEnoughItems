package mezz.jei.gui;

import javax.annotation.Nullable;
import java.awt.Rectangle;
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
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IGhostIngredientHandler;
import mezz.jei.api.gui.IGlobalGuiHandler;
import mezz.jei.api.gui.IGuiProperties;
import mezz.jei.api.gui.IGuiScreenHandler;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.util.MathUtil;

public class GuiScreenHelper {
	private final IngredientRegistry ingredientRegistry;
	private final List<IGlobalGuiHandler> globalGuiHandlers;
	private final List<IAdvancedGuiHandler<?>> advancedGuiHandlers;
	private final Map<Class, IGhostIngredientHandler> ghostIngredientHandlers;
	private final Map<Class, IGuiScreenHandler> guiScreenHandlers;
	private Set<Rectangle> guiExclusionAreas = Collections.emptySet();

	public GuiScreenHelper(IngredientRegistry ingredientRegistry, List<IGlobalGuiHandler> globalGuiHandlers, List<IAdvancedGuiHandler<?>> advancedGuiHandlers, Map<Class, IGhostIngredientHandler> ghostIngredientHandlers, Map<Class, IGuiScreenHandler> guiScreenHandlers) {
		this.ingredientRegistry = ingredientRegistry;
		this.globalGuiHandlers = globalGuiHandlers;
		this.advancedGuiHandlers = advancedGuiHandlers;
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
		Set<Rectangle> guiAreas = getPluginsExclusionAreas();
		if (!guiAreas.equals(this.guiExclusionAreas)) {
			// make a defensive copy because Rectangle is mutable
			this.guiExclusionAreas = guiAreas.stream()
				.map(Rectangle::new)
				.collect(Collectors.toSet());
			return true;
		}
		return false;
	}

	public Set<Rectangle> getGuiExclusionAreas() {
		return guiExclusionAreas;
	}

	public boolean isInGuiExclusionArea(int mouseX, int mouseY) {
		return MathUtil.contains(guiExclusionAreas, mouseX, mouseY);
	}

	private Set<Rectangle> getPluginsExclusionAreas() {
		GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
		if (guiScreen == null) {
			return Collections.emptySet();
		}
		Set<Rectangle> allGuiExtraAreas = new HashSet<>();
		if (guiScreen instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) guiScreen;
			List<IAdvancedGuiHandler<GuiContainer>> activeAdvancedGuiHandlers = getActiveAdvancedGuiHandlers(guiContainer);
			for (IAdvancedGuiHandler<GuiContainer> advancedGuiHandler : activeAdvancedGuiHandlers) {
				List<Rectangle> guiExtraAreas = advancedGuiHandler.getGuiExtraAreas(guiContainer);
				if (guiExtraAreas != null) {
					allGuiExtraAreas.addAll(guiExtraAreas);
				}
			}
		}
		for (IGlobalGuiHandler globalGuiHandler : globalGuiHandlers) {
			Collection<Rectangle> guiExtraAreas = globalGuiHandler.getGuiExtraAreas();
			allGuiExtraAreas.addAll(guiExtraAreas);
		}
		return allGuiExtraAreas;
	}


	@Nullable
	public <T extends GuiContainer> IClickedIngredient<?> getPluginsIngredientUnderMouse(T guiContainer, int mouseX, int mouseY) {
		List<IAdvancedGuiHandler<T>> activeAdvancedGuiHandlers = getActiveAdvancedGuiHandlers(guiContainer);
		for (IAdvancedGuiHandler<T> advancedGuiHandler : activeAdvancedGuiHandlers) {
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
		if (ingredient != null && ingredientRegistry.isValidIngredient(ingredient)) {
			Rectangle area = null;
			Slot slotUnderMouse = guiContainer.getSlotUnderMouse();
			if (ingredient instanceof ItemStack && slotUnderMouse != null && ItemStack.areItemStacksEqual(slotUnderMouse.getStack(), (ItemStack) ingredient)) {
				area = new Rectangle(slotUnderMouse.xPos, slotUnderMouse.yPos, 16, 16);
			}
			return ClickedIngredient.create(ingredient, area);
		}
		return null;
	}


	private <T extends GuiContainer> List<IAdvancedGuiHandler<T>> getActiveAdvancedGuiHandlers(T guiContainer) {
		List<IAdvancedGuiHandler<T>> activeAdvancedGuiHandler = new ArrayList<>();
		for (IAdvancedGuiHandler<?> advancedGuiHandler : advancedGuiHandlers) {
			Class<?> guiContainerClass = advancedGuiHandler.getGuiContainerClass();
			if (guiContainerClass.isInstance(guiContainer)) {
				@SuppressWarnings("unchecked")
				IAdvancedGuiHandler<T> advancedGuiHandlerCast = (IAdvancedGuiHandler<T>) advancedGuiHandler;
				activeAdvancedGuiHandler.add(advancedGuiHandlerCast);
			}
		}
		return activeAdvancedGuiHandler;
	}
}
