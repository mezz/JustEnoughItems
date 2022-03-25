package mezz.jei.core.util;

import javax.annotation.Nonnegative;

public class SubString {
	private final String string;
	private final int offset;
	private final int length;

	public SubString(String string) {
		this(string, 0, string.length());
	}

	public SubString(SubString subString) {
		this(subString.string, subString.offset, subString.length);
	}

	public SubString(String string, int offset) {
		this(string, offset, string.length() - offset);
	}

	@SuppressWarnings("ConstantConditions")
	public SubString(String string, @Nonnegative int offset, @Nonnegative int length) {
		assert length >= 0;
		assert offset >= 0;
		assert offset + length <= string.length();

		this.string = string;
		this.offset = offset;
		this.length = length;
	}

	public SubString substring(int offset) {
		return new SubString(string, this.offset + offset, this.length - offset);
	}

	public SubString shorten(int amount) {
		return new SubString(string, this.offset, this.length - amount);
	}

	public SubString append(char newChar) {
		assert this.offset + this.length < this.string.length();
		assert charAt(this.length) == newChar;

		return new SubString(string, this.offset, this.length + 1);
	}

	public boolean isEmpty() {
		return this.length == 0;
	}

	public char charAt(int index) {
		return this.string.charAt(this.offset + index);
	}

	public boolean regionMatches(int toffset, String other, int ooffset, int len) {
		//noinspection StringEquality
		if (this.string == other) {
			if (this.length >= len && (this.offset + toffset == ooffset)) {
				return true;
			}
		}
		return this.string.regionMatches(this.offset + toffset, other, ooffset, len);
	}

	public boolean regionMatches(SubString word, int lenToMatch) {
		if (lenToMatch > this.length) {
			return false;
		}
		return word.regionMatches(0, this.string, this.offset, lenToMatch);
	}

	public boolean isPrefix(SubString other) {
		return other.startsWith(this);
	}

	public boolean startsWith(SubString other) {
		return regionMatches(other, other.length());
	}

	@Nonnegative
	public int length() {
		return length;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": \"" +
			string.substring(this.offset, this.offset + this.length) +
			"\"\nBacking string: \"" + string + "\"";
	}

	public String commit() {
		return string.substring(offset, offset + length);
	}
}
