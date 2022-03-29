package mezz.jei.common.util;

import org.jetbrains.annotations.Nullable;
import java.util.Iterator;
import java.util.List;

import net.minecraft.world.item.ItemStack;

public class MatchingIterable implements Iterable<ItemStackMatchable<ItemStack>> {
	private final List<ItemStack> ingredients;

	public MatchingIterable(List<ItemStack> ingredients) {
		this.ingredients = ingredients;
	}

	@Override
	public Iterator<ItemStackMatchable<ItemStack>> iterator() {
		Iterator<ItemStack> stacks = ingredients.iterator();
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
