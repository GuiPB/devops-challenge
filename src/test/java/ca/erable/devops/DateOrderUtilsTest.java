package ca.erable.devops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

public class DateOrderUtilsTest {

    @Test
    public void givenFirstIsNull_thenReturnSecond() {

        Calendar currentCalendar = Calendar.getInstance();

        currentCalendar.set(2000, Calendar.JANUARY, 1);

        Date second = currentCalendar.getTime();

        Date latest = DateOrderUtils.returnLatest(null, second);

        assertTrue(latest.equals(second));
    }

    @Test
    public void givenSecondIsNull_thenReturnFirst() {
        Calendar currentCalendar = Calendar.getInstance();

        currentCalendar.set(2000, Calendar.JANUARY, 1);

        Date first = currentCalendar.getTime();

        Date latest = DateOrderUtils.returnLatest(first, null);

        assertTrue(latest.equals(first));
    }

    @Test
    public void givenBothAreNull_thenReturnNull() {
        Date latest = DateOrderUtils.returnLatest(null, null);

        assertTrue(latest == null);
    }

    @Test
    public void givenFirstIsOlder_thenReturnSecond() {
        Calendar currentCalendar = Calendar.getInstance();

        currentCalendar.set(2000, Calendar.JANUARY, 1);
        Date first = currentCalendar.getTime();

        currentCalendar.set(2000, Calendar.JANUARY, 2);
        Date second = currentCalendar.getTime();

        Date latest = DateOrderUtils.returnLatest(first, second);

        assertEquals(second, latest);
    }

    @Test
    public void givenSecondIsOlder_thenReturnFirst() {
        Calendar currentCalendar = Calendar.getInstance();

        currentCalendar.set(2000, Calendar.JANUARY, 2);
        Date first = currentCalendar.getTime();

        currentCalendar.set(2000, Calendar.JANUARY, 1);
        Date second = currentCalendar.getTime();

        Date latest = DateOrderUtils.returnLatest(first, second);

        assertEquals(first, latest);
    }
}
