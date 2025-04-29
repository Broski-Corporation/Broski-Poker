/**
 * GameController.java
 * <p>
 * Processes user input and controls game interactions.
 * Acts as the Controller in the MVC pattern.
 * <p>
 * Responsibilities:
 * - Process keyboard and mouse input
 * - Translate user actions into game commands
 * - Handle zoom controls and view adjustments
 * - Manage debug features for testing
 * - Coordinate between user input and game logic
 */

package io.github.broskipoker.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import io.github.broskipoker.game.PokerGame;

public class GameController extends InputAdapter {
    private final PokerGame pokerGame;
    private final GameRenderer renderer;

    public GameController(PokerGame pokerGame, GameRenderer renderer) {
        this.pokerGame = pokerGame;
        this.renderer = renderer;
    }

    public void update(float delta) {
        // Check if game has started
        if (renderer.getMenu().isGameStarted()) {
            // Switch input processor when game starts
            if (Gdx.input.getInputProcessor() != this) {
                Gdx.input.setInputProcessor(this);
            }

            // Update game state
            pokerGame.update(delta);

            // Debug state advancement
            handleDebugControls();

            // Handle zoom key
            handleZoom();
        }
    }

    private void handleDebugControls() {
        if (Gdx.input.justTouched() && Gdx.input.getY() < 150) {
            advanceGameState();
        }
    }

    private void handleZoom() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            renderer.toggleZoom();
        }
    }

    private void advanceGameState() {
        // Advance game state code here
    }

    public void dispose() {
        // Nothing to dispose
    }
}
