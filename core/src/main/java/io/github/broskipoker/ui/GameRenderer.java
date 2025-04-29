/**
 * GameRenderer.java
 * <p>
 * Handles all visual rendering aspects of the poker game.
 * Acts as the View in the MVC pattern.
 * <p>
 * Responsibilities:
 * - Render all game elements (cards, table, players)
 * - Manage visual assets (textures, sprites, fonts)
 * - Control camera positioning and zoom
 * - Handle card animations and visual effects
 * - Display game state information
 * - Provide menu and UI rendering
 */

package io.github.broskipoker.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.broskipoker.Menu;
import io.github.broskipoker.game.Card;
import io.github.broskipoker.game.Player;
import io.github.broskipoker.game.PokerGame;

import java.util.List;

public class GameRenderer {
    private final PokerGame pokerGame;
    private final SpriteBatch batch;
    private final Stage stage;
    private final Menu menu;
    private final BitmapFont font;

    // Camera settings
    private final OrthographicCamera camera;
    private final float defaultZoom = 1.0f;
    private final float focusedZoom = 0.5f;
    private boolean isZoomed = false;

    // Assets
    private final Texture backgroundTexture;
    private final Texture cardSheet;
    private final Texture enhancersSheet;
    private final TextureRegion[][] cardRegions;
    private final TextureRegion cardBackground;
    private final TextureRegion cardBack;

    // Card dimensions
    private static final int CARDS_PER_ROW = 13;
    private static final int SUITS = 4;
    private static final int CARD_WIDTH = 142;
    private static final int CARD_HEIGHT = 190;
    private static final int ENHANCER_WIDTH = 142;
    private static final int ENHANCER_HEIGHT = 190;
    private static final int DISPLAY_CARD_WIDTH = 60;
    private static final int DISPLAY_CARD_HEIGHT = 90;
    private static final int CARD_SPACING = 15;

    // Chair positions
    private final float[][] chairPositions;

    // Card dealing animation helper instance
    private final DealingAnimator dealingAnimator;

    public GameRenderer(PokerGame pokerGame) {
        this.pokerGame = pokerGame;

        // Initialize rendering components
        batch = new SpriteBatch();
        stage = new Stage(new ScreenViewport());
        menu = new Menu(stage);

        // Initialize font
        font = new BitmapFont();
        font.getData().setScale(3);
        font.setColor(1, 1, 1, 1);

        // Initialize camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

        // Load textures
        backgroundTexture = new Texture("textures/2x/pokerTable.png");
        cardSheet = new Texture(Gdx.files.internal("textures/2x/8BitDeck.png"));
        enhancersSheet = new Texture(Gdx.files.internal("textures/2x/Enhancers.png"));

        // Set up card regions
        cardBackground = new TextureRegion(enhancersSheet, ENHANCER_WIDTH, 0, ENHANCER_WIDTH, ENHANCER_HEIGHT);
        cardBack = new TextureRegion(enhancersSheet, 0, 0, ENHANCER_WIDTH, ENHANCER_HEIGHT);
        cardRegions = new TextureRegion[SUITS][CARDS_PER_ROW];

        for (int suit = 0; suit < SUITS; suit++) {
            for (int rank = 0; rank < CARDS_PER_ROW; rank++) {
                cardRegions[suit][rank] = new TextureRegion(cardSheet,
                    rank * CARD_WIDTH, suit * CARD_HEIGHT, CARD_WIDTH, CARD_HEIGHT);
            }
        }


        // Set up chair positions
        chairPositions = new float[][] {
            {Gdx.graphics.getWidth() * 0.2f, Gdx.graphics.getHeight() * 0.6f},
            {Gdx.graphics.getWidth() * 0.4f, Gdx.graphics.getHeight() * 0.6f},
            {Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.5f},
            {Gdx.graphics.getWidth() * 0.4f, Gdx.graphics.getHeight() * 0.3f},
            {Gdx.graphics.getWidth() * 0.2f, Gdx.graphics.getHeight() * 0.3f}
        };

        // Initialize dealing animator
        dealingAnimator = new DealingAnimator(5); // Max 5 players
    }

