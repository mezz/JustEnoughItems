package mezz.jei.common.config;

import mezz.jei.api.constants.ModIds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractModIdFormatConfig implements IModIdFormatConfig {
    private static final Logger LOGGER = LogManager.getLogger();

    protected static final String defaultModNameFormat = "blue italic";
    public static final String MOD_NAME_FORMAT_CODE = "%MODNAME%";

    @Nullable
    private String cachedOverride; // when we detect another mod is adding mod names to tooltips, use its formatting

    protected abstract List<Component> getTestTooltip(ItemStack itemStack);

    protected abstract String getFormat();

    private String getOverride() {
        if (cachedOverride == null) {
            cachedOverride = detectModNameTooltipFormatting();
        }
        return cachedOverride;
    }

    @Override
    public final String getModNameFormat() {
        String override = getOverride();
        if (!override.isEmpty()) {
            return override;
        }
        return getFormat();
    }

    @Override
    public final boolean isModNameFormatOverrideActive() {
        return !getOverride().isEmpty();
    }

    protected static String parseFriendlyModNameFormat(String formatWithEnumNames) {
        if (formatWithEnumNames.isEmpty()) {
            return "";
        }
        StringBuilder format = new StringBuilder();
        String[] strings = formatWithEnumNames.split(" ");
        for (String string : strings) {
            ChatFormatting valueByName = ChatFormatting.getByName(string);
            if (valueByName != null) {
                format.append(valueByName);
            } else {
                LOGGER.error("Invalid format: {}", string);
            }
        }
        return format.toString();
    }

    private String detectModNameTooltipFormatting() {
        List<Component> tooltip = getTestTooltip(new ItemStack(Items.APPLE));
        if (tooltip.size() <= 1) {
            return "";
        }

        for (int lineNum = 1; lineNum < tooltip.size(); lineNum++) {
            Component line = tooltip.get(lineNum);
            String lineString = line.getString();
            if (lineString.contains(ModIds.MINECRAFT_NAME)) {
                String withoutFormatting = ChatFormatting.stripFormatting(lineString);
                if (withoutFormatting != null && withoutFormatting.contains(ModIds.MINECRAFT_NAME)) {
                    return StringUtils.replaceOnce(lineString, ModIds.MINECRAFT_NAME, MOD_NAME_FORMAT_CODE);
                }
            }
        }
        return "";
    }
}
