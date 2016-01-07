package uk.ac.open.kmi.forge.ptAnywhere.analytics;

import org.joda.time.DateTime;
import org.junit.Test;
import static org.junit.Assert.*;


public class LRSTimestampTest {

    @Test
    public void testGetRelativeServerTime() {
        final LRSTimestamp time = new LRSTimestamp();
        time.lrs = new DateTime(2015, 1, 5, 10, 0, 0, 0);

        time.webserver = new DateTime(2015, 1, 5, 10, 0, 10);  // 10 seconds earlier than LRS
        assertEquals(new DateTime(2015, 1, 5, 16, 59, 50), time.getLRSTime(new DateTime(2015, 1, 5, 17, 0)));

        time.webserver = new DateTime(2015, 1, 5, 9, 59, 59, 990);  // 10 millis ahead
        assertEquals(new DateTime(2015, 1, 5, 17, 0, 0, 10), time.getLRSTime(new DateTime(2015, 1, 5, 17, 0, 0, 0)));
    }
}
