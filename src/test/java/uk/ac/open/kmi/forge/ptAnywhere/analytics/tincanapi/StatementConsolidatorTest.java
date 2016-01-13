package uk.ac.open.kmi.forge.ptAnywhere.analytics.tincanapi;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import java.util.List;
import java.util.ArrayList;
import java.net.URISyntaxException;
import com.rusticisoftware.tincan.Result;
import com.rusticisoftware.tincan.Statement;
import com.rusticisoftware.tincan.Verb;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.vocab.BaseVocabulary;


public class StatementConsolidatorTest {
    protected Statement createStatement(String verb, String response) throws URISyntaxException {
        final Statement stmt = new Statement();
        stmt.setVerb(new Verb(verb));
        final Result r = new Result();
        r.setResponse(response);
        stmt.setResult(r);
        return stmt;
    }

    @Test
    public void testConsolidateBeginning() throws URISyntaxException {
        final List<Statement> stmts = new ArrayList<>();
        stmts.add(createStatement(BaseVocabulary.READ, "out1"));
        stmts.add(createStatement(BaseVocabulary.READ, "out2"));
        stmts.add(createStatement(BaseVocabulary.READ, "out3"));
        stmts.add(createStatement("verb3", "out2"));

        final List<Statement> expected = new ArrayList<>();
        expected.add(createStatement(BaseVocabulary.READ, "out1out2out3"));
        expected.add(createStatement("verb3", "out2"));

        assertThat(StatementConsolidator.consolidate(stmts), is(expected));
    }

    @Test
    public void testConsolidateEnding() throws URISyntaxException {
        final List<Statement> stmts = new ArrayList<>();
        stmts.add(createStatement("verb1", "out1"));
        stmts.add(createStatement(BaseVocabulary.READ, "out1"));
        stmts.add(createStatement(BaseVocabulary.READ, "out2"));
        stmts.add(createStatement(BaseVocabulary.READ, "out3"));

        final List<Statement> expected = new ArrayList<>();
        expected.add(createStatement("verb1", "out1"));
        expected.add(createStatement(BaseVocabulary.READ, "out1out2out3"));

        assertThat(StatementConsolidator.consolidate(stmts), is(expected));
    }

    @Test
    public void testConsolidateMiddle() throws URISyntaxException {
        final List<Statement> stmts = new ArrayList<>();
        stmts.add(createStatement("verb1", "out1"));
        stmts.add(createStatement(BaseVocabulary.READ, "out1"));
        stmts.add(createStatement(BaseVocabulary.READ, "out2"));
        stmts.add(createStatement(BaseVocabulary.READ, "out3"));
        stmts.add(createStatement("verb3", "out2"));

        final List<Statement> expected = new ArrayList<>();
        expected.add(createStatement("verb1", "out1"));
        expected.add(createStatement(BaseVocabulary.READ, "out1out2out3"));
        expected.add(createStatement("verb3", "out2"));

        assertThat(StatementConsolidator.consolidate(stmts), is(expected));
    }

    @Test
    public void testConsolidateNoOne() throws URISyntaxException {
        final List<Statement> stmts = new ArrayList<>();
        stmts.add(createStatement("verb1", "out1"));
        stmts.add(createStatement(BaseVocabulary.READ, "out2"));
        stmts.add(createStatement("verb3", "out3"));

        assertThat(StatementConsolidator.consolidate(stmts), is(stmts));
    }
}

