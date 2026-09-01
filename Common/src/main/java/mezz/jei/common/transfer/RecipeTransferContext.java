package mezz.jei.common.transfer;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferContext;
import mezz.jei.api.recipe.transfer.RecipeTransferResult;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.function.BiConsumer;

public class RecipeTransferContext<R, C extends AbstractContainerMenu> implements IRecipeTransferContext<R, C> {
	private final int transferId;
	private final R recipe;
	private final RecipeType<R> recipeType;
	private final AbstractContainerScreen<C> screen;
	private final IRecipeSlotsView recipeSlots;
	private final Player player;
	private final boolean maxTransfer;
	private final BiConsumer<IRecipeTransferContext<R, C>, RecipeTransferResult> completionHandler;

	public RecipeTransferContext(
		int transferId,
		R recipe,
		RecipeType<R> recipeType,
		AbstractContainerScreen<C> screen,
		IRecipeSlotsView recipeSlots,
		Player player,
		boolean maxTransfer,
		RecipeTransferLifecycleManager recipeTransferLifecycleManager
	) {
		this(
			transferId,
			recipe,
			recipeType,
			screen,
			recipeSlots,
			player,
			maxTransfer,
			recipeTransferLifecycleManager::completeRecipeTransfer
		);
	}

	private RecipeTransferContext(
		int transferId,
		R recipe,
		RecipeType<R> recipeType,
		AbstractContainerScreen<C> screen,
		IRecipeSlotsView recipeSlots,
		Player player,
		boolean maxTransfer,
		BiConsumer<IRecipeTransferContext<R, C>, RecipeTransferResult> completionHandler
	) {
		this.transferId = transferId;
		this.recipe = recipe;
		this.recipeType = recipeType;
		this.screen = screen;
		this.recipeSlots = recipeSlots;
		this.player = player;
		this.maxTransfer = maxTransfer;
		this.completionHandler = completionHandler;
	}

	public static <R, C extends AbstractContainerMenu> RecipeTransferContext<R, C> copyWithRecipeSlots(
		IRecipeTransferContext<R, C> context,
		IRecipeSlotsView recipeSlots
	) {
		return new RecipeTransferContext<>(
			context.getTransferId(),
			context.getRecipe(),
			context.getRecipeType(),
			context.getScreen(),
			recipeSlots,
			context.getPlayer(),
			context.isMaxTransfer(),
			(ignoredContext, result) -> context.completeRecipeTransfer(result)
		);
	}

	@Override
	public int getTransferId() {
		return transferId;
	}

	@Override
	public void completeRecipeTransfer(RecipeTransferResult result) {
		completionHandler.accept(this, result);
	}

	@Override
	public R getRecipe() {
		return recipe;
	}

	@Override
	public RecipeType<R> getRecipeType() {
		return recipeType;
	}

	@Override
	public C getContainer() {
		return screen.getMenu();
	}

	@Override
	public AbstractContainerScreen<C> getScreen() {
		return screen;
	}

	@Override
	public IRecipeSlotsView getRecipeSlots() {
		return recipeSlots;
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public boolean isMaxTransfer() {
		return maxTransfer;
	}
}
