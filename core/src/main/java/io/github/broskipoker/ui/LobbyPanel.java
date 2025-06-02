package io.github.broskipoker.ui;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import io.github.broskipoker.server.ClientConnection;
import io.github.broskipoker.shared.GameStateUpdate;
import io.github.broskipoker.shared.PlayerInfo;

import java.util.ArrayList;
import java.util.List;

public class LobbyPanel extends Table {
    private List<String> playersList = new ArrayList<>();
    private Table playersTable;
    private TextButton startButton;
    private TextButton leaveButton;
    private ClientConnection clientConnection;
    private Runnable onStartGame;
    private Runnable onLeaveGame;
    private boolean isHost;
    private Skin skin;

    public LobbyPanel(Skin skin, String tableCode, ClientConnection clientConnection, boolean isHost) {
        super(skin);
        this.skin = skin;
        this.clientConnection = clientConnection;
        this.isHost = isHost;

        // Set table properties
        setFillParent(false);

        // Create the UI elements
        createLobbyUI(tableCode);

        // Position the panel on the left side of the screen
        setPosition(80, 150);
        setSize(250, 400);
    }

    private void createLobbyUI(String tableCode) {
        // Clear any existing content
        clear();

        // Add padding around content
        pad(15);

        // Set background (optional - you can customize this)
        if (skin.has("default-pane", NinePatch.class)) {
            setBackground(skin.getDrawable("default-pane"));
        }

        // Title
        Label titleLabel = new Label("Code: " + tableCode, skin);
        titleLabel.setAlignment(Align.center);
        titleLabel.setFontScale(1.2f);
        add(titleLabel).colspan(2).center().padBottom(15);
        row();

        // Players section
        Label playersLabel = new Label("Players:", skin);
        playersLabel.setFontScale(1.1f);
        add(playersLabel).left();
        row();

        // Players list container
        playersTable = new Table();
        updatePlayersList();

        ScrollPane scrollPane = new ScrollPane(playersTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setOverscroll(false, false);
        add(scrollPane).width(200).height(150).padBottom(20);
        row();

        // Buttons section
        createButtons();
    }

    private void createButtons() {
        // Start button
        startButton = new TextButton("Start Game", skin);
        startButton.setDisabled(!isHost);
        add(startButton).width(180).height(40).padBottom(10);
        row();

        // Leave button
        leaveButton = new TextButton("Leave Lobby", skin);
        add(leaveButton).width(180).height(40);
        row();

        // Add button listeners
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (onStartGame != null) {
                    onStartGame.run();
                }
            }
        });

        leaveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (onLeaveGame != null) {
                    onLeaveGame.run();
                }
                if (clientConnection != null) {
                    clientConnection.disconnect();
                }
            }
        });
    }

    public void updatePlayersList() {
        playersTable.clear();

        // If no players yet, show waiting message
        if (playersList.isEmpty()) {
            Label waitingLabel = new Label("Waiting for players...", skin);
            waitingLabel.setWrap(true);
            playersTable.add(waitingLabel).width(180).left().padBottom(5);
        } else {
            for (int i = 0; i < playersList.size(); i++) {
                String player = playersList.get(i);
                Label playerLabel = new Label((i + 1) + ". " + player, skin);
                playersTable.add(playerLabel).width(180).left().padBottom(3);
                playersTable.row();
            }
        }
    }

    public void addPlayer(String playerName) {
        if (!playersList.contains(playerName)) {
            playersList.add(playerName);
            updatePlayersList();
        }
    }

    public void removePlayer(String playerName) {
        if (playersList.contains(playerName)) {
            playersList.remove(playerName);
            updatePlayersList();
        }
    }

    public void setPlayers(List<String> players) {
        this.playersList.clear();
        this.playersList.addAll(players);
        updatePlayersList();
    }

    public void setOnStartGame(Runnable onStartGame) {
        this.onStartGame = onStartGame;
    }

    public void setOnLeaveGame(Runnable onLeaveGame) {
        this.onLeaveGame = onLeaveGame;
    }

    public void onGameStateUpdate(GameStateUpdate update) {
        // Update player list based on game state
        List<String> updatedPlayers = new ArrayList<>();
        for (PlayerInfo player : update.players) {
            updatedPlayers.add(player.name);
        }
        setPlayers(updatedPlayers);

        // Enable start button if we're host and there are at least 2 players
        if (isHost) {
            startButton.setDisabled(updatedPlayers.size() < 2);
        }
    }

    public void setHost(boolean isHost) {
        this.isHost = isHost;
        if (startButton != null) {
            startButton.setDisabled(!isHost || playersList.size() < 2);
        }
    }

    public void hide() {
        setVisible(false);
    }

    public void show() {
        setVisible(true);
    }

    // Method to position the panel on screen
    public void positionOnScreen(float screenWidth, float screenHeight) {
        setPosition(10, screenHeight - getHeight() - 10);
    }
}
