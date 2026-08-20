package mezz.jei.forge.tests.ingredients.fluid;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.forge.platform.FluidHelper;
import mezz.jei.forge.tests.lib.JeiGameTestHelper;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.plugins.vanilla.ingredients.fluid.FluidIngredientHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder("jei")
@PrefixGameTestTemplate(false)
public final class FluidIngredientGameTests {
	private FluidIngredientGameTests() {
	}

	@GameTest(template = "empty")
	public static void flowingFluidStacksAreInvalid(GameTestHelper gameTestHelper) {
		JeiGameTestHelper helper = new JeiGameTestHelper(gameTestHelper);
		SubtypeManager subtypeManager = new SubtypeManager(new SubtypeInterpreters());
		FluidIngredientHelper<FluidStack> ingredientHelper = new FluidIngredientHelper<>(
			subtypeManager,
			TestColorHelper.INSTANCE,
			new FluidHelper()
		);
		FluidStack flowingWater = new FluidStack(Fluids.FLOWING_WATER, FluidType.BUCKET_VOLUME);
		FluidStack flowingLava = new FluidStack(Fluids.FLOWING_LAVA, FluidType.BUCKET_VOLUME);

		helper.assertTrue(!ingredientHelper.isValidIngredient(flowingWater), "Expected flowing water to be invalid");
		helper.assertTrue(!ingredientHelper.isValidIngredient(flowingLava), "Expected flowing lava to be invalid");
		helper.succeed();
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
