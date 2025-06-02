package io.github.broskipoker.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import io.github.broskipoker.server.ClientConnection;
import io.github.broskipoker.shared.GameStateUpdate;
import io.github.broskipoker.shared.PlayerInfo;

import java.util.ArrayList;
import java.util.List;

public class LobbyPanel extends Table {
    private List<String> playersList = new ArrayList<>();
    private Table playersTable;
    private TextButton startButton;
    private ClientConnection clientConnection;
    private Runnable onStartGame;
    private boolean isHost;

    public LobbyPanel(Skin skin, String tableCode, ClientConnection clientConnection, boolean isHost) {
        super(skin);
        this.clientConnection = clientConnection;
        this.isHost = isHost;

        // Set background
        setBackground(skin.getDrawable("window"));

        // Position on the left side
        setPosition(100, 300);

        // Add padding inside the panel
        pad(15);

        // Set a fixed width for the panel
        setWidth(220);

        // Create the UI elements
        createLobbyUI(tableCode);
    }

    private void createLobbyUI(String tableCode) {
        // Game code display
        add(new Label("GAME CODE:", getSkin())).left().padBottom(5);
        row();

        Label codeLabel = new Label(tableCode, getSkin());
        codeLabel.setFontScale(1.2f);
        add(codeLabel).left().padBottom(15);
        row();

        add(new Label("PLAYERS:", getSkin())).left().padBottom(5);
        row();

        // Players list
        playersTable = new Table();
        updatePlayersList();
        ScrollPane scrollPane = new ScrollPane(playersTable, getSkin());
        scrollPane.setFadeScrollBars(false);
        add(scrollPane).width(180).height(200).padBottom(15);
        row();

        // Buttons
        startButton = new TextButton("Start Game", getSkin());
        startButton.setDisabled(!isHost);
        add(startButton).width(180).padBottom(10);
        row();

        TextButton leaveButton = new TextButton("Leave Lobby", getSkin());
        add(leaveButton).width(180);

        // Add listeners
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
                if (clientConnection != null) {
                    clientConnection.disconnect();
                }
                remove(); // Remove from parent
            }
        });
    }

    public void updatePlayersList() {
        playersTable.clear();

        // If no players yet, show waiting message
        if (playersList.isEmpty()) {
            playersTable.add(new Label("Waiting for players...", getSkin())).left().padBottom(5);
        } else {
            for (String player : playersList) {
                playersTable.add(new Label(player, getSkin())).left().padBottom(5);
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

    public void setPlayers(List<String> players) {
        this.playersList.clear();
        this.playersList.addAll(players);
        updatePlayersList();
    }

    public void setOnStartGame(Runnable onStartGame) {
        this.onStartGame = onStartGame;
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
}
