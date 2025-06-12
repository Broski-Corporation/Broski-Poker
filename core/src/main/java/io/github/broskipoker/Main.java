/**
 * Main.java
 * <p>
 * Main application entry point that initializes and coordinates all game components.
 * Follows the MVC pattern by separating the game (PokerGame), view (GameRenderer),
 * and controller (GameController) components.
 * <p>
 * Responsibilities:
 * - Initialize core game components
 * - Manage application lifecycle (create, render, resize, dispose)
 * - Connect the model, view, and controller components
 * - Coordinate the main game loop
 */

package io.github.broskipoker;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import io.github.broskipoker.game.PokerGame;
import io.github.broskipoker.ui.GameController;
import io.github.broskipoker.ui.GameRenderer;
import io.github.broskipoker.ui.MultiplayerGameScreen;
import io.github.broskipoker.utils.DatabaseConnection;
import io.github.broskipoker.server.ClientConnection;

public class Main extends ApplicationAdapter {
    private static Main instance;
    private PokerGame pokerGame;
    private static GameRenderer renderer;
    private GameController controller;
    public static final DatabaseConnection databaseConnection;

    // for muliplayer
    private ClientConnection pendingClient = null;
    private String pendingTableCode = null;

    static
    {
        databaseConnection = DatabaseConnection.getInstance();
    }

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void create() {
        instance = this;

        // Initialize core components
        pokerGame = new PokerGame();
        pokerGame.startNewHand();

        // Initialize renderer first
        renderer = new GameRenderer(pokerGame);

        // Initialize controller
        controller = new GameController(pokerGame, renderer);

        // Set controller in renderer after both are initialized to avoid circular dependency
        renderer.setGameController(controller);

        // Set input processor to handle menu first
        Gdx.input.setInputProcessor(renderer.getStage());
    }

    @Override
    public void render() {
        // check if we need to initialize a multiplayer game
        if (pendingClient != null) {
            initializeMultiplayerGame(pendingClient, pendingTableCode);
            pendingClient = null;
            pendingTableCode = null;
        }

        // Clear the screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update game state and handle input
        controller.update(Gdx.graphics.getDeltaTime());

        // Render the game
        renderer.render(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void resize(int width, int height) {
        renderer.resize(width, height);
    }

    public void startMultiplayerGame(ClientConnection client) {
        // store client info for initialization in the next render cycle
        this.pendingClient = client;
        this.pendingTableCode = client.getTableCode();
        System.out.println("Scheduled multiplyer game initialization for the next render cycle.");
    }

    public void initializeMultiplayerGame(ClientConnection client, String tableCode) {
        MultiplayerGameScreen multiplayerGameScreen = new MultiplayerGameScreen(client, tableCode);
        // clear current input processor
        Gdx.input.setInputProcessor(null);
        pokerGame = multiplayerGameScreen.getPokerGame();
        renderer = multiplayerGameScreen.getGameRenderer();
        // hide the menu
        renderer.setMenuStarted(true);
        controller = multiplayerGameScreen.getGameController();
        Gdx.input.setInputProcessor(renderer.getStage());
        client.requestGameStateUpdate();
        System.out.println("Multiplayer game initalized with table code: " + tableCode);
    }

    public static GameRenderer getRenderer() {
        return renderer;
    }

    public PokerGame getPokerGame() {
        return pokerGame;
    }

    public GameController getController() {
        return controller;
    }

    @Override
    public void dispose() {
        renderer.dispose();
        controller.dispose();
        pokerGame = null;
        instance = null;
    }
}

