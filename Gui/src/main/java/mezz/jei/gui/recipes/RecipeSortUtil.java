package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.common.config.RecipeSorterStage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class RecipeSortUtil {
	private static final Comparator<?> EQUAL_COMPARATOR = (a, b) -> 0;
	private static final Comparator<RecipeLayoutWithButtons<?>> BOOKMARK_COMPARATOR = createBookmarkComparator();

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

	public static Comparator<RecipeLayoutWithButtons<?>> createRecipeComparator(
		Set<RecipeSorterStage> recipeSorterStages,
		@Nullable AbstractContainerMenu container,
		@Nullable Player player
	) {
		Comparator<RecipeLayoutWithButtons<?>> comparator = getEqualComparator();

		if (recipeSorterStages.contains(RecipeSorterStage.BOOKMARKED)) {
			comparator = chainComparators(comparator, BOOKMARK_COMPARATOR);
		}

		if (recipeSorterStages.contains(RecipeSorterStage.CRAFTABLE)) {
			Comparator<RecipeLayoutWithButtons<?>> ingredientMatchCountComparator = createIngredientMatchCountComparator(container, player);
			comparator = chainComparators(comparator, ingredientMatchCountComparator);
		}

		return comparator;
	}

	private static <T> Comparator<T> chainComparators(Comparator<T> first, Comparator<T> second) {
		if (first == EQUAL_COMPARATOR) {
			return second;
		}
		return first.thenComparing(second);
	}

	@SuppressWarnings("unchecked")
	private static <T> Comparator<T> getEqualComparator() {
		return (Comparator<T>) EQUAL_COMPARATOR;
	}

	private static Comparator<RecipeLayoutWithButtons<?>> createBookmarkComparator() {
		return Comparator.comparing(r -> {
			RecipeBookmarkButton bookmarkButton = r.getBookmarkButton();
			return !bookmarkButton.isBookmarked();
		});
	}

	private static Comparator<RecipeLayoutWithButtons<?>> createIngredientMatchCountComparator(
		@Nullable AbstractContainerMenu container,
		@Nullable Player player
	) {
		return Comparator.comparingInt(r -> {
			IRecipeLayoutDrawable<?> recipeLayout = r.getRecipeLayout();
			List<IRecipeSlotView> inputSlotViews = recipeLayout.getRecipeSlotsView()
				.getSlotViews(RecipeIngredientRole.INPUT);
			RecipeTransferButton transferButton = r.getTransferButton();

			transferButton.update(container, player);

			int ingredientCount = ingredientCount(inputSlotViews);
			if (ingredientCount == 0) {
				return 0;
			}

			int matchCount = transferButton.getRecipeTransferError()
				.map(recipeTransferError -> {
					int missingCountHint = recipeTransferError.getMissingCountHint();
					if (missingCountHint < 0) {
						return 0;
					}
					return ingredientCount - missingCountHint;
				})
				.orElse(ingredientCount);

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
