package org.jabref.model.search.rules;

import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.pdf.search.PdfSearchResults;

public interface SearchRule {

    boolean applyRule(String query, BibEntry bibEntry);

    boolean applyRule(String query, BibEntry bibEntry, BibDatabaseContext bibDatabaseContext);

    PdfSearchResults getFulltextResults(String query, BibEntry bibEntry, BibDatabaseContext bibDatabaseContext);

    boolean validateSearchStrings(String query);

    boolean validateSearchStrings(String query, BibDatabaseContext bibDatabaseContext);
}
