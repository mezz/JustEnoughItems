package mezz.jei.library.config;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.config.file.IConfigCategoryBuilder;
import mezz.jei.common.config.file.IConfigSchemaBuilder;
import mezz.jei.common.platform.IPlatformItemStackHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.function.CachedSupplierTransformer;
import mezz.jei.library.config.serializers.ChatFormattingSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class ModIdFormatConfig implements IModIdFormatConfig {
	protected static final List<ChatFormatting> defaultModNameFormat = List.of(ChatFormatting.BLUE, ChatFormatting.ITALIC);
	public static final String MOD_NAME_FORMAT_CODE = "%MODNAME%";

	private final Supplier<Component> modNameFormat;
	@Nullable
	private Component cachedOverride; // when we detect another mod is adding mod names to tooltips, use its formatting

	public ModIdFormatConfig(IConfigSchemaBuilder builder) {
		IConfigCategoryBuilder modName = builder.addCategory("modName");
		Supplier<List<ChatFormatting>> configValue = modName.addList(
			"modNameFormat",
			defaultModNameFormat,
			ChatFormattingSerializer.INSTANCE
		);
		this.modNameFormat = new CachedSupplierTransformer<>(configValue, ModIdFormatConfig::toFormatString);
	}

	private static Component toFormatString(List<ChatFormatting> values) {
		if (values.isEmpty()) {
			return Component.empty();
		}
		return Component.literal(MOD_NAME_FORMAT_CODE)
			.withStyle(values.toArray(ChatFormatting[]::new));
	}

	private Component getOverride() {
		if (cachedOverride == null) {
			cachedOverride = detectModNameTooltipFormatting();
		}
		return cachedOverride;
	}

	@Override
	public final Component getModNameFormat() {
		Component override = getOverride();
		if (!override.getString().isEmpty()) {
			return override;
		}
		return modNameFormat.get();
	}

	@Override
	public final boolean isModNameFormatOverrideActive() {
		return !getOverride().getString().isEmpty();
	}

	private static Component detectModNameTooltipFormatting() {
		IPlatformItemStackHelper itemStackHelper = Services.PLATFORM.getItemStackHelper();
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		return detectModNameTooltipFormatting(itemStackHelper, player);
	}

	public static Component detectModNameTooltipFormatting(IPlatformItemStackHelper itemStackHelper, @Nullable Player player) {
		List<Component> tooltip = itemStackHelper.getTestTooltip(player, new ItemStack(Items.APPLE));
		if (tooltip.size() <= 1) {
			return Component.empty();
		}

		for (int lineNum = 1; lineNum < tooltip.size(); lineNum++) {
			Component line = tooltip.get(lineNum);
			Component result = detectModNameTooltipFormatting(line);
			if (!result.getString().isEmpty()) {
				return result;
			}
		}
		return Component.empty();
	}

	private static Component detectModNameTooltipFormatting(Component line) {
		return StyledTextHelper.replaceFirst(line, ModIds.MINECRAFT_NAME, Component.literal(MOD_NAME_FORMAT_CODE))
			.orElseGet(Component::empty);
	}

	public static Component replaceModNameFormatCode(Component format, String modName) {
		return StyledTextHelper.replaceFirst(format, MOD_NAME_FORMAT_CODE, Component.literal(modName))
			.orElseGet(() -> format.copy().append(Component.literal(modName)));
	}
}
