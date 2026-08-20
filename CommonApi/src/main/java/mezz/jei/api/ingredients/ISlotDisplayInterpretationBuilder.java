package mezz.jei.api.ingredients;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Builds JEI's interpretation of a slot display.
 * <p>
 * An interpretation can describe how a display delegates to other slot displays and add meaning that is lost when
 * Minecraft resolves the display into ordinary ingredients. JEI passes a new builder to each slot display
 * interpreter. If the interpreter does not change the builder, JEI handles the display as one ordinary ingredient
 * group.
 *
 * @param <T> the type of ingredient being interpreted
 *
 * @since 30.20.0
 */
@ApiStatus.NonExtendable
public interface ISlotDisplayInterpretationBuilder<T> {
	/**
	 * Set the display wrapped by this display.
	 * <p>
	 * Use this for a transparent wrapper that resolves to the same ingredients as the wrapped display.
	 * JEI will get interpretation details from the wrapped display, including interpreters registered by other mods.
	 * Information set directly on this builder is kept and takes priority over information from the wrapped display.
	 * <p>
	 * This replaces any wrapped display or child displays previously set on this builder.
	 * By default, the display is not treated as a wrapper.
	 *
	 * @param wrappedDisplay the display whose ingredients and interpretation should be used
	 * @return this builder, for chaining calls
	 *
	 * @since 30.20.0
	 * @deprecated use {@link #addChildDisplay(SlotDisplay)}
	 */
	@Deprecated(since = "30.25.0", forRemoval = true)
	ISlotDisplayInterpretationBuilder<T> setWrappedDisplay(SlotDisplay wrappedDisplay);

	/**
	 * Set the displays that make up this display.
	 * <p>
	 * JEI resolves and interprets each child as its own ingredient group, then concatenates the groups in order.
	 * This preserves different matching rules and tooltip information for mixed composites. Information set directly
	 * on this builder is applied to every child group and takes priority over information from the children.
	 * <p>
	 * This replaces any wrapped display or child displays previously set on this builder. An empty list produces no
	 * ingredients. By default, the display is resolved as one ingredient group.
	 *
	 * @param childDisplays the ordered displays that make up this display
	 * @return this builder, for chaining calls
	 *
	 * @since 30.20.0
	 * @deprecated call {@link #addChildDisplay(SlotDisplay)} for each child display
	 */
	@Deprecated(since = "30.25.0", forRemoval = true)
	ISlotDisplayInterpretationBuilder<T> setChildDisplays(List<? extends SlotDisplay> childDisplays);

	/**
	 * Append a display to the displays that make up this display.
	 * <p>
	 * JEI resolves and interprets the child as its own ingredient group. Displays that were previously added to this
	 * builder are retained.
	 *
	 * @param childDisplay the display to append
	 * @return this builder, for chaining calls
	 *
	 * @since 30.25.0
	 */
	ISlotDisplayInterpretationBuilder<T> addChildDisplay(SlotDisplay childDisplay);

	/**
	 * Append a display to the displays that make up this display, and transform each ingredient produced by that child.
	 * <p>
	 * JEI resolves and interprets the child first, then applies the transformer to each of its ingredients. This allows
	 * transformed and untransformed children to be combined while preserving their separate ingredient groups and
	 * interpretation details. Nested transformations are applied from the inside out.
	 * <p>
	 * The transformer must not mutate the ingredient it receives. It should return a new ingredient when making a
	 * change. Invalid ingredients returned by the transformer are omitted.
	 *
	 * @param childDisplay the display to append
	 * @param ingredientTransformer transforms each ingredient produced by this child
	 * @return this builder, for chaining calls
	 *
	 * @since 30.25.0
	 */
	ISlotDisplayInterpretationBuilder<T> addChildDisplay(
		SlotDisplay childDisplay,
		UnaryOperator<T> ingredientTransformer
	);

	/**
	 * Set whether each resolved ingredient can stand for all variants with the same
	 * {@link IIngredientHelper#getGroupingUid grouping UID}.
	 * The default is false, so JEI only matches the resolved ingredients exactly.
	 * <p>
	 * Calling this method with false is an explicit override: it suppresses a true value inherited from a wrapped or
	 * child display. Leaving the builder unchanged uses the inherited value when this display declares children.
	 *
	 * @param matchesAllSubtypes true to match every subtype, false to explicitly match only the resolved ingredients
	 * @return this builder, for chaining calls
	 *
	 * @since 30.20.0
	 * @deprecated use {@link #setWildcardForSubtypes(boolean)} to describe the slot display's subtype semantics
	 */
	@Deprecated(since = "30.21.0", forRemoval = true)
	ISlotDisplayInterpretationBuilder<T> setMatchesAllSubtypes(boolean matchesAllSubtypes);

	/**
	 * Set whether each resolved ingredient is a wildcard, representing every subtype with the same
	 * {@link IIngredientHelper#getGroupingUid grouping UID}.
	 * <p>
	 * When enabled, JEI matches all subtypes for input slots whose resolved ingredients can have subtypes according to
	 * their ingredient helper. JEI also adds its standard "Any &lt;ingredient&gt;" tooltip heading unless this display has
	 * a tag or an explicitly set or cleared tooltip heading.
	 * <p>
	 * The default is false.
	 * Calling this method with false suppresses wildcard behavior of wrapped or child displays.
	 *
	 * @param wildcardForSubtypes true if resolved ingredients represent every subtype, false to disable wildcard behavior
	 * @return this builder, for chaining calls
	 *
	 * @since 30.21.0
	 */
	ISlotDisplayInterpretationBuilder<T> setWildcardForSubtypes(boolean wildcardForSubtypes);

	/**
	 * Set the tag represented by the slot display so JEI can show it in recipe slot tooltips.
	 * By default, no tag is recorded.
	 *
	 * @param tagKey the tag represented by the display
	 * @return this builder, for chaining calls
	 *
	 * @since 30.20.0
	 */
	ISlotDisplayInterpretationBuilder<T> setTagKey(TagKey<?> tagKey);

	/**
	 * Prevent JEI from showing a tag for this display.
	 * Use this to suppress a tag inherited from a wrapped or child display, or one that JEI would otherwise infer from
	 * the resolved ingredients.
	 *
	 * @return this builder, for chaining calls
	 *
	 * @since 30.20.0
	 */
	ISlotDisplayInterpretationBuilder<T> clearTagKey();

	/**
	 * Set a heading that JEI shows above the normal tooltip for the currently displayed ingredient.
	 * Use this to explain a slot that displays concrete ingredients but has broader meaning, such as "Any Fuel".
	 * JEI preserves the component's styling.
	 * By default, no heading is added.
	 *
	 * @param tooltipHeader the heading to add
	 * @return this builder, for chaining calls
	 *
	 * @since 30.20.0
	 */
	ISlotDisplayInterpretationBuilder<T> setTooltipHeader(Component tooltipHeader);

	/**
	 * Prevent a tooltip heading inherited from a wrapped or child display from being shown for this display.
	 * <p>
	 * This has no effect unless this display declares a wrapped display or child displays.
	 *
	 * @return this builder, for chaining calls
	 *
	 * @since 30.20.0
	 */
	ISlotDisplayInterpretationBuilder<T> clearTooltipHeader();
}
