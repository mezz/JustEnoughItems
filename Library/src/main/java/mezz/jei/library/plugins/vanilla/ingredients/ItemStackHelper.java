package mezz.jei.library.plugins.vanilla.ingredients;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.platform.IPlatformItemStackHelper;
import mezz.jei.common.platform.IPlatformRegistry;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.StackHelper;
import mezz.jei.common.util.TagUtil;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemStackHelper implements IIngredientHelper<ItemStack> {
	private final StackHelper stackHelper;
	private final IColorHelper colorHelper;

	public ItemStackHelper(StackHelper stackHelper, IColorHelper colorHelper) {
		this.stackHelper = stackHelper;
		this.colorHelper = colorHelper;
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
		ErrorUtil.checkNotEmpty(ingredient);
		return stackHelper.getUniqueIdentifierForStack(ingredient, context);
	}

	@Override
	public String getWildcardId(ItemStack ingredient) {
		ErrorUtil.checkNotEmpty(ingredient);
		return StackHelper.getRegistryNameForStack(ingredient);
	}

	@Override
	public String getDisplayModId(ItemStack ingredient) {
		ErrorUtil.checkNotEmpty(ingredient);

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
				throw new IllegalStateException("null registryName for: " + stackInfo);
			});
	}

	@Override
	public Iterable<Integer> getColors(ItemStack ingredient) {
		return colorHelper.getColors(ingredient, 2);
	}

	@Override
	public ResourceLocation getResourceLocation(ItemStack ingredient) {
		ErrorUtil.checkNotEmpty(ingredient);

		Item item = ingredient.getItem();
		return Services.PLATFORM
			.getRegistry(Registry.ITEM_REGISTRY)
			.getRegistryName(item)
			.orElseThrow(() -> {
				String stackInfo = getErrorInfo(ingredient);
				throw new IllegalStateException("item.getRegistryName() returned null for: " + stackInfo);
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
		ItemStack copy = ingredient.copy();
		copy.setCount(1);
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
		return ingredient.getTags()
			.map(TagKey::location);
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
	public String getErrorInfo(@Nullable ItemStack ingredient) {
		return ErrorUtil.getItemStackInfo(ingredient);
	}

	@Override
	public Optional<ResourceLocation> getTagEquivalent(Collection<ItemStack> ingredients) {
		return TagUtil.getTagEquivalent(ingredients, ItemStack::getItem, Registry.ITEM::getTags);
	}

	@Override
	public CompoundTag serialize(ItemStack ingredient) {
		return ingredient.save(new CompoundTag());
	}

	@Override
	public Optional<ItemStack> deserialize(CompoundTag tag) {
		return Optional.of(ItemStack.of(tag));
	}

	@Override
	public Optional<ItemStack> merge(ItemStack ingredientA, ItemStack ingredientB) {
		if (ingredientA.isEmpty()) {
			return Optional.of(ingredientB.copy());
		}
		if (ingredientB.isEmpty()) {
			return Optional.of(ingredientA.copy());
		}
		if(ingredientA.sameItemStackIgnoreDurability(ingredientB) && ItemStack.tagMatches(ingredientA, ingredientB)) {
			ItemStack result = ingredientA.copy();
			result.grow(ingredientB.getCount());
			return Optional.of(result);
		}
		return Optional.empty();
	}
}
