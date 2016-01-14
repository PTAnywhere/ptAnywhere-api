package uk.ac.open.kmi.forge.ptAnywhere.identity.finder;


public class IdentityFinderFactory {

    public static IdentityFinder createUniqueAnonymous() {
        return new UniqueAnonymousFinder();
    }

}
