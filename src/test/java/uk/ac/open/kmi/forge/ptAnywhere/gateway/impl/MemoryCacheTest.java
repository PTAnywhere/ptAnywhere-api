package uk.ac.open.kmi.forge.ptAnywhere.gateway.impl;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MemoryCacheTest {

    MemoryCache cache;

    @Before
    public void setUp() {
        this.cache = new MemoryCache();
    }

    @Test
    public void testGetName() {
        assertNull(this.cache.getName("session1", "identifier"));  // Not cached yet
        this.cache.add("session1", "identifier", "device name");
        assertEquals("device name", this.cache.getName("session1", "identifier"));
    }

    @Test
    public void testGetNameRepeatedName() {
        this.cache.add("session1", "identifier", "device name");
        this.cache.add("session2", "identifier2", "device name");  // Not repeated because it is in another session
        assertEquals("device name", this.cache.getName("session1", "identifier"));

        // If there are multiple devices with the same name, we should return a null.
        this.cache.add("session1", "identifier3", "device name");
        assertNull(this.cache.getName("session1", "identifier1"));  // Repeated so, not anymore.
        assertEquals("device name", this.cache.getName("session2", "identifier2"));  // The other session still has only one
    }

    @Test
    public void testRemove() {
        this.cache.add("session1", "identifier", "device name");
        this.cache.add("session1", "identifier2", "device name 2");
        assertEquals("device name", this.cache.getName("session1", "identifier"));

        this.cache.remove("session1", "identifier");
        assertNull(this.cache.getName("session1", "identifier"));
        assertEquals("device name 2", this.cache.getName("session1", "identifier2"));
    }

    @Test
    public void testRemoveAll() {
        this.cache.add("session1", "identifier", "device name");
        this.cache.add("session1", "identifier2", "device name 2");
        this.cache.add("session2", "identifier3", "device name 3");

        this.cache.removeAll("session1");
        assertNull(this.cache.getName("session1", "identifier"));
        assertNull(this.cache.getName("session1", "identifier2"));
        assertEquals("device name 3", this.cache.getName("session2", "identifier3"));
    }
}
