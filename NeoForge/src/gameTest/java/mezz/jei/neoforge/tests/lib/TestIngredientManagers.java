package mezz.jei.neoforge.tests.lib;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.util.StackHelper;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.load.registration.IngredientManagerBuilder;
import mezz.jei.library.load.registration.SubtypeRegistration;
import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import mezz.jei.library.plugins.vanilla.VanillaRecipeFactory;
import mezz.jei.library.plugins.vanilla.ingredients.ItemStackHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.Collection;
import java.util.List;

public final class TestIngredientManagers {
	private TestIngredientManagers() {
	}

	public static VanillaRecipeFactory createVanillaRecipeFactory() {
		return new VanillaRecipeFactory(createVanillaItemStackHelper());
	}

	public static IIngredientManager createVanillaItemStackIngredientManager(ServerLevel level) {
		List<ItemStack> itemStacks = level.registryAccess()
			.lookupOrThrow(Registries.ITEM)
			.filterFeatures(level.enabledFeatures())
			.listElements()
			.map(ItemStack::new)
			.filter(stack -> !stack.isEmpty())
			.toList();
		return createVanillaItemStackIngredientManager(itemStacks);
	}

	public static IIngredientManager createVanillaItemStackIngredientManager(Collection<ItemStack> itemStacks) {
		SubtypeManager subtypeManager = createVanillaSubtypeManager();
		IColorHelper colorHelper = new NoOpColorHelper();
		IngredientManagerBuilder builder = new IngredientManagerBuilder(subtypeManager, colorHelper);
		ItemStackHelper itemStackHelper = createVanillaItemStackHelper(subtypeManager, colorHelper);
		builder.register(
			VanillaTypes.ITEM_STACK,
			itemStacks,
			itemStackHelper,
			new NoOpItemStackRenderer(),
			ItemStack.CODEC
		);
		return builder.build();
	}

	private static IIngredientHelper<ItemStack> createVanillaItemStackHelper() {
		SubtypeManager subtypeManager = createVanillaSubtypeManager();
		IColorHelper colorHelper = new NoOpColorHelper();
		return createVanillaItemStackHelper(subtypeManager, colorHelper);
	}

	private static SubtypeManager createVanillaSubtypeManager() {
		SubtypeRegistration subtypeRegistration = new SubtypeRegistration();
		new VanillaPlugin().registerItemSubtypes(subtypeRegistration);
		return new SubtypeManager(subtypeRegistration.getInterpreters());
	}

	private static ItemStackHelper createVanillaItemStackHelper(SubtypeManager subtypeManager, IColorHelper colorHelper) {
		StackHelper stackHelper = new StackHelper(subtypeManager);
		return new ItemStackHelper(stackHelper, colorHelper);
	}

	private static class NoOpColorHelper implements IColorHelper {
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

	private static class NoOpItemStackRenderer implements IIngredientRenderer<ItemStack> {
		@Override
		public void render(GuiGraphicsExtractor guiGraphics, ItemStack ingredient) {
		}

		@Override
		public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
			return List.of();
		}
	}
}
