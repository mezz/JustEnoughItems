package mezz.jei.runtime;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.IIngredientFilter;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.recipes.RecipeRegistry;
import net.minecraft.client.gui.inventory.GuiContainer;

public class JeiRuntime implements IJeiRuntime {

	private final RecipeRegistry recipeRegistry;
	private final IngredientListOverlay ingredientListOverlay;
	private final RecipesGui recipesGui;
	private final IngredientRegistry ingredientRegistry;
	private final List<IAdvancedGuiHandler<?>> advancedGuiHandlers;
	private final IngredientFilter ingredientFilter;

	public JeiRuntime(
			RecipeRegistry recipeRegistry,
			IngredientListOverlay ingredientListOverlay,
			RecipesGui recipesGui,
			IngredientRegistry ingredientRegistry,
			List<IAdvancedGuiHandler<?>> advancedGuiHandlers,
			IngredientFilter ingredientFilter
	) {
		this.recipeRegistry = recipeRegistry;
		this.ingredientListOverlay = ingredientListOverlay;
		this.recipesGui = recipesGui;
		this.ingredientRegistry = ingredientRegistry;
		this.advancedGuiHandlers = advancedGuiHandlers;
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
	public IIngredientFilter getIngredientFilter() {
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

}
