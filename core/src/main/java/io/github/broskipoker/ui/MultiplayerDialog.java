package io.github.broskipoker.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import io.github.broskipoker.server.ClientConnection;
import io.github.broskipoker.utils.UserService;

public class MultiplayerDialog extends Dialog {
    private TextField tableCodeField;
    private Label statusLabel;
    private TextButton joinButton;
    private TextButton createButton;
    private Runnable onJoinTableSuccess;
    private Runnable onCreateTableSuccess;
    private LobbyPanel lobbyPanel;
    private ClientConnection currentClient;

    public MultiplayerDialog(String title, Skin skin) {
        super(title, skin);
        createMultiplayerForm();
    }

    private void createMultiplayerForm() {
        // Create form elements
        tableCodeField = new TextField("", getSkin());
        tableCodeField.setMessageText("Table code");

        statusLabel = new Label("", getSkin());
        statusLabel.setColor(1, 0, 0, 1); // Red color for error messages

        // Create buttons
        joinButton = new TextButton("Join Table", getSkin());
        createButton = new TextButton("Create Table", getSkin());
        TextButton cancelButton = new TextButton("Cancel", getSkin());

        // Layout the dialog
        getContentTable().pad(30); // Add the padding

        // Create a container table for horizontal alignment
        Table inputContainer = new Table();

        // Input label with right alignment
        Label codeLabel = new Label("TABLE CODE:", getSkin());
        codeLabel.setAlignment(com.badlogic.gdx.utils.Align.right);
        inputContainer.add(codeLabel).width(120).padRight(15).right();

        // Input field
        tableCodeField.setAlignment(com.badlogic.gdx.utils.Align.center);
        inputContainer.add(tableCodeField).width(250).height(40).left();

        // Add the container to content table with padding
        getContentTable().add(inputContainer).fillX().padBottom(25).colspan(2);
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

    private void showLobbyAfterCreation(String tableCode, ClientConnection client) {
        // Create lobby panel and add it to the stage
        String clientUsername = UserService.getInstance().getCurrentUserOrThrow().getUsername();
        LobbyPanel lobbyPanel = new LobbyPanel("Game Lobby", getSkin(), tableCode, client, true, clientUsername);
        lobbyPanel.addPlayer(UserService.getInstance().getCurrentUserOrThrow().getUsername());
        client.setLobbyPanel(lobbyPanel); // Set lobby panel in client
        lobbyPanel.setPosition(
            (getStage().getWidth() - lobbyPanel.getWidth()) / 2 - 670,
            (getStage().getHeight() - lobbyPanel.getHeight()) / 2 - 70
        );

        // Set callback when game starts
        lobbyPanel.setOnStartGame(() -> {
            if (client != null) {
                client.requestStartGame();
            }
        });

        // Set callback when leaving
        lobbyPanel.setOnLeaveGame(() -> {
            System.out.println("Leaving lobby with code: " + tableCode);
            lobbyPanel.hide();
        });

        // Store client connection for later use
        this.currentClient = client;

        // Add the panel to the stage
        getStage().addActor(lobbyPanel);
    }

    private void showLobbyAfterJoin(String tableCode, ClientConnection client) {
        // Create lobby panel and add it to the stage (not host)
        String clientUsername = UserService.getInstance().getCurrentUserOrThrow().getUsername();
        LobbyPanel lobbyPanel = new LobbyPanel("Game Lobby", getSkin(), tableCode, client, true, clientUsername);
        lobbyPanel.addPlayer(UserService.getInstance().getCurrentUserOrThrow().getUsername());
        client.setLobbyPanel(lobbyPanel); // Set lobby panel in client
        lobbyPanel.setPosition(
            (getStage().getWidth() - lobbyPanel.getWidth()) / 2 - 670,
            (getStage().getHeight() - lobbyPanel.getHeight()) / 2 - 70
        );


        // Set callback when leaving
        lobbyPanel.setOnLeaveGame(() -> {
            System.out.println("Leaving lobby with code: " + tableCode);
            lobbyPanel.hide();
        });

        // Store client connection for later use
        this.currentClient = client;

        // Add the panel to the stage
        getStage().addActor(lobbyPanel);
    }

    private void handleJoinTable() {
        String tableCode = tableCodeField.getText().trim();

        if (tableCode.isEmpty()) {
            statusLabel.setText("Please enter a table code");
            statusLabel.setColor(1, 0, 0, 1); // Red color for error
            return;
        }

        // Disable buttons during loading
        joinButton.setDisabled(true);
        createButton.setDisabled(true);

        // Initial status
        statusLabel.setText("Joining table...");
        statusLabel.setColor(0.2f, 0.6f, 1f, 1); // Blue color for loading

        // Add loading animation using actions
        statusLabel.clearActions();
        statusLabel.addAction(
            com.badlogic.gdx.scenes.scene2d.actions.Actions.forever(
                com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.run(() -> {
                        String text = statusLabel.getText().toString();
                        if (text.endsWith("...")) {
                            statusLabel.setText("Joining table");
                        } else {
                            statusLabel.setText(text + ".");
                        }
                    }),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(0.5f)
                )
            )
        );

        // Run in a separate thread to avoid blocking UI
        new Thread(() -> {
            ClientConnection client = new ClientConnection(UserService.getInstance().getCurrentUserOrThrow().getUsername());
            try {
                client.connect();
                Thread.sleep(1000); // Network connection delay

                if (!client.isConnected()) {
                    updateUIThreadSafe(() -> {
                        statusLabel.clearActions();
                        statusLabel.setText("Failed to connect to server!");
                        statusLabel.setColor(1, 0, 0, 1);
                        joinButton.setDisabled(false);
                        createButton.setDisabled(false);
                    });
                    return;
                }

                // Attempt to join the table
                client.joinTable(tableCode, 10000);
                Thread.sleep(2000); // Wait for join response

                client.setTableCode(tableCode); // Set the table code in client
                final ClientConnection finalClient = client;

                // Check if join was successful
                if (isJoinSuccessful(client, tableCode)) {
                    updateUIThreadSafe(() -> {
                        statusLabel.clearActions();
                        statusLabel.setText("Successfully joined table!");
                        statusLabel.setColor(0, 1, 0, 1);

                        // Delay hiding the dialog to show success message
                        addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                            com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(1f),
                            com.badlogic.gdx.scenes.scene2d.actions.Actions.run(() -> {
                                hide();
                                showLobbyAfterJoin(tableCode, finalClient);
                                if (onJoinTableSuccess != null) {
                                    onJoinTableSuccess.run();
                                }
                            })
                        ));
                    });
                } else {
                    updateUIThreadSafe(() -> {
                        statusLabel.clearActions();
                        statusLabel.setText("Failed to join table - table not found or full");
                        statusLabel.setColor(1, 0, 0, 1);
                        tableCodeField.setText(""); // Clear code field
                        joinButton.setDisabled(false);
                        createButton.setDisabled(false);
                    });
                }
            } catch (Exception e) {
                updateUIThreadSafe(() -> {
                    statusLabel.clearActions();
                    statusLabel.setText("Failed to join table: " + e.getMessage());
                    statusLabel.setColor(1, 0, 0, 1);
                    tableCodeField.setText(""); // Clear code field
                    joinButton.setDisabled(false);
                    createButton.setDisabled(false);
                });
            }
        }).start();
    }

    private void handleCreateTable() {
        // Disable buttons during loading
        joinButton.setDisabled(true);
        createButton.setDisabled(true);

        // Initial status
        statusLabel.setText("Creating new table...");
        statusLabel.setColor(0.2f, 0.6f, 1f, 1); // Blue color for loading

        // Add loading animation using actions
        statusLabel.clearActions();
        statusLabel.addAction(
            com.badlogic.gdx.scenes.scene2d.actions.Actions.forever(
                com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.run(() -> {
                        String text = statusLabel.getText().toString();
                        if (text.endsWith("...")) {
                            statusLabel.setText("Creating new table");
                        } else {
                            statusLabel.setText(text + ".");
                        }
                    }),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(0.5f)
                )
            )
        );

        // Run in a separate thread to avoid blocking UI
        new Thread(() -> {
            ClientConnection client = new ClientConnection(UserService.getInstance().getCurrentUserOrThrow().getUsername());
            try {
                client.connect();
                Thread.sleep(1000); // Network connection delay

                if (!client.isConnected()) {
                    updateUIThreadSafe(() -> {
                        statusLabel.clearActions();
                        statusLabel.setText("Failed to connect to server!");
                        statusLabel.setColor(1, 0, 0, 1);
                        joinButton.setDisabled(false);
                        createButton.setDisabled(false);
                    });
                    return;
                }

                client.createTable(50, 100, 10000);
                Thread.sleep(3000); // Wait for response

                final ClientConnection finalClient = client;
                updateUIThreadSafe(() -> {
                    statusLabel.clearActions();

                    if (finalClient.getTableCode() != null) {
                        // Success case
                        statusLabel.setText("Table created!");
                        statusLabel.setColor(0, 1, 0, 1);
                        tableCodeField.setText(finalClient.getTableCode());

                        // Replace buttons
                        getButtonTable().clear();
                        TextButton proceedButton = new TextButton("Continue to Lobby", getSkin());
                        TextButton copyButton = new TextButton("Copy Code", getSkin());
                        TextButton cancelButton = new TextButton("Cancel", getSkin());

                        getButtonTable().add(copyButton).padRight(10);
                        getButtonTable().add(proceedButton).padRight(10);
                        getButtonTable().add(cancelButton);

                        copyButton.addListener(new ChangeListener() {
                            @Override
                            public void changed(ChangeEvent event, Actor actor) {
                                com.badlogic.gdx.Gdx.app.getClipboard().setContents(tableCodeField.getText());
                                statusLabel.setText("Code copied to clipboard!");
                                statusLabel.setColor(0, 1, 0, 1);
                            }
                        });

                        proceedButton.addListener(new ChangeListener() {
                            @Override
                            public void changed(ChangeEvent event, Actor actor) {
                                hide();
                                showLobbyAfterCreation(finalClient.getTableCode(), finalClient);
                                if (onCreateTableSuccess != null) {
                                    onCreateTableSuccess.run();
                                }
                            }
                        });

                        cancelButton.addListener(new ChangeListener() {
                            @Override
                            public void changed(ChangeEvent event, Actor actor) {
                                hide();
                            }
                        });
                    } else {
                        statusLabel.setText("Table creation failed or timed out");
                        statusLabel.setColor(1, 0, 0, 1);
                        joinButton.setDisabled(false);
                        createButton.setDisabled(false);
                    }
                });
            } catch (Exception e) {
                updateUIThreadSafe(() -> {
                    statusLabel.clearActions();
                    statusLabel.setText("Failed to connect to server: " + e.getMessage());
                    statusLabel.setColor(1, 0, 0, 1);
                    joinButton.setDisabled(false);
                    createButton.setDisabled(false);
                });
            }
        }).start();
    }

    // Helper method to update UI from background thread
    private void updateUIThreadSafe(Runnable runnable) {
        com.badlogic.gdx.Gdx.app.postRunnable(runnable);
    }

    // Helper method to check if join was successful
    // You'll need to implement this based on your ClientConnection class
    private boolean isJoinSuccessful(ClientConnection client, String tableCode) {
        // This is a placeholder - implement based on your ClientConnection's response
        // For example, you might check if client.getTableCode() equals the requested code
        // or if client has received a successful join response
        return client.isConnected() && client.getTableCode() != null;
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
