package com.sudoku.repository;

import com.sudoku.model.SudokuPuzzle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SudokuPuzzleRepository extends JpaRepository<SudokuPuzzle, Long> {
}