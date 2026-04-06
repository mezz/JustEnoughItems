package mezz.jei.library.ingredients.itemStacks;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;

import java.time.Duration;
import java.util.Optional;

public abstract class TypedItemStack implements ITypedIngredient<ItemStack> {
	private static final LoadingCache<TypedItemStack, ItemStack> CACHE = CacheBuilder.newBuilder()
		.expireAfterAccess(Duration.ofSeconds(1))
		.concurrencyLevel(1)
		.build(new CacheLoader<>() {
			@Override
			public ItemStack load(TypedItemStack key) {
				return key.createItemStackUncached();
			}
		});

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

	public static ITypedIngredient<ItemStack> normalize(ITypedIngredient<ItemStack> typedIngredient) {
		if (typedIngredient instanceof TypedItemStack typedItemStack) {
			return typedItemStack.getNormalized();
		}
		ItemStack itemStack = typedIngredient.getIngredient();
		return NormalizedTypedItemStack.create(itemStack.typeHolder(), itemStack.getComponentsPatch());
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

	protected abstract TypedItemStack getNormalized();

	protected abstract ItemStack createItemStackUncached();
}
