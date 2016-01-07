package uk.ac.open.kmi.forge.ptAnywhere.analytics;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.joda.time.DateTime;
import org.joda.time.Period;


/**
 * This class is used to create a basic synchronization between the web server and the LRS.
 *
 * To do this, we will record the time in LRS and then the time in the web server.
 * As the synchronization does not need to be very accurate, the delay added by the RTT
 * (round trip time) is not important for the application.
 *
 * The only important conditions are:
 *    1) the web server should always generate greater timestamps than the latest LRS time and
 *    2) the timestamp calculated by the web server should not be very different to the real LRS time.
 */
public class LRSTimestamp {

    final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    DateTime lrs;
    DateTime webserver;

    public LRSTimestamp() {
        // Not useful if at least a LRS response time is not updated after the constructor.
        // Before, getCurrentServerTime() will return the web server time.
        update(new DateTime());
    }

    public void update(DateTime lrsTime) {
        this.rwl.writeLock().lock();
        try {
            // E.g., if millis are ignored (time is rounded), the first webserver timestamp should be keep.
            if (this.lrs==null || !this.lrs.equals(lrsTime)) {
                this.lrs = lrsTime;
                this.webserver = new DateTime();
            }
        } finally {
            this.rwl.writeLock().unlock();
        }
    }

    protected DateTime getLRSTime(DateTime time) {
        this.rwl.readLock().lock();
        try {
            return this.lrs.plus( new Period(this.webserver, time) );
        } finally {
            this.rwl.readLock().unlock();
        }
    }

    public DateTime getCurrentServerTime() {
        return getLRSTime(new DateTime());
    }
}
