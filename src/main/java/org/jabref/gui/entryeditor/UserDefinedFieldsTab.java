package org.jabref.gui.entryeditor;

import java.util.LinkedHashSet;
import java.util.SequencedSet;
import java.util.Set;

import javax.swing.undo.UndoManager;

import org.jabref.gui.DialogService;
import org.jabref.gui.StateManager;
import org.jabref.gui.autocompleter.SuggestionProviders;
import org.jabref.gui.icon.IconTheme;
import org.jabref.gui.theme.ThemeManager;
import org.jabref.gui.util.TaskExecutor;
import org.jabref.logic.journals.JournalAbbreviationRepository;
import org.jabref.logic.search.LuceneManager;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.field.Field;
import org.jabref.preferences.PreferencesService;

public class UserDefinedFieldsTab extends FieldsEditorTab {
    private final LinkedHashSet<Field> fields;

    public UserDefinedFieldsTab(String name,
                                Set<Field> fields,
                                BibDatabaseContext databaseContext,
                                SuggestionProviders suggestionProviders,
                                UndoManager undoManager,
                                DialogService dialogService,
                                PreferencesService preferences,
                                StateManager stateManager,
                                ThemeManager themeManager,
                                TaskExecutor taskExecutor,
                                JournalAbbreviationRepository journalAbbreviationRepository,
                                LuceneManager luceneManager) {
        super(false, databaseContext, suggestionProviders, undoManager, dialogService, preferences, stateManager, themeManager, taskExecutor, journalAbbreviationRepository, luceneManager);

        this.fields = new LinkedHashSet<>(fields);

        setText(name);
        setGraphic(IconTheme.JabRefIcons.OPTIONAL.getGraphicNode());
    }

    @Override
    protected SequencedSet<Field> determineFieldsToShow(BibEntry entry) {
        return fields;
    }
}
