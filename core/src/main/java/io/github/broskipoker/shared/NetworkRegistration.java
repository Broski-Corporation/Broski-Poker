package io.github.broskipoker.shared;

import com.esotericsoftware.kryo.Kryo;
import io.github.broskipoker.game.Card;
import io.github.broskipoker.game.PokerGame;
import io.github.broskipoker.server.PokerServer;
import io.github.broskipoker.server.PokerServerMultipleTables;
import io.github.broskipoker.server.Table;
import io.github.broskipoker.server.TableManager;

import java.util.ArrayList;

public class NetworkRegistration {
    public static void register(Kryo kryo) {
        // Collections used in shared classes
        kryo.register(ArrayList.class);
        kryo.register(boolean[].class);

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

        // Create Table request
        kryo.register(CreateTableRequest.class);
        kryo.register(CreateTableResponse.class);
        kryo.register(JoinTableRequest.class);
        kryo.register(JoinTableResponse.class);
        kryo.register(Table.class);
        kryo.register(TableManager.class);

        // Server registration
        kryo.register(PokerServerMultipleTables.class);

        // for refreshing lobby panel
        kryo.register(GameStateRequest.class);

        // start game in lobby label
        kryo.register(StartGameRequest.class);
        kryo.register(StartGameResponse.class);
    }
}
