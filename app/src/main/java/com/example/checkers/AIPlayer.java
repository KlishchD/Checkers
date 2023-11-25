package com.example.checkers;

public class AIPlayer {
    Cell[] map = new Cell[64];

    AIPlayer() {
        for (int i = 0; i < 64; ++i) {
            map[i] = new Cell();
        }
    }

    public boolean makeAINextMove(Cell[] map) {
        for (int i = 0; i < 64; ++i) {
            this.map[i].state = map[i].state;
            this.map[i].type = map[i].type;
        }

        BestAIMove bestAIMove = estimatePath(6, MapCellState.AI);

        if (bestAIMove.score < 0) {
            return false;
        }

        if (bestAIMove.isAttack) {
            Utils.attack(bestAIMove.from, bestAIMove.move, MapCellState.AI, map);
            makeAINextMove(map);
        } else {
            Utils.move(bestAIMove.from, bestAIMove.move, MapCellState.AI, map);
        }

        return true;
    }

    private BestAIMove estimatePath(int iteration, MapCellState player) {
        BestAIMove bestAIMove = new BestAIMove();

        for (int position = 0; position < 64; ++position) {
            if (map[position].state == player) {
                int moves = map[position].type == PieceType.Regular ? 2 : 4;
                for (int i = 0; i < moves; ++i) {
                    int move = Utils.getMove(player, i);

                    PieceType type;
                    boolean isAttack = false;

                    if (Utils.canMoveTo(position, move, map)) {
                        type = Utils.move(position, move, player, map);
                    } else if (Utils.isLegitimateAttack(position, move, player, map)) {
                        type = Utils.attack(position, move, player, map);
                        isAttack = true;
                    } else {
                        continue;
                    }

                    float score;

                    if (iteration == 1) {
                        score = Utils.calculatePositionScore(map);
                    } else {
                        BestAIMove estimated = estimatePath(iteration - 1, player == MapCellState.AI ? MapCellState.Player : MapCellState.AI);
                        score = estimated.score;
                    }


                    if (player == MapCellState.AI) {
                        bestAIMove.max(position, move, isAttack, score);
                    } else {
                        bestAIMove.min(position, move, isAttack, score);
                    }

                    if (isAttack) {
                        Utils.resetAttack(position, move, player, map, type);
                    } else {
                        Utils.resetMove(position, move, player, map, type);
                    }
                }
            }
        }

        return bestAIMove;
    }
}
