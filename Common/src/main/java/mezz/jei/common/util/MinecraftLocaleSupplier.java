package mezz.jei.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.LanguageManager;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.function.Supplier;

public class MinecraftLocaleSupplier implements Supplier<Locale> {
    @Nullable
    private String cachedLocaleCode;
    @Nullable
    private Locale cachedLocale;

    @Override
    public Locale get() {
        Minecraft minecraft = Minecraft.getInstance();
        //noinspection ConstantValue
        if (minecraft == null) {
            return Locale.getDefault();
        }
        LanguageManager languageManager = minecraft.getLanguageManager();
        String code = languageManager.getSelected();
        if (cachedLocale == null || !code.equals(cachedLocaleCode)) {
            cachedLocaleCode = code;
            String[] splitLangCode = code.split("_", 2);
            if (splitLangCode.length == 1) { // Vanilla has some languages without underscores
                cachedLocale = new Locale(code);
            } else {
                cachedLocale = new Locale(splitLangCode[0], splitLangCode[1]);
            }
        }
        return cachedLocale;
    }
}
