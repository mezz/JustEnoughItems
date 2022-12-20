package mezz.jei.library.load.registration;

import com.google.common.base.Preconditions;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.gui.handlers.IScreenHandler;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.library.gui.GuiContainerHandlers;
import mezz.jei.library.gui.ScreenHelper;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiHandlerRegistration implements IGuiHandlerRegistration {
	private final GuiContainerHandlers guiContainerHandlers = new GuiContainerHandlers();
	private final List<IGlobalGuiHandler> globalGuiHandlers = new ArrayList<>();
	private final Map<Class<?>, IScreenHandler<?>> guiScreenHandlers = new HashMap<>();
	private final Map<Class<?>, IGhostIngredientHandler<?>> ghostIngredientHandlers = new HashMap<>();
	private final IJeiHelpers jeiHelpers;

	public GuiHandlerRegistration(IJeiHelpers jeiHelpers) {
		this.jeiHelpers = jeiHelpers;
	}

	@Override
	public <T extends AbstractContainerScreen<?>> void addGuiContainerHandler(Class<? extends T> guiClass, IGuiContainerHandler<T> guiHandler) {
		ErrorUtil.checkNotNull(guiClass, "guiClass");
		ErrorUtil.checkNotNull(guiHandler, "guiHandler");
		this.guiContainerHandlers.add(guiClass, guiHandler);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractContainerScreen<?>> void addGenericGuiContainerHandler(Class<? extends T> guiClass, IGuiContainerHandler<?> guiHandler) {
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

	@Unmodifiable
	private static final List<Class<? extends Screen>> ghostIngredientGuiBlacklist = List.of(
		Screen.class, InventoryScreen.class, CreativeModeInventoryScreen.class
	);

	@Override
	public <T extends Screen> void addGhostIngredientHandler(Class<T> guiClass, IGhostIngredientHandler<T> handler) {
		ErrorUtil.checkNotNull(guiClass, "guiClass");
		Preconditions.checkArgument(Screen.class.isAssignableFrom(guiClass), "guiClass must inherit from Screen");
		Preconditions.checkArgument(!ghostIngredientGuiBlacklist.contains(guiClass), "you cannot add a ghost ingredient handler for the following Guis, it would interfere with using JEI: %s", ghostIngredientGuiBlacklist);
		ErrorUtil.checkNotNull(handler, "handler");
		this.ghostIngredientHandlers.put(guiClass, handler);
	}

	public IScreenHelper createGuiScreenHelper(IIngredientManager ingredientManager) {
		return new ScreenHelper(ingredientManager, globalGuiHandlers, guiContainerHandlers, ghostIngredientHandlers, guiScreenHandlers);
	}

	@Override
	public IJeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}
}
