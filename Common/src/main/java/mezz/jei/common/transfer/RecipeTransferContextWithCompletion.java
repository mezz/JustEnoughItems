package mezz.jei.common.transfer;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferContext;
import mezz.jei.api.recipe.transfer.RecipeTransferResult;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.function.Consumer;

public final class RecipeTransferContextWithCompletion<R, C extends AbstractContainerMenu> implements IRecipeTransferContext<R, C> {
	private final IRecipeTransferContext<R, C> context;
	private final Consumer<RecipeTransferResult> completion;

	public RecipeTransferContextWithCompletion(IRecipeTransferContext<R, C> context, Consumer<RecipeTransferResult> completion) {
		this.context = context;
		this.completion = completion;
	}

	@Override
	public int getTransferId() {
		return context.getTransferId();
	}

	@Override
	public void completeRecipeTransfer(RecipeTransferResult result) {
		completion.accept(result);
	}

	@Override
	public R getRecipe() {
		return context.getRecipe();
	}

	@Override
	public IRecipeType<R> getRecipeType() {
		return context.getRecipeType();
	}

	@Override
	public C getContainer() {
		return context.getContainer();
	}

	@Override
	public AbstractContainerScreen<C> getScreen() {
		return context.getScreen();
	}

	@Override
	public IRecipeSlotsView getRecipeSlots() {
		return context.getRecipeSlots();
	}

	@Override
	public Player getPlayer() {
		return context.getPlayer();
	}

	@Override
	public boolean isMaxTransfer() {
		return context.isMaxTransfer();
	}
}
