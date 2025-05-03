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
import com.badlogic.gdx.InputMultiplexer;
import io.github.broskipoker.game.PokerGame;

public class GameController extends InputAdapter {
    private final PokerGame pokerGame;
    private final GameRenderer renderer;
    private final InputMultiplexer inputMultiplexer;
    private static final int HUMAN_PLAYER_INDEX = 3; // the human player is at index 3

    public GameController(PokerGame pokerGame, GameRenderer renderer) {
        this.pokerGame = pokerGame;
        this.renderer = renderer;
        this.inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(renderer.getStage());
        inputMultiplexer.addProcessor(this);
    }

    public void update(float delta) {
        // Check if game has started
        if (renderer.getMenu().isGameStarted()) {
            // Switch input processor when game starts
            if (Gdx.input.getInputProcessor() != inputMultiplexer) {
                Gdx.input.setInputProcessor(inputMultiplexer);
            }

            // Update game state
            pokerGame.update(delta);

            // Debug state advancement
            handleDebugControls();

            // Handle zoom key
            handleZoom();

            // Handle shortcuts for betting
            handleBettingKeyboardShortcuts();
        } else {
            // Set input processor to stage for menu interaction
            if (Gdx.input.getInputProcessor() != renderer.getStage()) {
                Gdx.input.setInputProcessor(renderer.getStage());
            }
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

    private void handleBettingKeyboardShortcuts() {
        // Only process keyboard shortcuts when it's the human player's turn
        if (pokerGame.getCurrentPlayerIndex() == HUMAN_PLAYER_INDEX && pokerGame.needsPlayerAction()) {
            // F key for Fold
            if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                pokerGame.performAction(PokerGame.PlayerAction.FOLD, 0);
            }

            // C key for Check/Call
            if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
                if (pokerGame.getCurrentPlayer().getCurrentBet() < pokerGame.getCurrentBet()) {
                    pokerGame.performAction(PokerGame.PlayerAction.CALL, 0);
                } else {
                    pokerGame.performAction(PokerGame.PlayerAction.CHECK, 0);
                }
            }

            // R key for minimum Raise
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                int minRaiseAmount = pokerGame.getCurrentBet() * 2;
                if (pokerGame.getCurrentPlayer().getChips() >= minRaiseAmount) {
                    pokerGame.performAction(PokerGame.PlayerAction.RAISE, minRaiseAmount);
                }
            }

            // A key for All-in
            if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                int allInAmount = pokerGame.getCurrentPlayer().getChips() + pokerGame.getCurrentPlayer().getCurrentBet();
                pokerGame.performAction(PokerGame.PlayerAction.RAISE, allInAmount);
            }
        }
    }

    private void advanceGameState() {
        PokerGame.GameState state = pokerGame.getGameState();

        // Simple debug controls to advance game state
        if (state == PokerGame.GameState.BETTING_PRE_FLOP) {
            pokerGame.dealFlop();
        } else if (state == PokerGame.GameState.BETTING_FLOP) {
            pokerGame.dealTurn();
        } else if (state == PokerGame.GameState.BETTING_TURN) {
            pokerGame.dealRiver();
        } else if (state == PokerGame.GameState.BETTING_RIVER) {
            pokerGame.goToShowdown();
        } else if (state == PokerGame.GameState.SHOWDOWN) {
            pokerGame.startNewHand();
        }
    }

    public void dispose() {
        // Nothing to dispose
    }
}
