package mezz.jei.util;

import javax.annotation.Nullable;
import java.util.Iterator;

import net.minecraft.item.ItemStack;

public class MatchingIterable implements Iterable<ItemStackMatchable<ItemStack>> {
	private final Iterable<ItemStack> list;

	public MatchingIterable(Iterable<ItemStack> list) {
		this.list = list;
	}

	@Override
	public Iterator<ItemStackMatchable<ItemStack>> iterator() {
		Iterator<ItemStack> stacks = list.iterator();
		return new DelegateIterator<ItemStack, ItemStackMatchable<ItemStack>>(stacks) {
			@Override
			public ItemStackMatchable<ItemStack> next() {
				final ItemStack stack = delegate.next();
				return new ItemStackMatchable<ItemStack>() {
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
