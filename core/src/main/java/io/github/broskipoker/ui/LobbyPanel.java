package io.github.broskipoker.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import io.github.broskipoker.server.ClientConnection;
import io.github.broskipoker.shared.GameStateUpdate;
import io.github.broskipoker.shared.PlayerInfo;

import java.util.ArrayList;
import java.util.List;

public class LobbyPanel extends Dialog {
    private List<String> players = new ArrayList<>();
    private Label[] playerLabels = new Label[5];
    private TextButton startButton;
    private ClientConnection clientConnection;
    private Runnable onStartGame;
    private Runnable onLeaveGame;
    private boolean isHost;

    public LobbyPanel(String title, Skin skin, String tableCode, ClientConnection clientConnection, boolean isHost) {
        super(title, skin);
        this.clientConnection = clientConnection;
        this.isHost = isHost;

        setupDialog();
        clientConnection.addGameStateListener(this::onGameStateUpdate);
        buildUI(tableCode);
    }

    private void setupDialog() {
        setModal(false);
        setMovable(true);
        setResizable(false);
        setSize(400, 450);
        getTitleLabel().setAlignment(Align.center);
    }

    private void buildUI(String tableCode) {
        Table content = getContentTable();
        content.pad(20);

        // Table code
        content.add(new Label("CODE: " + tableCode, getSkin())).center().padBottom(30);
        content.row();

        // Players section
        content.add(new Label("PLAYERS:", getSkin())).center();
        content.row();

        // Create player labels
        Table playersTable = new Table();
        for (int i = 0; i < 5; i++) {
            playerLabels[i] = new Label((i + 1) + ". NOT JOINED", getSkin());
            playersTable.add(playerLabels[i]).left().padBottom(8);
            playersTable.row();
        }

        content.add(playersTable).expand().fill().padBottom(20);
        content.row();

        // Buttons
        createButtons(content);
        updatePlayerDisplay();
    }

    private void createButtons(Table content) {
        startButton = new TextButton("START", getSkin());
        TextButton refreshButton = new TextButton("REFRESH", getSkin());
        TextButton leaveButton = new TextButton("LEAVE", getSkin());

        startButton.setDisabled(!isHost || players.size() < 2);

        Table buttonTable = new Table();
        buttonTable.add(startButton).padRight(-40);
        buttonTable.add(refreshButton).padRight(-40);
        buttonTable.add(leaveButton);

        content.add(buttonTable).center();

        // Button actions
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (onStartGame != null) onStartGame.run();
            }
        });

        refreshButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                refreshGameState();
            }
        });

        leaveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (onLeaveGame != null) onLeaveGame.run();
                if (clientConnection != null) clientConnection.disconnect();
                hide();
            }
        });
    }

    public void refreshGameState() {
        if (clientConnection != null && clientConnection.isConnected()) {
            clientConnection.requestGameStateUpdate();
        }
    }



    private void updatePlayerDisplay() {
        for (int i = 0; i < 5; i++) {
            String text = (i + 1) + ". ";
            text += (i < players.size()) ? players.get(i) : "NOT JOINED";
            playerLabels[i].setText(text);
        }

        if (startButton != null) {
            startButton.setDisabled(!isHost || players.size() < 2);
        }
    }

    // Public API methods
    public void addPlayer(String playerName) {
        if (!players.contains(playerName)) {
            players.add(playerName);
            updatePlayerDisplay();
        }
    }

    public void removePlayer(String playerName) {
        if (players.remove(playerName)) {
            updatePlayerDisplay();
        }
    }

    public void setPlayers(List<String> newPlayers) {
        players.clear();
        players.addAll(newPlayers);
        updatePlayerDisplay();
    }

    public void setOnStartGame(Runnable callback) {
        this.onStartGame = callback;
    }

    public void setOnLeaveGame(Runnable callback) {
        this.onLeaveGame = callback;
    }

    public void onGameStateUpdate(GameStateUpdate update) {
        List<String> updatedPlayers = new ArrayList<>();
        for (PlayerInfo player : update.players) {
            updatedPlayers.add(player.name);
        }
        setPlayers(updatedPlayers);
    }

    public void setHost(boolean isHost) {
        this.isHost = isHost;
        updatePlayerDisplay();
    }
}
