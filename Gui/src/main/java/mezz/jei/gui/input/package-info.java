/**
 * Input handling for JEI controls, shortcuts, and ingredient drags.
 *
 * <p>An {@link mezz.jei.gui.input.InputArea} groups the controls, shortcuts, and drag
 * sources for part of a Minecraft screen, such as the ingredient list or recipe display.
 * {@link mezz.jei.gui.input.GuiInputScene} selects the first area under the pointer,
 * from front to back. Ingredient lookup uses that same area.</p>
 *
 * <p>For example, a player points at an ingredient in a recipe tooltip and uses Show
 * Recipes. JEI looks up that ingredient rather than one hidden behind the tooltip.</p>
 *
 * <p>{@link mezz.jei.gui.input.InputCommands} defines shortcuts that work anywhere on
 * the screen or only over their input area, for both keyboard and mouse bindings.
 * {@link mezz.jei.gui.input.GuiInputController} gives focused text editing priority
 * over those shortcuts and tracks mouse clicks and drags until release or cancellation.</p>
 */
@NullMarked
package mezz.jei.gui.input;

import org.jspecify.annotations.NullMarked;
