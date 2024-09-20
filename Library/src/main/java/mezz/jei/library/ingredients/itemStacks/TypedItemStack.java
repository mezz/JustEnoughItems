package mezz.jei.library.ingredients.itemStacks;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.world.item.ItemStack;

import java.time.Duration;
import java.util.Optional;

public abstract class TypedItemStack implements ITypedIngredient<ItemStack> {
	private static final LoadingCache<TypedItemStack, ItemStack> CACHE = CacheBuilder.newBuilder()
		.expireAfterAccess(Duration.ofSeconds(1))
		.build(new CacheLoader<>() {
			@Override
			public ItemStack load(TypedItemStack key) {
				return key.createItemStackUncached();
			}
		});

	public static ITypedIngredient<ItemStack> create(ItemStack ingredient) {
		if (ingredient.getCount() == 1) {
			return NormalizedTypedItemStack.create(
				ingredient.getItemHolder(),
				ingredient.getTag()
			);
		}
		return new FullTypedItemStack(
			ingredient.getItemHolder(),
			ingredient.getTag(),
			ingredient.getCount()
		);
	}

	public static ITypedIngredient<ItemStack> normalize(ITypedIngredient<ItemStack> typedIngredient) {
		if (typedIngredient instanceof TypedItemStack typedItemStack) {
			return typedItemStack.getNormalized();
		}
		ItemStack itemStack = typedIngredient.getIngredient();
		return NormalizedTypedItemStack.create(itemStack.getItemHolder(), itemStack.getTag());
	}

	@Override
	public final ItemStack getIngredient() {
		return CACHE.getUnchecked(this);
	}

	@Override
	public final Optional<ItemStack> getItemStack() {
		return Optional.of(getIngredient());
	}

	@Override
	public final IIngredientType<ItemStack> getType() {
		return VanillaTypes.ITEM_STACK;
	}

	protected abstract TypedItemStack getNormalized();

	protected abstract ItemStack createItemStackUncached();
}
