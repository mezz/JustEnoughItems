package mezz.jei.library.plugins.vanilla.ingredients;

import com.google.common.collect.Streams;
import mezz.jei.api.constants.Tags;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.platform.IPlatformItemStackHelper;
import mezz.jei.common.platform.IPlatformRegistry;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.StackHelper;
import mezz.jei.common.util.TagUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemStackHelper implements IIngredientHelper<ItemStack> {
	private final ISubtypeManager subtypeManager;
	private final StackHelper stackHelper;
	private final IColorHelper colorHelper;
	private final TagKey<Item> itemHiddenFromRecipeViewers;
	private final TagKey<Block> blockHiddenFromRecipeViewers;

	public ItemStackHelper(ISubtypeManager subtypeManager, StackHelper stackHelper, IColorHelper colorHelper) {
		this.subtypeManager = subtypeManager;
		this.stackHelper = stackHelper;
		this.colorHelper = colorHelper;
		this.itemHiddenFromRecipeViewers = TagKey.create(Registry.ITEM_REGISTRY, Tags.HIDDEN_FROM_RECIPE_VIEWERS);
		this.blockHiddenFromRecipeViewers = TagKey.create(Registry.BLOCK_REGISTRY, Tags.HIDDEN_FROM_RECIPE_VIEWERS);
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

	@Override
	public String getUniqueId(ItemStack ingredient, UidContext context) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		return stackHelper.getUniqueIdentifierForStack(ingredient, context);
	}

	@Override
	public Object getUid(ItemStack ingredient, UidContext context) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		ErrorUtil.checkNotNull(context, "context");
		return stackHelper.getUidForStack(ingredient, context);
	}

	@Override
	public boolean hasSubtypes(ItemStack ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		return subtypeManager.hasSubtypes(ingredient);
	}

	@Override
	public String getWildcardId(ItemStack ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		return StackHelper.getRegistryNameForStack(ingredient);
	}

	@Override
	public String getGroupingUid(ITypedIngredient<ItemStack> typedIngredient) {
		Item item = typedIngredient.getBaseIngredient(VanillaTypes.ITEM_STACK);
		return Services.PLATFORM
			.getRegistry(Registry.ITEM_REGISTRY)
			.getRegistryName(item)
			.map(ResourceLocation::toString)
			.orElseThrow(() -> new IllegalStateException("item has no key in the Item registry: " + item));
	}

	@Override
	public String getDisplayModId(ItemStack ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		IPlatformItemStackHelper itemStackHelper = Services.PLATFORM.getItemStackHelper();
		return itemStackHelper.getCreatorModId(ingredient)
			.or(() ->
				Services.PLATFORM
				.getRegistry(Registry.ITEM_REGISTRY)
				.getRegistryName(ingredient.getItem())
				.map(ResourceLocation::getNamespace)
			)
			.orElseThrow(() -> {
				String stackInfo = getErrorInfo(ingredient);
				return new IllegalStateException("null registryName for: " + stackInfo);
			});
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
		return Services.PLATFORM
			.getRegistry(Registry.ITEM_REGISTRY)
			.getRegistryName(item)
			.orElseThrow(() -> {
				String stackInfo = getErrorInfo(ingredient);
				return new IllegalStateException("item.getRegistryName() returned null for: " + stackInfo);
			});
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
		IPlatformRegistry<Item> registry = Services.PLATFORM.getRegistry(Registry.ITEM_REGISTRY);
		return registry.contains(item);
	}

	@Override
	@Deprecated
	public Collection<ResourceLocation> getTags(ItemStack ingredient) {
		return getTagStream(ingredient)
			.collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public Stream<ResourceLocation> getTagStream(ItemStack ingredient) {
		Stream<ResourceLocation> itemTagStream = ingredient.getTags()
			.map(TagKey::location);

		if (ingredient.getItem() instanceof BlockItem blockItem) {
			Stream<ResourceLocation> blockTagStream = blockItem.getBlock()
				.defaultBlockState()
				.getTags()
				.map(TagKey::location);
			return Streams.concat(itemTagStream, blockTagStream);
		}
		return itemTagStream;
	}

	@Override
	public Collection<String> getCreativeTabNames(ItemStack ingredient) {
		Collection<String> creativeTabsStrings = new ArrayList<>();
		IPlatformItemStackHelper itemStackHelper = Services.PLATFORM.getItemStackHelper();
		for (CreativeModeTab itemGroup : itemStackHelper.getCreativeTabs(ingredient)) {
			if (itemGroup != null) {
				String creativeTabName = itemGroup.getDisplayName().getString();
				creativeTabsStrings.add(creativeTabName);
			}
		}
		return creativeTabsStrings;
	}

	@Override
	public boolean isHiddenFromRecipeViewersByTags(ItemStack ingredient) {
		return isHiddenFromRecipeViewersByTags(ingredient.getItemHolder());
	}

	@Override
	public boolean isHiddenFromRecipeViewersByTags(ITypedIngredient<ItemStack> ingredient) {
		Item item = ingredient.getBaseIngredient(VanillaTypes.ITEM_STACK);
		@SuppressWarnings("deprecation")
		Holder.Reference<Item> itemHolder = item.builtInRegistryHolder();
		return isHiddenFromRecipeViewersByTags(itemHolder);
	}

	private boolean isHiddenFromRecipeViewersByTags(Holder<Item> itemHolder) {
		if (itemHolder.is(itemHiddenFromRecipeViewers)) {
			return true;
		}
		if (itemHolder.value() instanceof BlockItem blockItem) {
			Block block = blockItem.getBlock();
			return block.builtInRegistryHolder().is(blockHiddenFromRecipeViewers);
		}
		return false;
	}

	@Override
	public String getErrorInfo(@Nullable ItemStack ingredient) {
		return ErrorUtil.getItemStackInfo(ingredient);
	}

	@Override
	public Optional<TagKey<?>> getTagKeyEquivalent(Collection<ItemStack> ingredients) {
		return TagUtil.getTagEquivalent(ingredients, ItemStack::getItem, Registry.ITEM::getTags);
	}
}
