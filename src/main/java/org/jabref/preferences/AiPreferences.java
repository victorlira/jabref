package org.jabref.preferences;

import java.util.List;
import java.util.Objects;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.jabref.gui.entryeditor.aichattab.AiChatTab;

public class AiPreferences {
    public static final List<String> OPENAI_CHAT_MODELS = List.of(
            "gpt-3.5-turbo",
            "gpt-4",
            "gpt-4-turbo",
            "gpt-4o"
    );

    public enum EmbeddingModel {
        ALL_MINILM_L6_V2("all-MiniLM-L6-v2"),
        ALL_MINILM_L6_V2_Q("all-MiniLM-L6-v2-q");

        private final String label;

        EmbeddingModel(String label) {
            this.label = label;
        }

        public String getLabel() {
             return label;
        }

        public String toString() {
            return label;
        }
    }

    private final BooleanProperty enableChatWithFiles;
    private final StringProperty openAiToken;

    private final BooleanProperty customizeExpertSettings;

    private final StringProperty chatModel;
    private final StringProperty apiBaseUrl;
    private final ObjectProperty<EmbeddingModel> embeddingModel;

    private final StringProperty instruction;
    private final DoubleProperty temperature;
    private final IntegerProperty contextWindowSize;
    private final IntegerProperty documentSplitterChunkSize;
    private final IntegerProperty documentSplitterOverlapSize;
    private final IntegerProperty ragMaxResultsCount;
    private final DoubleProperty ragMinScore;

    public AiPreferences(boolean enableChatWithFiles, String openAiToken, String chatModel, String apiBaseUrl, EmbeddingModel embeddingModel, boolean customizeExpertSettings, String instruction, double temperature, int contextWindowSize, int documentSplitterChunkSize, int documentSplitterOverlapSize, int ragMaxResultsCount, double ragMinScore) {
        this.enableChatWithFiles = new SimpleBooleanProperty(enableChatWithFiles);
        this.openAiToken = new SimpleStringProperty(openAiToken);

        this.customizeExpertSettings = new SimpleBooleanProperty(customizeExpertSettings);

        this.chatModel = new SimpleStringProperty(chatModel);
        this.apiBaseUrl = new SimpleStringProperty(apiBaseUrl);
        this.embeddingModel = new SimpleObjectProperty<>(embeddingModel);

        this.instruction = new SimpleStringProperty(instruction);
        this.temperature = new SimpleDoubleProperty(temperature);
        this.contextWindowSize = new SimpleIntegerProperty(contextWindowSize);
        this.documentSplitterChunkSize = new SimpleIntegerProperty(documentSplitterChunkSize);
        this.documentSplitterOverlapSize = new SimpleIntegerProperty(documentSplitterOverlapSize);
        this.ragMaxResultsCount = new SimpleIntegerProperty(ragMaxResultsCount);
        this.ragMinScore = new SimpleDoubleProperty(ragMinScore);
    }

    public BooleanProperty enableChatWithFilesProperty() {
        return enableChatWithFiles;
    }

    public boolean getEnableChatWithFiles() {
        return enableChatWithFiles.get();
    }

    public void setEnableChatWithFiles(boolean enableChatWithFiles) {
        this.enableChatWithFiles.set(enableChatWithFiles);
    }

    public StringProperty openAiTokenProperty() {
        return openAiToken;
    }

    public String getOpenAiToken() {
        return openAiToken.get();
    }

    public void setOpenAiToken(String openAiToken) {
        this.openAiToken.set(openAiToken);
    }

    public BooleanProperty customizeExpertSettingsProperty() {
        return customizeExpertSettings;
    }

    public boolean getCustomizeExpertSettings() {
        return customizeExpertSettings.get();
    }

    public void setCustomizeExpertSettings(boolean customizeExpertSettings) {
        this.customizeExpertSettings.set(customizeExpertSettings);
    }

    public StringProperty chatModelProperty() {
        return chatModel;
    }

    public String getChatModel() {
        return chatModel.get();
    }

    public void setChatModel(String chatModel) {
        this.chatModel.set(chatModel);
    }

    public ObjectProperty<EmbeddingModel> embeddingModelProperty() {
        return embeddingModel;
    }

    public EmbeddingModel getEmbeddingModel() {
        return embeddingModel.get();
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl.set(apiBaseUrl);
    }

    public StringProperty apiBaseUrlProperty() {
        return apiBaseUrl;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl.get();
    }

    public void setEmbeddingModel(EmbeddingModel embeddingModel) {
        this.embeddingModel.set(embeddingModel);
    }

    public StringProperty instructionProperty() {
        return instruction;
    }

