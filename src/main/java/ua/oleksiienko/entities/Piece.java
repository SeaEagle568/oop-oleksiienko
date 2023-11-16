package ua.oleksiienko.entities;

import java.util.Arrays;

public enum Piece {
    BISHOP(0),
    KNIGHT(1),
    ROOK(2),
    QUEEN(3),
    KING(4);

    private final int id;

    public int getId() {
        return id;
    }

    Piece(int id) {
        this.id = id;
    }

    public static Piece getById(int id) {
        return Arrays.stream(Piece.values())
                .filter(it -> it.getId() == id)
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }
}
