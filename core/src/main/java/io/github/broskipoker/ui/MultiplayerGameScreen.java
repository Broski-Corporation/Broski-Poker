package io.github.broskipoker.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.broskipoker.game.Card;
import io.github.broskipoker.game.Player;
import io.github.broskipoker.game.PokerGame;
import io.github.broskipoker.server.ClientConnection;
import io.github.broskipoker.shared.*;

import java.util.ArrayList;
import java.util.List;

public class MultiplayerGameScreen implements Screen {
    private Stage stage;
    private SpriteBatch batch;
    private PokerGame pokerGame;
    private GameRenderer gameRenderer;
    private GameController gameController;
    private BettingUI bettingUI;
    private ClientConnection clientConnection;
    private String tableCode;

    public MultiplayerGameScreen(ClientConnection clientConnection, String tableCode) {
        this.clientConnection = clientConnection;
        this.tableCode = tableCode;
        this.batch = new SpriteBatch();
        this.stage = new Stage(new ScreenViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        // get username from client connection
        String username = clientConnection.getUsername();

        // Create a placeholder PokerGame for initial rendering
        this.pokerGame = new PokerGame();
        this.pokerGame.setTableCode(tableCode); // Set the table code
        this.gameRenderer = new GameRenderer(pokerGame);
        this.gameRenderer.setMultiplayerMode(clientConnection, username);
        this.gameController = new GameController(pokerGame, gameRenderer);
        this.gameController.setMultiplayerMode(clientConnection, username);
        this.gameRenderer.setGameController(gameController);
        // hide the menu
        this.gameRenderer.setMenuStarted(true);

        // Register as listener for game state updates
        clientConnection.addGameStateListener(this::onGameStateUpdate);

        // Request initial game state
        clientConnection.requestGameStateUpdate();

        System.out.println("MultiplayerGameScreen initialized with username: " + username + "and table code: " + tableCode);
    }

private void onGameStateUpdate(GameStateUpdate update) {
    // Convert GameStateUpdate to local PokerGame state (data updates are thread-safe)
    updatePokerGameFromServerData(update);

    // Schedule UI updates to happen on the main render thread
    Gdx.app.postRunnable(() -> {
        // Update UI to reflect current game state
        if (gameRenderer.getBettingUI() != null) {
            gameRenderer.getBettingUI().update();
        }
    });
}

    private void updatePokerGameFromServerData(GameStateUpdate update) {
        // Update game state
        pokerGame.setGameState(update.gameState);
        pokerGame.setPot(update.pot);
        pokerGame.setCurrentBet(update.currentBet);
        pokerGame.setCurrentPlayerIndex(update.currentPlayerIndex);
        pokerGame.setNeedsPlayerAction(update.needsPlayerAction); // Add this line to sync the player action flag

        // Update community cards
        List<Card> communityCards = new ArrayList<>();
        for (CardInfo cardInfo : update.communityCards) {
            if (cardInfo != null) {
                Card card = PokerConverters.fromCardInfo(cardInfo);
                communityCards.add(card);
            }
        }
        pokerGame.setCommunityCards(communityCards);

        // Update players
        List<Player> players = new ArrayList<>();
        for (PlayerInfo playerInfo : update.players) {
            Player player = new Player(playerInfo.name, playerInfo.chips);
            player.setCurrentBet(playerInfo.currentBet);
            player.setActive(playerInfo.isActive);
            // Set hole cards if visible
            if (playerInfo.holeCards != null && !playerInfo.holeCards.isEmpty()) {
                // first clear the player's hand
                player.clearHoleCards();
                for (CardInfo cardInfo : playerInfo.holeCards) {
                    if (cardInfo != null) {
                        player.addCard(PokerConverters.fromCardInfo(cardInfo));
                    }
                }
            }
            players.add(player);
        }
        pokerGame.setPlayers(players);

        // Update has acted in round array
        if (update.hasActedInRound != null) {
            pokerGame.setHasActedInRound(update.hasActedInRound);
        }

        // Debug logging to help track state transitions
        System.out.println("Updated game state from server: " + update.gameState +
                          ", needsPlayerAction: " + update.needsPlayerAction);
    }

    public void sendPlayerAction(PokerGame.PlayerAction action, int amount) {
        PlayerAction playerAction = new PlayerAction();
        playerAction.action = action;
        playerAction.amount = amount;
        clientConnection.sendAction(playerAction);
    }

    public void startGame() {
        clientConnection.requestStartGame();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0.5f, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameRenderer.render(delta);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        gameRenderer.resize(width, height);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
        // Called when screen is no longer visible
    }

    @Override
    public void pause() {
        // Called when game is paused
    }

    @Override
    public void resume() {
        // Called when game is resumed
    }

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
        gameRenderer.dispose();
    }

    public PokerGame getPokerGame() {
        return pokerGame;
    }

    public GameRenderer getGameRenderer() {
        return gameRenderer;
    }

    public GameController getGameController() {
        return gameController;
    }

    public ClientConnection getClientConnection() {
        return clientConnection;
    }
}
