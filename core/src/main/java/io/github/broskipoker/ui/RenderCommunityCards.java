package io.github.broskipoker.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.broskipoker.game.Card;

public class RenderCommunityCards {
    private SpriteBatch batch;
    private TextureRegion[][] cardRegions;
    private TextureRegion cardBackground;
    private TextureRegion cardBack;

    // Card dimensions
    private static final int CARD_WIDTH = 142;
    private static final int CARD_HEIGHT = 190;
    private static final int DISPLAY_CARD_WIDTH = 60;
    private static final int DISPLAY_CARD_HEIGHT = 90;
    private static final int CARD_SPACING = 15;
    private static final int ENHANCER_WIDTH = 142;
    private static final int ENHANCER_HEIGHT = 190;

    public RenderCommunityCards(SpriteBatch batch, TextureRegion[][] cardRegions, TextureRegion cardBackground, TextureRegion cardBack) {
        this.batch = batch;
        this.cardRegions = cardRegions;
        this.cardBackground = cardBackground;
        this.cardBack = cardBack;
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
}
