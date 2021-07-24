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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

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
	private Set<Rect2i> guiExclusionAreas = Collections.emptySet();

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
		Set<Rect2i> guiAreas = getPluginsExclusionAreas();
		if (!MathUtil.equalRects(guiAreas, this.guiExclusionAreas)) {
			// make a defensive copy because Rectangle is mutable
			this.guiExclusionAreas = guiAreas.stream()
				.map(r -> new Rect2i(r.getX(), r.getY(), r.getWidth(), r.getHeight()))
				.collect(Collectors.toSet());
			return true;
		}
		return false;
	}

	public Set<Rect2i> getGuiExclusionAreas() {
		return guiExclusionAreas;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean isInGuiExclusionArea(double mouseX, double mouseY) {
		return MathUtil.contains(guiExclusionAreas, mouseX, mouseY);
	}

	private Set<Rect2i> getPluginsExclusionAreas() {
		Screen screen = Minecraft.getInstance().screen;
		if (screen == null) {
			return Collections.emptySet();
		}
		Set<Rect2i> allGuiExtraAreas = new HashSet<>();
		if (screen instanceof AbstractContainerScreen) {
			AbstractContainerScreen<?> guiContainer = (AbstractContainerScreen<?>) screen;
			Collection<Rect2i> guiExtraAreas = this.guiContainerHandlers.getGuiExtraAreas(guiContainer);
			allGuiExtraAreas.addAll(guiExtraAreas);
		}
		for (IGlobalGuiHandler globalGuiHandler : globalGuiHandlers) {
			Collection<Rect2i> guiExtraAreas = globalGuiHandler.getGuiExtraAreas();
			allGuiExtraAreas.addAll(guiExtraAreas);
		}
		return allGuiExtraAreas;
	}

	@Nullable
	public <T extends AbstractContainerScreen<?>> IClickedIngredient<?> getPluginsIngredientUnderMouse(T guiContainer, double mouseX, double mouseY) {
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
	private <T> IClickedIngredient<T> createClickedIngredient(@Nullable T ingredient, AbstractContainerScreen<?> guiContainer) {
		if (ingredient != null && ingredientManager.isValidIngredient(ingredient)) {
			Rect2i area = null;
			Slot slotUnderMouse = guiContainer.getSlotUnderMouse();
			if (ingredient instanceof ItemStack && slotUnderMouse != null && ItemStack.matches(slotUnderMouse.getItem(), (ItemStack) ingredient)) {
				area = new Rect2i(slotUnderMouse.x, slotUnderMouse.y, 16, 16);
			}
			return ClickedIngredient.create(ingredient, area);
		}
		return null;
	}

	@Nullable
	public IGuiClickableArea getGuiClickableArea(AbstractContainerScreen<?> guiContainer, double mouseX, double mouseY) {
		return this.guiContainerHandlers.getGuiClickableArea(guiContainer, mouseX, mouseY);
	}

}
