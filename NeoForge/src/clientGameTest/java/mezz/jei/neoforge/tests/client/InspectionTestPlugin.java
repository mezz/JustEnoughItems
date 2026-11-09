package mezz.jei.neoforge.tests.client;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferContext;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferListener;
import mezz.jei.api.recipe.transfer.RecipeTransferResult;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.IntStream;

@JeiPlugin
public final class InspectionTestPlugin implements IModPlugin {
	static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("jeiclienttests", "inspection");
	static final RecipeType<Integer> TYPE = RecipeType.create("jeiclienttests", "inspection", Integer.class);
	static volatile IJeiRuntime runtime;
	static int attempts;
	static int starts;
	static int completions;
	static int vanillaCompletions;
	static boolean lastMax;
	static boolean blocked;

	@Override
	public ResourceLocation getPluginUid() {
		return UID;
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		runtime = jeiRuntime;
	}

	@Override
	public void onRuntimeUnavailable() {
		runtime = null;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(new Category());
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		registration.addRecipes(TYPE, IntStream.range(0, 30).boxed().toList());
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		registration.addRecipeTransferHandler(new Transfer(), TYPE);
		registration.addRecipeTransferListener(new IRecipeTransferListener() {
			@Override
			public void beforeRecipeTransfer(IRecipeTransferContext<?, ?> context) {
				if (context.getRecipeType().equals(TYPE)) {
					starts++;
				}
			}

			@Override
			public void afterRecipeTransfer(IRecipeTransferContext<?, ?> context, RecipeTransferResult result) {
				if (context.getRecipeType().equals(mezz.jei.api.constants.RecipeTypes.CRAFTING) && result == RecipeTransferResult.SUCCESS) {
					vanillaCompletions++;
				}
				if (context.getRecipeType().equals(TYPE) && result == RecipeTransferResult.SUCCESS) {
					completions++;
				}
			}
		});
	}

	private static final class Category implements IRecipeCategory<Integer> {
		@Override
		public RecipeType<Integer> getRecipeType() { return TYPE; }
		@Override
		public Component getTitle() { return Component.literal("Inspection test"); }
		@Override
		public @Nullable IDrawable getIcon() { return null; }
		@Override
		public int getWidth() { return 120; }
		@Override
		public int getHeight() { return 60; }

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, Integer recipe, IFocusGroup focuses) {
			builder.addSlot(RecipeIngredientRole.INPUT, 4, 4).addItemStack(new ItemStack(Items.OAK_LOG));
			builder.addSlot(RecipeIngredientRole.OUTPUT, 90, 4).addItemStack(new ItemStack(Items.APPLE, recipe + 1));
		}
	}

	private static final class Transfer implements IRecipeTransferHandler<InventoryMenu, Integer> {
		@Override
		public Class<InventoryMenu> getContainerClass() { return InventoryMenu.class; }
		@Override
		public Optional<MenuType<InventoryMenu>> getMenuType() { return Optional.empty(); }
		@Override
		public RecipeType<Integer> getRecipeType() { return TYPE; }

		@Override
		public @Nullable IRecipeTransferError transferRecipe(InventoryMenu menu, Integer recipe, IRecipeSlotsView slots, Player player, boolean max, boolean transfer) {
			throw new AssertionError("The transfer manager must use the transfer context entry point");
		}

		@Override
		public @Nullable IRecipeTransferError transferRecipe(IRecipeTransferContext<Integer, InventoryMenu> context, boolean doTransfer) {
			if (blocked) {
				return () -> IRecipeTransferError.Type.USER_FACING;
			}
			if (doTransfer) {
				attempts++;
				lastMax = context.isMaxTransfer();
				context.completeRecipeTransfer(RecipeTransferResult.SUCCESS);
			}
			return null;
		}
	}
}
