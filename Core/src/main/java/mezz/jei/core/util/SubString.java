package mezz.jei.core.util;

import javax.annotation.Nonnegative;

public class SubString {
	private String string;
	private int offset;
	private int length;

	public SubString(String string) {
		this(string, 0, string.length(), false);
	}

	public SubString(SubString subString) {
		this(subString.string, subString.offset, subString.length, true);
	}

	public SubString(String string, int offset) {
		this(string, offset, string.length() - offset);
	}

	@SuppressWarnings("ConstantConditions")
	public SubString(String string, @Nonnegative int offset, @Nonnegative int length) {
		this(string, offset, length, false);
	}

	@SuppressWarnings("ConstantConditions")
	private SubString(String string, @Nonnegative int offset, @Nonnegative int length, boolean fromSubString) {
		assert length >= 0;
		assert offset >= 0;
		assert offset + length <= string.length();

		if (fromSubString) {
			this.string = string;
		} else {
			this.string = string.intern();
		}
		this.offset = offset;
		this.length = length;
	}

	public SubString substring(int offset) {
		return new SubString(string, this.offset + offset, this.length - offset, true);
	}

	public SubString shorten(int amount) {
		return new SubString(string, this.offset, this.length - amount, true);
	}

	public SubString append(char newChar) {
		assert this.offset + this.length < this.string.length();
		assert charAt(this.length) == newChar;

		return new SubString(string, this.offset, this.length + 1, true);
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

	public void set(SubString other) {
		string = other.string;
		offset = other.offset;
		length = other.length;
	}

	@Override
	public String toString() {
		return string.substring(offset, offset + length);
	}
}
