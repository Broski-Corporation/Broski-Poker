package io.github.broskipoker;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.broskipoker.game.Card;
import io.github.broskipoker.game.Player;
import io.github.broskipoker.game.PokerGame;
import io.github.broskipoker.ui.RenderCommunityCards;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.Stage;
import java.util.*;

public class Main extends ApplicationAdapter {
    private PokerGame pokerGame;

    private SpriteBatch batch;
    private Stage stage;
    private Menu menu;
    private Texture backgroundTexture;

    // Textures for cards and enhancers
    private Texture cardSheet;
    private Texture enhancersSheet;
    private TextureRegion[][] cardRegions;
    private TextureRegion cardBackground;
    private TextureRegion cardBack;
    private RenderCommunityCards cardRenderer;
    private BitmapFont font;

    private float elapsedTime = 0f;
    private int currentPlayerIndex = 0;
    private boolean dealingComplete = false;
    private boolean[][] dealtCards;
    private int dealingRound = 0;

    private OrthographicCamera camera;
    private float defaultZoom = 1.0f;
    private float focusedZoom = 0.5f; // Value < 1 gives zoom in effect
    private boolean isZoomed = false;

    // Constants for card dimensions
    private static final int CARDS_PER_ROW = 13;
    private static final int SUITS = 4;
    private static final int CARD_WIDTH = 142;
    private static final int CARD_HEIGHT = 190;
    private static final int ENHANCER_WIDTH = 142;
    private static final int ENHANCER_HEIGHT = 190;

    // Cards to be displayed
    private Card[] communityCards;

    @Override
    public void create() {
        backgroundTexture = new Texture("pokerTable.png");
        batch = new SpriteBatch();
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        menu = new Menu(stage);

        // Initialize the font
        font = new BitmapFont();
        font.getData().setScale(3);
        font.setColor(1, 1, 1, 1);

        // Initialize camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.zoom = defaultZoom;
        camera.update();

        // Încarcă texturile pentru cărți
        cardSheet = new Texture(Gdx.files.internal("NeedsReview/textures/2x/8BitDeck.png"));
        enhancersSheet = new Texture(Gdx.files.internal("NeedsReview/textures/2x/Enhancers.png"));

        // Extrage fundalul cărții
        cardBackground = new TextureRegion(enhancersSheet,
            ENHANCER_WIDTH * 1, 0, ENHANCER_WIDTH, ENHANCER_HEIGHT);

        cardBack = new TextureRegion(enhancersSheet,
            0, 0, ENHANCER_WIDTH, ENHANCER_HEIGHT);

        // Crează regiunile pentru cărți
        cardRegions = new TextureRegion[SUITS][CARDS_PER_ROW];
        for (int suit = 0; suit < SUITS; suit++) {
            for (int rank = 0; rank < CARDS_PER_ROW; rank++) {
                int x = rank * CARD_WIDTH;
                int y = suit * CARD_HEIGHT;
                cardRegions[suit][rank] = new TextureRegion(cardSheet, x, y, CARD_WIDTH, CARD_HEIGHT);
            }
        }

        // Inițializează renderer-ul pentru cărți comunitare
        cardRenderer = new RenderCommunityCards(batch, cardRegions, cardBackground, cardBack);

    }

    public void startNewHand() {
        // Reset dealing animation state
        currentPlayerIndex = 0;
        dealingRound = 0;
        dealingComplete = false;
        dealtCards = new boolean[5][2]; // For 5 players, 2 cards each (adjust if needed)
    }

    // This method is called every frame
    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Handle space key for zooming
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
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

