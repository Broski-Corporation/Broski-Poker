package io.github.broskipoker.server;

import com.esotericsoftware.kryonet.Connection;
import java.util.*;

public class TableManager {

    private static final Map<String, Table> codeToTable = new HashMap<>();
    private final Map<Connection, String> connectionToCode = new HashMap<>();
    private final Random random = new Random();

    // Generate a random 6-character code (A-Z, 0-9)
    public String generateUniqueTableCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
            code = sb.toString();
        } while (codeToTable.containsKey(code));
        return code;
    }

    public synchronized Table createTable(int smallBlind, int bigBlind) {
        String code = generateUniqueTableCode();
        Table table = new Table(code, smallBlind, bigBlind);
        codeToTable.put(code, table);
        return table;
    }

    public synchronized Table getTableByCode(String code) {
        return codeToTable.get(code.toUpperCase());
    }

    public synchronized Table joinTableByCode(Connection conn, String code, String username, int chips) {
        Table table = getTableByCode(code);
        if (table != null) {
            table.addPlayer(conn, username, chips);
            connectionToCode.put(conn, code.toUpperCase());
        }
        return table;
    }

    public synchronized void joinExistingTable(Connection conn, Table table) {
        connectionToCode.put(conn, table.getCode());
    }

    public synchronized void leaveTable(Connection conn) {
        String code = connectionToCode.remove(conn);
        if (code != null) {
            Table table = codeToTable.get(code);
            if (table != null) table.removePlayer(conn);
        }
    }

    public synchronized Table getTableByConnection(Connection conn) {
        String code = connectionToCode.get(conn);
        return code != null ? codeToTable.get(code) : null;
    }

    public static Map<String, Table> getCodeToTable() {
        return codeToTable;
    }
}
