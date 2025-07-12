package com.sudoku.controller;

import com.sudoku.model.SudokuPuzzle;
import com.sudoku.repository.SudokuPuzzleRepository;
import com.sudoku.service.BacktrackingSolver;
import com.sudoku.service.DLXSolver;
import com.sudoku.service.PuzzleGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sudoku")
@CrossOrigin(origins = "*")
public class SudokuController {

    @Autowired
    private SudokuPuzzleRepository repository;

    @Autowired
    private BacktrackingSolver backtrackingSolver;

    @Autowired
    private DLXSolver dlxSolver;

    @Autowired
    private PuzzleGenerator puzzleGenerator;

    @GetMapping("/puzzles")
    public ResponseEntity<List<SudokuPuzzle>> getAllPuzzles() {
        try {
            List<SudokuPuzzle> puzzles = repository.findAll();
            return ResponseEntity.ok(puzzles);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/save")
    public ResponseEntity<SudokuPuzzle> savePuzzle(@RequestBody SudokuPuzzle puzzle) {
        try {
            SudokuPuzzle savedPuzzle = repository.save(puzzle);
            return ResponseEntity.ok(savedPuzzle);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/puzzle/{id}")
    public ResponseEntity<SudokuPuzzle> getPuzzle(@PathVariable Long id) {
        try {
            Optional<SudokuPuzzle> puzzle = repository.findById(id);
            return puzzle.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/solve/backtracking")
    public ResponseEntity<char[][]> solveWithBacktracking(@RequestBody char[][] board) {
        try {
            if (!isValidBoard(board)) {
                return ResponseEntity.badRequest().build();
            }

            // Create a copy of the board to avoid modifying the original
            char[][] boardCopy = new char[9][9];
            for (int i = 0; i < 9; i++) {
                System.arraycopy(board[i], 0, boardCopy[i], 0, 9);
            }

            if (backtrackingSolver.solveSudoku(boardCopy)) {
                return ResponseEntity.ok(boardCopy);
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/solve/dlx")
    public ResponseEntity<char[][]> solveWithDLX(@RequestBody char[][] board) {
        try {
            if (!isValidBoard(board)) {
                return ResponseEntity.badRequest().build();
            }

            // Create a copy of the board to avoid modifying the original
            char[][] boardCopy = new char[9][9];
            for (int i = 0; i < 9; i++) {
                System.arraycopy(board[i], 0, boardCopy[i], 0, 9);
            }

            if (dlxSolver.solve(boardCopy)) {
                return ResponseEntity.ok(boardCopy);
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/generate/{difficulty}")
    public ResponseEntity<char[][]> generatePuzzle(@PathVariable String difficulty) {
        try {
            int clues;
            switch (difficulty.toLowerCase()) {
                case "easy": clues = 40; break;
                case "medium": clues = 30; break;
                case "hard": clues = 25; break;
                default: clues = 30;
            }
            char[][] puzzle = puzzleGenerator.generate(clues);
            return ResponseEntity.ok(puzzle);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/puzzle/{id}")
    public ResponseEntity<Void> deletePuzzle(@PathVariable Long id) {
        try {
            if (repository.existsById(id)) {
                repository.deleteById(id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private boolean isValidBoard(char[][] board) {
        if (board == null || board.length != 9) {
            return false;
        }

        for (int i = 0; i < 9; i++) {
            if (board[i] == null || board[i].length != 9) {
                return false;
            }
            for (int j = 0; j < 9; j++) {
                char cell = board[i][j];
                if (cell != '.' && (cell < '1' || cell > '9')) {
                    return false;
                }
                if (cell != '.' && !isSafe(board, i, j, cell - '0')) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isSafe(char[][] board, int row, int col, int number) {
        // Check row
        for (int i = 0; i < 9; i++) {
            if (board[row][i] == (char) (number + '0') && i != col) {
                return false;
            }
        }

        // Check column
        for (int i = 0; i < 9; i++) {
            if (board[i][col] == (char) (number + '0') && i != row) {
                return false;
            }
        }

        // Check 3x3 box
        int startRow = 3 * (row / 3);
        int startCol = 3 * (col / 3);
        for (int i = startRow; i < startRow + 3; i++) {
            for (int j = startCol; j < startCol + 3; j++) {
                if (board[i][j] == (char) (number + '0') && (i != row || j != col)) {
                    return false;
                }
            }
        }
        return true;
    }
}