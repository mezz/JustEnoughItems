package mezz.jei.test;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.forge.platform.FluidHelper;
import mezz.jei.library.ingredients.DisplayIngredientAcceptor;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.load.registration.IngredientManagerBuilder;
import mezz.jei.library.plugins.vanilla.ingredients.fluid.FluidIngredientHelper;
import mezz.jei.test.lib.ForgeTestBootstrap;
import mezz.jei.test.lib.TestColorHelper;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FluidHelperTest {
	@BeforeAll
	public static void setup() {
		SharedConstants.setVersion(DetectedVersion.BUILT_IN);
		ForgeTestBootstrap.bootStrap();
	}

	@Test
	public void emptyFluidStackIsInvalidIngredient() {
		FluidIngredientHelper<FluidStack> ingredientHelper = createIngredientHelper();

		assertTrue(FluidStack.EMPTY.isEmpty());
		assertSame(Fluids.EMPTY, ForgeTypes.FLUID_STACK.getBase(FluidStack.EMPTY));
		assertFalse(ingredientHelper.isValidIngredient(FluidStack.EMPTY));
	}

	@Test
	public void zeroAmountFluidStackIsInvalidIngredient() {
		FluidIngredientHelper<FluidStack> ingredientHelper = createIngredientHelper();
		FluidStack ingredient = new FluidStack(Fluids.WATER, 1000);
		ingredient.setAmount(0);
		assertTrue(ingredient.isEmpty());
		assertSame(Fluids.WATER, ingredient.getRawFluid());

		assertFalse(ingredientHelper.isValidIngredient(ingredient));
	}

	@Test
	public void emptyFluidStacksAreFilteredBeforeDisplay() {
		DisplayIngredientAcceptor acceptor = new DisplayIngredientAcceptor(createIngredientManager());
		FluidStack zeroAmountWater = new FluidStack(Fluids.WATER, 1000);
		zeroAmountWater.setAmount(0);

		acceptor.addIngredient(ForgeTypes.FLUID_STACK, FluidStack.EMPTY);
		acceptor.addIngredient(ForgeTypes.FLUID_STACK, zeroAmountWater);

		assertTrue(acceptor.getAllIngredients().stream().noneMatch(Objects::nonNull));
	}

	@Test
	public void nonEmptyFluidStackIsAcceptedForDisplay() {
		DisplayIngredientAcceptor acceptor = new DisplayIngredientAcceptor(createIngredientManager());
		FluidStack ingredient = new FluidStack(Fluids.WATER, 1000);

		acceptor.addIngredient(ForgeTypes.FLUID_STACK, ingredient);

		ITypedIngredient<?> typedIngredient = acceptor.getAllIngredients().stream()
			.filter(Objects::nonNull)
			.findFirst()
			.orElseThrow();
		FluidStack accepted = typedIngredient.getIngredient(ForgeTypes.FLUID_STACK).orElseThrow();
		assertSame(Fluids.WATER, accepted.getRawFluid());
	}

	@Test
	public void copyWithAmountPreservesEmptyFluidStack() {
		FluidHelper fluidHelper = new FluidHelper();

		FluidStack result = fluidHelper.copyWithAmount(FluidStack.EMPTY, 250);

		assertSame(FluidStack.EMPTY, result);
	}

	@Test
	public void copyWithAmountRecoversZeroAmountFluidStack() {
		FluidHelper fluidHelper = new FluidHelper();
		FluidStack original = new FluidStack(Fluids.WATER, 1000);
		original.setAmount(0);
		assertTrue(original.isEmpty());
		assertSame(Fluids.WATER, original.getRawFluid());

		FluidStack result = fluidHelper.copyWithAmount(original, 250);

		assertFalse(result.isEmpty());
		assertSame(Fluids.WATER, result.getFluid());
		assertEquals(250, result.getAmount());
		assertEquals(0, original.getAmount());
	}

	@Test
	public void copyWithAmountCopiesNonEmptyFluidStack() {
		FluidHelper fluidHelper = new FluidHelper();
		FluidStack original = new FluidStack(Fluids.WATER, 1000);

		FluidStack result = fluidHelper.copyWithAmount(original, 250);

		assertNotSame(original, result);
		assertSame(Fluids.WATER, result.getFluid());
		assertEquals(250, result.getAmount());
		assertEquals(1000, original.getAmount());
	}

	private static FluidIngredientHelper<FluidStack> createIngredientHelper() {
		SubtypeManager subtypeManager = new SubtypeManager(new SubtypeInterpreters());
		return new FluidIngredientHelper<>(subtypeManager, new TestColorHelper(), new FluidHelper());
	}

	private static IIngredientManager createIngredientManager() {
		SubtypeManager subtypeManager = new SubtypeManager(new SubtypeInterpreters());
		IngredientManagerBuilder builder = new IngredientManagerBuilder(subtypeManager, new TestColorHelper());
		FluidIngredientHelper<FluidStack> ingredientHelper = new FluidIngredientHelper<>(subtypeManager, new TestColorHelper(), new FluidHelper());
		builder.register(
			ForgeTypes.FLUID_STACK,
			List.of(),
			ingredientHelper,
			TestFluidRenderer.INSTANCE
		);
		return builder.build();
	}

	private enum TestFluidRenderer implements IIngredientRenderer<FluidStack> {
		INSTANCE;

		@Override
		public void render(PoseStack poseStack, FluidStack ingredient) {

		}

		@Override
		public List<Component> getTooltip(FluidStack ingredient, TooltipFlag tooltipFlag) {
			return List.of();
		}
	}
}