        if (menu.isGameStarted()) {

            // Hide menu buttons
            for (TextButton button : menu.getButtons()) {
                button.setVisible(false);
            }

            // Initialize the game when player first clicks start
            if (pokerGame == null) {
                pokerGame = new PokerGame();
                pokerGame.startNewHand(); // Start the first hand
                startNewHand();
            }

            // Update game state
            pokerGame.update(Gdx.graphics.getDeltaTime());

            // Set batch to use camera projection
            batch.setProjectionMatrix(camera.combined);

            // Render background
            batch.begin();
            batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.end();


            // Render community cards and player cards based on game state
            batch.begin();
            float centerX = Gdx.graphics.getWidth() / 4.15f;
            float centerY = Gdx.graphics.getHeight() / 2.15f;

            PokerGame.GameState state = pokerGame.getGameState();
            List<Card> communityCards = pokerGame.getCommunityCards();

            // DEBUG: Display deck position and game controls
            cardRenderer.renderCardStack(Gdx.graphics.getWidth() / 7f, centerY, 5, enhancersSheet);

            float[][] chairPositions = {
                    {Gdx.graphics.getWidth() * 0.2f, Gdx.graphics.getHeight() * 0.6f}, // chair 1
                    {Gdx.graphics.getWidth() * 0.4f, Gdx.graphics.getHeight() * 0.6f}, // chair 2
                    {Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.5f}, // chair 3
                    {Gdx.graphics.getWidth() * 0.4f, Gdx.graphics.getHeight() * 0.3f}, // chair 4
                    {Gdx.graphics.getWidth() * 0.2f, Gdx.graphics.getHeight() * 0.3f}  // chair 5
                };

            // Render all players' cards
            List<Player> players = pokerGame.getPlayers();
            for (int i = 0; i < players.size() && i < chairPositions.length; i++) {
                float x = chairPositions[i][0];
                float y = chairPositions[i][1];
                Card[] playerCards = players.get(i).getHoleCards().toArray(new Card[0]);
                for (int j = 0; j < playerCards.length && j < dealtCards[i].length; j++) {
                    if (dealtCards[i][j]) {
                        if (i == 2) {
                            cardRenderer.renderRotatedCards(new Card[]{playerCards[j]}, x, y - j * 70, 90);
                        } else if (i == 3) {
                            cardRenderer.renderCommunityCards(new Card[]{playerCards[j]}, x + j * 70, y, true);
                        } else {
                            cardRenderer.renderCommunityCards(new Card[]{playerCards[j]}, x + j * 70, y, false);
                        }
                    }
                }
            }

            // Show cards based on game state
            if (state == PokerGame.GameState.BETTING_PRE_FLOP) {
                // Animate card distribution one at a time
                if (!dealingComplete) {
                    // Now handle dealing the next card with timing
                    elapsedTime += Gdx.graphics.getDeltaTime();
                    if (elapsedTime > 0.3f) { // Deal one card every 0.3 seconds
                        players = pokerGame.getPlayers();
                        if (currentPlayerIndex < players.size() && currentPlayerIndex < chairPositions.length) {
                            Card[] playerCards = players.get(currentPlayerIndex).getHoleCards().toArray(new Card[0]);
                            if (dealingRound < 2 && playerCards.length >= 2) {
                                dealtCards[currentPlayerIndex][dealingRound] = true; // Mark this card as dealt
                                currentPlayerIndex++; // Move to next player
                                if (currentPlayerIndex >= players.size() || currentPlayerIndex >= chairPositions.length) { // If we've dealt to all players in this round, move to next round
                                    if (dealingRound < 1) {
                                        dealingRound++; // Start second round
                                        currentPlayerIndex = 0; // Back to first player
                                    } else {
                                        dealingComplete = true; // Both rounds completed
                                    }
                                }
                            }
                        }
                        elapsedTime = 0f;
                    }
                }

            } else if (state == PokerGame.GameState.BETTING_FLOP || state == PokerGame.GameState.FLOP) {
                if (communityCards.size() >= 3) {
                    Card[] flopCards = {communityCards.get(0), communityCards.get(1), communityCards.get(2), null, null};
                    cardRenderer.renderCommunityCards(flopCards, centerX, centerY, true);
                }
            } else if (state == PokerGame.GameState.BETTING_TURN || state == PokerGame.GameState.TURN) {
                if (communityCards.size() >= 4) {
                    Card[] turnCards = {communityCards.get(0), communityCards.get(1), communityCards.get(2),
                                       communityCards.get(3), null};
                    cardRenderer.renderCommunityCards(turnCards, centerX, centerY, true);
                }
            } else {
                Card[] displayCards = communityCards.toArray(new Card[0]);
                cardRenderer.renderCommunityCards(displayCards, centerX, centerY, true);
            }

            // Debug info
            font.draw(batch, "Game State: " + state.toString(), 50, 100);
            font.draw(batch, "Community Cards: " + communityCards.size(), 50, 50);

            // DEBUG: Add buttons to force next state (for testing)
            if (Gdx.input.justTouched() && Gdx.input.getY() < 150) {
                // This is a simple way to advance game state for testing
                // In real gameplay, you should use player actions
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
                    startNewHand();
                }
            }

            batch.end();
        } else {
            stage.act(Gdx.graphics.getDeltaTime());
            stage.draw();
        }
}

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
        font.dispose();
        cardSheet.dispose();
        enhancersSheet.dispose();
        backgroundTexture.dispose();
        menu.dispose();
        pokerGame = null;
    }
}

