package mezz.jei.util;

import com.google.common.base.Joiner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraftforge.fml.common.registry.GameData;

import mezz.jei.Internal;
import mezz.jei.config.Config;

/**
 * For getting properties of ItemStacks efficiently
 */
public class ItemStackElement {
	@Nonnull
	private final ItemStack itemStack;
	@Nonnull
	private String searchString;
	@Nonnull
	private final String modNameString;
	@Nullable
	private String tooltipString;

	public ItemStackElement(@Nonnull ItemStack itemStack) {
		this.itemStack = itemStack;

		ResourceLocation itemResourceLocation = GameData.getItemRegistry().getNameForObject(itemStack.getItem());
		String modId = itemResourceLocation.getResourceDomain().toLowerCase(Locale.ENGLISH);
		String modName = Internal.getItemRegistry().getModNameForItem(itemStack.getItem()).toLowerCase(Locale.ENGLISH);

		String displayName = itemStack.getDisplayName();
		if (displayName == null) {
			throw new NullPointerException("No display name for item. " + itemResourceLocation + ' ' + itemStack.getItem().getClass());
		}

		this.modNameString = modId + ' ' + modName;

		StringBuilder searchStringBuilder = new StringBuilder(displayName.toLowerCase());

		if (!Config.isPrefixRequiredForModNameSearch()) {
			searchStringBuilder.append(' ').append(this.modNameString);
		}

		this.searchString = searchStringBuilder.toString();
	}

	public void setTooltip(EntityPlayer player) {
		ResourceLocation itemResourceLocation = GameData.getItemRegistry().getNameForObject(itemStack.getItem());
		String modId = itemResourceLocation.getResourceDomain().toLowerCase(Locale.ENGLISH);
		String modName = Internal.getItemRegistry().getModNameForItem(itemStack.getItem()).toLowerCase(Locale.ENGLISH);
		String displayName = itemStack.getDisplayName().toLowerCase();

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

		if (!Config.isPrefixRequiredForTooltipSearch()) {
			this.searchString = this.searchString + ' ' + this.tooltipString;
		}
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

	@Nullable
	public String getTooltipString() {
		return tooltipString;
	}
}
