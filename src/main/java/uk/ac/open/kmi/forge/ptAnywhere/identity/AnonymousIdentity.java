package uk.ac.open.kmi.forge.ptAnywhere.identity;


public class AnonymousIdentity implements Identifiable {

    final String tokenId;

    public AnonymousIdentity(String currentSessionId) {
        this.tokenId = currentSessionId;
    }

    public String getName() {
        return "Anonymous user (" + this.tokenId + ")";
    }


    public String getHomePage() {
        return "http://forge.kmi.open.ac.uk/pt/user/anonymous/" + this.tokenId;
    }
}
