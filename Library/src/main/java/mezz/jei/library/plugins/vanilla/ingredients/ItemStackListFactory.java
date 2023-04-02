package mezz.jei.library.plugins.vanilla.ingredients;

import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.StackHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class ItemStackListFactory {
	private static final Logger LOGGER = LogManager.getLogger();

	public static List<ItemStack> create(StackHelper stackHelper) {
		final List<ItemStack> itemList = new ArrayList<>();
		final Set<String> itemNameSet = new HashSet<>();

		Minecraft minecraft = Minecraft.getInstance();
		FeatureFlagSet features = Optional.of(minecraft)
				.map(m -> m.player)
				.map(p -> p.connection)
				.map(ClientPacketListener::enabledFeatures)
				.orElse(FeatureFlagSet.of());

		final boolean hasPermissions =
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
			new CreativeModeTab.ItemDisplayParameters(features, hasPermissions, registryAccess);

		for (CreativeModeTab itemGroup : CreativeModeTabs.allTabs()) {
			if (itemGroup.getType() != CreativeModeTab.Type.CATEGORY) {
				continue;
			}
			try {
				itemGroup.buildContents(displayParameters);
			} catch (RuntimeException | LinkageError e) {
				LOGGER.error("Item Group crashed while building contents." +
						"Items from this group will be missing from the JEI ingredient list. {}", itemGroup, e);
				continue;
			}

			final @Unmodifiable Collection<ItemStack> creativeTabItemStacks;
			try {
				creativeTabItemStacks = itemGroup.getDisplayItems();
			} catch (RuntimeException | LinkageError e) {
				LOGGER.error("Item Group crashed while getting display items." +
					"Some items from this group will be missing from the JEI ingredient list. {}", itemGroup, e);
				continue;
			}
			for (ItemStack itemStack : creativeTabItemStacks) {
				if (itemStack.isEmpty()) {
					LOGGER.error("Found an empty itemStack from creative tab: {}", itemGroup);
				} else {
					addItemStack(stackHelper, itemStack, itemList, itemNameSet);
				}
			}
		}
		return itemList;
	}

	private static void addItemStack(StackHelper stackHelper, ItemStack stack, List<ItemStack> itemList, Set<String> itemNameSet) {
		//TODO: Test to make sure this is actually fixed in 1.17 and if so remove this check
		// Game freezes when loading player skulls, see https://bugs.mojang.com/browse/MC-65587
		if (stack.getItem() == Items.PLAYER_HEAD) {
			return;
		}

		final String itemKey;

		try {
			itemKey = stackHelper.getUniqueIdentifierForStack(stack, UidContext.Ingredient);
		} catch (RuntimeException | LinkageError e) {
			String stackInfo = ErrorUtil.getItemStackInfo(stack);
			LOGGER.error("Couldn't get unique name for itemStack {}", stackInfo, e);
			return;
		}

		if (!itemNameSet.contains(itemKey)) {
			itemNameSet.add(itemKey);
			itemList.add(stack);
		}
	}

}
