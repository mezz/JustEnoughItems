package mezz.jei.library.gui.recipes.supplier.builder;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.widgets.ISlottedWidgetFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.gui.recipes.RecipeLayoutIngredientSupplier;
import mezz.jei.library.gui.recipes.layout.builder.RecipeSlotIngredients;
import mezz.jei.library.ingredients.IIngredientSupplier;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal version of {@link IRecipeLayoutBuilder} that can only return the ingredients,
 * but doesn't bother building anything for drawing on screen.
 */
public class IngredientSupplierBuilder implements IRecipeLayoutBuilder {
	private final List<IngredientSlotBuilder> slots = new ArrayList<>();
	private final IIngredientManager ingredientManager;

	public IngredientSupplierBuilder(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}

	@Override
	public IRecipeSlotBuilder addSlot(RecipeIngredientRole role, int x, int y) {
		IngredientSlotBuilder slot = new IngredientSlotBuilder(ingredientManager, role);
		this.slots.add(slot);
		return slot;
	}

	@Override
	public IRecipeSlotBuilder addSlotToWidget(RecipeIngredientRole role, ISlottedWidgetFactory<?> widgetFactory) {
		return addSlot(role, 0, 0);
	}

	@Override
	public IIngredientAcceptor<?> addInvisibleIngredients(RecipeIngredientRole role) {
		return addSlot(role, 0, 0);
	}

	@Override
	public void moveRecipeTransferButton(int posX, int posY) {

	}

	@Override
	public void setShapeless() {

	}

	@Override
	public void setShapeless(int posX, int posY) {

	}

	@Override
	public void createFocusLink(IIngredientAcceptor<?>... slots) {

	}

	public IIngredientSupplier buildIngredientSupplier() {
		List<RecipeSlotIngredients> ingredients = new ArrayList<>();
		for (IngredientSlotBuilder slot : this.slots) {
			ingredients.add(slot.getRecipeSlotIngredients());
		}
		return new RecipeLayoutIngredientSupplier(ingredients);
	}
}
