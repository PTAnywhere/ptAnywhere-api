package uk.ac.open.kmi.forge.ptAnywhere.identity.factories;

import uk.ac.open.kmi.forge.ptAnywhere.identity.Identifiable;


public class AnonymousIdentityFactory implements IdentityFactory {
    String tokenId;

    public AnonymousIdentityFactory(String tokenId) {
        this.tokenId = tokenId;
    }

    public Identifiable create() {
        return new AnonymousIdentity(this.tokenId);
    }

    public String getKey() {
        return this.tokenId;
    }

    public static class AnonymousIdentity implements Identifiable {

        public final static String ANONYMOUS_URI = "http://forge.kmi.open.ac.uk/pt/user/anonymous";
        final String tokenId;

        protected AnonymousIdentity(String tokenId) {
            this.tokenId = tokenId;
        }

        public String getName() {
            return "Anonymous user (" + this.tokenId + ")";
        }

        public String getAccountName() {
            return this.tokenId;
        }

        public String getHomePage() {
            return ANONYMOUS_URI;
        }
    }
}
