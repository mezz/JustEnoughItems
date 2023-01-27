package mezz.jei.api.search;

import net.minecraft.resources.ResourceLocation;

/**
 * A language transformer lets you change string tokens that are added to the search tree.
 *
 * This is useful for some languages where there may be multiple ways
 * to type the same thing, and the transformer can change the text.
 * For example, "nihao" can be transformed to match "你好".
 *
 * @since 12.2.0
 */
public interface ILanguageTransformer {
    /**
     * Get the unique ID for this language transformer.
     */
    ResourceLocation getId();

    /**
     * Change the token into something else.
     *
     * This is called before inserting any ingredients into the search tree,
     * and also for looking up ingredients in the search tree.
     *
     * In order to work, this must always be consistent.
     * For an input string, it must always return the same output
     * string every time it is called.
     */
    String transformToken(String token);
}
