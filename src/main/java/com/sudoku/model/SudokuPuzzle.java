package com.sudoku.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sudoku_puzzles")
public class SudokuPuzzle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String board;

    @Column(name = "difficulty")
    private String difficulty;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    public SudokuPuzzle() {
        this.createdAt = java.time.LocalDateTime.now();
    }

    public SudokuPuzzle(String board) {
        this.board = board;
        this.createdAt = java.time.LocalDateTime.now();
    }

    public SudokuPuzzle(String board, String difficulty) {
        this.board = board;
        this.difficulty = difficulty;
        this.createdAt = java.time.LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public char[][] toCharArray() {
        if (board == null || board.isEmpty()) {
            return new char[9][9];
        }
        char[][] result = new char[9][9];
        String[] rows = board.split("\n");
        for (int i = 0; i < 9 && i < rows.length; i++) {
            String row = rows[i];
            for (int j = 0; j < 9 && j < row.length(); j++) {
                result[i][j] = row.charAt(j);
            }
            // Fill remaining cells with '.' if row is shorter
            for (int j = row.length(); j < 9; j++) {
                result[i][j] = '.';
            }
        }
        // Fill remaining rows with '.' if fewer than 9 rows
        for (int i = rows.length; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                result[i][j] = '.';
            }
        }
        return result;
    }

    public static SudokuPuzzle fromCharArray(char[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                sb.append(board[i][j]);
            }
            if (i < 8) sb.append("\n");
        }
        return new SudokuPuzzle(sb.toString());
    }
}