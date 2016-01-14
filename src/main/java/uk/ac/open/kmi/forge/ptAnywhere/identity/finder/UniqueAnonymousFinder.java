package uk.ac.open.kmi.forge.ptAnywhere.identity.finder;

import uk.ac.open.kmi.forge.ptAnywhere.identity.AnonymousIdentity;
import uk.ac.open.kmi.forge.ptAnywhere.identity.Identifiable;


public class UniqueAnonymousFinder implements IdentityFinder<UniqueAnonymousFinder.ByToken> {

    public Identifiable findIdentity(ByToken criteria) {
        return new AnonymousIdentity(criteria.getToken());
    }

    public static class ByToken implements  FindingCriterion {
        String tokenId;

        public ByToken(String tokenId) {
            this.tokenId = tokenId;
        }

        protected String getToken() {
            return this.tokenId;
        }
    }
}
