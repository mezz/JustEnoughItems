package mezz.jei.library.gui.recipes.supplier.builder;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import mezz.jei.library.ingredients.RecipeIngredientSupplier;
import mezz.jei.library.ingredients.RecipeIngredientSupplier.FocusLink;
import mezz.jei.library.ingredients.SlotIngredient;
import net.minecraft.util.context.ContextMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Minimal version of {@link IRecipeLayoutBuilder} that can only return the ingredients,
 * but doesn't bother building real slots or anything else for drawing on screen.
 */
public class IngredientSupplierBuilder implements IRecipeLayoutBuilder {
	private final IIngredientManagerInternal ingredientManager;
	private final ContextMap contextMap;
	private final Map<RecipeIngredientRole, List<IngredientSlotBuilder>> ingredientSlotBuilders;
	private final List<List<IngredientSlotBuilder>> focusLinkedSlots = new ArrayList<>();

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
		IngredientSlotBuilder slot = new IngredientSlotBuilder(ingredientManager, contextMap, role);
		ingredientSlotBuilders.computeIfAbsent(role, key -> new ArrayList<>())
			.add(slot);
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
		List<IngredientSlotBuilder> builders = Arrays.stream(slots)
			.map(IngredientSlotBuilder.class::cast)
			.toList();
		long ingredientCounts = builders.stream()
			.map(IngredientSlotBuilder::getAllSlotIngredients)
			.mapToInt(Collection::size)
			.distinct()
			.count();
		if (ingredientCounts > 1) {
			throw new IllegalArgumentException("All slots must have the same number of ingredients in order to create a focus link.");
		}
		this.focusLinkedSlots.add(builders);
	}

	public RecipeIngredientSupplier buildIngredientSupplier() {
		Map<RecipeIngredientRole, List<SlotIngredient<?>>> ingredientsByRole = new EnumMap<>(RecipeIngredientRole.class);
		ingredientSlotBuilders.forEach(
			(role, builders) -> ingredientsByRole.put(
				role,
				builders.stream()
					.flatMap(builder -> builder.getAllSlotIngredients().stream())
					.filter(Objects::nonNull)
					.toList()
			)
		);
		List<FocusLink> focusLinks = this.focusLinkedSlots.stream()
			.map(linkedSlots -> linkedSlots.stream()
				.map(slot -> new FocusLink.Slot(slot.getRole(), slot.getAllSlotIngredients()))
				.toList()
			)
			.map(FocusLink::new)
			.toList();
		return new RecipeIngredientSupplier(ingredientsByRole, focusLinks);
	}
}
