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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class RecipeSlotIngredientGroupingTest {
	private static final IIngredientType<String> INGREDIENT_TYPE = () -> String.class;
	private static final IIngredientType<Integer> SECOND_INGREDIENT_TYPE = () -> Integer.class;

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
		// Setup: a recipe slot has more visible ingredients than the requested limit.
		int limit = 10;
		List<SlotIngredient<?>> ingredients = IntStream.range(0, limit + 10)
			.<SlotIngredient<?>>mapToObj(i -> new SlotIngredient<>(createIngredient(Integer.toString(i))))
			.toList();

		// Operation: filter one ingredient out while applying the display limit.
		List<@Nullable SlotIngredient<?>> visible = RecipeSlotIngredients.filterVisibleIngredients(
			ingredients,
			ingredient -> !ingredient.getIngredient().equals("5"),
			limit
		);

		// Assertions: the hidden ingredient is excluded and the result is capped after filtering.
		assertEquals(limit, visible.size());
		assertFalse(visible.stream()
			.filter(Objects::nonNull)
			.anyMatch(ingredient -> ingredient.typedIngredient().getIngredient().equals("5")));
	}

	@ParameterizedTest
	@ValueSource(ints = {0, 1, 7, 128})
	void largeRotationStopsCheckingVisibilityAtTheLimit(int limit) {
		List<SlotIngredient<?>> ingredients = IntStream.range(0, 10_000)
			.<SlotIngredient<?>>mapToObj(i -> new SlotIngredient<>(createIngredient(Integer.toString(i))))
			.toList();
		AtomicInteger visibilityChecks = new AtomicInteger();
		List<@Nullable SlotIngredient<?>> rotation = RecipeSlotIngredients.filterVisibleIngredients(ingredients, ingredient -> {
			visibilityChecks.incrementAndGet();
			return true;
		}, limit);
		assertEquals(limit, rotation.size());
		assertEquals(limit, visibilityChecks.get());
		assertEquals(10_000, RecipeSlotIngredients.getDisplayGroupIngredients(ingredients, ingredients.getFirst()).size());
	}

	@Test
	void allHiddenIngredientsFallbackRespectsLimit() {
		int limit = 2;
		List<SlotIngredient<?>> ingredients = IntStream.range(0, limit + 3)
			.<SlotIngredient<?>>mapToObj(i -> new SlotIngredient<>(createIngredient(Integer.toString(i))))
			.toList();

		List<@Nullable SlotIngredient<?>> displayedIngredients = RecipeSlotIngredients.filterVisibleIngredients(
			ingredients,
			ingredient -> false,
			limit
		);

		assertEquals(ingredients.subList(0, limit), displayedIngredients);
	}

	@Test
	void uninterpretedDisplayGroupUsesCanonicalIngredientsBeforeDisplayLimit() {
		// Setup: an uninterpreted recipe slot has more canonical ingredients than the requested limit.
		int limit = 5;
		List<SlotIngredient<?>> ingredients = IntStream.range(0, limit + 10)
			.<SlotIngredient<?>>mapToObj(i -> new SlotIngredient<>(createIngredient(Integer.toString(i))))
			.toList();

		// Operation: apply the display limit, then get the displayed ingredient's group from the canonical source.
		List<@Nullable SlotIngredient<?>> displayedIngredients = RecipeSlotIngredients.filterVisibleIngredients(
			ingredients,
			ingredient -> true,
			limit
		);

		SlotIngredient<?> displayed = Objects.requireNonNull(displayedIngredients.getFirst());
		List<SlotIngredient<?>> displayGroup = RecipeSlotIngredients.getDisplayGroupIngredients(ingredients, displayed);

		// Assertions: display rotation is capped, but metadata calculations retain the complete ingredient group.
		assertEquals(limit, displayedIngredients.size());
		assertEquals(ingredients.size(), displayGroup.size());
	}

	@Test
	void uninterpretedDisplayGroupRetainsMixedIngredientTypes() {
		// Setup: one uninterpreted recipe slot combines two registered ingredient types.
		SlotIngredient<String> displayed = new SlotIngredient<>(createIngredient("first type"));
		ITypedIngredient<Integer> secondType = TypedIngredient.createUnvalidated(SECOND_INGREDIENT_TYPE, 1);
		List<SlotIngredient<?>> ingredients = List.of(
			displayed,
			new SlotIngredient<>(secondType)
		);

		// Operation: get the candidates represented by the displayed ingredient.
		List<IIngredientType<?>> ingredientTypes = RecipeSlotIngredients.getDisplayGroupIngredients(ingredients, displayed)
			.stream()
			.<IIngredientType<?>>map(candidate -> candidate.typedIngredient().getType())
			.toList();

		// Assertions: the candidate group retains every type for tooltip rendering and cycling.
		assertEquals(List.of(INGREDIENT_TYPE, SECOND_INGREDIENT_TYPE), ingredientTypes);
	}

	private static ITypedIngredient<String> createIngredient(String ingredient) {
		return TypedIngredient.createUnvalidated(INGREDIENT_TYPE, ingredient);
	}
}
