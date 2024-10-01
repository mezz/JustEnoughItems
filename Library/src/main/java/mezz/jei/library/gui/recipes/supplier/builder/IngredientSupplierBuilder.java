package mezz.jei.library.gui.recipes.supplier.builder;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.widgets.ISlottedWidgetFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.gui.recipes.RecipeLayoutIngredientSupplier;
import mezz.jei.library.ingredients.IIngredientSupplier;

import java.util.EnumMap;
import java.util.Map;

/**
 * Minimal version of {@link IRecipeLayoutBuilder} that can only return the ingredients,
 * but doesn't bother building real slots or anything else for drawing on screen.
 */
public class IngredientSupplierBuilder implements IRecipeLayoutBuilder {
	private final IIngredientManager ingredientManager;
	private final Map<RecipeIngredientRole, IngredientSlotBuilder> ingredientSlotBuilders;

	public IngredientSupplierBuilder(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
		this.ingredientSlotBuilders = new EnumMap<>(RecipeIngredientRole.class);
	}

	@Override
	public IRecipeSlotBuilder addSlot(RecipeIngredientRole role, int x, int y) {
		IngredientSlotBuilder slot = ingredientSlotBuilders.get(role);
		if (slot == null) {
			slot = new IngredientSlotBuilder(ingredientManager);
			ingredientSlotBuilders.put(role, slot);
		}
		return slot;
	}

	@Override
	public IRecipeSlotBuilder addSlot(RecipeIngredientRole role) {
		IngredientSlotBuilder slot = ingredientSlotBuilders.get(role);
		if (slot == null) {
			slot = new IngredientSlotBuilder(ingredientManager);
			ingredientSlotBuilders.put(role, slot);
		}
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
		return new RecipeLayoutIngredientSupplier(this.ingredientSlotBuilders);
	}
}
