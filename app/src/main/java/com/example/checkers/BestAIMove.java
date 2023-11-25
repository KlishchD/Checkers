package com.example.checkers;

public class BestAIMove {
    public int from;
    public int move;
    public boolean isAttack;

    public float score = -1.0f;

    public void max(int position, int move, boolean isAttack, float score) {
        if (score > this.score) {
            this.from = position;
            this.move = move;
            this.isAttack = isAttack;
            this.score = score;
        }
    }

    public void min(int position, int move, boolean isAttack, float score) {
        if (this.score < 0 || score < this.score) {
            this.from = position;
            this.move = move;
            this.isAttack = isAttack;
            this.score = score;
        }
    }
}
