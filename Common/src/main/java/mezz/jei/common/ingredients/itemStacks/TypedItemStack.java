package mezz.jei.common.ingredients.itemStacks;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheLoader;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.UncheckedExecutionException;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.Optional;

public abstract sealed class TypedItemStack implements ITypedIngredient<ItemStack>
	permits FullTypedItemStack, NormalizedTypedItem, NormalizedTypedItemStack {
	private static final long CACHE_EXPIRE_NANOS = Duration.ofSeconds(1).toNanos();
	private static final Object CACHE_LOCK = new Object();

	// The ItemStack of this ingredient, kept until one second after its last access.
	// Holds a CachedItemStack, or an ItemStackLoad while the ItemStack is being created.
	private volatile @Nullable Object cache;

	public static ITypedIngredient<ItemStack> create(ItemStack ingredient) {
		if (ingredient.getCount() == 1) {
			return NormalizedTypedItemStack.create(
				ingredient.typeHolder(),
				ingredient.getComponentsPatch()
			);
		}
		return new FullTypedItemStack(
			ingredient.typeHolder(),
			ingredient.getComponentsPatch(),
			ingredient.getCount()
		);
	}

	public static ITypedIngredient<ItemStack> create(ItemStackTemplate ingredient) {
		if (ingredient.count() == 1) {
			return NormalizedTypedItemStack.create(
				ingredient.typeHolder(),
				ingredient.components()
			);
		}
		return new FullTypedItemStack(
			ingredient.typeHolder(),
			ingredient.components(),
			ingredient.count()
		);
	}

	public static ITypedIngredient<ItemStack> create(ItemLike itemLike) {
		Item item = itemLike.asItem();
		@SuppressWarnings("deprecation")
		Holder.Reference<Item> itemHolder = item.builtInRegistryHolder();
		return new NormalizedTypedItem(itemHolder);
	}

	// Works like a Guava LoadingCache with expireAfterAccess(1 second) that is keyed by instance,
	// including the exceptions it throws when creating the ItemStack fails
	// and how concurrent callers share one load.
	// The ItemStack is kept on the ingredient itself, so creating one for a new ingredient
	// does not have to maintain a shared expiration queue.
	@Override
	public final ItemStack getIngredient() {
		long now = System.nanoTime();
		Object current = this.cache;
		if (current instanceof CachedItemStack cached && now - cached.accessTime < CACHE_EXPIRE_NANOS) {
			cached.accessTime = now;
			return cached.itemStack;
		}
		if (current instanceof ItemStackLoad activeLoad) {
			return waitForLoad(activeLoad);
		}
		ItemStackLoad load;
		boolean isNewLoad = false;
		synchronized (CACHE_LOCK) {
			current = this.cache;
			if (current instanceof ItemStackLoad activeLoad) {
				load = activeLoad;
			} else {
				if (current instanceof CachedItemStack cached) {
					now = System.nanoTime();
					if (now - cached.accessTime < CACHE_EXPIRE_NANOS) {
						cached.accessTime = now;
						return cached.itemStack;
					}
				}
				load = new ItemStackLoad();
				this.cache = load;
				isNewLoad = true;
			}
		}
		if (isNewLoad) {
			return createItemStack(load);
		}
		return waitForLoad(load);
	}

	@Override
	public abstract TypedItemStack normalize(IIngredientHelper<ItemStack> ingredientHelper);

	@Override
	public final Optional<ItemStack> getItemStack() {
		return Optional.of(getIngredient());
	}

	@Override
	public final <B> B getBaseIngredient(IIngredientTypeWithSubtypes<B, ItemStack> ingredientType) {
		Item item = getItem();
		Class<? extends B> ingredientBaseClass = ingredientType.getIngredientBaseClass();
		return ingredientBaseClass.cast(item);
	}

	@Override
	public final IIngredientType<ItemStack> getType() {
		return VanillaTypes.ITEM_STACK;
	}

	protected abstract Item getItem();

	protected abstract ItemStack createItemStackUncached();

	private ItemStack createItemStack(ItemStackLoad load) {
		ItemStack itemStack;
		try {
			itemStack = createItemStackUncached();
		} catch (Throwable t) {
			finishLoad(load, null, t);
			if (t instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw wrapLoadFailure(t);
		}
		//noinspection ConstantValue
		if (itemStack == null) {
			finishLoad(load, null, null);
			throw new CacheLoader.InvalidCacheLoadException("CacheLoader returned null for key " + this + ".");
		}
		CachedItemStack cached = new CachedItemStack(itemStack, System.nanoTime());
		finishLoad(load, cached, null);
		return itemStack;
	}

	private void finishLoad(ItemStackLoad load, @Nullable CachedItemStack cached, @Nullable Throwable failure) {
		synchronized (CACHE_LOCK) {
			if (this.cache == load) {
				this.cache = cached;
			}
		}
		load.finish(cached, failure);
	}

	private ItemStack waitForLoad(ItemStackLoad load) {
		Preconditions.checkState(load.thread != Thread.currentThread(), "Recursive load of: %s", this);
		load.await();
		Throwable failure = load.failure;
		if (failure != null) {
			throw wrapLoadFailure(failure);
		}
		CachedItemStack cached = load.cached;
		if (cached == null) {
			throw new CacheLoader.InvalidCacheLoadException("CacheLoader returned null for key " + this + ".");
		}
		cached.accessTime = System.nanoTime();
		return cached.itemStack;
	}

	private static RuntimeException wrapLoadFailure(Throwable failure) {
		if (failure instanceof Error error) {
			throw new ExecutionError(error);
		}
		return new UncheckedExecutionException(failure);
	}

	private static final class CachedItemStack {
		private final ItemStack itemStack;
		private volatile long accessTime;

		private CachedItemStack(ItemStack itemStack, long accessTime) {
			this.itemStack = itemStack;
			this.accessTime = accessTime;
		}
	}

	private static final class ItemStackLoad {
		private final Thread thread = Thread.currentThread();
		private boolean done;
		private @Nullable CachedItemStack cached;
		private @Nullable Throwable failure;

		private synchronized void finish(@Nullable CachedItemStack cached, @Nullable Throwable failure) {
			this.cached = cached;
			this.failure = failure;
			this.done = true;
			notifyAll();
		}

		// Waiting is not interruptible, the interrupt is restored afterwards.
		private synchronized void await() {
			boolean interrupted = false;
			while (!done) {
				try {
					wait();
				} catch (InterruptedException e) {
					interrupted = true;
				}
			}
			if (interrupted) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
