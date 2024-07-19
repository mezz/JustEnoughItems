package mezz.jei.gui.recipes.layouts;

import mezz.jei.common.config.RecipeSorterStage;
import mezz.jei.gui.recipes.IRecipeLayoutWithButtons;
import mezz.jei.gui.recipes.RecipeSortUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

public class LazySortedRecipeLayoutList implements IRecipeLayoutList {
	private final @Nullable Player player;
	private final List<IRecipeLayoutWithButtons<?>> results;
	private final List<IRecipeLayoutWithButtons<?>> craftMissing;
	private final Iterator<? extends IRecipeLayoutWithButtons<?>> unsortedIterator;
	private final int size;

	private final boolean matchingCraftable;

	LazySortedRecipeLayoutList(
		Set<RecipeSorterStage> recipeSorterStages,
		@Nullable Player player,
		List<? extends IRecipeLayoutWithButtons<?>> unsortedList
	) {
		boolean matchingBookmarks = recipeSorterStages.contains(RecipeSorterStage.BOOKMARKED);
		this.matchingCraftable = recipeSorterStages.contains(RecipeSorterStage.CRAFTABLE);
		this.player = player;
		this.results = new ArrayList<>();
		this.craftMissing = new ArrayList<>();
		this.size = unsortedList.size();

		if (matchingBookmarks) {
			// if bookmarks go first, start by grabbing all the bookmarked elements, it's relatively cheap
			unsortedList = new ArrayList<>(unsortedList);
			Iterator<? extends IRecipeLayoutWithButtons<?>> iterator = unsortedList.iterator();
			while (iterator.hasNext()) {
				IRecipeLayoutWithButtons<?> layoutWithButtons = iterator.next();
				if (layoutWithButtons.isBookmarked()) {
					this.results.add(layoutWithButtons);
					iterator.remove();
				}
			}
		}
		if (!matchingCraftable) {
			this.results.addAll(unsortedList);
			this.unsortedIterator = new EmptyIterator();
		} else {
			this.unsortedIterator = unsortedList.iterator();
		}
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public List<IRecipeLayoutWithButtons<?>> subList(int from, int to, @Nullable AbstractContainerMenu container) {
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
	public Optional<IRecipeLayoutWithButtons<?>> findFirst(@Nullable AbstractContainerMenu container) {
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
				IRecipeLayoutWithButtons<?> next = unsortedIterator.next();
				next.updateTransferButton(container, player);
				int missingCountHint = next.getMissingCountHint();
				if (missingCountHint == 0) {
					results.add(next);
					return true;
				}
				craftMissing.add(next);
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

	private static class EmptyIterator implements Iterator<IRecipeLayoutWithButtons<?>> {
		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public IRecipeLayoutWithButtons<?> next() {
			throw new NoSuchElementException();
		}
	}
}
