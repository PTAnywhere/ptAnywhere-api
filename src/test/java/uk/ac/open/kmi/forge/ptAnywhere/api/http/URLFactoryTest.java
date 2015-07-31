package uk.ac.open.kmi.forge.ptAnywhere.api.http;

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
}