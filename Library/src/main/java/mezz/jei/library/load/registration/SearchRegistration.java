package mezz.jei.library.load.registration;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.ISearchRegistration;
import mezz.jei.api.search.ILanguageTransformer;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SearchRegistration implements ISearchRegistration {
    private static final Logger LOGGER = LogManager.getLogger();

    private final IJeiHelpers jeiHelpers;
    private final Map<ResourceLocation, ILanguageTransformer> languageTransformers = new LinkedHashMap<>();

    public SearchRegistration(IJeiHelpers jeiHelpers) {
        this.jeiHelpers = jeiHelpers;
    }

    @Override
    public IJeiHelpers getJeiHelpers() {
        return jeiHelpers;
    }

    @Override
    public void addLanguageTransformer(ILanguageTransformer languageTransformer) {
        ErrorUtil.checkNotNull(languageTransformer, "languageTransformer");

        LOGGER.info("Registering language transformer: {}", languageTransformer.getId());
        this.languageTransformers.put(languageTransformer.getId(), languageTransformer);
    }

    public Collection<ILanguageTransformer> getLanguageTransformers() {
        return List.copyOf(languageTransformers.values());
    }
}
