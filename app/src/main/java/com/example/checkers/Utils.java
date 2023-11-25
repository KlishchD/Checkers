package com.example.checkers;

import android.graphics.Point;

public class Utils {
    private static final int[] FirstPlayerMoves = { -7, -9, 7, 9 };
    private static final int[] SecondPlayerMoves = { 7, 9, -7, -9 };

    public static boolean isLegitimatePosition(int position) {
        return position >= 0 && position < 64;
    }

    public static boolean isLegitimateAxisMove(int from, int to) {
        return Math.abs(from - to) == 1;
    }

    public static boolean isLegitimateMove(int from, int move) {
        int toX = (from + move) % 8;
        int toY = (from + move) / 8;
        return isLegitimateAxisMove(from % 8, toX) && isLegitimateAxisMove(from / 8, toY) && isLegitimatePosition(from) && isLegitimatePosition(from + move);
    }

    public static boolean canMoveTo(int from, int move, Cell[] map) {
        return isLegitimateMove(from, move) && map[from + move].state == MapCellState.Empty;
    }

    public static boolean isValidPosition(Point point) {
        return point.x >= 0 && point.y >= 0 && point.x < 8 && point.y < 8;
    }

    public static boolean isValidPosition(int position) {
        return position >= 0 && position < 64;
    }

    public static boolean isFirstPlayerPieceSelected(Point point, Cell[] map) {
        int position = calculatePosition(point);
        return isValidPosition(point) && map[position].state == MapCellState.Player;
    }

    public static int calculatePosition(Point point) {
        return point.y * 8 + point.x;
    }

    public static int calculatePosition(int x, int y) {
        return y * 8 + x;
    }

    public static boolean canAttackFromPosition(int position, int activePosition, CellAction[] actions) {
        return position == activePosition || actions[position].state == CellActionState.Attack;
    }

    public static boolean isLegitimateAttack(int from, int move, MapCellState player, Cell[] map, CellAction[] actions) {
        int enemy = from + move;
        int to = from + 2 * move;
        return isLegitimateMove(from, move) && isLegitimateMove(enemy, move) &&
                map[to].state == MapCellState.Empty &&
                map[enemy].state == (player == MapCellState.Player ? MapCellState.AI : MapCellState.Player) &&
                actions[to].state != CellActionState.Attack;
    }

    public static boolean isLegitimateAttack(int from, int move, MapCellState player, Cell[] map) {
        int enemy = from + move;
        int to = from + 2 * move;
        return isLegitimateMove(from, move) && isLegitimateMove(enemy, move) &&
                map[to].state == MapCellState.Empty &&
                map[enemy].state == (player == MapCellState.Player ? MapCellState.AI : MapCellState.Player);
    }

    public static float calculatePositionScore(Cell[] map) {
        int ai = 0, total = 0;
        for (int i = 0; i < 64; ++i) {
            if (map[i].state == MapCellState.AI) {
                ++ai;
            }
            if (map[i].state != MapCellState.Empty) {
                ++total;
            }
        }

        return 1.0f * ai / (total * total);
    }

    public static PieceType move(int from, int move, MapCellState player, Cell[] map) {
        PieceType type = map[from].type;

        int to = from + move;
        map[to].state = player;
        if (player == MapCellState.Player) {
            map[to].type = to < 8 ? PieceType.Queen : map[from].type;
        } else {
            map[to].type = to >= 55 ? PieceType.Queen : map[from].type;
        }

        map[from].reset();

        return type;
    }

    public static void resetMove(int from, int move, MapCellState player, Cell[] map, PieceType type) {
        map[from].state = player;
        map[from].type = type;

        map[from + move].reset();
    }

    public static PieceType attack(int from, int move, MapCellState player, Cell[] map) {
        PieceType attackedType = map[from + move].type;

        int to = from + 2 * move;

        map[to].state = player;
        if (player == MapCellState.Player) {
            map[to].type = to < 8 ? PieceType.Queen : map[from].type;
        } else {
            map[to].type = to >= 55 ? PieceType.Queen : map[from].type;
        }

        map[from].reset();

        map[from + move].reset();

        return attackedType;
    }

    public static void resetAttack(int position, int move, MapCellState player, Cell[] map, PieceType attackedType) {
        map[position].set(player, map[position + 2 * move].type);

        map[position + move].set(player == MapCellState.AI ? MapCellState.Player : MapCellState.AI, attackedType);

        map[position + 2 * move].reset();
    }

    public static int getMove(MapCellState player, int move) {
        return player == MapCellState.Player ? FirstPlayerMoves[move] : SecondPlayerMoves[move];
    }

    public static int getMovesCount(PieceType type) {
        return type == PieceType.Queen ? 4 : 2;
    }
}
