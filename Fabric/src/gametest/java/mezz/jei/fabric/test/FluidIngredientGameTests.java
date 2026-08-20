package mezz.jei.fabric.test;

import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.util.RegistryUtil;
import mezz.jei.fabric.ingredients.fluid.JeiFluidIngredient;
import mezz.jei.fabric.platform.FluidHelper;
import mezz.jei.library.ingredients.DisplayIngredientAcceptor;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.load.registration.IngredientManagerBuilder;
import mezz.jei.library.plugins.vanilla.ingredients.fluid.FluidIngredientHelper;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.Objects;

public final class FluidIngredientGameTests {
	@GameTest
	public void emptyFluidsAreInvalid(GameTestHelper helper) {
		// Setup: one blank variant and one zero-sized water variant are both empty Fabric ingredients.
		RegistryUtil.setRegistryAccess(helper.getLevel().registryAccess());
		FluidIngredientHelper<IJeiFluidIngredient> ingredientHelper = createIngredientHelper();
		DisplayIngredientAcceptor acceptor = createIngredientAcceptor();
		IJeiFluidIngredient blank = new JeiFluidIngredient(FluidVariant.blank(), FluidConstants.BUCKET);
		IJeiFluidIngredient zeroAmountWater = new JeiFluidIngredient(FluidVariant.of(Fluids.WATER), 0);

		// Operation: submit both empty forms through JEI's display ingredient boundary.
		acceptor.add(FabricTypes.FLUID_STACK, blank);
		acceptor.add(FabricTypes.FLUID_STACK, zeroAmountWater);

		// Assertions: neither empty form is valid or reaches display state.
		helper.assertFalse(ingredientHelper.isValidIngredient(blank), "Expected a blank fluid variant to be invalid");
		helper.assertFalse(ingredientHelper.isValidIngredient(zeroAmountWater), "Expected zero-sized water to be invalid");
		helper.assertTrue(
			acceptor.getAllIngredients().stream().noneMatch(Objects::nonNull),
			"Expected empty fluid ingredients to be filtered before display"
		);
		helper.succeed();
	}

	@GameTest
	public void flowingFluidsAreCanonicalizedToSources(GameTestHelper helper) {
		// Setup: Fabric's transfer API converts flowing fluids to their obtainable source variants.
		RegistryUtil.setRegistryAccess(helper.getLevel().registryAccess());
		FluidIngredientHelper<IJeiFluidIngredient> ingredientHelper = createIngredientHelper();
		IJeiFluidIngredient flowingWater = new JeiFluidIngredient(FluidVariant.of(Fluids.FLOWING_WATER), FluidConstants.BUCKET);
		IJeiFluidIngredient flowingLava = new JeiFluidIngredient(FluidVariant.of(Fluids.FLOWING_LAVA), FluidConstants.BUCKET);

		// Assertions: no flowing identity reaches JEI, and the canonical source ingredients remain valid.
		helper.assertTrue(
			flowingWater.getFluidVariant().getFluid() == Fluids.WATER,
			"Expected flowing water to be canonicalized to source water"
		);
		helper.assertTrue(
			flowingLava.getFluidVariant().getFluid() == Fluids.LAVA,
			"Expected flowing lava to be canonicalized to source lava"
		);
		helper.assertTrue(ingredientHelper.isValidIngredient(flowingWater), "Expected canonical source water to be valid");
		helper.assertTrue(ingredientHelper.isValidIngredient(flowingLava), "Expected canonical source lava to be valid");
		helper.succeed();
	}

	@GameTest
	public void nonEmptyFluidIsAccepted(GameTestHelper helper) {
		// Setup: a concrete water variant and a display acceptor backed by JEI's real fluid helper.
		RegistryUtil.setRegistryAccess(helper.getLevel().registryAccess());
		DisplayIngredientAcceptor acceptor = createIngredientAcceptor();
		IJeiFluidIngredient water = new JeiFluidIngredient(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET);

		// Operation: submit the ingredient through JEI's display ingredient boundary.
		acceptor.add(FabricTypes.FLUID_STACK, water);

		// Assertions: a valid ingredient remains available with its fluid identity intact.
		ITypedIngredient<?> typedIngredient = acceptor.getAllIngredients().stream()
			.filter(Objects::nonNull)
			.findFirst()
			.orElseThrow();
		IJeiFluidIngredient accepted = typedIngredient.getIngredient(FabricTypes.FLUID_STACK).orElseThrow();
		helper.assertTrue(
			accepted.getFluidVariant().getFluid() == Fluids.WATER,
			"Expected water to remain available for display"
		);
		helper.succeed();
	}

	@GameTest
	public void copyWithAmountKeepsEmptyIngredientsEmpty(GameTestHelper helper) {
		// Setup: blank, zero-sized water, and normal water exercise every copying path.
		FluidHelper fluidHelper = new FluidHelper();
		IJeiFluidIngredient blank = new JeiFluidIngredient(FluidVariant.blank(), FluidConstants.BUCKET);
		IJeiFluidIngredient zeroAmountWater = new JeiFluidIngredient(FluidVariant.of(Fluids.WATER), 0);
		IJeiFluidIngredient water = new JeiFluidIngredient(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET);

		// Operation: request a full-bucket amount for every source ingredient.
		IJeiFluidIngredient blankResult = fluidHelper.copyWithAmount(blank, FluidConstants.BUCKET);
		IJeiFluidIngredient zeroAmountWaterResult = fluidHelper.copyWithAmount(zeroAmountWater, FluidConstants.BUCKET);
		IJeiFluidIngredient waterResult = fluidHelper.copyWithAmount(water, 250);

		// Assertions: every empty input stays empty and valid water copies normally.
		helper.assertTrue(blankResult.getFluidVariant().isBlank(), "Expected the blank variant to remain blank");
		helper.assertTrue(blankResult.getAmount() == 0, "Expected a blank result to have zero amount");
		helper.assertTrue(zeroAmountWaterResult.getFluidVariant().isBlank(), "Expected zero-sized water to become blank");
		helper.assertTrue(zeroAmountWaterResult.getAmount() == 0, "Expected zero-sized water to remain empty");
		helper.assertTrue(zeroAmountWater.getAmount() == 0, "Expected the zero-sized source to remain unchanged");
		helper.assertTrue(waterResult != water, "Expected a distinct copy of valid water");
		helper.assertTrue(waterResult.getFluidVariant().getFluid() == Fluids.WATER, "Expected copied water");
		helper.assertTrue(waterResult.getAmount() == 250, "Expected the copied amount");
		helper.assertTrue(water.getAmount() == FluidConstants.BUCKET, "Expected the valid source to remain unchanged");
		helper.succeed();
	}

	private static FluidIngredientHelper<IJeiFluidIngredient> createIngredientHelper() {
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

	private enum TestFluidRenderer implements IIngredientRenderer<IJeiFluidIngredient> {
		INSTANCE;

		@Override
		public void render(GuiGraphicsExtractor guiGraphics, IJeiFluidIngredient ingredient) {
		}

		@Override
		public List<Component> getTooltip(IJeiFluidIngredient ingredient, TooltipFlag tooltipFlag) {
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
