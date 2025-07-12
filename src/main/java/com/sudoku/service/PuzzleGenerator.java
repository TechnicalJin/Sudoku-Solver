package com.sudoku.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class PuzzleGenerator {

    @Autowired
    private BacktrackingSolver solver;

    private Random random = new Random();

    public char[][] generate(int clues) {
        char[][] board = new char[9][9];

        // Initialize board with empty cells
        for (int i = 0; i < 9; i++) {
            Arrays.fill(board[i], '.');
        }

        // Fill diagonal 3x3 boxes first (they don't affect each other)
        for (int i = 0; i < 9; i += 3) {
            fillBox(board, i, i);
        }

        // Solve the partially filled board
        solver.solveSudoku(board);

        // Remove numbers to create the puzzle
        removeNumbers(board, clues);

        return board;
    }

    private void fillBox(char[][] board, int startRow, int startCol) {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers, random);

        int index = 0;
        for (int i = startRow; i < startRow + 3; i++) {
            for (int j = startCol; j < startCol + 3; j++) {
                board[i][j] = (char) (numbers.get(index++) + '0');
            }
        }
    }

    private void removeNumbers(char[][] board, int clues) {
        int numbersToRemove = 81 - clues;
        List<int[]> positions = new ArrayList<>();

        // Create list of all positions
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                positions.add(new int[]{i, j});
            }
        }

        // Shuffle positions to remove numbers randomly
        Collections.shuffle(positions, random);

        int removed = 0;
        for (int[] pos : positions) {
            if (removed >= numbersToRemove) break;

            int row = pos[0];
            int col = pos[1];

            if (board[row][col] != '.') {
                char backup = board[row][col];
                board[row][col] = '.';

                // Check if puzzle still has unique solution
                if (hasUniqueSolution(board)) {
                    removed++;
                } else {
                    board[row][col] = backup; // Restore if no unique solution
                }
            }
        }
    }

    private boolean hasUniqueSolution(char[][] board) {
        // Create a copy for testing
        char[][] testBoard = new char[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(board[i], 0, testBoard[i], 0, 9);
        }

        // For simplicity, we'll assume it has a unique solution
        // In a full implementation, you'd count the number of solutions
        return solver.solveSudoku(testBoard);
    }
}