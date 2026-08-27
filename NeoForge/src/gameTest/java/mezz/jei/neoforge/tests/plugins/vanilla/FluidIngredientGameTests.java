package mezz.jei.neoforge.tests.plugins.vanilla;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.library.ingredients.DisplayIngredientAcceptor;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.load.registration.IngredientManagerBuilder;
import mezz.jei.library.plugins.vanilla.ingredients.fluid.FluidIngredientHelper;
import mezz.jei.neoforge.platform.FluidHelper;
import mezz.jei.neoforge.tests.lib.JeiGameTestHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@ForEachTest(groups = "fluid_ingredients")
public final class FluidIngredientGameTests {
	private FluidIngredientGameTests() {
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Empty and flowing NeoForge fluid stacks are invalid and filtered before display.")
	public static void emptyAndFlowingFluidStacksAreInvalid(JeiGameTestHelper helper) {
		// Setup: native empty forms and unobtainable flowing variants exercise JEI's validity boundary.
		FluidIngredientHelper<FluidStack> ingredientHelper = createIngredientHelper();
		DisplayIngredientAcceptor acceptor = createIngredientAcceptor();
		FluidStack emptyFluid = new FluidStack(Fluids.EMPTY, 1000);
		FluidStack zeroAmountWater = new FluidStack(Fluids.WATER, 1000);
		zeroAmountWater.setAmount(0);
		FluidStack flowingWater = new FluidStack(Fluids.FLOWING_WATER, 1000);
		FluidStack flowingLava = new FluidStack(Fluids.FLOWING_LAVA, 1000);

		// Operation: submit every invalid form through JEI's display ingredient boundary.
		acceptor.add(NeoForgeTypes.FLUID_STACK, FluidStack.EMPTY);
		acceptor.add(NeoForgeTypes.FLUID_STACK, emptyFluid);
		acceptor.add(NeoForgeTypes.FLUID_STACK, zeroAmountWater);
		acceptor.add(NeoForgeTypes.FLUID_STACK, flowingWater);
		acceptor.add(NeoForgeTypes.FLUID_STACK, flowingLava);

		// Assertions: empty and flowing stacks are invalid and never reach display state.
		helper.assertTrue(FluidStack.EMPTY.isEmpty(), "Expected FluidStack.EMPTY to be empty");
		helper.assertTrue(emptyFluid.isEmpty(), "Expected an empty-fluid stack to be empty");
		helper.assertTrue(zeroAmountWater.isEmpty(), "Expected zero-sized water to be empty");
		helper.assertTrue(!ingredientHelper.isValidIngredient(FluidStack.EMPTY), "Expected FluidStack.EMPTY to be invalid");
		helper.assertTrue(!ingredientHelper.isValidIngredient(emptyFluid), "Expected an empty-fluid stack to be invalid");
		helper.assertTrue(!ingredientHelper.isValidIngredient(zeroAmountWater), "Expected zero-sized water to be invalid");
		helper.assertTrue(!ingredientHelper.isValidIngredient(flowingWater), "Expected flowing water to be invalid");
		helper.assertTrue(!ingredientHelper.isValidIngredient(flowingLava), "Expected flowing lava to be invalid");
		helper.assertTrue(
			acceptor.getAllIngredients().stream().noneMatch(Objects::nonNull),
			"Expected empty and flowing fluid stacks to be filtered before display"
		);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Non-empty NeoForge fluid stacks remain valid display ingredients.")
	public static void nonEmptyFluidStackIsAccepted(JeiGameTestHelper helper) {
		// Setup: a concrete water stack and a display acceptor backed by JEI's real fluid helper.
		DisplayIngredientAcceptor acceptor = createIngredientAcceptor();
		FluidStack water = new FluidStack(Fluids.WATER, 1000);

		// Operation: submit the stack through JEI's display ingredient boundary.
		acceptor.add(NeoForgeTypes.FLUID_STACK, water);

		// Assertions: a valid stack remains available with its fluid identity intact.
		ITypedIngredient<?> typedIngredient = acceptor.getAllIngredients().stream()
			.filter(Objects::nonNull)
			.findFirst()
			.orElseThrow();
		FluidStack accepted = typedIngredient.getIngredient(NeoForgeTypes.FLUID_STACK).orElseThrow();
		helper.assertTrue(accepted.getFluid() == Fluids.WATER, "Expected water to remain available for display");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Copying NeoForge fluid amounts keeps every empty stack empty.")
	public static void copyWithAmountHandlesEmptyAndNonEmptyStacks(JeiGameTestHelper helper) {
		// Setup: every native empty form plus normal water exercise the guarded and copying paths.
		FluidHelper fluidHelper = new FluidHelper();
		FluidStack emptyFluid = new FluidStack(Fluids.EMPTY, 1000);
		FluidStack zeroAmountWater = new FluidStack(Fluids.WATER, 1000);
		zeroAmountWater.setAmount(0);
		FluidStack water = new FluidStack(Fluids.WATER, 1000);

		// Operation: request a positive amount for every source stack.
		FluidStack emptyResult = fluidHelper.copyWithAmount(FluidStack.EMPTY, 250);
		FluidStack emptyFluidResult = fluidHelper.copyWithAmount(emptyFluid, 250);
		FluidStack zeroAmountWaterResult = fluidHelper.copyWithAmount(zeroAmountWater, 250);
		FluidStack waterResult = fluidHelper.copyWithAmount(water, 250);

		// Assertions: every empty input stays empty and valid water copies normally.
		helper.assertTrue(emptyResult == FluidStack.EMPTY, "Expected the canonical empty stack to be preserved");
		helper.assertTrue(emptyFluidResult == FluidStack.EMPTY, "Expected an empty-fluid stack to become canonical empty");
		helper.assertTrue(zeroAmountWaterResult == FluidStack.EMPTY, "Expected zero-sized water to become canonical empty");
		helper.assertTrue(emptyFluid.isEmpty(), "Expected the empty-fluid source to remain empty");
		helper.assertTrue(zeroAmountWater.isEmpty(), "Expected the zero-sized source to remain empty");
		helper.assertTrue(waterResult != water, "Expected a distinct copy of the water stack");
		helper.assertTrue(waterResult.getFluid() == Fluids.WATER, "Expected the copied fluid to remain water");
		helper.assertEquals(250, waterResult.getAmount(), "Expected the copied amount");
		helper.assertEquals(1000, water.getAmount(), "Expected the original amount to remain unchanged");
		helper.succeed();
	}

	private static FluidIngredientHelper<FluidStack> createIngredientHelper() {
		SubtypeManager subtypeManager = new SubtypeManager(new SubtypeInterpreters());
		return new FluidIngredientHelper<>(subtypeManager, TestColorHelper.INSTANCE, new FluidHelper());
	}

	private static DisplayIngredientAcceptor createIngredientAcceptor() {
		return new DisplayIngredientAcceptor(
			createIngredientManager(),
			new ContextMap.Builder().create(new ContextKeySet.Builder().build()),
			RecipeIngredientRole.INPUT
		);
	}

	private static IIngredientManagerInternal createIngredientManager() {
		SubtypeManager subtypeManager = new SubtypeManager(new SubtypeInterpreters());
		IngredientManagerBuilder builder = new IngredientManagerBuilder(
			subtypeManager,
			TestColorHelper.INSTANCE,
			new ContextMap.Builder().create(new ContextKeySet.Builder().build())
		);
		FluidHelper fluidHelper = new FluidHelper();
		FluidIngredientHelper<FluidStack> ingredientHelper = new FluidIngredientHelper<>(subtypeManager, TestColorHelper.INSTANCE, fluidHelper);
		builder.register(
			NeoForgeTypes.FLUID_STACK,
			List.of(),
			ingredientHelper,
			TestFluidRenderer.INSTANCE,
			fluidHelper.getCodec()
		);
		return builder.build();
	}

	private enum TestFluidRenderer implements IIngredientRenderer<FluidStack> {
		INSTANCE;

		@Override
		public void render(GuiGraphicsExtractor guiGraphics, FluidStack ingredient) {
		}

		@Override
		@Deprecated(since = "30.26.0", forRemoval = true)
		@SuppressWarnings("removal")
		public List<Component> getTooltip(FluidStack ingredient, TooltipFlag tooltipFlag) {
			return List.of();
		}

		@Override
		@Deprecated(since = "30.26.0", forRemoval = true)
		@SuppressWarnings("removal")
		public void getTooltip(ITooltipBuilder tooltip, FluidStack ingredient, TooltipFlag tooltipFlag) {

		}

		@Override
		public List<Component> getTooltip(FluidStack ingredient, Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
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
