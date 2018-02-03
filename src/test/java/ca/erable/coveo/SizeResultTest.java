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
}
