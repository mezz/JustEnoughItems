package mezz.jei.library.plugins.vanilla.ingredients;

import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IJeiClientConfigs;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.RegistryUtil;
import mezz.jei.common.util.StackHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class ItemStackListFactory {
	private static final Logger LOGGER = LogManager.getLogger();

	public static List<ItemStack> create(StackHelper stackHelper) {
		IJeiClientConfigs jeiClientConfigs = Internal.getJeiClientConfigs();
		IClientConfig clientConfig = jeiClientConfigs.getClientConfig();
		final boolean showHidden = clientConfig.isShowHiddenItemsEnabled();

		final List<ItemStack> itemList = new ArrayList<>();
		final Set<Object> itemUidSet = new HashSet<>();

		Minecraft minecraft = Minecraft.getInstance();
		FeatureFlagSet features = Optional.ofNullable(minecraft.player)
			.map(p -> p.connection)
			.map(ClientPacketListener::enabledFeatures)
			.orElse(FeatureFlagSet.of());

		final boolean hasOperatorItemsTabPermissions =
			showHidden ||
			minecraft.options.operatorItemsTab().get() ||
			Optional.of(minecraft)
			.map(m -> m.player)
			.map(Player::canUseGameMasterBlocks)
			.orElse(false);

		ClientLevel level = minecraft.level;
		if (level == null) {
			throw new NullPointerException("minecraft.level must be set before JEI fetches ingredients");
		}
		RegistryAccess registryAccess = level.registryAccess();
		final CreativeModeTab.ItemDisplayParameters displayParameters =
			new CreativeModeTab.ItemDisplayParameters(features, hasOperatorItemsTabPermissions, registryAccess);

		for (CreativeModeTab tab : CreativeModeTabs.allTabs()) {
			if (tab.getType() != CreativeModeTab.Type.CATEGORY) {
				LOGGER.debug(
					"Skipping creative tab: '{}' because it is type: {}",
					tab.getDisplayName().getString(),
					tab.getType()
				);
				continue;
			}
			try {
				tab.buildContents(displayParameters);
			} catch (RuntimeException | LinkageError e) {
				LOGGER.error(
					"Item Group crashed while building contents." +
					"Items from this group will be missing from the JEI ingredient list: {}",
					tab.getDisplayName().getString(),
					e
				);
				continue;
			}

			@Unmodifiable Collection<ItemStack> displayItems;
			@Unmodifiable Collection<ItemStack> searchTabDisplayItems;
			try {
				displayItems = tab.getDisplayItems();
				searchTabDisplayItems = tab.getSearchTabDisplayItems();
			} catch (RuntimeException | LinkageError e) {
				LOGGER.error(
					"Item Group crashed while getting search tab display items." +
					"Some items from this group will be missing from the JEI ingredient list: {}",
					tab.getDisplayName().getString(),
					e
				);
				continue;
			}

			if (displayItems.isEmpty() && searchTabDisplayItems.isEmpty()) {
				LOGGER.warn(
					"Item Group has no display items and no search tab display items. " +
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
				itemList,
				itemUidSet
			);
			if (!displayItems.equals(searchTabDisplayItems)) {
				addFromTab(
					searchTabDisplayItems,
					"searchTabDisplayItems",
					tab,
					stackHelper,
					itemList,
					itemUidSet
				);
			}
		}

		if (showHidden) {
			addItemsFromRegistries(stackHelper, itemList, itemUidSet, features);
		}

		return itemList;
	}

	private static void addFromTab(
		Collection<ItemStack> tabDisplayItems,
		String displayType,
		CreativeModeTab tab,
		StackHelper stackHelper,
		List<ItemStack> itemList,
		Set<Object> itemUidSet
	) {
		Set<Object> tabUidSet = new HashSet<>();
		int added = 0;
		Set<Object> duplicateInTab = new HashSet<>();
		int duplicateInTabCount = 0;
		for (ItemStack itemStack : tabDisplayItems) {
			if (itemStack.isEmpty()) {
				LOGGER.error("Found an empty itemStack in '{}' creative tab's {}", tab, displayType);
			} else {
				Object itemKey = safeGetUid(stackHelper, itemStack);
				if (itemKey != null) {
					if (tabUidSet.contains(itemKey)) {
						duplicateInTab.add(itemKey);
						duplicateInTabCount++;
					}
					if (itemUidSet.add(itemKey)) {
						tabUidSet.add(itemKey);
						itemList.add(itemStack);
						added++;
					}
				}
			}
		}
		if (LOGGER.isDebugEnabled()) {
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
		FeatureFlagSet features
	) {
		{
			List<ItemStack> itemStacks = RegistryUtil.getRegistry(Registries.ITEM)
				.asLookup()
				.filterFeatures(features)
				.listElements()
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

			LOGGER.debug(
				"Added {}/{} new items from the item registry (this is run because ShowHiddenItems is set to true in JEI's config)",
				added,
				itemStacks.size()
			);
		}

		{
			List<ItemStack> itemStacks = RegistryUtil.getRegistry(Registries.BLOCK)
				.asLookup()
				.filterFeatures(features)
				.listElements()
				.map(Holder.Reference::value)
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

			LOGGER.debug(
				"Added {}/{} new items from the block registry (this is run because ShowHiddenItems is set to true in JEI's config)",
				added,
				itemStacks.size()
			);
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
