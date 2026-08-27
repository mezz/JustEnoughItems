package mezz.jei.neoforge.tests.lib;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.registration.ISlotDisplayInterpreterRegistration;
import mezz.jei.common.util.StackHelper;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import mezz.jei.library.load.registration.IngredientManagerBuilder;
import mezz.jei.library.load.registration.SubtypeRegistration;
import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import mezz.jei.library.plugins.vanilla.VanillaRecipeFactory;
import mezz.jei.library.plugins.vanilla.ingredients.ItemStackCodecs;
import mezz.jei.library.plugins.vanilla.ingredients.ItemStackHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public final class TestIngredientManagers {
	private TestIngredientManagers() {
	}

	public static VanillaRecipeFactory createVanillaRecipeFactory(ContextMap contextMap) {
		return new VanillaRecipeFactory(createVanillaItemStackHelper(), contextMap);
	}

	public static IIngredientManagerInternal createVanillaItemStackIngredientManager(ServerLevel level) {
		List<ItemStack> itemStacks = level.registryAccess()
			.lookupOrThrow(Registries.ITEM)
			.filterFeatures(level.enabledFeatures())
			.listElements()
			.map(ItemStack::new)
			.filter(stack -> !stack.isEmpty())
			.toList();
		return createVanillaItemStackIngredientManager(itemStacks);
	}

	public static IIngredientManagerInternal createVanillaItemStackIngredientManager(Collection<ItemStack> itemStacks) {
		return createVanillaItemStackIngredientManager(
			itemStacks,
			registration -> new VanillaPlugin().registerSlotDisplayInterpreters(registration)
		);
	}

	public static IIngredientManagerInternal createVanillaItemStackIngredientManager(
		Collection<ItemStack> itemStacks,
		Consumer<ISlotDisplayInterpreterRegistration> registerSlotDisplayInterpreters
	) {
		SubtypeManager subtypeManager = createVanillaSubtypeManager();
		IColorHelper colorHelper = new NoOpColorHelper();
		IngredientManagerBuilder builder = new IngredientManagerBuilder(
			subtypeManager,
			colorHelper,
			new ContextMap.Builder().create(new ContextKeySet.Builder().build())
		);
		ItemStackHelper itemStackHelper = createVanillaItemStackHelper(subtypeManager, colorHelper);
		builder.register(
			VanillaTypes.ITEM_STACK,
			itemStacks,
			itemStackHelper,
			new NoOpItemStackRenderer(),
			ItemStackCodecs.createStrictSingleItemCodec()
		);
		registerSlotDisplayInterpreters.accept(builder.getSlotDisplayInterpreterRegistration());
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
		@Deprecated(since = "29.31.0", forRemoval = true)
		@SuppressWarnings("removal")
		public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
			return getTooltip(ingredient, Item.TooltipContext.EMPTY, null, tooltipFlag);
		}

		@Override
		@Deprecated(since = "29.31.0", forRemoval = true)
		@SuppressWarnings("removal")
		public void getTooltip(ITooltipBuilder tooltip, ItemStack ingredient, TooltipFlag tooltipFlag) {
			getTooltip(tooltip, ingredient, Item.TooltipContext.EMPTY, null, tooltipFlag);
		}

		@Override
		public List<Component> getTooltip(ItemStack ingredient, Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
			return List.of();
		}
	}
}
