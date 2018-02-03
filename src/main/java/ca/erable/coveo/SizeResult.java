package ca.erable.coveo;

import java.util.function.Function;

public enum SizeResult {
	BYTE((e) -> String.valueOf(e) + " B"), KBYTE((e) -> {
		return e + " KB";
	}), MBYTE((e) -> String.valueOf(e / 1000000.0));

	private Function<Long, String> fct;

	private SizeResult(Function<Long, String> fct) {
		this.fct = fct;
	}

	public String get(long bytes) {
		return fct.apply(bytes);
	}
}
