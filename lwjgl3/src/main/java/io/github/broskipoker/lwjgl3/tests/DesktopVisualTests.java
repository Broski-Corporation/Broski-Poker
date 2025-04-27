package io.github.broskipoker.lwjgl3.tests;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.broskipoker.game.tests.CoreGameTests;

public class DesktopVisualTests {
    public static void main(String[] args) {
        // First run console tests
        CoreGameTests.runConsoleTests();

        // Then run visual tests
        runVisualTests();
    }

    private static void runVisualTests() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Card Visual Test");
        config.setWindowedMode(1920, 1080);
        new Lwjgl3Application(new CardVisualTestApp(), config);
    }

    // Inner class for visual tests
    static class CardVisualTestApp extends ApplicationAdapter {
        private SpriteBatch batch;
        private Texture cardSheet;
        private Texture enhancersSheet;
        private TextureRegion[][] cardRegions;
        private TextureRegion cardBackground;
        private BitmapFont font;

        // Based on analyzing the actual sprite sheet
        private static final int CARDS_PER_ROW = 13; // A-K
        private static final int SUITS = 4; // Hearts, Clubs, Diamonds, Spades

        // Card dimensions calculated from the 1846x760 sprite sheet
        private static final int CARD_WIDTH = 142; // 1846 ÷ 13 ≈ 142
        private static final int CARD_HEIGHT = 190; // 760 ÷ 4 ≈ 190

        // Enhancers dimensions (994x950 with 7 columns and 5 rows)
        private static final int ENHANCER_WIDTH = 142; // 994 ÷ 7 ≈ 142
        private static final int ENHANCER_HEIGHT = 190; // 950 ÷ 5 ≈ 190

        @Override
        public void create() {
            batch = new SpriteBatch();
            font = new BitmapFont();
            font.getData().setScale(1.2f);

            // Load card sheet
            cardSheet = new Texture(Gdx.files.internal("NeedsReview/textures/2x/8BitDeck.png"));

            // Load enhancers sheet
            enhancersSheet = new Texture(Gdx.files.internal("NeedsReview/textures/2x/Enhancers.png"));

            // Extract the card background (line 1, col 2)
            cardBackground = new TextureRegion(enhancersSheet,
                                              ENHANCER_WIDTH * 1, // col 2 (0-indexed = 1)
                                              0, // line 1 (0-indexed = 0)
                                              ENHANCER_WIDTH,
                                              ENHANCER_HEIGHT);

            // Create regions for the cards (4 suits, 13 ranks)
            cardRegions = new TextureRegion[SUITS][CARDS_PER_ROW];

            // Calculate card positions based on the actual sprite sheet layout
            for (int suit = 0; suit < SUITS; suit++) {
                for (int rank = 0; rank < CARDS_PER_ROW; rank++) {
                    // Calculate x,y position in the sprite sheet
                    int x = rank * CARD_WIDTH;
                    int y = suit * CARD_HEIGHT;

                    cardRegions[suit][rank] = new TextureRegion(cardSheet, x, y, CARD_WIDTH, CARD_HEIGHT);
                }
            }
        }

        @Override
        public void render() {
            Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            batch.begin();

            // Draw all cards in a grid with proper spacing
            for (int suit = 0; suit < SUITS; suit++) {
                for (int rank = 0; rank < CARDS_PER_ROW; rank++) {
                    float x = 50 + rank * 70;
                    float y = 650 - suit * 150;

                    // Draw the card background first
                    batch.draw(cardBackground, x, y, 60, 90);

                    // Draw the card face on top
                    batch.draw(cardRegions[suit][rank], x, y, 60, 90);

                    // Draw card name below card
                    String cardName = getCardName(rank, suit);
                    font.draw(batch, cardName, x + 20, y - 10);
                }
            }

            batch.end();
        }

        private String getCardName(int rank, int suit) {
            String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
            String[] suits = {"♥", "♣", "♦", "♠"};
            return ranks[rank] + suits[suit];
        }

        @Override
        public void dispose() {
            batch.dispose();
            cardSheet.dispose();
            enhancersSheet.dispose();
            font.dispose();
        }
    }
}
