package com.sudoku.service;

import org.springframework.stereotype.Service;

@Service
public class DLXSolver {
    public boolean solve(char[][] board) {
        // Placeholder for DLX implementation
        // In a full implementation, this would convert the board to a constraint matrix
        // and solve using Dancing Links algorithm
        return new BacktrackingSolver().solveSudoku(board); // Temporary fallback
    }
}