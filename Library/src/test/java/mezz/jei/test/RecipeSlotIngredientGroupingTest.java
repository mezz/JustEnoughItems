package mezz.jei.test;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.library.gui.ingredients.RecipeSlotIngredients;
import mezz.jei.library.ingredients.SlotDisplayData;
import mezz.jei.library.ingredients.SlotDisplayInfo;
import mezz.jei.library.ingredients.SlotIngredient;
import mezz.jei.common.ingredients.TypedIngredient;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

public class RecipeSlotIngredientGroupingTest {
	private static final IIngredientType<String> INGREDIENT_TYPE = () -> String.class;

	@BeforeAll
	static void setup() {
		SharedConstants.setVersion(DetectedVersion.BUILT_IN);
		Bootstrap.bootStrap();
	}

	@Test
	void displayedIngredientUsesOnlyItsSlotDisplayGroup() {
		// Setup: two interpreted display groups contribute ingredients to the same recipe slot.
		ITypedIngredient<String> firstA = createIngredient("first a");
		ITypedIngredient<String> secondA = createIngredient("second a");
		ITypedIngredient<String> firstB = createIngredient("first b");
		SlotDisplayData<String> groupA = new SlotDisplayData<>(
			List.of(firstA, secondA),
			SlotDisplayInfo.EMPTY
		);
		SlotDisplayData<String> groupB = new SlotDisplayData<>(
			List.of(firstB),
			SlotDisplayInfo.EMPTY
		);
		SlotIngredient<String> displayed = new SlotIngredient<>(firstA, groupA);
		List<SlotIngredient<?>> slotIngredients = List.of(
			displayed,
			new SlotIngredient<>(firstB, groupB)
		);

		// Operation: get the display group for an ingredient from the first interpreted group.
		List<String> displayGroup = RecipeSlotIngredients.getDisplayGroupIngredients(slotIngredients, displayed)
			.stream()
			.map(SlotIngredient::typedIngredient)
			.map(ingredient -> ingredient.getIngredient(INGREDIENT_TYPE))
			.flatMap(Optional::stream)
			.toList();

		// Assertions: only ingredients declared by the displayed ingredient's group are returned.
		assertEquals(List.of("first a", "second a"), displayGroup);
	}

	@Test
	void displayedCandidatesUseOnlyTheCurrentExpandedGroup() {
		// Setup: two expanded display groups contribute visible candidates to the same recipe slot.
		ITypedIngredient<String> firstA = createIngredient("first a");
		ITypedIngredient<String> secondA = createIngredient("second a");
		ITypedIngredient<String> firstB = createIngredient("first b");
		SlotDisplayData<String> groupA = new SlotDisplayData<>(
			List.of(firstA, secondA),
			SlotDisplayInfo.EMPTY
		);
		SlotDisplayData<String> groupB = new SlotDisplayData<>(
			List.of(firstB),
			SlotDisplayInfo.EMPTY
		);
		SlotIngredient<String> displayed = new SlotIngredient<>(secondA, groupA);
		List<SlotIngredient<?>> displayIngredients = List.of(
			new SlotIngredient<>(firstA, groupA),
			displayed,
			new SlotIngredient<>(firstB, groupB)
		);

		// Operation: get candidates for the group containing the currently displayed ingredient.
		List<String> candidates = RecipeSlotIngredients.getDisplayedIngredientsInGroup(displayIngredients, displayed)
			.map(ingredient -> ingredient.getIngredient(INGREDIENT_TYPE))
			.flatMap(Optional::stream)
			.toList();

		// Assertions: the candidate browser does not mix in ingredients from another display group.
		assertEquals(List.of("first a", "second a"), candidates);
	}

	@Test
	void displayedIngredientCanBeTemporarilySelectedFromTheCurrentCandidates() {
		ITypedIngredient<String> first = createIngredient("first");
		ITypedIngredient<String> hovered = createIngredient("hovered");
		SlotIngredient<String> hoveredSlotIngredient = new SlotIngredient<>(hovered);
		List<SlotIngredient<?>> displayIngredients = List.of(
			new SlotIngredient<>(first),
			hoveredSlotIngredient
		);

		Optional<SlotIngredient<?>> selected = RecipeSlotIngredients.getDisplayedIngredient(displayIngredients, hovered);

		assertSame(hoveredSlotIngredient, selected.orElseThrow());
	}

	@Test
	void multipleDisplayGroupsAreNotTreatedAsOneTag() {
		ITypedIngredient<String> firstA = createIngredient("first a");
		ITypedIngredient<String> firstB = createIngredient("first b");
		SlotDisplayData<String> groupA = new SlotDisplayData<>(List.of(firstA), SlotDisplayInfo.EMPTY);
		SlotDisplayData<String> groupB = new SlotDisplayData<>(List.of(firstB), SlotDisplayInfo.EMPTY);
		List<SlotIngredient<?>> slotIngredients = List.of(
			new SlotIngredient<>(firstA, groupA),
			new SlotIngredient<>(firstB, groupB)
		);

		Optional<?> tagKey = RecipeSlotIngredients.getSingleDisplayGroupTagKey(
			slotIngredients,
			() -> {
				throw new AssertionError("multiple groups must not use a whole-slot tag fallback");
			}
		);

		assertEquals(Optional.empty(), tagKey);
	}

	@Test
	void displayFilteringAppliesVisibilityAndLimitTogether() {
		// Setup: a recipe slot has more visible ingredients than JEI's display limit.
		List<SlotIngredient<?>> ingredients = IntStream.range(0, 110)
			.<SlotIngredient<?>>mapToObj(i -> new SlotIngredient<>(createIngredient(Integer.toString(i))))
			.toList();

		// Operation: filter one ingredient out while applying the display limit.
		List<SlotIngredient<?>> visible = RecipeSlotIngredients.filterVisibleIngredients(
			ingredients,
			ingredient -> !ingredient.getIngredient().equals("5")
		);

		// Assertions: the hidden ingredient is excluded and the result is capped after filtering.
		assertEquals(100, visible.size());
		assertFalse(visible.stream().anyMatch(ingredient -> ingredient.typedIngredient().getIngredient().equals("5")));
	}

	@Test
	void uninterpretedDisplayGroupUsesCanonicalIngredientsBeforeDisplayLimit() {
		// Setup: an uninterpreted recipe slot has more canonical ingredients than JEI can display.
		List<SlotIngredient<?>> ingredients = IntStream.range(0, 110)
			.<SlotIngredient<?>>mapToObj(i -> new SlotIngredient<>(createIngredient(Integer.toString(i))))
			.toList();

		// Operation: apply the display limit, then get the displayed ingredient's group from the canonical source.
		List<SlotIngredient<?>> displayedIngredients = RecipeSlotIngredients.filterVisibleIngredients(
			ingredients,
			ingredient -> true
		);

		SlotIngredient<?> displayed = displayedIngredients.getFirst();
		List<?> displayGroup = RecipeSlotIngredients.getDisplayGroupIngredients(ingredients, displayed);

		// Assertions: display rotation is capped, but metadata calculations retain the complete ingredient group.
		assertEquals(100, displayedIngredients.size());
		assertEquals(110, displayGroup.size());
	}

	private static ITypedIngredient<String> createIngredient(String ingredient) {
		return TypedIngredient.createUnvalidated(INGREDIENT_TYPE, ingredient);
	}
}
