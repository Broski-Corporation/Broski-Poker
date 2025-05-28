package io.github.broskipoker.shared;

import com.esotericsoftware.kryo.Kryo;
import java.util.ArrayList;
import java.util.HashMap;

public class NetworkRegistration {
    public static void register(Kryo kryo) {
        // Register collections
        kryo.register(ArrayList.class);
        kryo.register(HashMap.class);

        // Register your existing game classes
        kryo.register(io.github.broskipoker.game.Card.class);
        kryo.register(io.github.broskipoker.game.Card.Suit.class);
        kryo.register(io.github.broskipoker.game.Card.Rank.class);

        // Register network messages
        kryo.register(LoginRequest.class);
        kryo.register(LoginResponse.class);
    }
}
