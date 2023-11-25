package com.example.checkers;

public class Cell {
    public MapCellState state = MapCellState.Empty;
    public PieceType type = PieceType.None;

    void reset() {
        state = MapCellState.Empty;
        type = PieceType.None;
    }

    void set(MapCellState state, PieceType type) {
        this.state = state;
        this.type = type;
    }
}
