package mezz.jei.gui;

import javax.annotation.Nullable;
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
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.util.MathUtil;

public class GuiScreenHelper {
	private final IngredientManager ingredientManager;
	private final List<IGlobalGuiHandler> globalGuiHandlers;
	private final GuiContainerHandlers guiContainerHandlers;
	private final Map<Class<?>, IGhostIngredientHandler<?>> ghostIngredientHandlers;
	private final Map<Class<?>, IScreenHandler<?>> guiScreenHandlers;
	private Set<Rectangle2d> guiExclusionAreas = Collections.emptySet();

	public GuiScreenHelper(
		IngredientManager ingredientManager,
		List<IGlobalGuiHandler> globalGuiHandlers,
		GuiContainerHandlers guiContainerHandlers,
		Map<Class<?>, IGhostIngredientHandler<?>> ghostIngredientHandlers,
		Map<Class<?>, IScreenHandler<?>> guiScreenHandlers
	) {
		this.ingredientManager = ingredientManager;
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

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
			ContainerScreen<?> guiContainer = (ContainerScreen<?>) screen;
			Collection<Rectangle2d> guiExtraAreas = this.guiContainerHandlers.getGuiExtraAreas(guiContainer);
			allGuiExtraAreas.addAll(guiExtraAreas);
		}
		for (IGlobalGuiHandler globalGuiHandler : globalGuiHandlers) {
			Collection<Rectangle2d> guiExtraAreas = globalGuiHandler.getGuiExtraAreas();
			allGuiExtraAreas.addAll(guiExtraAreas);
		}
		return allGuiExtraAreas;
	}

	@Nullable
	public <T extends ContainerScreen<?>> IClickedIngredient<?> getPluginsIngredientUnderMouse(T guiContainer, double mouseX, double mouseY) {
		List<IGuiContainerHandler<? super T>> activeAdvancedGuiHandlers = this.guiContainerHandlers.getActiveGuiHandlers(guiContainer);
		for (IGuiContainerHandler<? super T> advancedGuiHandler : activeAdvancedGuiHandlers) {
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

	@Nullable
	private <T> IClickedIngredient<T> createClickedIngredient(@Nullable T ingredient, ContainerScreen<?> guiContainer) {
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

	@Nullable
	public IGuiClickableArea getGuiClickableArea(ContainerScreen<?> guiContainer, double mouseX, double mouseY) {
		return this.guiContainerHandlers.getGuiClickableArea(guiContainer, mouseX, mouseY);
	}

}
