package mezz.jei.gui.recipes.builder;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.Focus;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.ingredients.IIngredientSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class RecipeLayoutBuilder implements IRecipeLayoutBuilder, IIngredientSupplier {
	private final List<IRecipeLayoutSlotSource> slots = new ArrayList<>();
	private final IIngredientManager ingredientManager;
	private boolean shapeless = false;
	private int recipeTransferX = -1;
	private int recipeTransferY = -1;
	private int shapelessX = -1;
	private int shapelessY = -1;

	public RecipeLayoutBuilder(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}

	@Override
	public IRecipeSlotBuilder addSlot(RecipeIngredientRole role, int x, int y) {
		RecipeSlotBuilder slotBuilder = new RecipeSlotBuilder(ingredientManager, role, x, y);
		this.slots.add(slotBuilder);
		return slotBuilder;
	}

	@Override
	public IIngredientAcceptor<?> addInvisibleIngredients(RecipeIngredientRole role) {
		InvisibleRecipeLayoutSlotSource slot = new InvisibleRecipeLayoutSlotSource(this.ingredientManager, role);
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

	@Override
	public void setShapeless(int posX, int posY) {
		this.shapeless = true;
		this.shapelessX = posX;
		this.shapelessY = posY;
	}

	/**
	 * Returns `true` if this builder has been used,
	 * useful for detecting when plugins use the builder or need legacy support.
	 */
	public boolean isUsed() {
		return !this.slots.isEmpty();
	}

	public <R> void setRecipeLayout(RecipeLayout<R> recipeLayout, List<Focus<?>> focuses) {
		if (this.shapeless) {
			if (this.shapelessX >= 0 && this.shapelessY >= 0) {
				recipeLayout.setShapeless(this.shapelessX, this.shapelessY);
			} else {
				recipeLayout.setShapeless();
			}
		}
		if (this.recipeTransferX >= 0 && this.recipeTransferY >= 0) {
			recipeLayout.moveRecipeTransferButton(this.recipeTransferX, this.recipeTransferY);
		}

		for (IRecipeLayoutSlotSource slot : this.slots) {
			slot.setRecipeLayout(recipeLayout, focuses);
		}
	}

	@Override
	public Stream<? extends IIngredientType<?>> getIngredientTypes(RecipeIngredientRole role) {
		return this.slots.stream()
			.filter(slot -> slot.getRole() == role)
			.flatMap(IRecipeLayoutSlotSource::getIngredientTypes)
			.distinct();
	}

	@Override
	public <T> Stream<T> getIngredientStream(IIngredientType<T> ingredientType, RecipeIngredientRole role) {
		return this.slots.stream()
			.filter(slot -> slot.getRole() == role)
			.flatMap(slot -> slot.getIngredients(ingredientType));
	}
}
