package mezz.jei.gui.recipes.builder;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeLayoutSlotBuilder;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.ingredients.IIngredientSupplier;
import mezz.jei.ingredients.IngredientsForTypeMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class RecipeLayoutBuilder implements IRecipeLayoutBuilder, IIngredientSupplier {
	private final List<IRecipeLayoutSlotSource> slots = new ArrayList<>();
	private final IntSet slotIds = new IntArraySet();
	private boolean shapeless = false;
	private int recipeTransferX = -1;
	private int recipeTransferY = -1;

	@Override
	public IRecipeLayoutSlotBuilder addSlot(int slotIndex, RecipeIngredientRole role, int x, int y) {
		if (!this.slotIds.add(slotIndex)) {
			throw new IllegalArgumentException("A slot has already been created at slot index " + slotIndex);
		}
		RecipeLayoutSlotBuilder slotBuilder = new RecipeLayoutSlotBuilder(slotIndex, role, x, y);
		this.slots.add(slotBuilder);
		return slotBuilder;
	}

	@Override
	public IIngredientAcceptor<?> addInvisibleIngredients(RecipeIngredientRole role) {
		InvisibleRecipeLayoutSlotSource slot = new InvisibleRecipeLayoutSlotSource(role);
		this.slots.add(slot);
		return slot;
	}

	@Override
	public void moveRecipeTransferButton(int posX, int posY) {
		this.recipeTransferX = posX;
		this.recipeTransferY = posY;
	}

	@Override
	public void setShapeless() {
		this.shapeless = true;
	}

	/**
	 * Returns `true` if this builder has been used,
	 * useful for detecting when plugins use the builder or need legacy support.
	 */
	public boolean isUsed() {
		return !this.slots.isEmpty();
	}

	public <R> void setRecipeLayout(RecipeLayout<R> recipeLayout) {
		if (this.shapeless) {
			recipeLayout.setShapeless();
		}
		if (this.recipeTransferX >= 0 && this.recipeTransferY >= 0) {
			recipeLayout.moveRecipeTransferButton(this.recipeTransferX, this.recipeTransferY);
		}

		for (IRecipeLayoutSlotSource slot : this.slots) {
			slot.setRecipeLayout(recipeLayout);
		}
	}

	@Override
	public List<? extends IIngredientType<?>> getIngredientTypes(RecipeIngredientRole role) {
		return this.slots.stream()
			.filter(slot -> slot.getRole() == role)
			.map(IRecipeLayoutSlotSource::getIngredientsForTypeMap)
			.map(IngredientsForTypeMap::getIngredientTypes)
			.flatMap(Collection::stream)
			.distinct()
			.toList();
	}

	@Override
	public <T> Stream<T> getIngredientStream(IIngredientType<T> ingredientType, RecipeIngredientRole role) {
		return this.slots.stream()
			.filter(slot -> slot.getRole() == role)
			.map(IRecipeLayoutSlotSource::getIngredientsForTypeMap)
			.map(ingredientsForTypeMap -> ingredientsForTypeMap.getIngredients(ingredientType))
			.flatMap(Collection::stream);
	}
}
