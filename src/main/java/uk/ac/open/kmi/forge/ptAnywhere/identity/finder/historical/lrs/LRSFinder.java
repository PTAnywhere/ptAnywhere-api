package uk.ac.open.kmi.forge.ptAnywhere.identity.finder.historical.lrs;

import com.rusticisoftware.tincan.RemoteLRS;
import com.rusticisoftware.tincan.TCAPIVersion;
import com.rusticisoftware.tincan.lrsresponses.StatementsResultLRSResponse;
import com.rusticisoftware.tincan.v10x.StatementsQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.vocab.BaseVocabulary;
import uk.ac.open.kmi.forge.ptAnywhere.api.http.Utils;
import uk.ac.open.kmi.forge.ptAnywhere.identity.Identifiable;
import uk.ac.open.kmi.forge.ptAnywhere.identity.factories.IdentityFactory;
import uk.ac.open.kmi.forge.ptAnywhere.identity.finder.IdentityFinder;
import uk.ac.open.kmi.forge.ptAnywhere.identity.finder.historical.BySessionId;
import uk.ac.open.kmi.forge.ptAnywhere.properties.InteractionRecordingProperties;
import java.net.MalformedURLException;
import java.net.URISyntaxException;


/**
 * Check the user of an statement.
 */
public class LRSFinder implements IdentityFinder<BySessionId> {

    private static final Log LOGGER = LogFactory.getLog(LRSFinder.class);

    final InteractionRecordingProperties properties;

    public LRSFinder(InteractionRecordingProperties properties) {
        this.properties = properties;
    }

    public Identifiable findIdentity(BySessionId criteria) {
        return findIdentity(criteria, null);
    }

    public Identifiable findIdentity(BySessionId criteria, IdentityFactory factory) {
        Identifiable ret = null;
        try {
            final RemoteLRS lrs = new RemoteLRS();
            lrs.setEndpoint(this.properties.getEndpoint());
            lrs.setVersion(TCAPIVersion.V100);
            lrs.setUsername(this.properties.getUsername());
            lrs.setPassword(this.properties.getPassword());

            final StatementsQuery query = new StatementsQuery();
            query.setRegistration(Utils.toUUID(criteria.getPreviousSessionId()));
            query.setVerbID(BaseVocabulary.INITIALIZED);
            final StatementsResultLRSResponse lrsRes = lrs.queryStatements(query);

            if (lrsRes.getSuccess()) {
                if (lrsRes.getContent().getStatements().size() > 0) {
                    ret = new HistoricalIdentity(lrsRes.getContent().getStatements().get(0).getActor());
                } else {
                    if (factory!=null) {
                        ret = factory.create();
                    }
                }
            }
            return null;
        } catch(MalformedURLException |URISyntaxException e) {
            LOGGER.error(e);
        } finally {
            return ret;
        }
    }
}
