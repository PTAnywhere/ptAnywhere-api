package uk.ac.open.kmi.forge.ptAnywhere.api.http;


import com.cisco.pt.impl.UUIDImpl;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.UUID;

public class UtilsTest {

    // All the data has been double checked with an external tool.
    //      http://kjur.github.io/jsjws/tool_b64udec.html
    final String[][] TEST_SAMPLES = {
            {"6fc7797b-1a33-4fd7-8db1-1d6e7468db65", "b8d5exozT9eNsR1udGjbZQ--"},
            {"a9101f6b-ef7c-4372-91c2-9391e94ee233", "qRAfa.98Q3KRwpOR6U7iMw--"},
    };

    @Test
    public void testToSimplifiedId() {
        for (String[] sample: TEST_SAMPLES) {
            final UUID uuid = UUID.fromString(sample[0]);
            assertEquals(sample[1], Utils.toSimplifiedId(uuid));
        }
    }

    @Test
    public void testToSimplifiedIdFromCisco() {
        for (String[] sample: TEST_SAMPLES) {
            final com.cisco.pt.UUID uuid = new UUIDImpl("{" + sample[0] + "}");
            assertEquals(sample[1], Utils.toSimplifiedId(uuid));
        }
    }

    @Test
    public void testToUUID() {
        for (String[] sample: TEST_SAMPLES) {
            final UUID expectedUuid = UUID.fromString(sample[0]);
            assertEquals(expectedUuid, Utils.toUUID(sample[1]));
        }
    }

    @Test
    public void testToCiscoUUID() {
        for (String[] sample: TEST_SAMPLES) {
            final com.cisco.pt.UUID expectedUuid = new UUIDImpl("{" + sample[0] + "}");
            assertEquals(expectedUuid, Utils.toCiscoUUID(sample[1]));
        }
    }
}