package mezz.jei.library.gui.recipes.supplier.builder;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import mezz.jei.library.ingredients.RecipeIngredientSupplier;
import mezz.jei.library.ingredients.SlotIngredient;
import net.minecraft.util.context.ContextMap;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal version of {@link IRecipeLayoutBuilder} that can only return the ingredients,
 * but doesn't bother building real slots or anything else for drawing on screen.
 */
public class IngredientSupplierBuilder implements IRecipeLayoutBuilder {
	private final IIngredientManagerInternal ingredientManager;
	private final ContextMap contextMap;
	private final Map<RecipeIngredientRole, IngredientSlotBuilder> ingredientSlotBuilders;

	public IngredientSupplierBuilder(IIngredientManagerInternal ingredientManager, ContextMap contextMap) {
		this.ingredientManager = ingredientManager;
		this.contextMap = contextMap;
		this.ingredientSlotBuilders = new EnumMap<>(RecipeIngredientRole.class);
	}

	@Override
	public IRecipeSlotBuilder addSlot(RecipeIngredientRole role, int x, int y) {
		return addSlot(role);
	}

	@Override
	public IRecipeSlotBuilder addSlot(RecipeIngredientRole role) {
		IngredientSlotBuilder slot = ingredientSlotBuilders.get(role);
		if (slot == null) {
			slot = new IngredientSlotBuilder(ingredientManager, contextMap, role);
			ingredientSlotBuilders.put(role, slot);
		}
		return slot;
	}

	@Override
	public IIngredientAcceptor<?> addInvisibleIngredients(RecipeIngredientRole role) {
		return addSlot(role);
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

	public RecipeIngredientSupplier buildIngredientSupplier() {
		Map<RecipeIngredientRole, List<SlotIngredient<?>>> ingredientsByRole = new EnumMap<>(RecipeIngredientRole.class);
		ingredientSlotBuilders.forEach(
			(role, builder) -> ingredientsByRole.put(role, builder.getAllSlotIngredients())
		);
		return new RecipeIngredientSupplier(ingredientsByRole);
	}
}
