package io.github.broskipoker.server;

import com.esotericsoftware.kryonet.*;
import io.github.broskipoker.shared.*;

public class PokerServerMultipleTables {
    private static final TableManager tableManager = new TableManager();

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        NetworkRegistration.register(server.getKryo());
        server.start();
        server.bind(8080);

        System.out.println("PokerServer with multi-table support running on port 8080!");

        server.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                // Handle table creation
                if (object instanceof CreateTableRequest) {
                    CreateTableRequest req = (CreateTableRequest) object;
                    Table table = tableManager.createTable(req.smallBlind, req.bigBlind);
                    table.addPlayer(connection, req.username, req.chips);

                    CreateTableResponse resp = new CreateTableResponse();
                    resp.code = table.getCode();
                    resp.success = true;
                    connection.sendTCP(resp);
                }

                // Handle join by code
                if (object instanceof JoinTableRequest) {
                    JoinTableRequest req = (JoinTableRequest) object;
                    Table table = tableManager.joinTableByCode(connection, req.code, req.username, req.chips);
                    JoinTableResponse resp = new JoinTableResponse();
                    if (table != null) {
                        resp.success = true;
                        resp.code = req.code;
                    } else {
                        resp.success = false;
                        resp.failReason = "Table not found or full";
                    }
                    connection.sendTCP(resp);
                }

                // Handle player actions (route to correct table)
                if (object instanceof PlayerAction) {
                    Table table = tableManager.getTableByConnection(connection);
                    if (table == null) return;
                    // ... route action to table.getPokerGame(), then broadcast state
                }
            }

            @Override
            public void disconnected(Connection connection) {
                tableManager.leaveTable(connection);
            }
        });

        while (true) Thread.sleep(10000);
    }
}
