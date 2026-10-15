package mezz.jei.library.gui.recipes.supplier.builder;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.ingredients.RecipeIngredientSupplier;
import mezz.jei.library.ingredients.RecipeIngredientSupplier.FocusLink;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Collects recipe ingredients and focus links for selected roles without building a drawable layout.
 */
public class IngredientSupplierBuilder implements IRecipeLayoutBuilder {
	private final IIngredientManager ingredientManager;
	private final Set<RecipeIngredientRole> roles;
	private final IgnoredIngredientSlotBuilder ignoredSlot = new IgnoredIngredientSlotBuilder();
	private final Map<RecipeIngredientRole, List<IngredientSlotBuilder>> ingredientSlotBuilders;
	private final List<List<IngredientSlotBuilder>> focusLinkedSlots = new ArrayList<>();

	public IngredientSupplierBuilder(IIngredientManager ingredientManager) {
		this(ingredientManager, EnumSet.allOf(RecipeIngredientRole.class));
	}

	public IngredientSupplierBuilder(IIngredientManager ingredientManager, Set<RecipeIngredientRole> roles) {
		this.roles = Set.copyOf(roles);
		this.ingredientManager = ingredientManager;
		this.ingredientSlotBuilders = new EnumMap<>(RecipeIngredientRole.class);
	}

	@Override
	public IRecipeSlotBuilder addSlot(RecipeIngredientRole role, int x, int y) {
		return addSlot(role);
	}

	@Override
	public IRecipeSlotBuilder addSlot(RecipeIngredientRole role) {
		Objects.requireNonNull(role, "role");
		if (!roles.contains(role)) {
			return ignoredSlot;
		}
		IngredientSlotBuilder slot = new IngredientSlotBuilder(ingredientManager, role);
		ingredientSlotBuilders.computeIfAbsent(role, key -> new ArrayList<>())
			.add(slot);
		return slot;
	}

	@SuppressWarnings("removal")
	@Override
	public IRecipeSlotBuilder addSlotToWidget(RecipeIngredientRole role, mezz.jei.api.gui.widgets.ISlottedWidgetFactory<?> widgetFactory) {
		return addSlot(role);
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
		createFocusLink(List.of(slots));
	}

	@Override
	public void createFocusLink(Collection<? extends IIngredientAcceptor<?>> slots) {
		List<IngredientSlotBuilder> builders = new ArrayList<>(slots.size());
		for (IIngredientAcceptor<?> slot : slots) {
			if (slot instanceof IngredientSlotBuilder slotBuilder) {
				builders.add(slotBuilder);
			}
		}
		if (builders.isEmpty()) {
			return;
		}
		long ingredientCounts = builders.stream()
			.map(IngredientSlotBuilder::getAllIngredients)
			.mapToInt(Collection::size)
			.distinct()
			.count();
		if (ingredientCounts > 1) {
			throw new IllegalArgumentException("All slots must have the same number of ingredients in order to create a focus link.");
		}
		this.focusLinkedSlots.add(builders);
	}

	public RecipeIngredientSupplier buildIngredientSupplier() {
		Map<RecipeIngredientRole, List<ITypedIngredient<?>>> ingredientsByRole = new EnumMap<>(RecipeIngredientRole.class);
		ingredientSlotBuilders.forEach(
			(role, builders) -> ingredientsByRole.put(
				role,
				builders.stream()
					.flatMap(builder -> builder.getAllIngredients().stream())
					.filter(Objects::nonNull)
					.toList()
			)
		);
		List<FocusLink> focusLinks = this.focusLinkedSlots.stream()
			.map(linkedSlots -> linkedSlots.stream()
				.map(slot -> new FocusLink.Slot(slot.getRole(), slot.getAllIngredients()))
				.toList()
			)
			.map(FocusLink::new)
			.toList();
		return new RecipeIngredientSupplier(ingredientsByRole, focusLinks);
	}
}
