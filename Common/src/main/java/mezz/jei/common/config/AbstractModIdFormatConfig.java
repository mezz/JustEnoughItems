package mezz.jei.common.config;

import net.minecraft.ChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractModIdFormatConfig implements IModIdFormatConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    protected static final String defaultModNameFormatFriendly = "blue italic";
    public static final String MOD_NAME_FORMAT_CODE = "%MODNAME%";

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
}
