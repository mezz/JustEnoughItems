package mezz.jei.util;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.google.common.base.Joiner;
import mezz.jei.Internal;
import mezz.jei.api.IItemRegistry;
import mezz.jei.config.Config;
import mezz.jei.util.color.ColorNamer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.oredict.OreDictionary;

/**
 * For getting properties of ItemStacks efficiently
 */
public class ItemStackElement {
	private final ItemStack itemStack;
	private final String searchString;
	private final String modNameString;
	private final String tooltipString;
	private final String oreDictString;
	private final String creativeTabsString;
	private final String colorString;

	@Nullable
	public static ItemStackElement create(ItemStack itemStack, IItemRegistry itemRegistry) {
		try {
			return new ItemStackElement(itemStack, itemRegistry);
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

	private ItemStackElement(ItemStack itemStack, IItemRegistry itemRegistry) {
		this.itemStack = itemStack;
		Item item = itemStack.getItem();

		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		ResourceLocation itemResourceLocation = item.getRegistryName();
		if (itemResourceLocation == null) {
			String stackInfo = ErrorUtil.getItemStackInfo(itemStack);
			throw new NullPointerException("No registry name for item. " + stackInfo);
		}

		String modId = itemResourceLocation.getResourceDomain().toLowerCase(Locale.ENGLISH);
		String modName = itemRegistry.getModNameForModId(modId).toLowerCase(Locale.ENGLISH);

		String displayName = itemStack.getDisplayName();
		if (displayName == null) {
			String stackInfo = ErrorUtil.getItemStackInfo(itemStack);
			throw new NullPointerException("No display name for item. " + stackInfo);
		}
		displayName = displayName.toLowerCase();

		this.modNameString = modId + ' ' + modName;

		String tooltipString;
		try {
			List<String> tooltip = itemStack.getTooltip(player, false);
			tooltipString = Joiner.on(' ').join(tooltip).toLowerCase();
			tooltipString = TextFormatting.getTextWithoutFormattingCodes(tooltipString);
			if (tooltipString != null) {
				tooltipString = tooltipString.replace(modId, "");
				tooltipString = tooltipString.replace(modName, "");
				tooltipString = tooltipString.replace(displayName, "");
			}
		} catch (RuntimeException ignored) {
			tooltipString = "";
		} catch (LinkageError ignored) {
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
			this.colorString = getColorString(itemStack);
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

	private static String getColorString(ItemStack itemStack) {
		ColorNamer colorNamer = Internal.getColorNamer();
		if (colorNamer == null) {
			return "";
		}

		final Collection<String> colorNames;
		try {
			colorNames = colorNamer.getColorNames(itemStack);
		} catch (RuntimeException ignored) {
			return "";
		} catch (LinkageError ignored) {
			return "";
		}

		return Joiner.on(' ').join(colorNames).toLowerCase();
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	public String getSearchString() {
		return searchString;
	}

	public String getModNameString() {
		return modNameString;
	}

	public String getTooltipString() {
		return tooltipString;
	}

	public String getOreDictString() {
		return oreDictString;
	}

	public String getCreativeTabsString() {
		return creativeTabsString;
	}

	public String getColorString() {
		return colorString;
	}
}
