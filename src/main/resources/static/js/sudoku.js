let board = Array(9).fill().map(() => Array(9).fill('.'));
let isSolving = false;
let timerInterval;

function updateBoard(element, i, j) {
    const value = element.value;

    // Only allow digits 1-9 or empty
    if (value !== '' && !value.match(/^[1-9]$/)) {
        element.value = board[i][j] === '.' ? '' : board[i][j];
        return;
    }

    board[i][j] = value === '' ? '.' : value;

    // Visual feedback for valid/invalid entries
    if (value !== '' && !isValidMove(i, j, value)) {
        element.classList.add('invalid');
    } else {
        element.classList.remove('invalid');
    }
}

function isValidMove(row, col, num) {
    const tempBoard = board.map(row => [...row]);
    tempBoard[row][col] = num;

    // Check row
    for (let i = 0; i < 9; i++) {
        if (i !== col && tempBoard[row][i] === num) {
            return false;
        }
    }

    // Check column
    for (let i = 0; i < 9; i++) {
        if (i !== row && tempBoard[i][col] === num) {
            return false;
        }
    }

    // Check 3x3 box
    const startRow = 3 * Math.floor(row / 3);
    const startCol = 3 * Math.floor(col / 3);
    for (let i = startRow; i < startRow + 3; i++) {
        for (let j = startCol; j < startCol + 3; j++) {
            if ((i !== row || j !== col) && tempBoard[i][j] === num) {
                return false;
            }
        }
    }

    return true;
}

function solve(method) {
    if (isSolving) return;

    isSolving = true;
    startTimer();
    $('#status').text('Solving...');

    $.ajax({
        url: `/api/sudoku/solve/${method}`,
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(board),
        success: function (response) {
            if (response) {
                board = response;
                updateGrid();
                $('#status').text(`Puzzle solved using ${method === 'dlx' ? 'DLX' : 'backtracking'} algorithm!`);
            } else {
                $('#status').text('No solution found!');
            }
            stopTimer();
            isSolving = false;
        },
        error: function (xhr, status, error) {
            $('#status').text('Error solving puzzle: ' + (xhr.responseText || error));
            stopTimer();
            isSolving = false;
        }
    });
}

function generatePuzzle() {
    const difficulty = $('#difficulty').val();
    $('#status').text('Generating puzzle...');

    $.ajax({
        url: `/api/sudoku/generate/${difficulty}`,
        type: 'GET',
        success: function (response) {
            if (response) {
                board = response;
                updateGrid();
                $('#status').text(`New ${difficulty} puzzle generated!`);
            } else {
                $('#status').text('Error generating puzzle!');
            }
        },
        error: function (xhr, status, error) {
            $('#status').text('Error generating puzzle: ' + (xhr.responseText || error));
        }
    });
}

function savePuzzle() {
    const puzzle = {
        board: board.map(row => row.join('')).join('\n'),
        difficulty: $('#difficulty').val()
    };

    $.ajax({
        url: '/api/sudoku/save',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(puzzle),
        success: function (response) {
            $('#status').text(`Puzzle saved successfully! ID: ${response.id}`);
        },
        error: function (xhr, status, error) {
            $('#status').text('Error saving puzzle: ' + (xhr.responseText || error));
        }
    });
}

function loadPuzzle() {
    $.ajax({
        url: '/api/sudoku/puzzles',
        type: 'GET',
        success: function (puzzles) {
            if (puzzles.length === 0) {
                $('#status').text('No saved puzzles found!');
                return;
            }

            // Create a list of puzzles for user to choose from
            let puzzleList = 'Available puzzles:\n';
            puzzles.forEach(p => {
                puzzleList += `ID: ${p.id}, Difficulty: ${p.difficulty || 'Unknown'}, Created: ${p.createdAt ? new Date(p.createdAt).toLocaleString() : 'Unknown'}\n`;
            });

            const id = prompt(puzzleList + '\nEnter puzzle ID to load:');
            if (id) {
                $.ajax({
                    url: `/api/sudoku/puzzle/${id}`,
                    type: 'GET',
                    success: function (puzzle) {
                        if (puzzle && puzzle.board) {
                            const rows = puzzle.board.split('\n');
                            board = rows.map(row => row.split(''));
                            updateGrid();
                            $('#status').text(`Puzzle ${id} loaded successfully!`);
                        } else {
                            $('#status').text('Invalid puzzle data!');
                        }
                    },
                    error: function (xhr, status, error) {
                        $('#status').text('Error loading puzzle: ' + (xhr.responseText || error));
                    }
                });
            }
        },
        error: function (xhr, status, error) {
            $('#status').text('Error fetching puzzles: ' + (xhr.responseText || error));
        }
    });
}

function clearBoard() {
    board = Array(9).fill().map(() => Array(9).fill('.'));
    updateGrid();
    $('#status').text('Board cleared!');
    clearInvalidCells();
}

function clearInvalidCells() {
    for (let i = 0; i < 9; i++) {
        for (let j = 0; j < 9; j++) {
            $(`#cell-${i}-${j}`).removeClass('invalid');
        }
    }
}

function updateGrid() {
    for (let i = 0; i < 9; i++) {
        for (let j = 0; j < 9; j++) {
            const cell = $(`#cell-${i}-${j}`);
            const value = board[i][j] === '.' ? '' : board[i][j];
            cell.val(value);

            // Remove invalid class when updating grid
            cell.removeClass('invalid');
        }
    }
}

function startTimer() {
    let seconds = 0;
    timerInterval = setInterval(() => {
        seconds++;
        const minutes = Math.floor(seconds / 60);
        const secs = seconds % 60;
        $('#timer').text(`Time: ${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`);
    }, 1000);
}

function stopTimer() {
    clearInterval(timerInterval);
}

function resetTimer() {
    stopTimer();
    $('#timer').text('Time: 00:00');
}

function validateBoard() {
    let isValid = true;
    clearInvalidCells();

    for (let i = 0; i < 9; i++) {
        for (let j = 0; j < 9; j++) {
            const value = board[i][j];
            if (value !== '.' && !isValidMove(i, j, value)) {
                $(`#cell-${i}-${j}`).addClass('invalid');
                isValid = false;
            }
        }
    }

    if (isValid) {
        $('#status').text('Board is valid!');
    } else {
        $('#status').text('Board contains invalid entries!');
    }

    return isValid;
}

// Placeholder for step-by-step solving (client-side animation)
function stepByStepSolve() {
    alert('Step-by-step solving feature is not yet implemented. This would show the solving process step by step with animations.');
}

// Initialize the board when the page loads
$(document).ready(function() {
    updateGrid();
    $('#status').text('Enter a Sudoku puzzle or generate a new one');
});