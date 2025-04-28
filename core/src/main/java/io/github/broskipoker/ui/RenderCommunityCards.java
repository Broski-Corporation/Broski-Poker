package io.github.broskipoker.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.broskipoker.game.Card;

public class RenderCommunityCards {
    private SpriteBatch batch;
    private TextureRegion[][] cardRegions;
    private TextureRegion cardBackground;

    // Card dimensions
    private static final int CARD_WIDTH = 142;
    private static final int CARD_HEIGHT = 190;
    private static final int DISPLAY_CARD_WIDTH = 60;
    private static final int DISPLAY_CARD_HEIGHT = 90;
    private static final int CARD_SPACING = 15;

    public RenderCommunityCards(SpriteBatch batch, TextureRegion[][] cardRegions, TextureRegion cardBackground) {
        this.batch = batch;
        this.cardRegions = cardRegions;
        this.cardBackground = cardBackground;
    }

    /**
     * Renders the community cards on the poker table
     * @param communityCards - array with 0-5 cards to be displayed
     * @param x - starting x coordinate for the first card
     * @param y - y coordinate for the cards
     */
    public void renderCommunityCards(Card[] communityCards, float x, float y) {
        if (communityCards == null) return;

        for (int i = 0; i < communityCards.length; i++) {
            if (communityCards[i] != null) {
                float cardX = x + i * (DISPLAY_CARD_WIDTH + CARD_SPACING);

                // Draw card background
                batch.draw(cardBackground, cardX, y, DISPLAY_CARD_WIDTH, DISPLAY_CARD_HEIGHT);

                // Draw card face
                Card card = communityCards[i];
                batch.draw(cardRegions[card.getSuit().ordinal()][card.getRank().ordinal()], cardX, y,
                    DISPLAY_CARD_WIDTH, DISPLAY_CARD_HEIGHT);
            }
        }
    }

    /**
     * Renders a full set of 5 community cards on the table
     * @param flop1 - first card of the flop
     * @param flop2 - second card of the flop
     * @param flop3 - third card of the flop
     * @param turn - turn card
     * @param river - river card
     * @param x - starting x coordinate
     * @param y - y coordinate
     */
    public void renderFullCommunityCards(Card flop1, Card flop2, Card flop3, Card turn, Card river, float x, float y) {
        Card[] cards = {flop1, flop2, flop3, turn, river};
        renderCommunityCards(cards, x, y);
    }

    /**
     * Renders only the flop cards (first 3)
     */
    public void renderFlop(Card flop1, Card flop2, Card flop3, float x, float y) {
        Card[] flop = {flop1, flop2, flop3, null, null};
        renderCommunityCards(flop, x, y);
    }

    /**
     * Renders the flop + turn (first 4 cards)
     */
    public void renderFlopAndTurn(Card flop1, Card flop2, Card flop3, Card turn, float x, float y) {
        Card[] cards = {flop1, flop2, flop3, turn, null};
        renderCommunityCards(cards, x, y);
    }
}
