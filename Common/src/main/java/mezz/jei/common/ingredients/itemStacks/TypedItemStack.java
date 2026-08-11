package mezz.jei.common.ingredients.itemStacks;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.time.Duration;
import java.util.Optional;

public abstract sealed class TypedItemStack implements ITypedIngredient<ItemStack>
	permits FullTypedItemStack, NormalizedTypedItem, NormalizedTypedItemStack {
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
				ingredient.getItemHolder(),
				ingredient.getComponentsPatch()
			);
		}
		return new FullTypedItemStack(
			ingredient.getItemHolder(),
			ingredient.getComponentsPatch(),
			ingredient.getCount()
		);
	}

	public static ITypedIngredient<ItemStack> create(ItemLike itemLike) {
		Item item = itemLike.asItem();
		@SuppressWarnings("deprecation")
		Holder.Reference<Item> itemHolder = item.builtInRegistryHolder();
		return new NormalizedTypedItem(itemHolder);
	}

	@Override
	public final ItemStack getIngredient() {
		return CACHE.getUnchecked(this);
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
}
