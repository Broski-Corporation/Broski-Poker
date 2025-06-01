package io.github.broskipoker.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class MultiplayerDialog extends Dialog {
    private TextField tableCodeField;
    private Label statusLabel;
    private TextButton joinButton;
    private TextButton createButton;
    private Runnable onJoinTableSuccess;
    private Runnable onCreateTableSuccess;

    public MultiplayerDialog(String title, Skin skin) {
        super(title, skin);
        createMultiplayerForm();
    }

    private void createMultiplayerForm() {
        // Create form elements
        tableCodeField = new TextField("", getSkin());
        tableCodeField.setMessageText("Enter table code");

        statusLabel = new Label("", getSkin());
        statusLabel.setColor(1, 0, 0, 1); // Red color for error messages

        // Create buttons
        joinButton = new TextButton("Join Table", getSkin());
        createButton = new TextButton("Create Table", getSkin());
        TextButton cancelButton = new TextButton("Cancel", getSkin());

        // Layout the dialog
        getContentTable().pad(30); // Add the padding
        getContentTable().add(new Label("Table Code:", getSkin())).padRight(10);
        getContentTable().add(tableCodeField).width(250).padBottom(20);
        getContentTable().row();

        getContentTable().add(statusLabel).colspan(2).padBottom(20);
        getContentTable().row();

        // Add buttons to button table
        getButtonTable().pad(20); // Buttons padding
        getButtonTable().add(joinButton).padRight(10).minWidth(100);
        getButtonTable().add(createButton).padRight(10).minWidth(100);
        getButtonTable().add(cancelButton).minWidth(100);

        // Add listeners
        joinButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                handleJoinTable();
            }
        });

        createButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                handleCreateTable();
            }
        });

        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });

        // Set dialog properties
        setModal(true);
        setMovable(true);
        setResizable(false);

        // Center the title
        getTitleLabel().setAlignment(com.badlogic.gdx.utils.Align.center);
    }

    private void handleJoinTable() {
        String tableCode = tableCodeField.getText().trim();

        if (tableCode.isEmpty()) {
            statusLabel.setText("Please enter a table code");
            statusLabel.setColor(1, 0, 0, 1); // Red color for error
            return;
        }

        // TODO: Implement actual table joining logic here
        // For now, we'll simulate validation
        if (isValidTableCode(tableCode)) {
            statusLabel.setText("Joining table...");
            statusLabel.setColor(0, 1, 0, 1); // Green color for success

            // Delay hiding the dialog to show success message
            addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(1f),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.run(() -> {
                    hide();
                    if (onJoinTableSuccess != null) {
                        onJoinTableSuccess.run();
                    }
                })
            ));
        } else {
            statusLabel.setText("Invalid table code");
            statusLabel.setColor(1, 0, 0, 1); // Red color for error
            tableCodeField.setText(""); // Clear code field
        }
    }

    private void handleCreateTable() {
        statusLabel.setText("Creating new table...");
        statusLabel.setColor(0, 1, 0, 1); // Green color for success

        // TODO: Implement actual table creation logic here
        // For now, we'll simulate table creation
        String newTableCode = generateTableCode();

        // Show the generated table code
        statusLabel.setText("Table created! Code: " + newTableCode);
        tableCodeField.setText(newTableCode);

        // Delay hiding the dialog to show success message
        addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
            com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(2f),
            com.badlogic.gdx.scenes.scene2d.actions.Actions.run(() -> {
                hide();
                if (onCreateTableSuccess != null) {
                    onCreateTableSuccess.run();
                }
            })
        ));
    }

    // TODO: Replace with actual validation logic
    private boolean isValidTableCode(String code) {
        // Simple validation - you should replace this with actual network call
        return code.length() >= 4 && code.matches("[A-Z0-9]+");
    }

    // TODO: Replace with actual table code generation
    private String generateTableCode() {
        // Simple random code generation - you should replace this with server-generated code
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return code.toString();
    }

    public void setOnJoinTableSuccess(Runnable callback) {
        this.onJoinTableSuccess = callback;
    }

    public void setOnCreateTableSuccess(Runnable callback) {
        this.onCreateTableSuccess = callback;
    }

    @Override
    public void hide() {
        super.hide();
        // Clear fields when dialog is hidden
        tableCodeField.setText("");
        statusLabel.setText("");
        statusLabel.setColor(1, 0, 0, 1);
    }
}