    public void render(float delta) {
        if (menu.isGameStarted()) {
            // Hide menu buttons
            for (TextButton button : menu.getButtons()) {
                button.setVisible(false);
            }

            batch.setProjectionMatrix(camera.combined);

            // Draw background
            batch.begin();
            batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.end();

            // Draw game elements
            renderGameElements(delta);
        } else {
            // Draw menu
            stage.act(delta);
            stage.draw();
        }
    }

    /**
     * Renders the community cards on the poker table
     * @param communityCards - array with 0-5 cards to be displayed
     * @param x - starting x coordinate for the first card
     * @param y - y coordinate for the cards
     * @param isFaceUp - true if the cards should be displayed face up, false for face down
     */
    public void renderCards(Card[] communityCards, float x, float y, boolean isFaceUp) {
        if (communityCards == null) return;

        for (int i = 0; i < communityCards.length; i++) {
            if (communityCards[i] != null) {
                float cardX = x + i * (DISPLAY_CARD_WIDTH + CARD_SPACING);

                if (isFaceUp) {
                    // Draw card background
                    batch.draw(cardBackground, cardX, y, DISPLAY_CARD_WIDTH, DISPLAY_CARD_HEIGHT);

                    // Draw card face
                    Card card = communityCards[i];
                    batch.draw(cardRegions[card.getSuit().ordinal()][card.getRank().ordinal()], cardX, y,
                        DISPLAY_CARD_WIDTH, DISPLAY_CARD_HEIGHT);
                } else {
                    // Draw card back
                    batch.draw(cardBack, cardX, y, DISPLAY_CARD_WIDTH, DISPLAY_CARD_HEIGHT);
                }
            }
        }
    }

    /**
     * Renders a stack of cards (dealer's deck) at a specific position.
     * @param x - x coordinate of the stack
     * @param y - y coordinate of the stack
     * @param stackSize - number of cards in the stack
     */
    public void renderCardStack(float x, float y, int stackSize, Texture enhancersSheet) {
        // Extract the card stack background
        TextureRegion cardStackBackground = new TextureRegion(enhancersSheet, 0, 0, ENHANCER_WIDTH, ENHANCER_HEIGHT);
        for (int i = 0; i < stackSize; i++) {
            // Slight offset for each card in the stack
            float offset = i * -(float) 1.75; // Adjust for visual effect
            batch.draw(cardStackBackground, x + offset, y - offset, DISPLAY_CARD_WIDTH, DISPLAY_CARD_HEIGHT);
        }
    }

    /**
     * Renders cards with the specified rotation
     * @param communityCards - array with cards to be displayed
     * @param x - starting x coordinate
     * @param y - y coordinate
     * @param rotation - rotation angle in degrees
     */
    public void renderRotatedCards(Card[] communityCards, float x, float y, float rotation) {
        if (communityCards == null) return;

        float originX = DISPLAY_CARD_WIDTH / 2;
        float originY = DISPLAY_CARD_HEIGHT / 2;

        for (int i = 0; i < communityCards.length; i++) {
            if (communityCards[i] != null) {
                float cardX = x + (DISPLAY_CARD_WIDTH + CARD_SPACING);

                // Draw card background (rotated)
                batch.draw(cardBack,
                           cardX, y - i * (DISPLAY_CARD_WIDTH + CARD_SPACING), // position
                           originX, originY, // origin for rotation
                           DISPLAY_CARD_WIDTH, DISPLAY_CARD_HEIGHT, // size
                           1, 1, // scale
                           rotation); // rotation
            }
        }
    }

    private void renderPlayerCards(List<Player> players) {
        for (int i = 0; i < players.size() && i < chairPositions.length; i++) {
            float x = chairPositions[i][0];
            float y = chairPositions[i][1];
            Card[] playerCards = players.get(i).getHoleCards().toArray(new Card[0]);

            for (int j = 0; j < playerCards.length && j < 2; j++) {
                if (dealingAnimator.isCardDealt(i, j)) {
                    if (i == 2) {
                        renderRotatedCards(new Card[]{playerCards[j]}, x, y - j * 70, 90);
                    } else if (i == 3) {
                        renderCards(new Card[]{playerCards[j]}, x + j * 70, y, true);
                    } else {
                        renderCards(new Card[]{playerCards[j]}, x + j * 70, y, false);
                    }
                }
            }
        }
    }

