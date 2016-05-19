package uk.ac.open.kmi.forge.ptAnywhere.api.http;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class URLFactoryTest {
    @Test
    public void testExtractElement() {
        assertEquals("jon", URLFactory.extractElement("http://fake.org/people/jon", "people"));
        assertEquals("mikel", URLFactory.extractElement("http://fake.org/people/mikel/pet/", "people"));
        assertEquals("ander", URLFactory.extractElement("http://fake.org/v1/people/ander/", "people"));
        assertEquals(null, URLFactory.extractElement("http://fake.org/v1/people2/ander/", "people"));
        assertEquals(null, URLFactory.extractElement("http://fake.org/v1/people", "people"));
    }

    @Test
    public void testExtractPort() {
        final String nonConflictive = "port1";
        assertEquals(nonConflictive, URLFactory.parsePortId("http://fake.org/ports/port1"));
        final String conflictive = "port/1";
        final String conflictiveEscaped = Utils.escapePort(conflictive);
        assertEquals(conflictive, URLFactory.parsePortId("http://fake.org/ports/" + conflictiveEscaped));
    }

    @Test
    public void testParse() {
        final DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        final DateTime t = fmt.parseDateTime("2016-01-19T17:23:40.827400+00:00");
        System.out.println(t);
    }
}