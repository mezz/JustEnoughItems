package mezz.jei.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IGhostIngredientHandler;
import mezz.jei.api.gui.IGuiProperties;
import mezz.jei.api.gui.IGuiScreenHandler;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.recipes.RecipeRegistry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import javax.annotation.Nullable;

public class JeiRuntime implements IJeiRuntime {

	private final RecipeRegistry recipeRegistry;
	private final IngredientListOverlay ingredientListOverlay;
	private final RecipesGui recipesGui;
	private final IngredientRegistry ingredientRegistry;
	private final List<IAdvancedGuiHandler<?>> advancedGuiHandlers;
	private final Map<Class, IGuiScreenHandler> guiScreenHandlers;
	private final Map<Class, IGhostIngredientHandler> ghostIngredientHandlers;
	private final IngredientFilter ingredientFilter;

	public JeiRuntime(
			RecipeRegistry recipeRegistry,
			IngredientListOverlay ingredientListOverlay,
			RecipesGui recipesGui,
			IngredientRegistry ingredientRegistry,
			List<IAdvancedGuiHandler<?>> advancedGuiHandlers,
			Map<Class, IGuiScreenHandler> guiScreenHandlers,
			Map<Class, IGhostIngredientHandler> ghostIngredientHandlers,
			IngredientFilter ingredientFilter
	) {
		this.recipeRegistry = recipeRegistry;
		this.ingredientListOverlay = ingredientListOverlay;
		this.recipesGui = recipesGui;
		this.ingredientRegistry = ingredientRegistry;
		this.advancedGuiHandlers = advancedGuiHandlers;
		this.guiScreenHandlers = guiScreenHandlers;
		this.ghostIngredientHandlers = ghostIngredientHandlers;
		this.ingredientFilter = ingredientFilter;
	}

	public void close() {
		this.recipesGui.close();
	}

	@Override
	public RecipeRegistry getRecipeRegistry() {
		return recipeRegistry;
	}

	@Override
	public IngredientListOverlay getItemListOverlay() {
		return ingredientListOverlay;
	}

	@Override
	public IngredientFilter getIngredientFilter() {
		return ingredientFilter;
	}

	@Override
	public IngredientListOverlay getIngredientListOverlay() {
		return ingredientListOverlay;
	}

	@Override
	public RecipesGui getRecipesGui() {
		return recipesGui;
	}

	public IngredientRegistry getIngredientRegistry() {
		return ingredientRegistry;
	}

	public <T extends GuiContainer> List<IAdvancedGuiHandler<T>> getActiveAdvancedGuiHandlers(T guiContainer) {
		List<IAdvancedGuiHandler<T>> activeAdvancedGuiHandler = new ArrayList<>();
		for (IAdvancedGuiHandler<?> advancedGuiHandler : advancedGuiHandlers) {
			Class<?> guiContainerClass = advancedGuiHandler.getGuiContainerClass();
			if (guiContainerClass.isInstance(guiContainer)) {
				//noinspection unchecked
				activeAdvancedGuiHandler.add((IAdvancedGuiHandler<T>) advancedGuiHandler);
			}
		}
		return activeAdvancedGuiHandler;
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
}
