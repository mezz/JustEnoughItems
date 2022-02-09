package mezz.jei.plugins.vanilla.ingredients.item;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.color.ColorGetter;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.StackHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;

public class ItemStackHelper implements IIngredientHelper<ItemStack> {
	private final StackHelper stackHelper;

	public ItemStackHelper(StackHelper stackHelper) {
		this.stackHelper = stackHelper;
	}

	@Override
	public IIngredientType<ItemStack> getIngredientType() {
		return VanillaTypes.ITEM;
	}

	@Nullable
	@Override
	public ItemStack getMatch(Iterable<ItemStack> ingredients, ItemStack toMatch, UidContext context) {
		for (ItemStack stack : ingredients) {
			if (stackHelper.isEquivalent(toMatch, stack, context)) {
				return stack;
			}
		}
		return null;
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
	public String getModId(ItemStack ingredient) {
		ErrorUtil.checkNotEmpty(ingredient);

		Item item = ingredient.getItem();
		ResourceLocation itemName = item.getRegistryName();
		if (itemName == null) {
			String stackInfo = getErrorInfo(ingredient);
			throw new IllegalStateException("item.getRegistryName() returned null for: " + stackInfo);
		}

		return itemName.getNamespace();
	}

	@Override
	public String getDisplayModId(ItemStack ingredient) {
		ErrorUtil.checkNotEmpty(ingredient);

		Item item = ingredient.getItem();
		String modId = item.getCreatorModId(ingredient);
		if (modId == null) {
			String stackInfo = getErrorInfo(ingredient);
			throw new IllegalStateException("item.getCreatorModId() returned null for: " + stackInfo);
		}
		return modId;
	}

	@Override
	public Iterable<Integer> getColors(ItemStack ingredient) {
		return ColorGetter.getColors(ingredient, 2);
	}

	@Override
	public String getResourceId(ItemStack ingredient) {
		ErrorUtil.checkNotEmpty(ingredient);

		Item item = ingredient.getItem();
		ResourceLocation itemName = item.getRegistryName();
		if (itemName == null) {
			String stackInfo = getErrorInfo(ingredient);
			throw new IllegalStateException("item.getRegistryName() returned null for: " + stackInfo);
		}

		return itemName.getPath();
	}

	@Override
	public ResourceLocation getResourceLocation(ItemStack ingredient) {
		ErrorUtil.checkNotEmpty(ingredient);

		Item item = ingredient.getItem();
		ResourceLocation name = item.getRegistryName();
		if (name == null) {
			String stackInfo = getErrorInfo(ingredient);
			throw new IllegalStateException("item.getRegistryName() returned null for: " + stackInfo);
		}

		return name;
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
		return ForgeRegistries.ITEMS.containsValue(item);
	}

	@Override
	public Collection<ResourceLocation> getTags(ItemStack ingredient) {
		return ingredient.getItem().getTags();
	}

	@Override
	public Collection<String> getCreativeTabNames(ItemStack ingredient) {
		Collection<String> creativeTabsStrings = new ArrayList<>();
		Item item = ingredient.getItem();
		for (CreativeModeTab itemGroup : item.getCreativeTabs()) {
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
}
