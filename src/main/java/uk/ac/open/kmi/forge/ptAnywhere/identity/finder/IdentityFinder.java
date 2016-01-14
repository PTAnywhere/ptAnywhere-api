package uk.ac.open.kmi.forge.ptAnywhere.identity.finder;

import uk.ac.open.kmi.forge.ptAnywhere.identity.Identifiable;


public interface IdentityFinder<T extends FindingCriterion> {

    Identifiable findIdentity(T criteria);

}