package mezz.jei.test;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.library.focus.FocusGroup;
import mezz.jei.library.gui.ingredients.RecipeSlotIngredients;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import mezz.jei.library.ingredients.SlotDisplayData;
import mezz.jei.library.ingredients.SlotIngredient;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.load.registration.IngredientManagerBuilder;
import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import net.minecraft.ChatFormatting;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.context.ContextMap;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotDisplayIngredientResolverTest {
	private static final IIngredientType<TestIngredient> INGREDIENT_TYPE = () -> TestIngredient.class;
	private static final Component CHILD_HEADER = Component.literal("Custom child");

	@BeforeAll
	static void setup() {
		SharedConstants.setVersion(DetectedVersion.BUILT_IN);
		Bootstrap.bootStrap();
	}

	@Test
	void customIngredientInterpreterComposesThroughVanillaComposite() {
		// Setup: a custom ingredient display is wrapped in a vanilla composite display.
		IIngredientManagerInternal ingredientManager = createIngredientManager(List.of(new TestIngredient(1)), false);
		SlotDisplay composite = new SlotDisplay.Composite(List.of(TestSlotDisplay.INSTANCE));

		// Operation: resolve the composite through the custom ingredient type and interpreter.
		List<SlotIngredient<TestIngredient>> resolved = resolve(ingredientManager, composite);

		// Assertions: the custom ingredient resolves and keeps the metadata supplied by its child interpreter.
		assertEquals(List.of(new TestIngredient(1)), resolved.stream()
			.map(SlotIngredient::typedIngredient)
			.map(ingredient -> ingredient.getIngredient())
			.toList());
		SlotDisplayData<TestIngredient> displayData = resolved.getFirst().slotDisplayData();
		assertNotNull(displayData);
		assertEquals(CHILD_HEADER, displayData.info().tooltipHeader().orElseThrow());
	}

	@Test
	void groupedDisplayExpansionStopsAtTheDisplayLimit() {
		// Setup: one grouping UID contains more registered ingredients than JEI's display limit.
		List<TestIngredient> registeredIngredients = IntStream.range(0, 110)
			.mapToObj(TestIngredient::new)
			.toList();
		IIngredientManagerInternal ingredientManager = createIngredientManager(registeredIngredients, true);

		// Operation: resolve the wildcard display and calculate its visible rotation.
		List<SlotIngredient<TestIngredient>> resolved = resolve(ingredientManager, TestSlotDisplay.INSTANCE);

		List<SlotIngredient<?>> displayed = RecipeSlotIngredients.calculateDisplayIngredients(
			resolved,
			ingredientManager,
			FocusGroup.EMPTY,
			RecipeIngredientRole.INPUT,
			ingredient -> true
		);
		SlotIngredient<?> displayedIngredient = displayed.getFirst();
		List<SlotIngredient<?>> candidates = RecipeSlotIngredients.getVisibleSlotIngredientsInDisplayGroup(
				resolved,
				displayedIngredient,
				ingredientManager,
				ingredient -> true
			)
			.toList();

		// Assertions: subtype wildcard handling adds wildcard matching and a generic heading before expansion.
		SlotDisplayData<TestIngredient> displayData = resolved.getFirst().slotDisplayData();
		assertNotNull(displayData);
		assertTrue(displayData.info().matchesAllSubtypes());
		assertEquals(
			Component.translatable("jei.tooltip.recipe.any", "1")
				.withStyle(ChatFormatting.GOLD)
				.withStyle(ChatFormatting.ITALIC),
			displayData.info().tooltipHeader().orElseThrow()
		);

		// Assertions: visible rotation is capped, while candidate browsing can still access the complete group.
		assertEquals(100, displayed.size());
		assertEquals(110, candidates.size());
	}

	private static IIngredientManagerInternal createIngredientManager(
		List<TestIngredient> registeredIngredients,
		boolean matchesAllSubtypes
	) {
		IngredientManagerBuilder builder = new IngredientManagerBuilder(
			new SubtypeManager(new SubtypeInterpreters()),
			DummyColorHelper.INSTANCE
		);
		builder.register(
			INGREDIENT_TYPE,
			registeredIngredients,
			new TestIngredientHelper(),
			new TestIngredientRenderer(),
			Codec.INT.xmap(TestIngredient::new, TestIngredient::value)
		);
		new VanillaPlugin().registerSlotDisplayInterpreters(builder.getSlotDisplayInterpreterRegistration());
		builder.getSlotDisplayInterpreterRegistration().register(
			TestSlotDisplay.TYPE,
			INGREDIENT_TYPE,
			(display, context, interpretationBuilder) -> {
				if (matchesAllSubtypes) {
					interpretationBuilder.setWildcardForSubtypes(true);
				} else {
					interpretationBuilder.setTooltipHeader(CHILD_HEADER);
				}
			}
		);
		return builder.build();
	}

	private static List<SlotIngredient<TestIngredient>> resolve(
		IIngredientManagerInternal ingredientManager,
		SlotDisplay display
	) {
		return ingredientManager.resolveSlotDisplay(
				INGREDIENT_TYPE,
				new ContextMap.Builder().create(new ContextKeySet.Builder().build()),
				RecipeIngredientRole.INPUT,
				display
			)
			.toList();
	}

	private record TestIngredient(int value) {
	}

	private enum TestContentsFactory implements DisplayContentsFactory<TestIngredient> {
		INSTANCE
	}

	private enum TestSlotDisplay implements SlotDisplay {
		INSTANCE;

		private static final MapCodec<TestSlotDisplay> MAP_CODEC = MapCodec.unit(INSTANCE);
		private static final StreamCodec<RegistryFriendlyByteBuf, TestSlotDisplay> STREAM_CODEC = StreamCodec.unit(INSTANCE);
		private static final SlotDisplay.Type<TestSlotDisplay> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

		@Override
		public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
			if (factory == TestContentsFactory.INSTANCE) {
				@SuppressWarnings("unchecked")
				T ingredient = (T) new TestIngredient(1);
				return Stream.of(ingredient);
			}
			return Stream.empty();
		}

		@Override
		public Type<TestSlotDisplay> type() {
			return TYPE;
		}
	}

	private static class TestIngredientHelper implements IIngredientHelper<TestIngredient> {
		@Override
		public IIngredientType<TestIngredient> getIngredientType() {
			return INGREDIENT_TYPE;
		}

		@Override
		public String getDisplayName(TestIngredient ingredient) {
			return Integer.toString(ingredient.value());
		}

		@Override
		public Object getUid(TestIngredient ingredient, UidContext context) {
			return ingredient.value();
		}

		@Override
		public Object getGroupingUid(TestIngredient ingredient) {
			return TestIngredient.class;
		}

		@Override
		public boolean hasSubtypes(TestIngredient ingredient) {
			return true;
		}

		@Override
		public Identifier getIdentifier(TestIngredient ingredient) {
			return Identifier.fromNamespaceAndPath("test", Integer.toString(ingredient.value()));
		}

		@Override
		public TestIngredient copyIngredient(TestIngredient ingredient) {
			return ingredient;
		}

		@Override
		public String getErrorInfo(@Nullable TestIngredient ingredient) {
			return String.valueOf(ingredient);
		}

		@Override
		public Optional<DisplayContentsFactory<TestIngredient>> getDisplayContentsFactory() {
			return Optional.of(TestContentsFactory.INSTANCE);
		}
	}

	private static class TestIngredientRenderer implements IIngredientRenderer<TestIngredient> {
		@Override
		public void render(GuiGraphics guiGraphics, TestIngredient ingredient) {
		}

		@Override
		public List<Component> getTooltip(TestIngredient ingredient, TooltipFlag tooltipFlag) {
			return List.of();
		}
	}
}
