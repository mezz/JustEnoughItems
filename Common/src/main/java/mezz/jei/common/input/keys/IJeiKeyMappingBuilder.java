package mezz.jei.common.input.keys;

public interface IJeiKeyMappingBuilder {
    IJeiKeyMappingBuilder setContext(JeiKeyConflictContext context);
    IJeiKeyMappingBuilder setModifier(JeiKeyModifier modifier);

    IJeiKeyMapping buildMouseLeft();
    IJeiKeyMapping buildMouseRight();
    IJeiKeyMapping buildMouseMiddle();
    IJeiKeyMapping buildKeyboardKey(int key);
    IJeiKeyMapping buildUnbound();
}
