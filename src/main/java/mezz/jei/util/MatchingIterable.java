package mezz.jei.util;

import org.jetbrains.annotations.Nullable;
import java.util.Iterator;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.world.item.ItemStack;

public class MatchingIterable implements Iterable<ItemStackMatchable<ItemStack>> {
	private final IRecipeSlotView recipeSlotView;

	public MatchingIterable(IRecipeSlotView recipeSlotView) {
		this.recipeSlotView = recipeSlotView;
	}

	@Override
	public Iterator<ItemStackMatchable<ItemStack>> iterator() {
		Iterator<ItemStack> stacks = recipeSlotView.getIngredients(VanillaTypes.ITEM).iterator();
		return new DelegateIterator<>(stacks) {
			@Override
			public ItemStackMatchable<ItemStack> next() {
				final ItemStack stack = delegate.next();
				return new ItemStackMatchable<>() {
					@Nullable
					@Override
					public ItemStack getStack() {
						return stack;
					}

					@Nullable
					@Override
					public ItemStack getResult() {
						return stack;
					}
				};
			}
		};
	}

	public static abstract class DelegateIterator<T, R> implements Iterator<R> {
		protected final Iterator<T> delegate;

		public DelegateIterator(Iterator<T> delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}

		@Override
		public void remove() {
			delegate.remove();
		}
	}
}
