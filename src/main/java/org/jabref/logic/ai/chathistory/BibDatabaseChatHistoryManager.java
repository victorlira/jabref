package org.jabref.logic.ai.chathistory;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import org.jabref.gui.StateManager;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.event.FieldChangedEvent;
import org.jabref.model.entry.field.InternalField;

import com.airhacks.afterburner.injection.Injector;
import com.google.common.eventbus.Subscribe;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.UserMessage;
import org.h2.mvstore.MVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class stores the chat history with AI for .bib files. The chat history file is stored in a user local folder.
 * <p>
 * It uses MVStore for serializing the messages. In case any error occurs while opening the MVStore,
 * the class will notify the user of this error and continue with in-memory store (meaning all messages will
 * be thrown away on exit).
 *
 * @implNote If something is changed in the data model, increase {@link org.jabref.logic.ai.AiService#VERSION}
 */
public class BibDatabaseChatHistoryManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(BibDatabaseChatHistoryManager.class);

    private final StateManager stateManager = Injector.instantiateModelOrService(StateManager.class);

    private record ChatHistoryRecord(ChatMessageType type, String content) implements Serializable {
        public ChatMessage toLangchainMessage() {
            if (type == ChatMessageType.AI) {
                return new AiMessage(content);
            } else if (type == ChatMessageType.USER) {
                return new UserMessage(content);
            } else {
                LOGGER.warn("BibDatabaseChatHistoryManager supports only AI and user messages, but retrieved message has other type: {}. Will treat as an AI message", type);
                return new AiMessage(content);
            }
        }
    }

    private final MVStore mvStore;

    public BibDatabaseChatHistoryManager(MVStore mvStore) {
        this.mvStore = mvStore;
    }

    private Map<Integer, ChatHistoryRecord> getMap(Path bibDatabasePath, String citationKey) {
        return mvStore.openMap("chathistory-" + bibDatabasePath + "-" + citationKey);
    }

    public AiChatHistory getChatHistory(Path bibDatabasePath, String citationKey) {
        return new AiChatHistory() {
            @Override
            public List<ChatMessage> getMessages() {
                Map<Integer, ChatHistoryRecord> messages = getMap(bibDatabasePath, citationKey);
                return IntStream.range(0, messages.size())
                                .mapToObj(key -> messages.get(key).toLangchainMessage())
                                .toList();
            }

            @Override
            public void add(ChatMessage chatMessage) {
                Map<Integer, ChatHistoryRecord> map = getMap(bibDatabasePath, citationKey);

                // We count 0-based, thus "size()" is the next number
                // 0 entries -> 0 is the first new id
                // 1 entry -> 0 is assigned, 1 is the next number, which is also the size
                int id = map.keySet().size();

                String content;

                if (chatMessage instanceof AiMessage aiMessage) {
                    content = aiMessage.text();
                } else if (chatMessage instanceof UserMessage userMessage) {
                    content = userMessage.singleText();
                } else {
                    LOGGER.warn("BibDatabaseChatHistoryFile supports only AI and user messages, but added message has other type: {}", chatMessage.type().name());
                    return;
                }

                map.put(id, new ChatHistoryRecord(chatMessage.type(), content));
            }

            @Override
            public void clear() {
                getMap(bibDatabasePath, citationKey).clear();
            }
        };
    }

    @Subscribe
    private void fieldChangedEventListener(FieldChangedEvent event) {
        // TODO: This methods doesn't take into account if the new citation key is valid.

        if (event.getField() != InternalField.KEY_FIELD) {
            return;
        }

        Optional<BibDatabaseContext> bibDatabaseContext = stateManager.getOpenDatabases().stream().filter(dbContext -> dbContext.getDatabase().getEntries().contains(event.getBibEntry())).findFirst();

        if (bibDatabaseContext.isEmpty()) {
            LOGGER.error("Could not listen to field change event because no database context was found. BibEntry: {}", event.getBibEntry());
            return;
        }

        Optional<Path> bibDatabasePath = bibDatabaseContext.get().getDatabasePath();

        if (bibDatabasePath.isEmpty()) {
            LOGGER.error("Could not listen to field change event because no database path was found. BibEntry: {}", event.getBibEntry());
            return;
        }

        Map<Integer, ChatHistoryRecord> oldMap = getMap(bibDatabasePath.get(), event.getOldValue());
        getMap(bibDatabasePath.get(), event.getNewValue()).putAll(oldMap);
        oldMap.clear();
    }
}