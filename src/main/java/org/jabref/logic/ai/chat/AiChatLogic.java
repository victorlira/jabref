package org.jabref.logic.ai.chat;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jabref.logic.ai.AiService;
import org.jabref.logic.ai.chathistory.ChatMessage;
import org.jabref.preferences.AiPreferences;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.filter.Filter;

/**
 * Wrapper around langchain4j algorithms for chatting functionality.
 * It also supports filtering of documents that are used for question answering.
 * <p>
 * This class listens to chat language model change.
 * <p>
 * Notice: this class does not manage the chat history.
 * You should add messages to history on your own to {@link org.jabref.logic.ai.chathistory.BibDatabaseChatHistory}.
 */
public class AiChatLogic {
    private final AiService aiService;

    private final Filter filter;

    private ConversationalRetrievalChain chain;
    private ChatMemory chatMemory;

    public AiChatLogic(AiService aiService, Filter filter) {
        this.aiService = aiService;
        this.filter = filter;

        rebuild();

        setupListeningToPreferencesChanges();
    }

    private void setupListeningToPreferencesChanges() {
        aiService.getChatLanguageModel().chatLanguageModelObjectProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.isPresent()) {
                rebuild();
            }
        });

        aiService.getEmbeddingModel().embeddingModelObjectProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                rebuild();
            }
        });

        AiPreferences aiPreferences = aiService.getPreferences();

        aiPreferences.instructionProperty().addListener(obs -> {
            rebuild();
        });

        aiPreferences.messageWindowSizeProperty().addListener(obs -> {
            rebuild();
        });

        aiPreferences.onEmbeddingsParametersChange(this::rebuild);
    }

    private void rebuild() {
        // When the user turns off the AI features all AiChat classes should be destroyed.
        // So this assert should never fail.
        assert aiService.getChatLanguageModel().getChatLanguageModel().isPresent();

        List<dev.langchain4j.data.message.ChatMessage> oldMessages;
        if (chatMemory == null) {
            oldMessages = List.of();
        } else {
            oldMessages = chatMemory.messages();
        }

        AiPreferences aiPreferences = aiService.getPreferences();

        this.chatMemory = MessageWindowChatMemory
                .builder()
                .maxMessages(aiPreferences.getMessageWindowSize())
                .build();

        oldMessages.forEach(message -> chatMemory.add(message));

        ContentRetriever contentRetirever = EmbeddingStoreContentRetriever
                .builder()
                .embeddingStore(aiService.getEmbeddingsManager().getEmbeddingsStore())
                .filter(filter)
                .embeddingModel(aiService.getEmbeddingModel().getEmbeddingModel())
                .maxResults(aiPreferences.getRagMaxResultsCount())
                .minScore(aiPreferences.getRagMinScore())
                .build();

        this.chain = ConversationalRetrievalChain
                .builder()
                .chatLanguageModel(aiService.getChatLanguageModel().getChatLanguageModel().get())
                .contentRetriever(contentRetirever)
                .chatMemory(chatMemory)
                .build();

        if (!aiPreferences.getInstruction().isEmpty()) {
            this.chatMemory.add(new SystemMessage(aiPreferences.getInstruction()));
        }
    }

    public String execute(String prompt) {
        // chain.execute() will automatically add messages to ChatMemory.
        return chain.execute(prompt);
    }

    public void restoreMessages(Stream<ChatMessage> messages) {
        messages.map(ChatMessage::toLangchainMessage).filter(Optional::isPresent).map(Optional::get).forEach(this.chatMemory::add);
    }
}
