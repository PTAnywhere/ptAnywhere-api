package uk.ac.open.kmi.forge.ptAnywhere.analytics.tincanapi;


import com.rusticisoftware.tincan.Statement;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.vocab.BaseVocabulary;

import java.util.ArrayList;
import java.util.List;


/**
 * PT's command line generates a lot of output events which result in a lot of statements
 * being recorded.
 *
 * However, most of these statements can be joined together without loosing any meaning.
 * Joining them, we can enhance their readability by humans and simplify clients (less
 * statements to process).
 */
public class StatementConsolidator {

    public static List<Statement>  consolidate(List<Statement> stmts) {
        final List<Statement> ret = new ArrayList<>();
        Statement newStmt = null;
        for (Statement toCheck: stmts) {
            if (toCheck.getVerb().getId().toString().equals(BaseVocabulary.READ)) {
                if (newStmt==null) {
                    newStmt = toCheck;
                } else {
                    // TODO There should not be a huge difference, but which DateTime should have the consolidated Stmt?
                    final String consolidatedResponse = newStmt.getResult().getResponse() + toCheck.getResult().getResponse();
                    // Assumption: the rest of elements of both statements are equal.
                    newStmt.getResult().setResponse( consolidatedResponse );
                }
            } else {
                if (newStmt!=null) {
                    ret.add(newStmt);  // Adds previous consolidated statement
                    newStmt = null;  // Clear for the next possible consolidation
                }
                ret.add(toCheck);
            }
        }
        if (newStmt!=null) {
            ret.add(newStmt);  // Add last consolidated statement
        }
        return ret;
    }

}
