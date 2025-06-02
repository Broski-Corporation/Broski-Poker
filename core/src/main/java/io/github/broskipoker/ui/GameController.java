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
 * - Handle AI decision making for bot players
 */

package io.github.broskipoker.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import io.github.broskipoker.game.Player;
import io.github.broskipoker.game.PokerBot;
import io.github.broskipoker.game.PokerGame;

public class GameController extends InputAdapter {
    private final PokerGame pokerGame;
    private final GameRenderer renderer;
    private final InputMultiplexer inputMultiplexer;
    private static final int HUMAN_PLAYER_INDEX = 3; // the human player is at index 3

    // Bot thinking variables (moved from BettingUI)
    private float botDecisionTimer = 0;
    private boolean isBotThinking = false;
    private int thinkingBotIndex = -1;
    private static final float BOT_THINKING_TIME = 2.0f;

    // Previous game state for transition handling
    private PokerGame.GameState previousGameState = PokerGame.GameState.BETTING_PRE_FLOP;
    private PokerGame.GameState currentState = PokerGame.GameState.BETTING_PRE_FLOP;

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

            // Check for showdown -> newHand transition to disable the winning cards rendering
            currentState = pokerGame.getGameState();
            // Clear winning hand display when entering BETTING_PRE_FLOP state
            if (currentState == PokerGame.GameState.BETTING_PRE_FLOP &&
                previousGameState != PokerGame.GameState.BETTING_PRE_FLOP) {
                renderer.clearWinningHand();
            }
            previousGameState = currentState;

            // Update game state
            pokerGame.update(delta);

            // Handle bot thinking and decisions
            updateBotThinking(delta);

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

    // Method to update bot thinking status and execute decisions
    private void updateBotThinking(float delta) {
        // Check if game needs player action
        if (pokerGame.needsPlayerAction()) {
            int currentPlayerIndex = pokerGame.getCurrentPlayerIndex();

            // If it's not the human player's turn and a player action is needed
            if (currentPlayerIndex != HUMAN_PLAYER_INDEX) {
                // Start thinking if not already or if the bot changed
                if (!isBotThinking || thinkingBotIndex != currentPlayerIndex) {
                    startBotThinking(currentPlayerIndex);
                }

                // Update thinking timer
                if (isBotThinking && thinkingBotIndex == currentPlayerIndex) {
                    botDecisionTimer -= delta;
                    if (botDecisionTimer <= 0) {
                        executeBotDecision(thinkingBotIndex);
                    }
                }
            } else {
                // It's human's turn, stop any bot thinking
                isBotThinking = false;
                thinkingBotIndex = -1;
            }
        } else {
            // No player action needed, reset bot thinking state
            isBotThinking = false;
            thinkingBotIndex = -1;
        }
    }

    public void startBotThinking(int playerIndex) {
        // Only start bot thinking if dealing animation is complete
        if (GameRenderer.isDealingAnimationComplete() ||
            pokerGame.getGameState() != PokerGame.GameState.BETTING_PRE_FLOP) {

            isBotThinking = true;
            botDecisionTimer = BOT_THINKING_TIME; // Use the defined constant (0.5f)
            thinkingBotIndex = playerIndex;
        }
        // Otherwise, don't start bot thinking process at all
    }

    // Execute bot decision after thinking time
    private void executeBotDecision(int playerIndex) {
        // Ensure it's still this bot's turn before acting
        if (pokerGame.getCurrentPlayerIndex() == playerIndex && pokerGame.needsPlayerAction()) {
            // Simple bot logic: always check if possible, otherwise call
            Player botPlayer = pokerGame.getCurrentPlayer();
            PokerBot botPlayer1 = new PokerBot(botPlayer, PokerBot.BotStrategy.AGGRESSIVE);

            PokerGame.PlayerAction action = botPlayer1.decideAction(pokerGame, pokerGame.getCommunityCards());

            int betAmount = 0;
            if (action == PokerGame.PlayerAction.RAISE) {
                double handStrength = botPlayer1.evaluateHandStrength(pokerGame.getCommunityCards());
                betAmount = botPlayer1.calculateBetAmount(pokerGame.getPot(), handStrength);
            }

            pokerGame.performAction(action, betAmount);
        }
        // Reset thinking state after action attempt
        isBotThinking = false;
        thinkingBotIndex = -1;
    }

    // Getter methods for BettingUI to access bot thinking status
    public boolean isBotThinking() {
        return isBotThinking;
    }

    public int getThinkingBotIndex() {
        return thinkingBotIndex;
    }

    private void handleDebugControls() {
        if (Gdx.input.justTouched() && Gdx.input.getY() < 150) {
            advanceGameState();
        }
    }

    private void handleZoom() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            renderer.toggleZoom();
            if (renderer.getBettingUI() != null) {
                renderer.getBettingUI().setVisible(false);
            }
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
//            pokerGame.startNewHand();
//            DealingAnimator dealingAnimator = new DealingAnimator(5, PokerGame.getDealerPosition());
//            dealingAnimator.reset();
        }
    }

    public void dispose() {
        // Nothing to dispose
    }

    public void reset() {
        // Reset bot thinking logic
        isBotThinking = false;
        thinkingBotIndex = -1;
        botDecisionTimer = 0;

        // Reset game states
        currentState = PokerGame.GameState.BETTING_PRE_FLOP;
        previousGameState = PokerGame.GameState.BETTING_PRE_FLOP;

        // Reset input multiplexer
        Gdx.input.setInputProcessor(renderer.getStage()); // revine la input-ul pentru meniu
    }

}
