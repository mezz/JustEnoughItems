package mezz.jei.util;

import com.google.common.base.Joiner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.Minecraft;
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
	private final String searchString;
	@Nonnull
	private final String modName;
	@Nonnull
	private final String tooltipString;

	@Nullable
	public static ItemStackElement create(@Nonnull ItemStack itemStack) {
		try {
			return new ItemStackElement(itemStack);
		} catch (RuntimeException e) {
			Log.warning("Found broken itemStack.", e);
			return null;
		}
	}

	private ItemStackElement(@Nonnull ItemStack itemStack) {
		this.itemStack = itemStack;

		ResourceLocation itemResourceLocation = GameData.getItemRegistry().getNameForObject(itemStack.getItem());
		String modId = itemResourceLocation.getResourceDomain().toLowerCase(Locale.ENGLISH);
		String modName = Internal.getItemRegistry().getModNameForItem(itemStack.getItem()).toLowerCase(Locale.ENGLISH);

		String displayName = itemStack.getDisplayName();
		if (displayName == null) {
			throw new NullPointerException("No display name for item. " + itemResourceLocation + ' ' + itemStack.getItem().getClass());
		}

		String tooltipString;
		try {
			Minecraft minecraft = Minecraft.getMinecraft();
			List<String> tooltip = itemStack.getTooltip(minecraft.thePlayer, false);
			tooltipString = Joiner.on(' ').join(tooltip).toLowerCase();
			tooltipString = ChatFormatting.stripFormatting(tooltipString);
			tooltipString = tooltipString.replace(modId, "");
			tooltipString = tooltipString.replace(modName, "");
		} catch (RuntimeException ignored) {
			// some tooltips require a real player, and minecraft.thePlayer is normally null here.
			tooltipString = "";
		}
		this.tooltipString = tooltipString;
		this.modName = modId + ' ' + modName;

		StringBuilder searchStringBuilder = new StringBuilder(displayName.toLowerCase());

		if (!Config.isPrefixRequiredForModNameSearch()) {
			searchStringBuilder.append(' ').append(this.modName);
		}

		if (!Config.isPrefixRequiredForTooltipSearch()) {
			searchStringBuilder.append(' ').append(this.tooltipString);
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
	public String getModName() {
		return modName;
	}

	@Nonnull
	public String getTooltipString() {
		return tooltipString;
	}
}
