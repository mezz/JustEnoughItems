package mezz.jei.util;

import com.google.common.base.Joiner;
import com.mojang.realmsclient.gui.ChatFormatting;
import mezz.jei.Internal;
import mezz.jei.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * For getting properties of ItemStacks efficiently
 */
public class ItemStackElement {
	@Nonnull
	private final ItemStack itemStack;
	@Nonnull
	private final String searchString;
	@Nonnull
	private final String modNameString;
	@Nonnull
	private final String tooltipString;
	@Nonnull
	private final String oreDictString;
	@Nonnull
	private final String creativeTabsString;
	@Nonnull
	private final String colorString;

	@Nullable
	public static ItemStackElement create(@Nonnull ItemStack itemStack) {
		try {
			return new ItemStackElement(itemStack);
		} catch (RuntimeException e) {
			try {
				String itemStackInfo = ErrorUtil.getItemStackInfo(itemStack);
				Log.warning("Found a broken itemStack {}", itemStackInfo, e);
			} catch (RuntimeException e2) {
				Log.warning("Found a broken itemStack.", e2);
			}
			return null;
		}
	}

	private ItemStackElement(@Nonnull ItemStack itemStack) {
		this.itemStack = itemStack;
		Item item = itemStack.getItem();

		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		ResourceLocation itemResourceLocation = GameData.getItemRegistry().getNameForObject(item);
		String modId = itemResourceLocation.getResourceDomain().toLowerCase(Locale.ENGLISH);
		String modName = Internal.getItemRegistry().getModNameForItem(item).toLowerCase(Locale.ENGLISH);

		String displayName = itemStack.getDisplayName();
		if (displayName == null) {
			throw new NullPointerException("No display name for item. " + itemResourceLocation + ' ' + item.getClass());
		}
		displayName = displayName.toLowerCase();

		this.modNameString = modId + ' ' + modName;

		String tooltipString;
		try {
			List<String> tooltip = itemStack.getTooltip(player, false);
			tooltipString = Joiner.on(' ').join(tooltip).toLowerCase();
			tooltipString = ChatFormatting.stripFormatting(tooltipString);
			tooltipString = tooltipString.replace(modId, "");
			tooltipString = tooltipString.replace(modName, "");
			tooltipString = tooltipString.replace(displayName, "");
		} catch (RuntimeException ignored) {
			tooltipString = "";
		}
		this.tooltipString = tooltipString;

		StringBuilder oreDictStringBuilder = new StringBuilder();
		for (int oreId : OreDictionary.getOreIDs(itemStack)) {
			String oreName = OreDictionary.getOreName(oreId).toLowerCase(Locale.ENGLISH);
			oreDictStringBuilder.append(oreName).append(' ');
		}
		this.oreDictString = oreDictStringBuilder.toString();

		StringBuilder creativeTabStringBuilder = new StringBuilder();
		for (CreativeTabs creativeTab : item.getCreativeTabs()) {
			if (creativeTab != null) {
				String creativeTabName = I18n.format(creativeTab.getTranslatedTabLabel()).toLowerCase();
				creativeTabStringBuilder.append(creativeTabName).append(' ');
			}
		}
		this.creativeTabsString = creativeTabStringBuilder.toString();

		if (Config.isColorSearchEnabled()) {
			Collection<String> colorNames = Internal.getColorNamer().getColorNames(itemStack);
			this.colorString = Joiner.on(' ').join(colorNames).toLowerCase();
		} else {
			this.colorString = "";
		}

		StringBuilder searchStringBuilder = new StringBuilder(displayName);

		if (!Config.isPrefixRequiredForModNameSearch()) {
			searchStringBuilder.append(' ').append(this.modNameString);
		}

		if (!Config.isPrefixRequiredForTooltipSearch()) {
			searchStringBuilder.append(' ').append(this.tooltipString);
		}

		if (!Config.isPrefixRequiredForOreDictSearch()) {
			searchStringBuilder.append(' ').append(this.oreDictString);
		}

		if (!Config.isPrefixRequiredForCreativeTabSearch()) {
			searchStringBuilder.append(' ').append(this.creativeTabsString);
		}

		if (!Config.isPrefixRequiredForColorSearch()) {
			searchStringBuilder.append(' ').append(this.colorString);
		}

		this.searchString = searchStringBuilder.toString();
	}

	@Nonnull
	public ItemStack getItemStack() {
		return itemStack;
	}

	@Nonnull
	public String getSearchString() {
		return searchString;
	}

	@Nonnull
	public String getModNameString() {
		return modNameString;
	}

	@Nonnull
	public String getTooltipString() {
		return tooltipString;
	}

	@Nonnull
	public String getOreDictString() {
		return oreDictString;
	}

	@Nonnull
	public String getCreativeTabsString() {
		return creativeTabsString;
	}

	@Nonnull
	public String getColorString() {
		return colorString;
	}
}
