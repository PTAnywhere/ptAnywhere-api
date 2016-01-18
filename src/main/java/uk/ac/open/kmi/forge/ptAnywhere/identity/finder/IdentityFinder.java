package uk.ac.open.kmi.forge.ptAnywhere.identity.finder;

import uk.ac.open.kmi.forge.ptAnywhere.identity.Identifiable;
import uk.ac.open.kmi.forge.ptAnywhere.identity.factories.IdentityFactory;


/**
 * Finder of identities.
 * @param <T>
 *     Criterion used to find an identity.
 */
public interface IdentityFinder<T extends FindingCriterion> {

    /**
     * @param criteria
     * @return
     *      The identity associated to the criterion or null if it doesn't exist.
     */
    Identifiable findIdentity(T criteria);

    /**
     * @param criteria
     * @param factory
     *      If the identity is not found and the factory is not null, should a new one be created using it?
     * @return
     *      The identity associated to the criterion or null or a new identity if it doesn't exist.
     */
    Identifiable findIdentity(T criteria, IdentityFactory factory);

}