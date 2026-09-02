package caitlyn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Provides a JavaFX chat window for interacting with Caitlyn.
 */
public final class CaitlynGui extends Application {
    /** The initial width of the chat window. */
    private static final double WINDOW_WIDTH = 720;

    /** The initial height of the chat window. */
    private static final double WINDOW_HEIGHT = 620;

    /** The text area containing the conversation history. */
    private TextArea transcript;

    /** The field where the user enters a command. */
    private TextField commandInput;

    /** The button that submits the command in the input field. */
    private Button sendButton;

    /** The label showing the number of tasks currently loaded. */
    private Label taskCountLabel;

    /** The tasks shared by all commands in this application session. */
    private List<Task> tasks;

    /** The UI adapter that sends command responses to the transcript. */
    private Ui ui;

    /** Whether the user has ended the current session. */
    private boolean isSessionEnded;

    /** Whether saved task data could not be loaded. */
    private boolean hasLoadingError;

    /**
     * Creates the JavaFX window and initializes a session with saved tasks.
     *
     * @param stage the primary JavaFX stage.
     */
    @Override
    public void start(Stage stage) {
        tasks = loadTasks();
        transcript = createTranscript();
        commandInput = new TextField();
        commandInput.setPromptText("Enter a command, for example: todo read book");
        commandInput.setOnAction(event -> submitCommand());

        sendButton = new Button("Send");
        sendButton.setDefaultButton(true);
        sendButton.setOnAction(event -> submitCommand());

        taskCountLabel = new Label();
        updateTaskCount();

        VBox content = new VBox(12, createHeader(), transcript, createCommandBar(), taskCountLabel);
        content.setPadding(new Insets(18));
        VBox.setVgrow(transcript, Priority.ALWAYS);

        BorderPane root = new BorderPane(content);
        root.setStyle("-fx-background-color: #f5f7fb;");

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setTitle("Caitlyn");
        stage.setMinWidth(520);
        stage.setMinHeight(460);
        stage.setScene(scene);
        stage.show();

        ui = new Ui(this::appendCaitlynMessage);
        ui.showWelcome();
        if (hasLoadingError) {
            ui.showLoadingError();
        }
        commandInput.requestFocus();
    }

    /**
     * Creates the title and short usage hint shown above the transcript.
     *
     * @return the header layout.
     */
    private VBox createHeader() {
        Label title = new Label("Caitlyn");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #243b53;");

        Label subtitle = new Label(
                "Your task assistant · Try todo, deadline, event, list, find, mark, unmark, or delete");
        subtitle.setStyle("-fx-text-fill: #627d98;");
        return new VBox(4, title, subtitle);
    }

    /**
     * Creates the read-only conversation transcript.
     *
     * @return the configured transcript text area.
     */
    private TextArea createTranscript() {
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setFocusTraversable(false);
        textArea.setStyle("-fx-control-inner-background: white; -fx-font-family: 'Menlo'; "
                + "-fx-font-size: 13px; -fx-border-color: #d9e2ec;");
        return textArea;
    }

    /**
     * Creates the command input row.
     *
     * @return the configured command input row.
     */
    private HBox createCommandBar() {
        HBox commandBar = new HBox(8, commandInput, sendButton);
        commandBar.setAlignment(Pos.CENTER);
        HBox.setHgrow(commandInput, Priority.ALWAYS);
        return commandBar;
    }

    /**
     * Loads saved tasks for the session.
     *
     * @return the saved tasks, or an empty list if the saved data is invalid or unreadable.
     */
    private List<Task> loadTasks() {
        try {
            return TaskStorage.load();
        } catch (IOException | IllegalArgumentException exception) {
            hasLoadingError = true;
            return new ArrayList<>();
        }
    }

    /** Submits the current input to Caitlyn and displays the response. */
    private void submitCommand() {
        if (isSessionEnded) {
            return;
        }

        String fullCommand = commandInput.getText().trim();
        if (fullCommand.isEmpty()) {
            return;
        }

        appendUserMessage(fullCommand);
        commandInput.clear();
        ui.showSeparator();
        try {
            Command command = Parser.parse(fullCommand);
            command.execute(tasks, ui);
            if (command.isExit()) {
                endSession();
            }
        } catch (CaitlynException exception) {
            ui.showError(exception.getMessage());
        } finally {
            ui.showSeparator();
            updateTaskCount();
        }
    }

    /** Displays a command as a user message in the transcript. */
    private void appendUserMessage(String command) {
        appendTranscriptLine("You: " + command);
    }

    /** Displays a response line from Caitlyn in the transcript. */
    private void appendCaitlynMessage(String message) {
        String cleanedMessage = message.stripLeading();
        if (!cleanedMessage.isEmpty()) {
            transcript.appendText(cleanedMessage);
            if (!cleanedMessage.endsWith("\n")) {
                transcript.appendText("\n");
            }
        }
        transcript.positionCaret(transcript.getLength());
    }

    /** Appends one visually separated line to the transcript. */
    private void appendTranscriptLine(String line) {
        if (transcript.getLength() > 0) {
            transcript.appendText("\n");
        }
        transcript.appendText(line + "\n");
        transcript.positionCaret(transcript.getLength());
    }

    /** Updates the task count shown below the command input. */
    private void updateTaskCount() {
        if (taskCountLabel != null && tasks != null) {
            taskCountLabel.setText(tasks.size() + (tasks.size() == 1 ? " task" : " tasks") + " saved");
        }
    }

    /** Disables command input after the user enters {@code bye}. */
    private void endSession() {
        isSessionEnded = true;
        commandInput.setDisable(true);
        sendButton.setDisable(true);
        taskCountLabel.setText("Session ended");
    }
}
