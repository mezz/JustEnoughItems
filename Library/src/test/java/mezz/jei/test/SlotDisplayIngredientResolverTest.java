package mezz.jei.test;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.context.ContextMap;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
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
			.map(ITypedIngredient::getIngredient)
			.toList());
		SlotDisplayData<TestIngredient> displayData = resolved.getFirst().slotDisplayData();
		assertNotNull(displayData);
		assertEquals(CHILD_HEADER, displayData.info().tooltipHeader().orElseThrow());
	}

	@Test
	void transformingWrapperPreservesNestedGroupsAndMetadata() {
		// Setup: two interpreted child groups are wrapped by two result-transforming displays.
		IIngredientManagerInternal ingredientManager = createIngredientManager(List.of(new TestIngredient(1)), false);
		SlotDisplay composite = new SlotDisplay.Composite(List.of(TestSlotDisplay.INSTANCE, TestSlotDisplay.INSTANCE));
		SlotDisplay innerWrapper = new TransformingTestSlotDisplay(composite, 10);
		SlotDisplay outerWrapper = new TransformingTestSlotDisplay(innerWrapper, 100);

		// Operation: resolve and interpret the nested wrappers.
		List<SlotIngredient<TestIngredient>> resolved = resolve(ingredientManager, outerWrapper);

		// Assertions: transformations run inside-out, and the composite's separate child groups and metadata remain.
		assertEquals(
			List.of(new TestIngredient(111), new TestIngredient(111)),
			resolved.stream()
				.map(SlotIngredient::typedIngredient)
				.map(ITypedIngredient::getIngredient)
				.toList()
		);
		SlotDisplayData<TestIngredient> firstDisplayData = resolved.getFirst().slotDisplayData();
		SlotDisplayData<TestIngredient> secondDisplayData = resolved.getLast().slotDisplayData();
		assertNotNull(firstDisplayData);
		assertNotNull(secondDisplayData);
		assertNotSame(firstDisplayData, secondDisplayData);
		assertEquals(CHILD_HEADER, firstDisplayData.info().tooltipHeader().orElseThrow());
		assertEquals(CHILD_HEADER, secondDisplayData.info().tooltipHeader().orElseThrow());
	}

	@Test
	void childDisplaysCanBeTransformedIndependently() {
		// Setup: one display delegates to an unchanged child and a transformed child.
		IIngredientManagerInternal ingredientManager = createIngredientManager(List.of(new TestIngredient(1)), false);
		SlotDisplay display = new SelectiveChildrenTestSlotDisplay(
			TestSlotDisplay.INSTANCE,
			TestSlotDisplay.INSTANCE,
			10
		);

		// Operation: resolve and interpret both children.
		List<SlotIngredient<TestIngredient>> resolved = resolve(ingredientManager, display);

		// Assertions: only the selected child is transformed, and both child groups keep their metadata.
		assertEquals(
			List.of(new TestIngredient(1), new TestIngredient(11)),
			resolved.stream()
				.map(SlotIngredient::typedIngredient)
				.map(ITypedIngredient::getIngredient)
				.toList()
		);
		assertNotSame(resolved.getFirst().slotDisplayData(), resolved.getLast().slotDisplayData());
		assertEquals(CHILD_HEADER, resolved.getFirst().slotDisplayData().info().tooltipHeader().orElseThrow());
		assertEquals(CHILD_HEADER, resolved.getLast().slotDisplayData().info().tooltipHeader().orElseThrow());
	}

	@Test
	void nestedCompositeKeepsListCandidatesAcrossChildGroups() {
		// Setup: a singleton and a nested list contribute separate metadata groups to one recipe slot.
		IIngredientManagerInternal ingredientManager = createIngredientManager(List.of(new TestIngredient(1)), false);
		SlotDisplay nestedList = new SlotDisplay.Composite(List.of(
			new TransformingTestSlotDisplay(TestSlotDisplay.INSTANCE, 10),
			new TransformingTestSlotDisplay(TestSlotDisplay.INSTANCE, 100)
		));
		SlotDisplay display = new SlotDisplay.Composite(List.of(TestSlotDisplay.INSTANCE, nestedList));
		List<SlotIngredient<TestIngredient>> resolved = resolve(ingredientManager, display);

		// Operation: collect the visible candidates used by the slot's list badge and tooltip grid.
		List<TestIngredient> candidates = RecipeSlotIngredients.getVisibleSlotIngredients(resolved, ingredientManager, ingredient -> true)
			.map(SlotIngredient::typedIngredient)
			.map(ingredient -> ingredient.getIngredient(INGREDIENT_TYPE))
			.flatMap(Optional::stream)
			.toList();

		// Assertions: group metadata stays independent, but every cycled child still exposes the full list.
		assertEquals(List.of(new TestIngredient(1), new TestIngredient(11), new TestIngredient(101)), candidates);
		assertNotSame(resolved.get(0).slotDisplayData(), resolved.get(1).slotDisplayData());
		assertNotSame(resolved.get(1).slotDisplayData(), resolved.get(2).slotDisplayData());
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

		List<@Nullable SlotIngredient<?>> displayed = RecipeSlotIngredients.calculateDisplayIngredients(
			resolved,
			ingredientManager,
			FocusGroup.EMPTY,
			RecipeIngredientRole.INPUT,
			ingredient -> true
		);
		SlotIngredient<?> displayedIngredient = Objects.requireNonNull(displayed.getFirst());
		List<SlotIngredient<?>> displayGroup = RecipeSlotIngredients.getDisplayGroupIngredients(
			resolved,
			displayedIngredient
		);
		List<SlotIngredient<?>> candidates = RecipeSlotIngredients.getVisibleSlotIngredients(displayGroup, ingredientManager, ingredient -> true)
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
		builder.getSlotDisplayInterpreterRegistration().register(
			TransformingTestSlotDisplay.TYPE,
			INGREDIENT_TYPE,
			(display, ignoredContext, interpretationBuilder) -> interpretationBuilder.addChildDisplay(
				display.source(),
				ingredient -> new TestIngredient(ingredient.value() + display.offset())
			)
		);
		builder.getSlotDisplayInterpreterRegistration().register(
			SelectiveChildrenTestSlotDisplay.TYPE,
			INGREDIENT_TYPE,
			(display, ignoredContext, interpretationBuilder) -> interpretationBuilder
				.addChildDisplay(display.unchanged())
				.addChildDisplay(
					display.transformed(),
					ingredient -> new TestIngredient(ingredient.value() + display.offset())
				)
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

	private record TransformingTestSlotDisplay(SlotDisplay source, int offset) implements SlotDisplay {
		private static final TransformingTestSlotDisplay DEFAULT = new TransformingTestSlotDisplay(TestSlotDisplay.INSTANCE, 0);
		private static final MapCodec<TransformingTestSlotDisplay> MAP_CODEC = MapCodec.unit(DEFAULT);
		private static final StreamCodec<RegistryFriendlyByteBuf, TransformingTestSlotDisplay> STREAM_CODEC = StreamCodec.unit(DEFAULT);
		private static final SlotDisplay.Type<TransformingTestSlotDisplay> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

		@Override
		public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
			return source.resolve(context, factory)
				.map(this::transform);
		}

		@SuppressWarnings("unchecked")
		private <T> T transform(T ingredient) {
			if (ingredient instanceof TestIngredient(int value)) {
				return (T) new TestIngredient(value + offset);
			}
			return ingredient;
		}

		@Override
		public Type<TransformingTestSlotDisplay> type() {
			return TYPE;
		}
	}

	private record SelectiveChildrenTestSlotDisplay(
		SlotDisplay unchanged,
		SlotDisplay transformed,
		int offset
	) implements SlotDisplay {
		private static final SelectiveChildrenTestSlotDisplay DEFAULT = new SelectiveChildrenTestSlotDisplay(
			TestSlotDisplay.INSTANCE,
			TestSlotDisplay.INSTANCE,
			0
		);
		private static final MapCodec<SelectiveChildrenTestSlotDisplay> MAP_CODEC = MapCodec.unit(DEFAULT);
		private static final StreamCodec<RegistryFriendlyByteBuf, SelectiveChildrenTestSlotDisplay> STREAM_CODEC = StreamCodec.unit(DEFAULT);
		private static final SlotDisplay.Type<SelectiveChildrenTestSlotDisplay> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

		@Override
		public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
			return Stream.concat(
				unchanged.resolve(context, factory),
				transformed.resolve(context, factory).map(this::transform)
			);
		}

		@SuppressWarnings("unchecked")
		private <T> T transform(T ingredient) {
			if (ingredient instanceof TestIngredient(int value)) {
				return (T) new TestIngredient(value + offset);
			}
			return ingredient;
		}

		@Override
		public Type<SelectiveChildrenTestSlotDisplay> type() {
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
		public void render(GuiGraphicsExtractor guiGraphics, TestIngredient ingredient) {
		}

		@Override
		@Deprecated(since = "30.26.0", forRemoval = true)
		@SuppressWarnings("removal")
		public List<Component> getTooltip(TestIngredient ingredient, TooltipFlag tooltipFlag) {
			return getTooltip(ingredient, Item.TooltipContext.EMPTY, null, tooltipFlag);
		}

		@Override
		@Deprecated(since = "30.26.0", forRemoval = true)
		@SuppressWarnings("removal")
		public void getTooltip(ITooltipBuilder tooltip, TestIngredient ingredient, TooltipFlag tooltipFlag) {
			getTooltip(tooltip, ingredient, Item.TooltipContext.EMPTY, null, tooltipFlag);
		}

		@Override
		public List<Component> getTooltip(TestIngredient ingredient, Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
			return List.of();
		}
	}
}
