package uk.ac.open.kmi.forge.ptAnywhere.identity.finder.historical;

import static org.mockito.Mockito.*;
import org.mockito.Matchers;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.internal.verification.VerificationModeFactory;
import uk.ac.open.kmi.forge.ptAnywhere.identity.Identifiable;
import uk.ac.open.kmi.forge.ptAnywhere.identity.factories.AnonymousIdentityFactory;
import uk.ac.open.kmi.forge.ptAnywhere.identity.factories.IdentityFactory;
import uk.ac.open.kmi.forge.ptAnywhere.identity.finder.historical.cache.RedisCacheFinder;
import uk.ac.open.kmi.forge.ptAnywhere.identity.finder.historical.lrs.LRSFinder;


public class HistoricalAnonymousFinderTest {

    final static String NAME = "Identity name";
    final static String HOMEPAGE = "http://identity.is/";
    final static String ACCOUNT_NAME = "aitor";
    final static String PREVIOUS_SESSION = "previous";
    final static String CURRENT_SESSION = "current";

    HistoricalAnonymousFinder testable;
    Identifiable identifiable;
    IdentityFactory factory;
    BySessionId criterion;

    @Before
    public void setUp() {
        final RedisCacheFinder cache = mock(RedisCacheFinder.class);
        final LRSFinder lrs = mock(LRSFinder.class);
        this.testable = new HistoricalAnonymousFinder(cache, lrs);

        this.criterion = new BySessionId(PREVIOUS_SESSION);
        this.factory = new AnonymousIdentityFactory(CURRENT_SESSION);

        this.identifiable = mock(Identifiable.class);
        when(this.identifiable.getName()).thenReturn(NAME);
        when(this.identifiable.getHomePage()).thenReturn(HOMEPAGE);
        when(this.identifiable.getAccountName()).thenReturn(ACCOUNT_NAME);
    }

    @Test
    public void testFindIdentityCached() {
        when(this.testable.cacheFinder.findIdentity(this.criterion)).thenReturn(this.identifiable);

        assertEquals(NAME, this.testable.findIdentity(this.criterion).getName());
        assertEquals(HOMEPAGE, this.testable.findIdentity(this.criterion).getHomePage());
        assertEquals(ACCOUNT_NAME, this.testable.findIdentity(this.criterion).getAccountName());
    }

    @Test
    public void testFindIdentityWithFactoryCached() {
        when(this.testable.cacheFinder.findIdentity(this.criterion)).thenReturn(this.identifiable);

        assertEquals(NAME, this.testable.findIdentity(this.criterion, this.factory).getName());
        assertEquals(HOMEPAGE, this.testable.findIdentity(this.criterion, this.factory).getHomePage());
        assertEquals(ACCOUNT_NAME, this.testable.findIdentity(this.criterion, this.factory).getAccountName());

        // findIdentity() called three times in assertExpectedIdentity
        verify(this.testable.cacheFinder, VerificationModeFactory.times(3)).cacheIdentity(CURRENT_SESSION, this.identifiable);
    }

    @Test
    public void testFindIdentityLRS() {
        when(this.testable.cacheFinder.findIdentity(this.criterion)).thenReturn(null);
        when(this.testable.lrsFinder.findIdentity(this.criterion)).thenReturn(this.identifiable);

        assertEquals(NAME, this.testable.findIdentity(this.criterion).getName());
        assertEquals(HOMEPAGE, this.testable.findIdentity(this.criterion).getHomePage());
        assertEquals(ACCOUNT_NAME, this.testable.findIdentity(this.criterion).getAccountName());

        // findIdentity() called three times in this test
        verify(this.testable.cacheFinder, VerificationModeFactory.times(3)).cacheIdentity(PREVIOUS_SESSION, this.identifiable);
    }

    @Test
    public void testFindIdentityWithFactoryLRS() {
        when(this.testable.cacheFinder.findIdentity(this.criterion)).thenReturn(null);
        when(this.testable.lrsFinder.findIdentity(this.criterion)).thenReturn(this.identifiable);

        assertEquals(NAME, this.testable.findIdentity(this.criterion, this.factory).getName());
        assertEquals(HOMEPAGE, this.testable.findIdentity(this.criterion, this.factory).getHomePage());
        assertEquals(ACCOUNT_NAME, this.testable.findIdentity(this.criterion, this.factory).getAccountName());

        // findIdentity() called three times in this test
        verify(this.testable.cacheFinder, VerificationModeFactory.times(3)).cacheIdentity(CURRENT_SESSION, this.identifiable);
    }

    @Test
    public void testFindIdentityNotFound() {
        when(this.testable.cacheFinder.findIdentity(this.criterion)).thenReturn(null);
        when(this.testable.lrsFinder.findIdentity(this.criterion)).thenReturn(null);
        assertNull(this.testable.findIdentity(this.criterion));
    }

    @Test
    public void testFindIdentityWithFactoryNotFound() {
        when(this.testable.cacheFinder.findIdentity(this.criterion)).thenReturn(null);
        when(this.testable.lrsFinder.findIdentity(this.criterion)).thenReturn(null);

        assertEquals("Anonymous user (current)", this.testable.findIdentity(this.criterion, this.factory).getName());
        assertEquals(AnonymousIdentityFactory.AnonymousIdentity.ANONYMOUS_URI, this.testable.findIdentity(this.criterion, this.factory).getHomePage());
        assertEquals("current", this.testable.findIdentity(this.criterion, this.factory).getAccountName());

        // findIdentity() called three times in this test
        verify(this.testable.cacheFinder, VerificationModeFactory.times(3)).cacheIdentity(eq(CURRENT_SESSION), Matchers.<Identifiable>any());
    }
}