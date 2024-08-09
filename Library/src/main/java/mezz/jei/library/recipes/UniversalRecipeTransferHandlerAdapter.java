package mezz.jei.library.recipes;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class UniversalRecipeTransferHandlerAdapter<C extends AbstractContainerMenu, R> implements IRecipeTransferHandler<C, R> {
	private final IUniversalRecipeTransferHandler<C> universalRecipeTransferHandler;

	public UniversalRecipeTransferHandlerAdapter(IUniversalRecipeTransferHandler<C> universalRecipeTransferHandler) {
		this.universalRecipeTransferHandler = universalRecipeTransferHandler;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<C> getContainerClass() {
		return (Class<C>) universalRecipeTransferHandler.getContainerClass();
	}

	@Override
	public Optional<MenuType<C>> getMenuType() {
		return universalRecipeTransferHandler.getMenuType();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<R> getRecipeClass() {
		return (Class<R>) (Class<?>) Object.class;
	}

	@Override
	public @Nullable IRecipeTransferError transferRecipe(C container, R recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
		return universalRecipeTransferHandler.transferRecipe(container, recipe, recipeSlots, player, maxTransfer, doTransfer);
	}
}
