package mezz.jei.library.plugins.vanilla.ingredients;

import com.google.common.collect.Streams;
import mezz.jei.api.constants.Tags;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IJeiClientConfigs;
import mezz.jei.common.platform.IPlatformItemStackHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.RegistryUtil;
import mezz.jei.common.util.StackHelper;
import mezz.jei.common.util.TagUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class ItemStackHelper implements IIngredientHelper<ItemStack> {
	private final StackHelper stackHelper;
	private final IColorHelper colorHelper;
	private final TagKey<Item> itemHiddenFromRecipeViewers;
	private final TagKey<Block> blockHiddenFromRecipeViewers;

	public ItemStackHelper(StackHelper stackHelper, IColorHelper colorHelper) {
		this.stackHelper = stackHelper;
		this.colorHelper = colorHelper;
		//noinspection deprecation
		this.itemHiddenFromRecipeViewers = new TagKey<>(Registries.ITEM, Tags.HIDDEN_FROM_RECIPE_VIEWERS);
		//noinspection deprecation
		this.blockHiddenFromRecipeViewers = new TagKey<>(Registries.BLOCK, Tags.HIDDEN_FROM_RECIPE_VIEWERS);
	}

	@Override
	public IIngredientType<ItemStack> getIngredientType() {
		return VanillaTypes.ITEM_STACK;
	}

	@Override
	public String getDisplayName(ItemStack ingredient) {
		Component displayNameTextComponent = ingredient.getHoverName();
		String displayName = displayNameTextComponent.getString();
		ErrorUtil.checkNotNull(displayName, "itemStack.getDisplayName()");
		return displayName;
	}

	@SuppressWarnings("removal")
	@Override
	public String getUniqueId(ItemStack ingredient, UidContext context) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		return stackHelper.getUniqueIdentifierForStack(ingredient, context);
	}

	@Override
	public Object getUid(ItemStack ingredient, UidContext context) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		ErrorUtil.checkNotNull(context, "type");
		return stackHelper.getUidForStack(ingredient, context);
	}

	@Override
	public Object getUid(ITypedIngredient<ItemStack> typedIngredient, UidContext context) {
		ErrorUtil.checkNotNull(typedIngredient, "typedIngredient");
		ErrorUtil.checkNotNull(context, "type");
		return stackHelper.getUidForStack(typedIngredient, context);
	}

	@Override
	public Object getGroupingUid(ItemStack ingredient) {
		return ingredient.getItem();
	}

	@Override
	public boolean hasSubtypes(ItemStack ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		return stackHelper.hasSubtypes(ingredient);
	}

	@SuppressWarnings("removal")
	@Override
	public String getWildcardId(ItemStack ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		return StackHelper.getRegistryNameForStack(ingredient);
	}

	@Override
	public String getDisplayModId(ItemStack ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		IPlatformItemStackHelper itemStackHelper = Services.PLATFORM.getItemStackHelper();
		return itemStackHelper.getCreatorModId(ingredient)
			.or(() -> getNamespace(ingredient))
			.orElseThrow(() -> {
				String stackInfo = getErrorInfo(ingredient);
				return new IllegalStateException("null registryName for: " + stackInfo);
			});
	}

	private static Optional<String> getNamespace(ItemStack ingredient) {
		ResourceLocation key = RegistryUtil
			.getRegistry(Registries.ITEM)
			.getKey(ingredient.getItem());
		return Optional.ofNullable(key)
			.map(ResourceLocation::getNamespace);
	}

	@Override
	public long getAmount(ItemStack ingredient) {
		return ingredient.getCount();
	}

	@Override
	public ItemStack copyWithAmount(ItemStack ingredient, long amount) {
		ItemStack copy = ingredient.copy();
		int intAmount = Math.toIntExact(amount);
		copy.setCount(intAmount);
		return copy;
	}

	@Override
	public Iterable<Integer> getColors(ItemStack ingredient) {
		return colorHelper.getColors(ingredient, 2);
	}

	@Override
	public ResourceLocation getResourceLocation(ItemStack ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		Item item = ingredient.getItem();
		ResourceLocation key = RegistryUtil
			.getRegistry(Registries.ITEM)
			.getKey(item);

		if (key == null) {
			String stackInfo = getErrorInfo(ingredient);
			throw new IllegalStateException("item has no key in the Item registry: " + stackInfo);
		}
		return key;
	}

	@Override
	public ItemStack getCheatItemStack(ItemStack ingredient) {
		return ingredient;
	}

	@Override
	public ItemStack copyIngredient(ItemStack ingredient) {
		return ingredient.copy();
	}

	@Override
	public ItemStack normalizeIngredient(ItemStack ingredient) {
		if (ingredient.getCount() == 1) {
			return ingredient;
		}
		// Temporarily setting the count on the original stack this way can "recover" some empty ItemStacks.
		// Copying it first results in the copy being a hard-coded ItemStack#EMPTY that cannot be recovered.
		int originalCount = ingredient.getCount();
		ingredient.setCount(1);
		ItemStack copy = ingredient.copy();
		ingredient.setCount(originalCount);
		return copy;
	}

	@Override
	public boolean isValidIngredient(ItemStack ingredient) {
		return !ingredient.isEmpty();
	}

	@Override
	public boolean isIngredientOnServer(ItemStack ingredient) {
		Item item = ingredient.getItem();
		Registry<Item> registry = RegistryUtil.getRegistry(Registries.ITEM);
		return registry.getKey(item) != null;
	}

	@Override
	public Stream<ResourceLocation> getTagStream(ItemStack ingredient) {
		Stream<ResourceLocation> itemTagStream = ingredient.getTags()
			.map(TagKey::location);

		if (ingredient.getItem() instanceof BlockItem blockItem) {
			IJeiClientConfigs jeiClientConfigs = Internal.getJeiClientConfigs();
			IClientConfig clientConfig = jeiClientConfigs.getClientConfig();
			if (clientConfig.isLookupBlockTagsEnabled()) {
				Stream<ResourceLocation> blockTagStream = blockItem.getBlock()
					.defaultBlockState()
					.getTags()
					.map(TagKey::location);
				return Streams.concat(itemTagStream, blockTagStream);
			}
		}
		return itemTagStream;
	}

	@Override
	public boolean isHiddenFromRecipeViewersByTags(ItemStack ingredient) {
		if (ingredient.is(itemHiddenFromRecipeViewers)) {
			return true;
		}
		if (ingredient.getItem() instanceof BlockItem blockItem) {
			IJeiClientConfigs jeiClientConfigs = Internal.getJeiClientConfigs();
			IClientConfig clientConfig = jeiClientConfigs.getClientConfig();
			if (clientConfig.isLookupBlockTagsEnabled()) {
				Block block = blockItem.getBlock();
				@SuppressWarnings("deprecation")
				Holder.Reference<Block> holder = block.builtInRegistryHolder();
				return holder.is(blockHiddenFromRecipeViewers);
			}
		}
		return false;
	}

	@Override
	public String getErrorInfo(@Nullable ItemStack ingredient) {
		return ErrorUtil.getItemStackInfo(ingredient);
	}

	@Override
	public Optional<TagKey<?>> getTagKeyEquivalent(Collection<ItemStack> ingredients) {
		Registry<Item> itemRegistry = RegistryUtil.getRegistry(Registries.ITEM);
		return TagUtil.getTagEquivalent(ingredients, ItemStack::getItem, itemRegistry::getTags);
	}
}
