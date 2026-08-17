package mezz.jei.test;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.fabric.ingredients.fluid.JeiFluidIngredient;
import mezz.jei.fabric.platform.FluidHelper;
import mezz.jei.library.ingredients.DisplayIngredientAcceptor;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.load.registration.IngredientManagerBuilder;
import mezz.jei.library.plugins.vanilla.ingredients.fluid.FluidIngredientHelper;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluids;
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
		Bootstrap.bootStrap();
	}

	@Test
	public void emptyFluidIsInvalidIngredient() {
		FluidIngredientHelper<IJeiFluidIngredient> ingredientHelper = createIngredientHelper();
		IJeiFluidIngredient ingredient = new JeiFluidIngredient(Fluids.EMPTY, 81000);

		assertSame(Fluids.EMPTY, FabricTypes.FLUID_STACK.getBase(ingredient));
		assertFalse(ingredientHelper.isValidIngredient(ingredient));
	}

	@Test
	public void zeroAmountFluidIsInvalidIngredient() {
		FluidIngredientHelper<IJeiFluidIngredient> ingredientHelper = createIngredientHelper();
		IJeiFluidIngredient ingredient = new JeiFluidIngredient(Fluids.WATER, 0);

		assertSame(Fluids.WATER, FabricTypes.FLUID_STACK.getBase(ingredient));
		assertFalse(ingredientHelper.isValidIngredient(ingredient));
	}

	@Test
	public void emptyFluidsAreFilteredBeforeDisplay() {
		DisplayIngredientAcceptor acceptor = new DisplayIngredientAcceptor(createIngredientManager());

		acceptor.addIngredient(FabricTypes.FLUID_STACK, new JeiFluidIngredient(Fluids.EMPTY, 81000));
		acceptor.addIngredient(FabricTypes.FLUID_STACK, new JeiFluidIngredient(Fluids.WATER, 0));

		assertTrue(acceptor.getAllIngredients().stream().noneMatch(Objects::nonNull));
	}

	@Test
	public void nonEmptyFluidIsAcceptedForDisplay() {
		DisplayIngredientAcceptor acceptor = new DisplayIngredientAcceptor(createIngredientManager());
		IJeiFluidIngredient ingredient = new JeiFluidIngredient(Fluids.WATER, 81000);

		acceptor.addIngredient(FabricTypes.FLUID_STACK, ingredient);

		ITypedIngredient<?> typedIngredient = acceptor.getAllIngredients().stream()
			.filter(Objects::nonNull)
			.findFirst()
			.orElseThrow();
		IJeiFluidIngredient accepted = typedIngredient.getIngredient(FabricTypes.FLUID_STACK).orElseThrow();
		assertSame(Fluids.WATER, accepted.getFluid());
	}

	@Test
	public void copyWithAmountCopiesEmptyFluidIngredient() {
		FluidHelper fluidHelper = new FluidHelper();
		IJeiFluidIngredient original = new JeiFluidIngredient(Fluids.EMPTY, 0);

		IJeiFluidIngredient result = fluidHelper.copyWithAmount(original, 0);

		assertNotSame(original, result);
		assertSame(Fluids.EMPTY, result.getFluid());
		assertEquals(0, result.getAmount());
	}

	@Test
	public void copyWithAmountCopiesNonEmptyFluidIngredient() {
		FluidHelper fluidHelper = new FluidHelper();
		IJeiFluidIngredient original = new JeiFluidIngredient(Fluids.WATER, 81000);

		IJeiFluidIngredient result = fluidHelper.copyWithAmount(original, 20250);

		assertNotSame(original, result);
		assertSame(Fluids.WATER, result.getFluid());
		assertEquals(20250, result.getAmount());
		assertEquals(81000, original.getAmount());
	}

	private static FluidIngredientHelper<IJeiFluidIngredient> createIngredientHelper() {
		SubtypeManager subtypeManager = new SubtypeManager(new SubtypeInterpreters());
		return new FluidIngredientHelper<>(subtypeManager, TestColorHelper.INSTANCE, new FluidHelper());
	}

	private static IIngredientManager createIngredientManager() {
		SubtypeManager subtypeManager = new SubtypeManager(new SubtypeInterpreters());
		IngredientManagerBuilder builder = new IngredientManagerBuilder(subtypeManager, TestColorHelper.INSTANCE);
		FluidIngredientHelper<IJeiFluidIngredient> ingredientHelper = new FluidIngredientHelper<>(subtypeManager, TestColorHelper.INSTANCE, new FluidHelper());
		builder.register(
			FabricTypes.FLUID_STACK,
			List.of(),
			ingredientHelper,
			TestFluidRenderer.INSTANCE
		);
		return builder.build();
	}

	private enum TestFluidRenderer implements IIngredientRenderer<IJeiFluidIngredient> {
		INSTANCE;

		@Override
		public void render(PoseStack poseStack, IJeiFluidIngredient ingredient) {

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
