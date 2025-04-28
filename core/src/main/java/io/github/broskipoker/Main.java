package io.github.broskipoker;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
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

@Override
public void render() {
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    if (menu.isGameStarted()) {
        // Initialize the game when player first clicks start
        if (pokerGame == null) {
            pokerGame = new PokerGame();
            pokerGame.startNewHand(); // Start the first hand
        }

        // Update game state
        pokerGame.update(Gdx.graphics.getDeltaTime());

        // Render background
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        // Hide menu buttons
        for (TextButton button : menu.getButtons()) {
            button.setVisible(false);
        }

        batch.begin();

        // Render community cards based on game state
        float centerX = Gdx.graphics.getWidth() / 4.15f;
        float centerY = Gdx.graphics.getHeight() / 2.1f;

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

            // Render two cards for each player
            List<Player> players = pokerGame.getPlayers();
            for (int i = 0; i < players.size(); i++) {
                if (i >= chairPositions.length) break; // Ensure we don't exceed chair positions

                float x = chairPositions[i][0];
                float y = chairPositions[i][1];

                Card[] playerCards = players.get(i).getHoleCards().toArray(new Card[0]);
                if (playerCards.length >= 2) {
                    // Chair 3 (index 2) gets cards rotated 90 degrees to the left
                    if (i == 2) {
                        cardRenderer.renderRotatedCards(new Card[]{playerCards[0], playerCards[1]}, x, y, 90);
                    } else {
                        // Regular rendering for other chairs
                        if (i == 3) { // Chair 4 (index 3) gets face-up cards
                            cardRenderer.renderCommunityCards(new Card[]{playerCards[0], playerCards[1]}, x, y, true);
                        }
                        else { // All other chairs get face-down cards
                            cardRenderer.renderCommunityCards(new Card[]{playerCards[0], playerCards[1]}, x, y, false);
                        }
                    }

                }
            }

        // Show cards based on game state
        if (state == PokerGame.GameState.BETTING_PRE_FLOP) {
//            // In pre-flop phase, show card backs where community cards will appear
//            TextureRegion cardBackRegion = new TextureRegion(enhancersSheet, 0, 0, ENHANCER_WIDTH, ENHANCER_HEIGHT);
//            for (int i = 0; i < 5; i++) {
//                batch.draw(cardBackRegion,
//                    centerX + i * (60 + 15), // 60 = card width, 15 = spacing
//                    centerY, 60, 90);
//            }
//            // In real gameplay these should be invisible, we're just showing back faces for debugging
            // Chair positions (x, y) for 5 players
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

