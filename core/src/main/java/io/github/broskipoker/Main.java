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
import io.github.broskipoker.ui.RenderCommunityCards;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Stage stage;
    private Menu menu;
    private Texture backgroundTexture;

    // Texturi și regiuni pentru cărți
    private Texture cardSheet;
    private Texture enhancersSheet;
    private TextureRegion[][] cardRegions;
    private TextureRegion cardBackground;
    private RenderCommunityCards cardRenderer;
    private BitmapFont font;

    // Constante pentru sprite sheet
    private static final int CARDS_PER_ROW = 13;
    private static final int SUITS = 4;
    private static final int CARD_WIDTH = 142;
    private static final int CARD_HEIGHT = 190;
    private static final int ENHANCER_WIDTH = 142;
    private static final int ENHANCER_HEIGHT = 190;

    // Cărțile comunitare pentru joc
    private Card[] communityCards;

    @Override
    public void create() {
        backgroundTexture = new Texture("pokerTable.png");
        batch = new SpriteBatch();
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        menu = new Menu(stage);

        // Inițializează fontul
        font = new BitmapFont();
        font.getData().setScale(3);
        font.setColor(1, 1, 1, 1);

        // Încarcă texturile pentru cărți
        cardSheet = new Texture(Gdx.files.internal("NeedsReview/textures/2x/8BitDeck.png"));
        enhancersSheet = new Texture(Gdx.files.internal("NeedsReview/textures/2x/Enhancers.png"));

        // Extrage fundalul cărții
        cardBackground = new TextureRegion(enhancersSheet,
            ENHANCER_WIDTH * 1, 0, ENHANCER_WIDTH, ENHANCER_HEIGHT);

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
        cardRenderer = new RenderCommunityCards(batch, cardRegions, cardBackground);

        // Inițializează cărțile comunitare (exemplu)
        initializeExampleCommunityCards();
    }

    // Metodă pentru inițializarea cărților comunitare (exemplu)
    private void initializeExampleCommunityCards() {
        // Poți schimba aceste valori sau adăuga logică de generare aleatorie
        communityCards = new Card[5];
        communityCards[0] = new Card(Card.Suit.HEARTS, Card.Rank.ACE);   // As de inimă
        communityCards[1] = new Card(Card.Suit.SPADES, Card.Rank.KING);  // Rege de pică
        communityCards[2] = new Card(Card.Suit.DIAMONDS, Card.Rank.TEN); // 10 de caro
        communityCards[3] = new Card(Card.Suit.CLUBS, Card.Rank.JACK);   // J de treflă
        communityCards[4] = new Card(Card.Suit.HEARTS, Card.Rank.QUEEN); // Q de inimă

    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (menu.isGameStarted()) {
            batch.begin();
            batch.draw(
                backgroundTexture,
                0,
                0,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
            );
            batch.end();

            for (TextButton button : menu.getButtons()) {
                button.setVisible(false);
            }

            // Draw the game elements here
            batch.begin();
            // Draw "YOU" text
            font.draw(batch, "YOU", Gdx.graphics.getWidth() / 2.4f, 80);

            // Draw the community cards
            float centerX = Gdx.graphics.getWidth() / 3.7f; // X position
            float centerY = Gdx.graphics.getHeight() / 2.1f; // Y position
            // Render the community cards
            cardRenderer.renderCommunityCards(communityCards, centerX, centerY);

            // Draw the dealer's card stack
            float stackX = Gdx.graphics.getWidth() / 5f; // X position for the stack
            float stackY = Gdx.graphics.getHeight() / 2.1f; // Y position for the stack
            cardRenderer.renderCardStack(stackX, stackY, 5, enhancersSheet); // Render a stack of 10 cards

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
    }
}