    public String getInstruction() {
        return instruction.get();
    }

    public void setInstruction(String instruction) {
        this.instruction.set(instruction);
    }

    public DoubleProperty temperatureProperty() {
        return temperature;
    }

    public double getTemperature() {
        return temperature.get();
    }

    public void setTemperature(double temperature) {
        this.temperature.set(temperature);
    }

    public IntegerProperty contextWindowSizeProperty() {
        return contextWindowSize;
    }

    public int getContextWindowSize() {
        return contextWindowSize.get();
    }

    public void setContextWindowSize(int contextWindowSize) {
        this.contextWindowSize.set(contextWindowSize);
    }

    public IntegerProperty documentSplitterChunkSizeProperty() {
        return documentSplitterChunkSize;
    }

    public int getDocumentSplitterChunkSize() {
        return documentSplitterChunkSize.get();
    }

    public void setDocumentSplitterChunkSize(int documentSplitterChunkSize) {
        this.documentSplitterChunkSize.set(documentSplitterChunkSize);
    }

    public IntegerProperty documentSplitterOverlapSizeProperty() {
        return documentSplitterOverlapSize;
    }

    public int getDocumentSplitterOverlapSize() {
        return documentSplitterOverlapSize.get();
    }

    public void setDocumentSplitterOverlapSize(int documentSplitterOverlapSize) {
        this.documentSplitterOverlapSize.set(documentSplitterOverlapSize);
    }

    public IntegerProperty ragMaxResultsCountProperty() {
        return ragMaxResultsCount;
    }

    public int getRagMaxResultsCount() {
        return ragMaxResultsCount.get();
    }

    public void setRagMaxResultsCount(int ragMaxResultsCount) {
        this.ragMaxResultsCount.set(ragMaxResultsCount);
    }

    public DoubleProperty ragMinScoreProperty() {
        return ragMinScore;
    }

    public double getRagMinScore() {
        return ragMinScore.get();
    }

    public void setRagMinScore(double ragMinScore) {
        this.ragMinScore.set(ragMinScore);
    }

    /**
     * Listen to changes of preferences that are related to embeddings generation.
     *
     * @param runnable The runnable that should be executed when the preferences change.
     */
    public void onEmbeddingsParametersChange(Runnable runnable) {
        embeddingModel.addListener((observableValue, oldValue, newValue) -> {
            if (newValue != oldValue) {
                runnable.run();
            }
        });

        documentSplitterChunkSize.addListener((observableValue, oldValue, newValue) -> {
            if (!Objects.equals(newValue, oldValue)) {
                runnable.run();
            }
        });

        documentSplitterOverlapSize.addListener((observableValue, oldValue, newValue) -> {
            if (!Objects.equals(newValue, oldValue)) {
                runnable.run();
            }
        });
    }

    /**
     * Listen to all changes of preferences related to AI.
     * This method is used in {@link AiChatTab} to update itself when preferences change.
     * JabRef would close the entry editor, but the last selected entry editor is not refreshed.
     *
     * @param runnable The runnable that should be executed when the preferences change.
     */
    public void onAnyParametersChange(Runnable runnable) {
        enableChatWithFiles.addListener((observableValue, oldValue, newValue) -> {
            if (oldValue != newValue) {
                runnable.run();
            }
        });

        openAiToken.addListener((observableValue, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                runnable.run();
            }
        });

        customizeExpertSettings.addListener((observableValue, oldValue, newValue) -> {
            if (oldValue != newValue) {
                runnable.run();
            }
        });

        chatModel.addListener((observableValue, oldValue, newValue) -> {
            if (oldValue != newValue) {
                runnable.run();
            }
        });

        embeddingModel.addListener((observableValue, oldValue, newValue) -> {
            if (oldValue != newValue) {
                runnable.run();
            }
        });

        instruction.addListener((observableValue, oldValue, newValue) -> {
            if (!oldValue.equals(newValue)) {
                runnable.run();
            }
        });

        temperature.addListener((observableValue, oldValue, newValue) -> {
            if (!oldValue.equals(newValue)) {
                runnable.run();
            }
        });

        contextWindowSize.addListener((observableValue, oldValue, newValue) -> {
            if (!oldValue.equals(newValue)) {
                runnable.run();
            }
        });

        onEmbeddingsParametersChange(runnable);

        ragMaxResultsCount.addListener((observableValue, oldValue, newValue) -> {
            if (!oldValue.equals(newValue)) {
                runnable.run();
            }
        });

        ragMinScore.addListener((observableValue, oldValue, newValue) -> {
            if (!oldValue.equals(newValue)) {
                runnable.run();
            }
        });
    }
}
