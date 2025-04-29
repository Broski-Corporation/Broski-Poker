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

public class Main extends ApplicationAdapter {
    private PokerGame pokerGame;
    private GameRenderer renderer;
    private GameController controller;

    @Override
    public void create() {
        // Initialize core components
        pokerGame = new PokerGame();
        pokerGame.startNewHand();
        renderer = new GameRenderer(pokerGame);
        controller = new GameController(pokerGame, renderer);

        // Set input processor to handle menu first
        Gdx.input.setInputProcessor(renderer.getStage());
    }

    @Override
    public void render() {
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

    @Override
    public void dispose() {
        renderer.dispose();
        controller.dispose();
        pokerGame = null;
    }
}
