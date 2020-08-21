package mezz.jei.load.registration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mezz.jei.gui.GuiContainerHandlers;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.gui.handlers.IScreenHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.util.ErrorUtil;

public class GuiHandlerRegistration implements IGuiHandlerRegistration {
	private final GuiContainerHandlers guiContainerHandlers = new GuiContainerHandlers();
	private final List<IGlobalGuiHandler> globalGuiHandlers = new ArrayList<>();
	private final Map<Class<?>, IScreenHandler<?>> guiScreenHandlers = new HashMap<>();
	private final Map<Class<?>, IGhostIngredientHandler<?>> ghostIngredientHandlers = new HashMap<>();

	@Override
	public <T extends ContainerScreen<?>> void addGuiContainerHandler(Class<? extends T> guiClass, IGuiContainerHandler<T> guiHandler) {
		ErrorUtil.checkNotNull(guiClass, "guiClass");
		ErrorUtil.checkNotNull(guiHandler, "guiHandler");
		this.guiContainerHandlers.add(guiClass, guiHandler);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ContainerScreen<?>> void addGenericGuiContainerHandler(Class<? extends T> guiClass, IGuiContainerHandler<?> guiHandler) {
		ErrorUtil.checkNotNull(guiClass, "guiClass");
		ErrorUtil.checkNotNull(guiHandler, "guiHandler");
		this.guiContainerHandlers.add(guiClass, (IGuiContainerHandler<? super T>) guiHandler);
	}

	@Override
	public void addGlobalGuiHandler(IGlobalGuiHandler globalGuiHandler) {
		ErrorUtil.checkNotNull(globalGuiHandler, "globalGuiHandler");
		this.globalGuiHandlers.add(globalGuiHandler);
	}

	@Override
	public <T extends Screen> void addGuiScreenHandler(Class<T> guiClass, IScreenHandler<T> handler) {
		ErrorUtil.checkNotNull(guiClass, "guiClass");
		Preconditions.checkArgument(Screen.class.isAssignableFrom(guiClass), "guiClass must inherit from Screen");
		Preconditions.checkArgument(!Screen.class.equals(guiClass), "you cannot add a handler for Screen, only a subclass.");
		ErrorUtil.checkNotNull(handler, "guiScreenHandler");
		this.guiScreenHandlers.put(guiClass, handler);
	}

	private static final List<Class<? extends Screen>> ghostIngredientGuiBlacklist = ImmutableList.of(
		Screen.class, InventoryScreen.class, CreativeScreen.class
	);

	@Override
	public <T extends Screen> void addGhostIngredientHandler(Class<T> guiClass, IGhostIngredientHandler<T> handler) {
		ErrorUtil.checkNotNull(guiClass, "guiClass");
		Preconditions.checkArgument(Screen.class.isAssignableFrom(guiClass), "guiClass must inherit from Screen");
		Preconditions.checkArgument(!ghostIngredientGuiBlacklist.contains(guiClass), "you cannot add a ghost ingredient handler for the following Guis, it would interfere with using JEI: %s", ghostIngredientGuiBlacklist);
		ErrorUtil.checkNotNull(handler, "handler");
		this.ghostIngredientHandlers.put(guiClass, handler);
	}


	public GuiContainerHandlers getGuiContainerHandlers() {
		return guiContainerHandlers;
	}

	public List<IGlobalGuiHandler> getGlobalGuiHandlers() {
		return globalGuiHandlers;
	}

	public Map<Class<?>, IScreenHandler<?>> getGuiScreenHandlers() {
		return guiScreenHandlers;
	}

	public Map<Class<?>, IGhostIngredientHandler<?>> getGhostIngredientHandlers() {
		return ghostIngredientHandlers;
	}

}
