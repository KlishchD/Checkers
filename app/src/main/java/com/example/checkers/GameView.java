package com.example.checkers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.LinkedList;

public class GameView extends View {
    Paint[] tilePaints = new Paint[64];
    Paint[] circlesPaints = new Paint[64];

    int green = Color.valueOf(0.02f, 0.33f, 0.24f).toArgb();
    int grey = Color.valueOf(0.68f, 0.67f, 0.71f).toArgb();
    int darkGrey = Color.valueOf(0.50f, 0.50f, 0.50f).toArgb();
    int red = Color.valueOf(0.35f, 0.04f, 0.04f).toArgb();
    int lightRed = Color.valueOf(0.50f, 0.04f, 0.04f).toArgb();

    LinkedList<RenderCallback> callbacks = new LinkedList<>();

    String statusText = "asdad";
    Paint statusTextPaint = new Paint();
    int textSize;

    public GameView(Context context) {
        super(context);

        statusTextPaint.setColor(Color.BLACK);
        statusTextPaint.setStyle(Paint.Style.FILL);
        statusTextPaint.setTextSize(150);

        int position = 0;
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                tilePaints[position] = new Paint();
                circlesPaints[position] = new Paint();

                ++position;
            }
        }
    }

    public void refreshMap(Cell[] map, CellAction[] actions) {
        int position = 0;
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                if ((i + j) % 2 == 0) {
                    tilePaints[position].setColor(grey);
                } else {
                    tilePaints[position].setColor(green);
                }

                if (map[position].state == MapCellState.Player) {
                    circlesPaints[position].setColor(map[position].type == PieceType.Regular ? red : lightRed);
                } else if (map[i * 8 + j].state == MapCellState.AI) {
                    circlesPaints[i * 8 + j].setColor(map[position].type == PieceType.Regular ? grey : darkGrey);
                } else {
                    circlesPaints[i * 8 + j].setColor(Color.TRANSPARENT);

                    if (actions[i * 8 + j].state == CellActionState.Move) {
                        circlesPaints[i * 8 + j].setColor(Color.WHITE);
                    } else if (actions[i * 8 + j].state == CellActionState.Attack) {
                        circlesPaints[i * 8 + j].setColor(Color.GREEN);
                    }
                }

                ++position;
            }
        }

        invalidate();
    }

    public void setWinner(Winner winner) {
        switch (winner) {
            case Player: {
                statusText = "You won";
                statusTextPaint.setColor(Color.GREEN);
                break;
            }
            case AI: {
                statusText = "You lost";
                statusTextPaint.setColor(Color.RED);
                break;
            }
            default: {
                statusText = "Draw";
                statusTextPaint.setColor(Color.BLACK);
            }
        }

        invalidate();
    }

    public void clearState() {
        statusText = "";
        statusTextPaint.setColor(Color.TRANSPARENT);
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int rectSize = Math.min(getWidth(), getHeight()) / 8;

        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                canvas.drawRect(j * rectSize, i * rectSize, (j + 1) * rectSize, (i + 1) * rectSize, tilePaints[i * 8 + j]);
                canvas.drawCircle((j + 0.5f) * rectSize, (i + 0.5f) * rectSize, rectSize / 3.0f, circlesPaints[i * 8 + j]);
            }
        }

        float textSize = statusTextPaint.measureText(statusText);
        canvas.drawText(statusText, (getWidth() - textSize) / 2, rectSize * 9, statusTextPaint);

        while (!callbacks.isEmpty()) {
            callbacks.poll().onDrawFinished();
        }
    }

    public void addRenderCallback(RenderCallback callback) {
        callbacks.add(callback);
    }
}
