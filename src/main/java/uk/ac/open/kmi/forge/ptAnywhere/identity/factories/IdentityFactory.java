package uk.ac.open.kmi.forge.ptAnywhere.identity.factories;

import uk.ac.open.kmi.forge.ptAnywhere.identity.Identifiable;


public interface IdentityFactory {
    Identifiable create();
    String getKey();
}
