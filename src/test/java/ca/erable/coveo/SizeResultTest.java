package ca.erable.coveo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SizeResultTest {

	@Test
	public void givenByteResult_thenReturnByteResult() {
		int sizeInBytes = 12875897;

		assertEquals("12875897 B", SizeResult.BYTE.get(sizeInBytes));
	}

	@Test
	public void givenKByteResult_thenReturnKByteFromByte() {
		int sizeInBytes = 12875897;

		assertEquals("12875.897", SizeResult.KBYTE.get(sizeInBytes));
	}

	@Test
	public void givenMBResult_thenReturnMBFromByte() {
		int sizeInBytes = 12875897;

		assertEquals("12.875897", SizeResult.MBYTE.get(sizeInBytes));
	}

	@Test
	public void test() {
		int sizeInBytes = 12875897;

		boolean si = true;

		int unit = si ? 1000 : 1024;
		int exp = (int) (Math.log(sizeInBytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		System.out.println(String.format("%.1f %sB", sizeInBytes / Math.pow(unit, exp), pre));
	}
}
