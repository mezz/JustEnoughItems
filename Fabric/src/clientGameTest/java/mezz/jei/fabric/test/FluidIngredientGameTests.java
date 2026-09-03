package mezz.jei.fabric.test;

import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.util.RegistryUtil;
import mezz.jei.fabric.ingredients.fluid.JeiFluidIngredient;
import mezz.jei.fabric.platform.FluidHelper;
import mezz.jei.library.ingredients.DisplayIngredientAcceptor;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.load.registration.IngredientManagerBuilder;
import mezz.jei.library.plugins.vanilla.ingredients.fluid.FluidIngredientHelper;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

final class FluidIngredientGameTests {
	private FluidIngredientGameTests() {

	}

	public static void register() {
		FabricClientTestRunner.register(getTestCase());
	}

	public static FabricClientTestRunner.ClientTestCase getTestCase() {
		return new FabricClientTestRunner.ClientTestCase(
			"fabric-client-fluid-ingredients",
			FluidIngredientGameTests.class.getSimpleName(),
			FluidIngredientGameTests::runTest
		);
	}

	private static void runTest() {
		try (FabricClientTestWorld ignored = FabricClientTestWorld.create()) {
			ClientTestUtil.runOnClient(client -> {
				RegistryUtil.setRegistryAccess(Objects.requireNonNull(client.level).registryAccess());
				emptyFluidsAreInvalid();
				flowingFluidsAreCanonicalizedToSources();
				nonEmptyFluidIsAccepted();
				copyWithAmountKeepsEmptyIngredientsEmpty();
			});
		}
	}

	private static void emptyFluidsAreInvalid() {
		FluidIngredientHelper<IJeiFluidIngredient> ingredientHelper = createIngredientHelper();
		DisplayIngredientAcceptor acceptor = createIngredientAcceptor();
		IJeiFluidIngredient blank = new JeiFluidIngredient(FluidVariant.blank(), FluidConstants.BUCKET);
		IJeiFluidIngredient zeroAmountWater = new JeiFluidIngredient(FluidVariant.of(Fluids.WATER), 0);

		acceptor.addIngredient(FabricTypes.FLUID_STACK, blank);
		acceptor.addIngredient(FabricTypes.FLUID_STACK, zeroAmountWater);

		assertFalse(ingredientHelper.isValidIngredient(blank), "Expected a blank fluid variant to be invalid");
		assertFalse(ingredientHelper.isValidIngredient(zeroAmountWater), "Expected zero-sized water to be invalid");
		assertTrue(
			acceptor.getAllIngredients().stream().noneMatch(Objects::nonNull),
			"Expected empty fluid ingredients to be filtered before display"
		);
	}

	private static void flowingFluidsAreCanonicalizedToSources() {
		FluidIngredientHelper<IJeiFluidIngredient> ingredientHelper = createIngredientHelper();
		IJeiFluidIngredient flowingWater = new JeiFluidIngredient(FluidVariant.of(Fluids.FLOWING_WATER), FluidConstants.BUCKET);
		IJeiFluidIngredient flowingLava = new JeiFluidIngredient(FluidVariant.of(Fluids.FLOWING_LAVA), FluidConstants.BUCKET);

		assertTrue(
			flowingWater.getFluidVariant().getFluid() == Fluids.WATER,
			"Expected flowing water to be canonicalized to source water"
		);
		assertTrue(
			flowingLava.getFluidVariant().getFluid() == Fluids.LAVA,
			"Expected flowing lava to be canonicalized to source lava"
		);
		assertTrue(ingredientHelper.isValidIngredient(flowingWater), "Expected canonical source water to be valid");
		assertTrue(ingredientHelper.isValidIngredient(flowingLava), "Expected canonical source lava to be valid");
	}

	private static void nonEmptyFluidIsAccepted() {
		DisplayIngredientAcceptor acceptor = createIngredientAcceptor();
		IJeiFluidIngredient water = new JeiFluidIngredient(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET);

		acceptor.addIngredient(FabricTypes.FLUID_STACK, water);

		ITypedIngredient<?> typedIngredient = acceptor.getAllIngredients().stream()
			.filter(Objects::nonNull)
			.findFirst()
			.orElseThrow();
		IJeiFluidIngredient accepted = typedIngredient.getIngredient(FabricTypes.FLUID_STACK).orElseThrow();
		assertTrue(
			accepted.getFluidVariant().getFluid() == Fluids.WATER,
			"Expected water to remain available for display"
		);
	}

