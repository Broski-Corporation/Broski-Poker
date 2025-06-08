package io.github.broskipoker.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.broskipoker.game.PokerGame;
import io.github.broskipoker.server.ClientConnection;
import io.github.broskipoker.shared.GameStateUpdate;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MultiplayerGameScreen {
    private Stage stage;
    private SpriteBatch batch;
    private PokerGame pokerGame;
    private GameRenderer gameRenderer;
    private BettingUI bettingUI;
    private ClientConnection clientConnection;
    private String tableCode;

    public MultiplayerGameScreen(ClientConnection clientConnection, String tableCode) {
        this.clientConnection = clientConnection;
        this.tableCode = tableCode;
        this.batch = new SpriteBatch();
        this.stage = new Stage(new ScreenViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        // Create a placeholder PokerGame for initial rendering
        this.pokerGame = new PokerGame();
        this.gameRenderer = new GameRenderer(pokerGame);
        this.bettingUI = new BettingUI(pokerGame, stage, gameRenderer);

        // Register as listener for game state updates
        clientConnection.addGameStateListener(this::onGameStateUpdate);

        // Request initial game state
        clientConnection.requestGameStateUpdate();
    }

    private void onGameStateUpdate(GameStateUpdate update) {
        // Convert GameStateUpdate to local PokerGame state
        updatePokerGameFromServerData(update);

        // Update UI to reflect current game state
        bettingUI.update();
    }

    private void updatePokerGameFromServerData(GameStateUpdate update) {
        // Update local game state based on server data
        // This would update players, cards, pot, etc.
        // Implementation depends on your PokerGame class structure
    }

    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0.5f, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameRenderer.render(delta);

        stage.act(delta);
        stage.draw();
    }

    // Implement other Screen methods
}
