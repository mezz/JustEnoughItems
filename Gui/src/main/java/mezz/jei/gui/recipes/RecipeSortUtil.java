package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Comparator;
import java.util.List;

public class RecipeSortUtil {
	private static final Comparator<RecipeLayoutWithButtons<?>> CRAFTABLE_COMPARATOR = createCraftableComparator();

	public static List<IRecipeCategory<?>> sortRecipeCategories(
		List<IRecipeCategory<?>> recipeCategories,
		IRecipeTransferManager recipeTransferManager
	) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return recipeCategories;
		}
		AbstractContainerMenu openContainer = player.containerMenu;
		//noinspection ConstantConditions
		if (openContainer == null) {
			return recipeCategories;
		}

		Comparator<IRecipeCategory<?>> comparator = Comparator.comparing((IRecipeCategory<?> r) -> {
			var recipeTransferHandler = recipeTransferManager.getRecipeTransferHandler(openContainer, r);
			return recipeTransferHandler.isPresent();
		})
			.reversed();

		return recipeCategories.stream()
			.sorted(comparator)
			.toList();
	}

	public static Comparator<RecipeLayoutWithButtons<?>> getCraftableComparator() {
		return CRAFTABLE_COMPARATOR;
	}

	private static Comparator<RecipeLayoutWithButtons<?>> createCraftableComparator() {
		return Comparator.comparingInt(r -> {
			IRecipeLayoutDrawable<?> recipeLayout = r.recipeLayout();
			List<IRecipeSlotView> inputSlotViews = recipeLayout.getRecipeSlotsView()
				.getSlotViews(RecipeIngredientRole.INPUT);

			RecipeTransferButton transferButton = r.transferButton();
			int missingCount = transferButton.getMissingCountHint();
			if (missingCount == -1) {
				return 0;
			}

			int ingredientCount = ingredientCount(inputSlotViews);
			if (ingredientCount == 0) {
				return 0;
			}

			int matchCount = ingredientCount - missingCount;
			int matchPercent = 100 * matchCount / ingredientCount;
			return -matchPercent;
		});
	}

	private static int ingredientCount(List<IRecipeSlotView> inputSlotViews) {
		int count = 0;
		for (IRecipeSlotView i : inputSlotViews) {
			if (!i.isEmpty()) {
				count++;
			}
		}
		return count;
	}
}
