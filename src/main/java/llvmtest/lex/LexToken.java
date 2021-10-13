package magnum.lex;

public class LexToken {
	public LexTokenType type;
	public String string;
	public LexToken(LexTokenType type, String string) {
		this.type = type;
		this.string = string;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LexToken)) {
			return false;
		}
		LexToken o = (LexToken) other;
		return type == o.type && string.equals(o.string);
	}

	@Override
	public String toString() {
		return type.name() + "(" + string + ")";
	}
}
