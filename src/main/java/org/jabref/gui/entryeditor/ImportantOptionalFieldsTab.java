package org.jabref.gui.entryeditor;

import javax.swing.undo.UndoManager;

import org.jabref.gui.DialogService;
import org.jabref.gui.StateManager;
import org.jabref.gui.autocompleter.SuggestionProviders;
import org.jabref.gui.theme.ThemeManager;
import org.jabref.gui.undo.RedoAction;
import org.jabref.gui.undo.UndoAction;
import org.jabref.gui.util.TaskExecutor;
import org.jabref.logic.ai.AiEmbeddingsTaskManager;
import org.jabref.logic.journals.JournalAbbreviationRepository;
import org.jabref.logic.l10n.Localization;
import org.jabref.logic.pdf.search.IndexingTaskManager;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntryTypesManager;
import org.jabref.preferences.PreferencesService;

public class ImportantOptionalFieldsTab extends OptionalFieldsTabBase {

    public static final String NAME = "Optional fields";

    public ImportantOptionalFieldsTab(BibDatabaseContext databaseContext,
                                      SuggestionProviders suggestionProviders,
                                      UndoManager undoManager,
                                      UndoAction undoAction,
                                      RedoAction redoAction,
                                      DialogService dialogService,
                                      PreferencesService preferences,
                                      StateManager stateManager,
                                      ThemeManager themeManager,
                                      IndexingTaskManager indexingTaskManager,
                                      AiEmbeddingsTaskManager aiEmbeddingsTaskManager,
                                      BibEntryTypesManager entryTypesManager,
                                      TaskExecutor taskExecutor,
                                      JournalAbbreviationRepository journalAbbreviationRepository) {
        super(
                Localization.lang("Optional fields"),
                true,
                databaseContext,
                suggestionProviders,
                undoManager,
                undoAction,
                redoAction,
                dialogService,
                preferences,
                stateManager,
                themeManager,
                indexingTaskManager,
                aiEmbeddingsTaskManager,
                entryTypesManager,
                taskExecutor,
                journalAbbreviationRepository
        );
    }
}
