package mezz.jei.library.plugins.vanilla.ingredients;

import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.Internal;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IJeiClientConfigs;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.StackHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ItemStackListFactory {
	private static final Logger LOGGER = LogManager.getLogger();

	public static List<ItemStack> create(StackHelper stackHelper, ItemStackHelper itemStackHelper) {
		IJeiClientConfigs jeiClientConfigs = Internal.getJeiClientConfigs();
		IClientConfig clientConfig = jeiClientConfigs.getClientConfig();
		final boolean showHidden = clientConfig.isShowHiddenItemsEnabled();
		final boolean debug = DebugConfig.isDebugIngredientsEnabled();

		final List<ItemStack> itemList = new ArrayList<>();
		final Set<Object> itemUidSet = new HashSet<>();

		for (CreativeModeTab tab : CreativeModeTab.TABS) {
			if (tab == CreativeModeTab.TAB_HOTBAR || tab == CreativeModeTab.TAB_INVENTORY) {
				if (debug) {
					LOGGER.debug(
						"Skipping creative tab: '{}'",
						tab.getDisplayName().getString()
					);
				}
				continue;
			}

			NonNullList<ItemStack> displayItems = NonNullList.create();
			try {
				tab.fillItemList(displayItems);
			} catch (RuntimeException | LinkageError e) {
				LOGGER.error(
					"Item Group crashed while getting items." +
					"Items from this group will be missing from the JEI ingredient list: {}",
					tab.getDisplayName().getString(),
					e
				);
				continue;
			}

			if (displayItems.isEmpty()) {
				LOGGER.warn(
					"Item Group has no display items. " +
					"Items from this group will be missing from the JEI ingredient list. {}",
					tab.getDisplayName().getString()
				);
				continue;
			}

			addFromTab(
				displayItems,
				"displayItems",
				tab,
				stackHelper,
				itemStackHelper,
				itemList,
				itemUidSet,
				debug
			);
		}

		if (showHidden) {
			addItemsFromRegistries(stackHelper, itemList, itemUidSet, debug);
		}

		return itemList;
	}

	private static void addFromTab(
		Collection<ItemStack> tabDisplayItems,
		String displayType,
		CreativeModeTab tab,
		StackHelper stackHelper,
		ItemStackHelper itemStackHelper,
		List<ItemStack> itemList,
		Set<Object> itemUidSet,
		boolean debug
	) {
		Set<Object> tabUidSet = new HashSet<>();
		int added = 0;
		Set<Object> duplicateInTab = new HashSet<>();
		int duplicateInTabCount = 0;
		for (ItemStack itemStack : tabDisplayItems) {
			if (itemStack.isEmpty()) {
				String errorInfo = itemStackHelper.getErrorInfo(itemStack);
				LOGGER.error("Found an empty itemStack in '{}' creative tab's {}: {}", tab, displayType, errorInfo);
				continue;
			}
			if (!itemStackHelper.isValidIngredient(itemStack)) {
				String errorInfo = itemStackHelper.getErrorInfo(itemStack);
				LOGGER.error("Ignoring ingredient in '{}' creative tab's {} that is considered invalid: {}", tab, displayType, errorInfo);
				continue;
			}
			if (!itemStackHelper.isIngredientOnServer(itemStack)) {
				String errorInfo = itemStackHelper.getErrorInfo(itemStack);
				LOGGER.warn("Ignoring ingredient in '{}' creative tab's {} that isn't on the server: {}", tab, displayType, errorInfo);
				continue;
			}
			Object itemKey = safeGetUid(stackHelper, itemStack);
			if (itemKey == null) {
				continue;
			}

			if (!tabUidSet.add(itemKey)) {
				duplicateInTab.add(itemKey);
				duplicateInTabCount++;
			}
			if (itemUidSet.add(itemKey)) {
				itemList.add(itemStack);
				added++;
			}
		}
		if (debug) {
			LOGGER.debug(
				"Added {}/{} new items from '{}' creative tab's {}",
				StringUtils.leftPad(Integer.toString(added), 4, ' '),
				StringUtils.leftPad(Integer.toString(tabDisplayItems.size()), 4, ' '),
				tab.getDisplayName().getString(),
				displayType
			);
		}
		if (duplicateInTabCount > 0) {
			Level level = Services.PLATFORM.getModHelper().isInDev() ? Level.WARN : Level.DEBUG;
			LOGGER.log(level,
				"""
					{} duplicate items were found in '{}' creative tab's: {}
					This may indicate that these types of item need a subtype interpreter added to JEI:
					{}""",
				duplicateInTabCount,
				tab.getDisplayName().getString(),
				displayType,
				duplicateInTab.stream().map(Object::toString).collect(Collectors.joining(", ", "[", "]"))
			);
		}
	}

	private static void addItemsFromRegistries(
		StackHelper stackHelper,
		List<ItemStack> itemList,
		Set<Object> itemUidSet,
		boolean debug
	) {
		{
			List<ItemStack> itemStacks = Registry.ITEM.stream()
				.map(ItemStack::new)
				.filter(i -> !i.isEmpty())
				.toList();

			int added = 0;
			for (ItemStack itemStack : itemStacks) {
				Object itemKey = safeGetUid(stackHelper, itemStack);
				if (itemKey != null && itemUidSet.add(itemKey)) {
					itemList.add(itemStack);
					added++;
				}
			}

			if (debug) {
				LOGGER.debug(
					"Added {}/{} new items from the item registry (this is run because ShowHiddenItems is set to true in JEI's config)",
					added,
					itemStacks.size()
				);
			}
		}

		{
			List<ItemStack> itemStacks = Registry.BLOCK.stream()
				.map(ItemStack::new)
				.filter(i -> !i.isEmpty())
				.toList();

			int added = 0;
			for (ItemStack itemStack : itemStacks) {
				Object itemKey = safeGetUid(stackHelper, itemStack);
				if (itemKey != null && itemUidSet.add(itemKey)) {
					itemList.add(itemStack);
					added++;
				}
			}

			if (debug) {
				LOGGER.debug(
					"Added {}/{} new items from the block registry (this is run because ShowHiddenItems is set to true in JEI's config)",
					added,
					itemStacks.size()
				);
			}
		}
	}

	@Nullable
	private static Object safeGetUid(StackHelper stackHelper, ItemStack stack) {
		if (stackHelper.hasSubtypes(stack)) {
			try {
				return stackHelper.getUniqueIdentifierForStack(stack, UidContext.Ingredient);
			} catch (RuntimeException | LinkageError e) {
				String stackInfo = ErrorUtil.getItemStackInfo(stack);
				LOGGER.error("Couldn't get unique name for itemStack {}", stackInfo, e);
				return null;
			}
		}
		return stack.getItem();
	}
}
