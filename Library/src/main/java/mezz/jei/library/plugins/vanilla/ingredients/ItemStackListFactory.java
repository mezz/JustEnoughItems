package mezz.jei.library.plugins.vanilla.ingredients;

import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.Internal;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IJeiClientConfigs;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.StackHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ItemStackListFactory {
	private static final Logger LOGGER = LogManager.getLogger();

	public static List<ItemStack> create(StackHelper stackHelper) {
		IJeiClientConfigs jeiClientConfigs = Internal.getJeiClientConfigs();
		IClientConfig clientConfig = jeiClientConfigs.getClientConfig();
		final boolean showHidden = clientConfig.isShowHiddenItemsEnabled();
		final boolean debug = DebugConfig.isDebugIngredientsEnabled();

		final List<ItemStack> itemList = new ArrayList<>();
		final Set<Object> itemUidSet = new HashSet<>();

		for (CreativeModeTab itemGroup : CreativeModeTab.TABS) {
			if (itemGroup == CreativeModeTab.TAB_HOTBAR || itemGroup == CreativeModeTab.TAB_INVENTORY) {
				if (debug) {
					LOGGER.debug(
						"Skipping creative tab: '{}'",
						itemGroup.getDisplayName().getString()
					);
				}
				continue;
			}
			NonNullList<ItemStack> creativeTabItemStacks = NonNullList.create();
			try {
				itemGroup.fillItemList(creativeTabItemStacks);
			} catch (RuntimeException | LinkageError e) {
				LOGGER.error(
					"Item Group crashed while getting items." +
					"Items from this group will be missing from the JEI ingredient list: {}",
					itemGroup.getDisplayName().getString(),
					e
				);
				continue;
			}

			int added = 0;
			Set<Object> tabUidSet = new HashSet<>();
			Set<Object> duplicateInTab = new HashSet<>();
			int duplicateInTabCount = 0;
			for (ItemStack itemStack : creativeTabItemStacks) {
				if (itemStack.isEmpty()) {
					LOGGER.error("Found an empty itemStack from creative tab: {}", itemGroup);
				} else {
					Object itemKey = getItemKey(stackHelper, itemStack);
					if (itemKey != null) {
						if (!tabUidSet.add(itemKey)) {
							duplicateInTab.add(itemKey);
							duplicateInTabCount++;
						}
						if (itemUidSet.add(itemKey)) {
							itemList.add(itemStack);
							added++;
						}
					}
				}
			}
			if (debug) {
				LOGGER.debug(
					"Added {}/{} new items from creative tab: {}",
					added,
					creativeTabItemStacks.size(),
					itemGroup.getDisplayName().getString()
				);
			}
			if (duplicateInTabCount > 0) {
				LOGGER.warn(
					"""
						{} duplicate items were found in creative tab: {}
						This may indicate that these types of item need a subtype interpreter added to JEI:
						{}""",
					duplicateInTabCount,
					itemGroup.getDisplayName().getString(),
					duplicateInTab.stream().map(Object::toString).collect(Collectors.joining(", ", "[", "]"))
				);
			}
		}

		if (showHidden) {
			addItemsFromRegistries(stackHelper, itemList, itemUidSet, debug);
		}

		return itemList;
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
				Object itemKey = getItemKey(stackHelper, itemStack);
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
				Object itemKey = getItemKey(stackHelper, itemStack);
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
	private static Object getItemKey(StackHelper stackHelper, ItemStack stack) {
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
