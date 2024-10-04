package mezz.jei.gui.recipes.layouts;

import mezz.jei.common.config.RecipeSorterStage;
import mezz.jei.gui.recipes.RecipeLayoutWithButtons;
import mezz.jei.gui.recipes.RecipeSortUtil;
import mezz.jei.gui.recipes.RecipeTransferButton;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LazySortedRecipeLayoutList implements IRecipeLayoutList {
	private final List<RecipeLayoutWithButtons<?>> results;
	private final List<RecipeLayoutWithButtons<?>> craftMissing;
	private final Iterator<? extends RecipeLayoutWithButtons<?>> unsortedIterator;
	private final int size;

	private final boolean matchingCraftable;

	LazySortedRecipeLayoutList(
		Set<RecipeSorterStage> recipeSorterStages,
		List<? extends RecipeLayoutWithButtons<?>> unsortedList
	) {
		boolean matchingBookmarks = recipeSorterStages.contains(RecipeSorterStage.BOOKMARKED);
		this.matchingCraftable = recipeSorterStages.contains(RecipeSorterStage.CRAFTABLE);
		this.results = new ArrayList<>();
		this.craftMissing = new ArrayList<>();
		this.size = unsortedList.size();

		if (matchingBookmarks) {
			// if bookmarks go first, start by grabbing all the bookmarked elements, it's relatively cheap
			unsortedList = new ArrayList<>(unsortedList);
			Iterator<? extends RecipeLayoutWithButtons<?>> iterator = unsortedList.iterator();
			while (iterator.hasNext()) {
				RecipeLayoutWithButtons<?> layoutWithButtons = iterator.next();
				if (layoutWithButtons.bookmarkButton().isBookmarked()) {
					this.results.add(layoutWithButtons);
					iterator.remove();
				}
			}
		}
		if (!matchingCraftable) {
			this.results.addAll(unsortedList);
			this.unsortedIterator = Collections.emptyIterator();
		} else {
			this.unsortedIterator = unsortedList.iterator();
		}
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public List<RecipeLayoutWithButtons<?>> subList(int from, int to, @Nullable AbstractContainerMenu container) {
		ensureResults(to - 1, container);
		return results.subList(from, to);
	}

	private void ensureResults(int index, @Nullable AbstractContainerMenu container) {
		while (index >= results.size()) {
			if (!calculateNextResult(container)) {
				return;
			}
		}
	}

	@Override
	public Optional<RecipeLayoutWithButtons<?>> findFirst(@Nullable AbstractContainerMenu container) {
		ensureResults(0, container);
		if (results.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(results.get(0));
	}

	@Override
	public void tick(@Nullable AbstractContainerMenu container) {
		calculateNextResult(container);
	}

	private boolean calculateNextResult(@Nullable AbstractContainerMenu container) {
		if (matchingCraftable) {
			// if craftables go first, look for a 100% craftable element
			while (unsortedIterator.hasNext()) {
				RecipeLayoutWithButtons<?> next = unsortedIterator.next();
				RecipeTransferButton transferButton = next.transferButton();
				if (!transferButton.isInitialized()) {
					Player player = Minecraft.getInstance().player;
					transferButton.update(container, player);
				}
				int missingCountHint = transferButton.getMissingCountHint();
				if (missingCountHint == 0) {
					results.add(next);
					return true;
				} else {
					craftMissing.add(next);
				}
			}

			// from here we're finished with calculating all the transfer handlers,
			// just sort and add everything left to the results
			if (!craftMissing.isEmpty()) {
				craftMissing.sort(RecipeSortUtil.getCraftableComparator());
				results.addAll(craftMissing);
				craftMissing.clear();
				return true;
			}
		}

		return false;
	}
}
