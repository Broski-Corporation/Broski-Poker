package io.github.broskipoker.shared;

import com.esotericsoftware.kryo.Kryo;
import io.github.broskipoker.game.Card;
import io.github.broskipoker.game.PokerGame;

import java.util.ArrayList;

public class NetworkRegistration {
    public static void register(Kryo kryo) {
        // Collections used in shared classes
        kryo.register(ArrayList.class);

        // Shared network message classes
        kryo.register(CardInfo.class);
        kryo.register(PlayerInfo.class);
        kryo.register(PlayerAction.class);
        kryo.register(GameStateUpdate.class);

        // Enums used in shared classes
        kryo.register(Card.Suit.class);
        kryo.register(Card.Rank.class);
        kryo.register(PokerGame.GameState.class);
        kryo.register(PokerGame.PlayerAction.class);

        // Login messages (if you use them)
        kryo.register(LoginRequest.class);
        kryo.register(LoginResponse.class);
    }
}
