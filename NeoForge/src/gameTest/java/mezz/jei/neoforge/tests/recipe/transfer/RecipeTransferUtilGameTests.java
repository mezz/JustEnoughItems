package mezz.jei.neoforge.tests.recipe.transfer;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.common.transfer.RecipeTransferErrorInternal;
import mezz.jei.common.transfer.RecipeTransferUtil;
import mezz.jei.neoforge.tests.lib.JeiGameTestHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@ForEachTest(groups = "recipe_transfer")
public final class RecipeTransferUtilGameTests {
	private static final RecipeType<Object> RECIPE_TYPE = RecipeType.create("jei", "test_recipe_transfer", Object.class);
	private static final TestRecipeCategory RECIPE_CATEGORY = new TestRecipeCategory();
	private static final TestRecipeLayout RECIPE_LAYOUT = new TestRecipeLayout();

	private RecipeTransferUtilGameTests() {
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Recipe transfer succeeds when the handler returns no error.")
	public static void transferErrorIsEmptyWhenHandlerSucceeds(JeiGameTestHelper helper) {
		// Setup: a registered transfer handler accepts the transfer check.
		TestMenu menu = new TestMenu();
		ServerPlayer player = helper.getPlayer();
		IRecipeTransferManager transferManager = new TestTransferManager(new TestTransferHandler(HandlerResult.SUCCESS));

		// Operation: ask JEI for the transfer error without executing the transfer.
		Optional<IRecipeTransferError> transferError = RecipeTransferUtil.getTransferRecipeError(
			transferManager,
			menu,
			RECIPE_LAYOUT,
			player
		);

		// Assertions: successful checks return no error.
		helper.assertTrue(transferError.isEmpty(), "Expected successful transfer check to return no error");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Recipe transfer returns handler errors.")
	public static void transferErrorReturnsHandlerError(JeiGameTestHelper helper) {
		// Setup: a registered transfer handler reports its own error.
		TestMenu menu = new TestMenu();
		ServerPlayer player = helper.getPlayer();
		IRecipeTransferManager transferManager = new TestTransferManager(new TestTransferHandler(HandlerResult.ERROR));

		// Operation: ask JEI for the transfer error without executing the transfer.
		Optional<IRecipeTransferError> transferError = RecipeTransferUtil.getTransferRecipeError(
			transferManager,
			menu,
			RECIPE_LAYOUT,
			player
		);

		// Assertions: the handler's error is returned to the caller.
		helper.assertEquals(RecipeTransferErrorInternal.INSTANCE, transferError.orElseThrow(), "Expected handler error");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Missing recipe transfer handlers return internal errors.")
	public static void missingTransferHandlerReturnsInternalError(JeiGameTestHelper helper) {
		// Setup: no transfer handler is registered for this menu and recipe category.
		TestMenu menu = new TestMenu();
		ServerPlayer player = helper.getPlayer();
		IRecipeTransferManager transferManager = new EmptyTransferManager();

		// Operation: ask JEI for the transfer error.
		Optional<IRecipeTransferError> transferError = RecipeTransferUtil.getTransferRecipeError(
			transferManager,
			menu,
			RECIPE_LAYOUT,
			player
		);

		// Assertions: missing handlers are treated as internal transfer errors.
		helper.assertEquals(RecipeTransferErrorInternal.INSTANCE, transferError.orElseThrow(), "Expected internal transfer error");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Broken recipe transfer handlers return internal errors.")
	public static void brokenTransferHandlerReturnsInternalError(JeiGameTestHelper helper) {
		// Setup: the registered transfer handler throws during execution.
		TestMenu menu = new TestMenu();
		ServerPlayer player = helper.getPlayer();
		IRecipeTransferManager transferManager = new TestTransferManager(new TestTransferHandler(HandlerResult.THROW));

		// Operation: execute the transfer through JEI's defensive wrapper.
		boolean transferred = RecipeTransferUtil.transferRecipe(
			transferManager,
			menu,
			RECIPE_LAYOUT,
			player,
			false
		);

		// Assertions: the exception is converted into a failed transfer instead of escaping.
		helper.assertTrue(!transferred, "Expected broken transfer handler to fail without throwing");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Recipe transfer returns true when the handler succeeds.")
	public static void transferRecipeReturnsTrueWhenHandlerSucceeds(JeiGameTestHelper helper) {
		// Setup: a registered transfer handler returns success while executing a transfer.
		TestMenu menu = new TestMenu();
		ServerPlayer player = helper.getPlayer();
		IRecipeTransferManager transferManager = new TestTransferManager(new TestTransferHandler(HandlerResult.SUCCESS));

		// Operation: execute the transfer through JEI's transfer utility.
		boolean transferred = RecipeTransferUtil.transferRecipe(
			transferManager,
			menu,
			RECIPE_LAYOUT,
			player,
			false
		);

		// Assertions: successful handlers allow the transfer to be reported as successful.
		helper.assertTrue(transferred, "Expected successful transfer handler to report success");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Recipe transfer returns false when the handler reports an error.")
	public static void transferRecipeReturnsFalseWhenHandlerReturnsError(JeiGameTestHelper helper) {
		// Setup: a registered transfer handler returns a blocking error while executing a transfer.
		TestMenu menu = new TestMenu();
		ServerPlayer player = helper.getPlayer();
		IRecipeTransferManager transferManager = new TestTransferManager(new TestTransferHandler(HandlerResult.ERROR));

		// Operation: execute the transfer through JEI's transfer utility.
		boolean transferred = RecipeTransferUtil.transferRecipe(
			transferManager,
			menu,
			RECIPE_LAYOUT,
			player,
			false
		);

		// Assertions: blocking handler errors report the transfer as failed.
		helper.assertTrue(!transferred, "Expected transfer handler error to report failure");
		helper.succeed();
	}

	private record TestTransferManager(
		IRecipeTransferHandler<TestMenu, Object> transferHandler
	) implements IRecipeTransferManager {
		@Override
		public <C extends AbstractContainerMenu, R> Optional<IRecipeTransferHandler<C, R>> getRecipeTransferHandler(
			C container,
			IRecipeCategory<R> recipeCategory
		) {
			@SuppressWarnings("unchecked")
			IRecipeTransferHandler<C, R> castHandler = (IRecipeTransferHandler<C, R>) transferHandler;
			return Optional.of(castHandler);
		}
	}

	private static class EmptyTransferManager implements IRecipeTransferManager {
		@Override
		public <C extends AbstractContainerMenu, R> Optional<IRecipeTransferHandler<C, R>> getRecipeTransferHandler(
			C container,
			IRecipeCategory<R> recipeCategory
		) {
			return Optional.empty();
		}
	}

	private enum HandlerResult {
		SUCCESS,
		ERROR,
		THROW
	}

	private record TestTransferHandler(
		HandlerResult result
	) implements IRecipeTransferHandler<TestMenu, Object> {
		@Override
		public Class<? extends TestMenu> getContainerClass() {
			return TestMenu.class;
		}

		@Override
		public Optional<MenuType<TestMenu>> getMenuType() {
			return Optional.empty();
		}

		@Override
		public RecipeType<Object> getRecipeType() {
			return RECIPE_TYPE;
		}

		@Override
		public @Nullable IRecipeTransferError transferRecipe(
			TestMenu container,
			Object recipe,
			IRecipeSlotsView recipeSlots,
			Player player,
			boolean maxTransfer,
			boolean doTransfer
		) {
			return switch (result) {
				case SUCCESS -> null;
				case ERROR -> RecipeTransferErrorInternal.INSTANCE;
				case THROW -> throw new IllegalStateException("transfer handler failed");
			};
		}
	}

	private static class TestRecipeLayout implements IRecipeLayoutDrawable<Object> {
		@Override
		public void setPosition(int posX, int posY) {

		}

		@Override
		public void drawRecipe(GuiGraphics guiGraphics, int mouseX, int mouseY) {

		}

		@Override
		public void drawOverlays(GuiGraphics guiGraphics, int mouseX, int mouseY) {

		}

		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			return false;
		}

		@Override
		public <T> Optional<T> getIngredientUnderMouse(int mouseX, int mouseY, IIngredientType<T> ingredientType) {
			return Optional.empty();
		}

		@Override
		public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
			return Optional.empty();
		}

		@Override
		public Optional<IRecipeSlotDrawable> getRecipeSlotUnderMouse(double mouseX, double mouseY) {
			return Optional.empty();
		}

		@Override
		public Rect2i getRect() {
			return new Rect2i(0, 0, 100, 50);
		}

		@Override
		public Rect2i getRectWithBorder() {
			return getRect();
		}

		@Override
		public Rect2i getSideButtonArea(int buttonIndex) {
			return new Rect2i(0, 0, 13, 13);
		}

		@Override
		public IRecipeSlotsView getRecipeSlotsView() {
			return () -> List.of();
		}

		@Override
		public IRecipeCategory<Object> getRecipeCategory() {
			return RECIPE_CATEGORY;
		}

		@Override
		public Object getRecipe() {
			return new Object();
		}

		@Override
		public IJeiInputHandler getInputHandler() {
			throw new UnsupportedOperationException("Not needed for this test");
		}

		@Override
		public void tick() {

		}
	}

	private static class TestRecipeCategory implements IRecipeCategory<Object> {
		@Override
		public RecipeType<Object> getRecipeType() {
			return RECIPE_TYPE;
		}

		@Override
		public net.minecraft.network.chat.Component getTitle() {
			return net.minecraft.network.chat.Component.literal("Test Recipe Transfer");
		}

		@Override
		public int getWidth() {
			return 100;
		}

		@Override
		public int getHeight() {
			return 50;
		}

		@Override
		public @Nullable IDrawable getIcon() {
			return null;
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, Object recipe, IFocusGroup focuses) {

		}
	}

	private static class TestMenu extends AbstractContainerMenu {
		private TestMenu() {
			super(null, 0);
		}

		@Override
		public ItemStack quickMoveStack(Player player, int slotIndex) {
			return ItemStack.EMPTY;
		}

		@Override
		public boolean stillValid(Player player) {
			return true;
		}
	}
}