	private static void copyWithAmountKeepsEmptyIngredientsEmpty() {
		FluidHelper fluidHelper = new FluidHelper();
		IJeiFluidIngredient blank = new JeiFluidIngredient(FluidVariant.blank(), FluidConstants.BUCKET);
		IJeiFluidIngredient zeroAmountWater = new JeiFluidIngredient(FluidVariant.of(Fluids.WATER), 0);
		IJeiFluidIngredient water = new JeiFluidIngredient(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET);

		IJeiFluidIngredient blankResult = fluidHelper.copyWithAmount(blank, FluidConstants.BUCKET);
		IJeiFluidIngredient zeroAmountWaterResult = fluidHelper.copyWithAmount(zeroAmountWater, FluidConstants.BUCKET);
		IJeiFluidIngredient waterResult = fluidHelper.copyWithAmount(water, 250);

		assertTrue(blankResult.getFluidVariant().isBlank(), "Expected the blank variant to remain blank");
		assertTrue(blankResult.getAmount() == 0, "Expected a blank result to have zero amount");
		assertTrue(zeroAmountWaterResult.getFluidVariant().isBlank(), "Expected zero-sized water to become blank");
		assertTrue(zeroAmountWaterResult.getAmount() == 0, "Expected zero-sized water to remain empty");
		assertTrue(zeroAmountWater.getAmount() == 0, "Expected the zero-sized source to remain unchanged");
		assertTrue(waterResult != water, "Expected a distinct copy of valid water");
		assertTrue(waterResult.getFluidVariant().getFluid() == Fluids.WATER, "Expected copied water");
		assertTrue(waterResult.getAmount() == 250, "Expected the copied amount");
		assertTrue(water.getAmount() == FluidConstants.BUCKET, "Expected the valid source to remain unchanged");
	}

	private static FluidIngredientHelper<IJeiFluidIngredient> createIngredientHelper() {
		SubtypeManager subtypeManager = new SubtypeManager(new SubtypeInterpreters());
		return new FluidIngredientHelper<>(subtypeManager, TestColorHelper.INSTANCE, new FluidHelper());
	}

	private static DisplayIngredientAcceptor createIngredientAcceptor() {
		return new DisplayIngredientAcceptor(createIngredientManager());
	}

	private static IIngredientManager createIngredientManager() {
		SubtypeManager subtypeManager = new SubtypeManager(new SubtypeInterpreters());
		IngredientManagerBuilder builder = new IngredientManagerBuilder(subtypeManager, TestColorHelper.INSTANCE);
		FluidHelper fluidHelper = new FluidHelper();
		FluidIngredientHelper<IJeiFluidIngredient> ingredientHelper = new FluidIngredientHelper<>(subtypeManager, TestColorHelper.INSTANCE, fluidHelper);
		builder.register(
			FabricTypes.FLUID_STACK,
			List.of(),
			ingredientHelper,
			TestFluidRenderer.INSTANCE,
			fluidHelper.getCodec()
		);
		return builder.build();
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private static void assertFalse(boolean condition, String message) {
		assertTrue(!condition, message);
	}

	private enum TestFluidRenderer implements IIngredientRenderer<IJeiFluidIngredient> {
		INSTANCE;

		@Override
		public void render(GuiGraphics guiGraphics, IJeiFluidIngredient ingredient) {
		}

		@Override
		@Deprecated(since = "19.49.0", forRemoval = true)
		@SuppressWarnings("removal")
		public List<Component> getTooltip(IJeiFluidIngredient ingredient, TooltipFlag tooltipFlag) {
			return List.of();
		}

		@Override
		@Deprecated(since = "19.49.0", forRemoval = true)
		@SuppressWarnings("removal")
		public void getTooltip(ITooltipBuilder tooltip, IJeiFluidIngredient ingredient, TooltipFlag tooltipFlag) {

		}

		@Override
		public List<Component> getTooltip(IJeiFluidIngredient ingredient, Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
			return List.of();
		}
	}

	private enum TestColorHelper implements IColorHelper {
		INSTANCE;

		@Override
		public List<Integer> getColors(TextureAtlasSprite textureAtlasSprite, int renderColor, int colorCount) {
			return List.of();
		}

		@Override
		public List<Integer> getColors(ItemStack itemStack, int colorCount) {
			return List.of();
		}

		@Override
		public String getClosestColorName(int color) {
			return "";
		}
	}
}