    private void renderGameElements(float delta) {
        // Get game data
        List<Player> players = pokerGame.getPlayers();
        PokerGame.GameState state = pokerGame.getGameState();
        List<Card> communityCards = pokerGame.getCommunityCards();

        batch.begin();

        // Calculate center positions for community cards
        float centerX = Gdx.graphics.getWidth() / 4.15f;
        float centerY = Gdx.graphics.getHeight() / 2.15f;

        // Render dealer position (deck)
        renderCardStack(Gdx.graphics.getWidth() / 7f, centerY, 5, enhancersSheet);

        // Render community cards based on game state
        if (state == PokerGame.GameState.BETTING_FLOP || state == PokerGame.GameState.FLOP) {
            if (communityCards.size() >= 3) {
                Card[] flopCards = {communityCards.get(0), communityCards.get(1), communityCards.get(2), null, null};
                renderCards(flopCards, centerX, centerY, true);
            }
        } else if (state == PokerGame.GameState.BETTING_TURN || state == PokerGame.GameState.TURN) {
            if (communityCards.size() >= 4) {
                Card[] turnCards = {communityCards.get(0), communityCards.get(1),
                                   communityCards.get(2), communityCards.get(3), null};
                renderCards(turnCards, centerX, centerY, true);
            }
        } else if (state == PokerGame.GameState.BETTING_RIVER || state == PokerGame.GameState.RIVER
                   || state == PokerGame.GameState.SHOWDOWN) {
            Card[] displayCards = communityCards.toArray(new Card[0]);
            renderCards(displayCards, centerX, centerY, true);
        }

        // Handle card dealing animation
        if (state == PokerGame.GameState.BETTING_PRE_FLOP) {
            dealingAnimator.update(delta, players, chairPositions.length);
        }

        // Render each player's cards
        renderPlayerCards(players);

        // Debug info
        font.draw(batch, "Game State: " + state.toString(), 50, 100);
        font.draw(batch, "Community Cards: " + communityCards.size(), 50, 50);

        // DEBUG: Add buttons to force next state (for testing)
        if (Gdx.input.justTouched() && Gdx.input.getY() < 150) {
            // Simple way to advance game state for testing
            if (state == PokerGame.GameState.BETTING_PRE_FLOP) {
                pokerGame.dealFlop();
            } else if (state == PokerGame.GameState.BETTING_FLOP) {
                pokerGame.dealTurn();
            } else if (state == PokerGame.GameState.BETTING_TURN) {
                pokerGame.dealRiver();
            } else if (state == PokerGame.GameState.BETTING_RIVER) {
                pokerGame.goToShowdown();
            } else if (state == PokerGame.GameState.SHOWDOWN) {
                // Start new hand
                pokerGame.startNewHand();
                dealingAnimator.reset();
            }
        }

        batch.end();
    }

    public void toggleZoom() {
        isZoomed = !isZoomed;

        float centerX = Gdx.graphics.getWidth() / 2.93f;
        float centerY = Gdx.graphics.getHeight() / 2f;

        if (isZoomed) {
            // Zoom in to flop area
            camera.zoom = focusedZoom;
            camera.position.set(centerX, centerY, 0);
        } else {
            // Reset to default view
            camera.zoom = defaultZoom;
            camera.position.set(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 0);
        }
        camera.update();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        camera.setToOrtho(false, width, height);
        camera.update();
    }

    public Stage getStage() {
        return stage;
    }

    public Menu getMenu() {
        return menu;
    }

    public void dispose() {
        batch.dispose();
        stage.dispose();
        font.dispose();
        cardSheet.dispose();
        enhancersSheet.dispose();
        backgroundTexture.dispose();
        menu.dispose();
    }
}
