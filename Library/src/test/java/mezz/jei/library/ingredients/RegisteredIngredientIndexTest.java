package mezz.jei.library.ingredients;

import mezz.jei.common.ingredients.TypedIngredient;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class RegisteredIngredientIndexTest {
	private static final IIngredientType<TestIngredient> INGREDIENT_TYPE = () -> TestIngredient.class;
	private static final IIngredientHelper<TestIngredient> INGREDIENT_HELPER = new TestIngredientHelper();

	@Test
	void resolvesExactAndGroupingUidsAndUpdatesThemTogether() {
		RegisteredIngredientIndex<TestIngredient> index = new RegisteredIngredientIndex<>(INGREDIENT_HELPER);
		ITypedIngredient<TestIngredient> first = createIngredient("first", "group", "initial");
		ITypedIngredient<TestIngredient> second = createIngredient("second", "group", "second");
		index.addAll(List.of(first, second));

		assertSame(first, index.getIngredientByUid("first"));
		assertEquals(List.of(first, second), index.getIngredientsByGroupingUid("group"));

		ITypedIngredient<TestIngredient> replacement = createIngredient("first", "group", "replacement");
		index.add(replacement);

		assertSame(replacement, index.getIngredientByUid("first"));
		assertEquals(List.of(replacement, second), index.getIngredientsByGroupingUid("group"));

		index.remove(createIngredient("first", "ignored", "removal key"));

		assertEquals(List.of(second), index.getIngredientsByGroupingUid("group"));
	}

	private static ITypedIngredient<TestIngredient> createIngredient(String uid, String group, String value) {
		return TypedIngredient.createUnvalidated(INGREDIENT_TYPE, new TestIngredient(uid, group, value));
	}

	private record TestIngredient(String uid, String group, String value) {
	}

	private static class TestIngredientHelper implements IIngredientHelper<TestIngredient> {
		@Override
		public IIngredientType<TestIngredient> getIngredientType() {
			return INGREDIENT_TYPE;
		}

		@Override
		public String getDisplayName(TestIngredient ingredient) {
			return ingredient.value();
		}

		@Override
		public Object getUid(TestIngredient ingredient, UidContext context) {
			return ingredient.uid();
		}

		@Override
		public Object getGroupingUid(TestIngredient ingredient) {
			return ingredient.group();
		}

		@Override
		public Identifier getIdentifier(TestIngredient ingredient) {
			return Identifier.fromNamespaceAndPath("test", ingredient.uid());
		}

		@Override
		public TestIngredient copyIngredient(TestIngredient ingredient) {
			return ingredient;
		}

		@Override
		public String getErrorInfo(@Nullable TestIngredient ingredient) {
			return String.valueOf(ingredient);
		}
	}
}
