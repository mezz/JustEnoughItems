package mezz.jei.load.registration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import mezz.jei.api.gui.IGhostIngredientHandler;
import mezz.jei.api.gui.IGlobalGuiHandler;
import mezz.jei.api.gui.IGuiContainerHandler;
import mezz.jei.api.gui.IGuiScreenHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.collect.ListMultiMap;
import mezz.jei.util.ErrorUtil;

public class GuiHandlerRegistration implements IGuiHandlerRegistration {
	private final ListMultiMap<Class<? extends GuiContainer>, IGuiContainerHandler<?>> guiHandlers = new ListMultiMap<>();
	private final List<IGlobalGuiHandler> globalGuiHandlers = new ArrayList<>();
	private final Map<Class, IGuiScreenHandler> guiScreenHandlers = new HashMap<>();
	private final Map<Class, IGhostIngredientHandler> ghostIngredientHandlers = new HashMap<>();

	@Override
	public <T extends GuiContainer> void addGuiContainerHandler(Class<? extends T> guiClass, IGuiContainerHandler<T> guiHandler) {
		ErrorUtil.checkNotNull(guiClass, "guiClass");
		ErrorUtil.checkNotNull(guiHandler, "guiHandler");
		this.guiHandlers.put(guiClass, guiHandler);
	}

	@Override
	public void addGlobalGuiHandler(IGlobalGuiHandler globalGuiHandler) {
		ErrorUtil.checkNotNull(globalGuiHandler, "globalGuiHandler");
		this.globalGuiHandlers.add(globalGuiHandler);
	}

	@Override
	public <T extends GuiScreen> void addGuiScreenHandler(Class<T> guiClass, IGuiScreenHandler<T> handler) {
		ErrorUtil.checkNotNull(guiClass, "guiClass");
		Preconditions.checkArgument(GuiScreen.class.isAssignableFrom(guiClass), "guiClass must inherit from GuiScreen");
		Preconditions.checkArgument(!GuiScreen.class.equals(guiClass), "you cannot add a handler for GuiScreen, only a subclass.");
		ErrorUtil.checkNotNull(handler, "guiScreenHandler");
		this.guiScreenHandlers.put(guiClass, handler);
	}

	private static final List<Class<? extends GuiScreen>> ghostIngredientGuiBlacklist = ImmutableList.of(
		GuiScreen.class, GuiInventory.class, GuiContainerCreative.class
	);

	@Override
	public <T extends GuiScreen> void addGhostIngredientHandler(Class<T> guiClass, IGhostIngredientHandler<T> handler) {
		ErrorUtil.checkNotNull(guiClass, "guiClass");
		Preconditions.checkArgument(GuiScreen.class.isAssignableFrom(guiClass), "guiClass must inherit from GuiScreen");
		Preconditions.checkArgument(!ghostIngredientGuiBlacklist.contains(guiClass), "you cannot add a ghost ingredient handler for the following Guis, it would interfere with using JEI: %s", ghostIngredientGuiBlacklist);
		ErrorUtil.checkNotNull(handler, "handler");
		this.ghostIngredientHandlers.put(guiClass, handler);
	}


	public ListMultiMap<Class<? extends GuiContainer>, IGuiContainerHandler<?>> getGuiHandlers() {
		return guiHandlers;
	}

	public List<IGlobalGuiHandler> getGlobalGuiHandlers() {
		return globalGuiHandlers;
	}

	public Map<Class, IGuiScreenHandler> getGuiScreenHandlers() {
		return guiScreenHandlers;
	}

	public Map<Class, IGhostIngredientHandler> getGhostIngredientHandlers() {
		return ghostIngredientHandlers;
	}

}
