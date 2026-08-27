package mezz.jei.test;

import com.mojang.serialization.Codec;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.ingredients.IngredientInfo;
import mezz.jei.common.ingredients.TypedIngredient;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.load.registration.IngredientManagerBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class IngredientAliasTest {
	private static final IIngredientTypeWithSubtypes<TestBase, TestIngredient> TEST_TYPE = new IIngredientTypeWithSubtypes<>() {
		@Override
		public Class<? extends TestIngredient> getIngredientClass() {
			return TestIngredient.class;
		}

		@Override
		public Class<? extends TestBase> getIngredientBaseClass() {
			return TestBase.class;
		}

		@Override
		public TestBase getBase(TestIngredient ingredient) {
			return ingredient.base();
		}
	};

	private static final IIngredientHelper<TestIngredient> TEST_HELPER = new IIngredientHelper<>() {
		@Override
		public IIngredientTypeWithSubtypes<TestBase, TestIngredient> getIngredientType() {
			return TEST_TYPE;
		}

		@Override
		public String getDisplayName(TestIngredient ingredient) {
			return ingredient.base().name();
		}

		@Override
		public Object getUid(TestIngredient ingredient, UidContext context) {
			String subtype = ingredient.subtype();
			if (subtype == null) {
				return ingredient.base();
			}
			return List.of(ingredient.base(), subtype);
		}

		@Override
		public Identifier getIdentifier(TestIngredient ingredient) {
			return Identifier.fromNamespaceAndPath("test", ingredient.base().name());
		}

		@Override
		public TestIngredient copyIngredient(TestIngredient ingredient) {
			return ingredient;
		}

		@Override
		public String getErrorInfo(@Nullable TestIngredient ingredient) {
			return String.valueOf(ingredient);
		}
	};

	private static final Codec<TestIngredient> TEST_CODEC = Codec.STRING.xmap(
		value -> new TestIngredient(new TestBase(value), null),
		ingredient -> ingredient.base().name()
	);

	@Test
	public void exactIngredientAliasesDoNotApplyToSubtypes() {
		// Setup: exact aliases are stored under the ingredient UID, not the base ingredient.
		TestBase base = new TestBase("base");
		TestIngredient defaultIngredient = new TestIngredient(base, null);
		TestIngredient subtypeIngredient = new TestIngredient(base, "subtype");
		ITypedIngredient<TestIngredient> defaultTypedIngredient = createTypedIngredient(defaultIngredient);
		ITypedIngredient<TestIngredient> subtypeTypedIngredient = createTypedIngredient(subtypeIngredient);
		IngredientInfo<TestIngredient> ingredientInfo = createIngredientInfo(defaultTypedIngredient, subtypeTypedIngredient);
		ingredientInfo.addIngredientAlias(defaultIngredient, "default alias");

		// Operation: get aliases for both the default ingredient and a subtype with the same base.
		Collection<String> defaultAliases = ingredientInfo.getIngredientAliases(defaultTypedIngredient);
		Collection<String> subtypeAliases = ingredientInfo.getIngredientAliases(subtypeTypedIngredient);

		// Assertions: only the exact default ingredient has the alias.
		Assertions.assertEquals(List.of("default alias"), List.copyOf(defaultAliases));
		Assertions.assertTrue(subtypeAliases.isEmpty());
	}

	@Test
	public void baseIngredientAliasesApplyToSubtypes() {
		// Setup: base aliases are stored separately from exact ingredient aliases.
		TestBase base = new TestBase("base");
		TestIngredient defaultIngredient = new TestIngredient(base, null);
		TestIngredient subtypeIngredient = new TestIngredient(base, "subtype");
		ITypedIngredient<TestIngredient> defaultTypedIngredient = createTypedIngredient(defaultIngredient);
		ITypedIngredient<TestIngredient> subtypeTypedIngredient = createTypedIngredient(subtypeIngredient);
		IngredientInfo<TestIngredient> ingredientInfo = createIngredientInfo(defaultTypedIngredient, subtypeTypedIngredient);
		ingredientInfo.addIngredientAlias(defaultIngredient, "default alias");
		ingredientInfo.addIngredientAlias(subtypeIngredient, "subtype alias");
		ingredientInfo.addBaseIngredientAlias(base, "base alias");

		// Operation: get aliases for both the default ingredient and the subtype.
		Collection<String> defaultAliases = ingredientInfo.getIngredientAliases(defaultTypedIngredient);
		Collection<String> subtypeAliases = ingredientInfo.getIngredientAliases(subtypeTypedIngredient);

		// Assertions: both ingredients include their exact alias and the shared base alias.
		Assertions.assertEquals(List.of("default alias", "base alias"), List.copyOf(defaultAliases));
		Assertions.assertEquals(List.of("subtype alias", "base alias"), List.copyOf(subtypeAliases));
	}

	@Test
	public void baseIngredientAliasesUseIdentity() {
		// Setup: equal base ingredients are not interchangeable because subtype bases must be compared by identity.
		TestBase aliasBase = new TestBase("base");
		TestBase ingredientBase = new TestBase("base");
		TestIngredient ingredient = new TestIngredient(ingredientBase, "subtype");
		ITypedIngredient<TestIngredient> typedIngredient = createTypedIngredient(ingredient);
		IngredientInfo<TestIngredient> ingredientInfo = createIngredientInfo(typedIngredient);
		ingredientInfo.addBaseIngredientAlias(aliasBase, "base alias");

		// Operation: get aliases for an ingredient whose base equals, but is not identical to, the aliased base.
		Collection<String> aliases = ingredientInfo.getIngredientAliases(typedIngredient);

		// Assertions: no alias is returned for a different base instance.
		Assertions.assertTrue(aliases.isEmpty());
	}

	@Test
	public void builderDistinguishesIngredientAndBaseIngredientAliases() {
		// Setup: register a subtype-aware ingredient type with one default ingredient and one subtype.
		TestBase base = new TestBase("base");
		TestIngredient defaultIngredient = new TestIngredient(base, null);
		TestIngredient subtypeIngredient = new TestIngredient(base, "subtype");
		IngredientManagerBuilder builder = createIngredientManagerBuilder();
		builder.register(
			TEST_TYPE,
			List.of(defaultIngredient, subtypeIngredient),
			TEST_HELPER,
			createTestRenderer(),
			TEST_CODEC
		);
		builder.addAlias(TEST_TYPE, defaultIngredient, "default alias");
		builder.addAlias(TEST_TYPE, base, "base alias");
		IIngredientManager ingredientManager = builder.build();

		// Operation: get aliases through the public ingredient manager lookup path.
		Collection<String> defaultAliases = ingredientManager.getIngredientAliases(createTypedIngredient(defaultIngredient));
		Collection<String> subtypeAliases = ingredientManager.getIngredientAliases(createTypedIngredient(subtypeIngredient));

		// Assertions: exact and base aliases are both accepted by the overloaded API and retain their distinct behavior.
		Assertions.assertEquals(List.of("base alias", "default alias"), List.copyOf(defaultAliases));
		Assertions.assertEquals(List.of("base alias"), List.copyOf(subtypeAliases));
	}

	@Test
	public void builderAddsMultipleBaseIngredientAliases() {
		// Setup: register a subtype-aware ingredient type with multiple aliases for the base ingredient.
		TestBase base = new TestBase("base");
		TestIngredient defaultIngredient = new TestIngredient(base, null);
		TestIngredient subtypeIngredient = new TestIngredient(base, "subtype");
		IngredientManagerBuilder builder = createIngredientManagerBuilder();
		builder.register(
			TEST_TYPE,
			List.of(defaultIngredient, subtypeIngredient),
			TEST_HELPER,
			createTestRenderer(),
			TEST_CODEC
		);
		builder.addAliases(TEST_TYPE, base, List.of("first base alias", "second base alias"));
		IIngredientManager ingredientManager = builder.build();

		// Operation: get aliases for both the default ingredient and a subtype with the same base.
		Collection<String> defaultAliases = ingredientManager.getIngredientAliases(createTypedIngredient(defaultIngredient));
		Collection<String> subtypeAliases = ingredientManager.getIngredientAliases(createTypedIngredient(subtypeIngredient));

		// Assertions: all aliases from the base-ingredient overload apply to every subtype with that base.
		Assertions.assertEquals(List.of("first base alias", "second base alias"), List.copyOf(defaultAliases));
		Assertions.assertEquals(List.of("first base alias", "second base alias"), List.copyOf(subtypeAliases));
	}

	@Test
	public void builderRejectsIngredientAliasesWithWrongIngredientType() {
		// Setup: force a mismatched ingredient through the generic API.
		IngredientManagerBuilder builder = createIngredientManagerBuilder();
		IIngredientType<Object> castType = castTestTypeToObjectIngredientType();
		Object wrongIngredient = new OtherIngredient();

		// Operation: register aliases for an ingredient that does not match the ingredient type.
		// Assertions: both overloads reject the mismatched ingredient before alias registration.
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> builder.addAlias(castType, wrongIngredient, "alias")
		);
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> builder.addAliases(castType, wrongIngredient, List.of("alias"))
		);
	}

	@SafeVarargs
	private static IngredientInfo<TestIngredient> createIngredientInfo(ITypedIngredient<TestIngredient>... typedIngredients) {
		return new IngredientInfo<>(
			TEST_TYPE,
			List.of(typedIngredients),
			TEST_HELPER,
			createTestRenderer(),
			TEST_CODEC
		);
	}

	private static ITypedIngredient<TestIngredient> createTypedIngredient(TestIngredient ingredient) {
		return TypedIngredient.createUnvalidated(TEST_TYPE, ingredient);
	}

	private static IngredientManagerBuilder createIngredientManagerBuilder() {
		SubtypeManager subtypeManager = new SubtypeManager(new SubtypeInterpreters());
		return new IngredientManagerBuilder(
			subtypeManager,
			DummyColorHelper.INSTANCE,
			new ContextMap.Builder().create(new ContextKeySet.Builder().build())
		);
	}

	@SuppressWarnings("unchecked")
	private static IIngredientType<Object> castTestTypeToObjectIngredientType() {
		return (IIngredientType<Object>) (IIngredientType<?>) TEST_TYPE;
	}

	private static <T> IIngredientRenderer<T> createTestRenderer() {
		return new IIngredientRenderer<>() {
			@Override
			public void render(GuiGraphics guiGraphics, T ingredient) {

			}

			@Override
			@Deprecated(since = "27.32.0", forRemoval = true)
			@SuppressWarnings("removal")
			public List<Component> getTooltip(T ingredient, TooltipFlag tooltipFlag) {
				return getTooltip(ingredient, Item.TooltipContext.EMPTY, null, tooltipFlag);
			}

			@Override
			@Deprecated(since = "27.32.0", forRemoval = true)
			@SuppressWarnings("removal")
			public void getTooltip(ITooltipBuilder tooltip, T ingredient, TooltipFlag tooltipFlag) {
				getTooltip(tooltip, ingredient, Item.TooltipContext.EMPTY, null, tooltipFlag);
			}

			@Override
			public List<Component> getTooltip(T ingredient, Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
				return List.of();
			}
		};
	}

	private record TestBase(String name) {}

	private record TestIngredient(TestBase base, @Nullable String subtype) {}

	private record OtherIngredient() {}
}
