package com.example.checkers;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
import android.os.Bundle;
import android.view.MotionEvent;

import java.util.LinkedList;
import java.util.List;

public class GameActivity extends AppCompatActivity {
    GameView gameView;

    AIPlayer aiPlayer = new AIPlayer();

    Cell[] map = new Cell[64];
    Cell[] intermediateMap = new Cell[64];

    CellAction[] actions = new CellAction[64];

    boolean isPlaying = true;

    Point activePoint = new Point(-1, -1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int position = 0;
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                actions[position] = new CellAction();

                map[position] = new Cell();
                intermediateMap[position] = new Cell();

                ++position;
            }
        }

        gameView = new GameView(getApplicationContext());
        setContentView(gameView);

        gameView.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                onActionDown(event);
            }

            return true;
        });

        resetGame();
    }

    private void resetGame() {
        isPlaying = true;

        int position = 0;
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                actions[position].state = CellActionState.None;
                map[position].state = MapCellState.Empty;
                if ((i + j) % 2 == 1) {
                    if (i < 2) {
                        map[position].state = MapCellState.AI;
                        map[position].type = PieceType.Regular;
                    } else if (i > 5) {
                        map[position].state = MapCellState.Player;
                        map[position].type = PieceType.Regular;
                    }
                }

                ++position;
            }
        }

        gameView.clearState();
        gameView.refreshMap(map, actions);
    }

    private void onActionDown(MotionEvent event) {
        int rectSize = Math.min(gameView.getWidth(), gameView.getHeight()) / 8;
        int x = (int) (event.getX() / rectSize);
        int y = (int) (event.getY() / rectSize);

        int clickedPosition = Utils.calculatePosition(x, y);

        if (Utils.isValidPosition(clickedPosition)) {
            if (!isPlaying) return;

            boolean hasMoved = false;

            CellAction action = actions[clickedPosition];
            int previous = Utils.calculatePosition(action.previous);

            if (action.state == CellActionState.Move) {
                Utils.move(previous, clickedPosition - previous, MapCellState.Player, map);
                activePoint.set(-1, -1);
                hasMoved = true;
                checkMapForEndOfGame(true);
            } else if (action.state == CellActionState.Attack) {
                CellAction currentAction = action;

                int position = clickedPosition;

                LinkedList<Integer> attacks = new LinkedList<>();

                while (currentAction != null && currentAction.state == CellActionState.Attack) {
                    attacks.add(position);

                    if (currentAction.previous.x < 0 || currentAction.previous.y < 0) {
                        currentAction = null;
                    } else {
                        position = Utils.calculatePosition(currentAction.previous);
                        currentAction = actions[position];
                    }
                }

                while (!attacks.isEmpty()) {
                    int nextPosition = attacks.pollLast();
                    Utils.attack(position, (nextPosition - position) / 2, MapCellState.Player, map);
                    position = nextPosition;
                }

                activePoint.set(-1, -1);
                checkMapForEndOfGame(true);
            } else {
                if (map[clickedPosition].state == MapCellState.Player) {
                    activePoint.set(x, y);
                } else {
                    activePoint.set(-1, -1);
                }
            }

            refreshActions();

            gameView.refreshMap(map, actions);

            if (hasMoved) {
                gameView.addRenderCallback(this::makeAiMove);
            }
        } else {
            if (!isPlaying) {
                resetGame();
            }
        }
    }

    private void makeAiMove() {
        aiPlayer.makeAINextMove(map);

        gameView.refreshMap(map, actions);

        checkMapForEndOfGame(false);
    }

    void checkMapForEndOfGame(boolean lastMovedPlayer) {
        boolean aiHasPiece = false, playerHasPieces = false;
        for (int i = 0; i < 64; ++i) {
            aiHasPiece |= map[i].state == MapCellState.AI;
            playerHasPieces |= map[i].state == MapCellState.Player;
        }

        if (!aiHasPiece || !playerHasPieces) {
            isPlaying = false;
            gameView.setWinner(aiHasPiece ? Winner.AI : Winner.Player);
            return;
        }

        MapCellState player = lastMovedPlayer ? MapCellState.AI : MapCellState.Player;
        boolean hasNextMove = false;

        for (int position = 0; position < 64; ++position) {
            if (map[position].state == player) {
                int moves = map[position].type == PieceType.Regular ? 2 : 4;
                for (int i = 0; i < moves; ++i) {
                    int move = Utils.getMove(player, i);

                    if (Utils.canMoveTo(position, move, map) || Utils.isLegitimateAttack(position, move, player, map)) {
                        hasNextMove = true;
                        break;
                    }
                }
            }
        }

        if (!hasNextMove) {
            isPlaying = false;
            gameView.setWinner(Winner.Draw);
        }
    }

    private void refreshActions() {
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                actions[i * 8 + j].state = CellActionState.None;
            }
        }

        if (Utils.isFirstPlayerPieceSelected(activePoint, map)) {
            int activePosition = Utils.calculatePosition(activePoint);

            for (int k = 0; k < Utils.getMovesCount(map[activePosition].type); ++k) {
                int move = Utils.getMove(MapCellState.Player, k);
                int to = activePosition + move;
                if (Utils.canMoveTo(activePosition, move, map)) {
                    CellAction action = actions[to];
                    action.state = CellActionState.Move;
                    action.previous.set(activePoint.x, activePoint.y);
                }
            }

            boolean isDirty = true;
            while (isDirty) {
                isDirty = false;
                for (int y = 0; y < 8; ++y) {
                    for (int x = 0; x < 8; ++x) {
                        int position = Utils.calculatePosition(x, y);
                        if (Utils.canAttackFromPosition(position, activePosition, actions)) {
                            for (int k = 0; k < Utils.getMovesCount(map[position].type); ++k) {
                                int move = Utils.getMove(MapCellState.Player, k);
                                int to = position + 2 * move;
                                if (Utils.isLegitimateAttack(position, move, MapCellState.Player, map, actions)) {
                                    CellAction action = actions[to];
                                    action.state = CellActionState.Attack;
                                    action.previous.set(x, y);

                                    isDirty = true;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}