package mezz.jei.library.gui.recipes;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.library.ingredients.IIngredientSupplier;
import mezz.jei.library.gui.recipes.layout.builder.IRecipeLayoutSlotSource;
import mezz.jei.library.gui.recipes.layout.builder.InvisibleRecipeLayoutSlotSource;
import mezz.jei.library.gui.recipes.layout.builder.RecipeSlotBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.stream.Stream;

public class RecipeLayoutBuilder implements IRecipeLayoutBuilder, IIngredientSupplier {
	private final List<IRecipeLayoutSlotSource> slots = new ArrayList<>();
	private final List<List<IRecipeLayoutSlotSource>> focusLinkedSlots = new ArrayList<>();
	private final IIngredientManager ingredientManager;
	private final int ingredientCycleOffset;
	private boolean shapeless = false;
	private int recipeTransferX = -1;
	private int recipeTransferY = -1;
	private int shapelessX = -1;
	private int shapelessY = -1;

	public RecipeLayoutBuilder(IIngredientManager ingredientManager, int ingredientCycleOffset) {
		this.ingredientManager = ingredientManager;
		this.ingredientCycleOffset = ingredientCycleOffset;
	}

	@Override
	public IRecipeSlotBuilder addSlot(RecipeIngredientRole role, int x, int y) {
		RecipeSlotBuilder slotBuilder = new RecipeSlotBuilder(ingredientManager, role, x, y, ingredientCycleOffset);
		this.slots.add(slotBuilder);
		return slotBuilder;
	}

	@Override
	public IIngredientAcceptor<?> addInvisibleIngredients(RecipeIngredientRole role) {
		InvisibleRecipeLayoutSlotSource slot = new InvisibleRecipeLayoutSlotSource(ingredientManager, role);
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

	@Override
	public void createFocusLink(IIngredientAcceptor<?>... slots) {
		List<IRecipeLayoutSlotSource> builders = Arrays.stream(slots)
			.map(IRecipeLayoutSlotSource.class::cast)
			.toList();

		// The focus-linked slots should have the same number of ingredients.
		// Users can technically add more ingredients to the slots later,
		// but it's probably not worth the effort of enforcing this very strictly.
		IntSummaryStatistics stats = builders.stream()
			.mapToInt(IRecipeLayoutSlotSource::getIngredientCount)
			.summaryStatistics();
		if (stats.getMin() != stats.getMax()) {
			throw new IllegalArgumentException(
				"All slots must have the same number of ingredients in order to create a focus link. " +
					String.format("slot stats: %s", stats)
			);
		}

		this.slots.removeAll(builders);
		this.focusLinkedSlots.add(builders);
	}

	/**
	 * Returns `true` if this builder has been used,
	 * useful for detecting when plugins use the builder or need legacy support.
	 */
	public boolean isUsed() {
		return !this.slots.isEmpty() || !this.focusLinkedSlots.isEmpty();
	}

	public <R> void setRecipeLayout(RecipeLayout<R> recipeLayout, IFocusGroup focuses, IIngredientVisibility ingredientVisibility) {
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
			IntSet focusMatches = slot.getMatches(focuses);
			slot.setRecipeSlots(recipeLayout.getRecipeSlots(), focusMatches, ingredientVisibility);
		}

		for (List<IRecipeLayoutSlotSource> slots : this.focusLinkedSlots) {
			IntSet focusMatches = new IntArraySet();
			for (IRecipeLayoutSlotSource slot : slots) {
				focusMatches.addAll(slot.getMatches(focuses));
			}
			for (IRecipeLayoutSlotSource slot : slots) {
				slot.setRecipeSlots(recipeLayout.getRecipeSlots(), focusMatches, ingredientVisibility);
			}
		}
	}

	private Stream<IRecipeLayoutSlotSource> slotStream() {
		return Stream.concat(
			this.slots.stream(),
			this.focusLinkedSlots.stream().flatMap(Collection::stream)
		);
	}

	@Override
	public Stream<? extends IIngredientType<?>> getIngredientTypes(RecipeIngredientRole role) {
		return slotStream()
			.filter(slot -> slot.getRole() == role)
			.flatMap(IRecipeLayoutSlotSource::getIngredientTypes)
			.distinct();
	}

	@Override
	public <T> Stream<T> getIngredientStream(IIngredientType<T> ingredientType, RecipeIngredientRole role) {
		return slotStream()
			.filter(slot -> slot.getRole() == role)
			.flatMap(slot -> slot.getIngredients(ingredientType));
	}
}
