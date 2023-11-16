package ua.oleksiienko.entities;

import java.util.*;

public class Board {

    private final Cell[][] cells;
    private final int size;

    private List<Cell> minedCells;
    private int numberOfMines;
    private int flaggedCount;

    public Board(int size) {
        this.size = size;

        cells = new Cell[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j] = new Cell(i, j);
            }
        }
        this.generate();
    }

    public int getNumberOfMines() {
        return numberOfMines;
    }

    public int getFlaggedCount() {
        return this.flaggedCount;
    }

    public Cell getCell(int x, int y) {
        return cells[x][y];
    }

    public void openCell(int x, int y, boolean direct) {
        if (x < 0 || y < 0 || x >= size || y >= size) {
            return;
        }
        Cell cell = cells[x][y];
        if (cell.getVisibility() == CellVisibility.OPENED) {
            return;
        }
        if (cell.getStatus() == CellStatus.PIECED) {
            if (direct) {
                endGame();
            }
            return;
        }
        if (cell.getVisibility() == CellVisibility.FLAGGED) {
            flaggedCount = Math.max(0, flaggedCount-1);
        }
        if (cell.getStatus() == CellStatus.EMPTY) {
            cell.setVisibility(CellVisibility.OPENED);
            openCell(x+1, y, false);
            openCell(x-1, y, false);
            openCell(x, y+1, false);
            openCell(x, y-1, false);
            openCell(x+1, y+1, false);
            openCell(x-1, y-1, false);
            openCell(x-1, y+1, false);
            openCell(x+1, y-1, false);
        }
        cell.setVisibility(CellVisibility.OPENED);
    }

    public void endGame() {
        for (Cell mine : minedCells) {
            mine.setVisibility(CellVisibility.OPENED);
        }
    }

    public void flagNext(int x, int y) {
        Cell cell = cells[x][y];
        if (cell.getVisibility() == CellVisibility.OPENED) {
            return;
        }
        if (cell.getVisibility() == CellVisibility.CLOSED) {
            cell.setFlag(null);
            cell.setVisibility(CellVisibility.FLAGGED);
        } else {
            if (cell.getFlag() == null) {
                cell.setFlag(Piece.getById(0));
                flaggedCount++;
            }
            else if (cell.getFlag().getId() == 4) {
                cell.setVisibility(CellVisibility.CLOSED);
                flaggedCount = Math.max(0, flaggedCount-1);
            }
            else {
                Piece next = Piece.getById((cell.getFlag().getId() + 1) % 5);
                cell.setFlag(next);
            }
        }
    }

    public boolean checkWin() {
        return minedCells.stream()
                .allMatch(mine -> mine.getPiece() == mine.getFlag());
    }

    public boolean checkAllFlagged() {
        return minedCells.stream()
                .allMatch(mine -> mine.getFlag() != null);
    }

    private void generate() {
        Random random = new Random();
        int numberOfPieces = random.nextInt(4, 8);
        this.numberOfMines = numberOfPieces;
        List<Cell> mines = new LinkedList<>();
        while (numberOfPieces > 0) {
            int x = random.nextInt(size);
            int y = random.nextInt(size);
            if (cells[x][y].getStatus() != CellStatus.PIECED) {
                Piece piece = Piece.getById(random.nextInt(5));
                cells[x][y].setPiece(piece);
                mines.add(cells[x][y]);
                numberOfPieces--;
            }
        }
        for (var mine : mines) {
            Piece piece = mine.getPiece();
/*            if (mine.getStatus() == CellStatus.BEATEN) {
                for (var aggressor : mine.getBeatenBy()) {
                    unmarkFigure(aggressor, mine);
                }
            }*/
            switch (piece) {
                case BISHOP -> markBishop(mine);
                case KNIGHT -> markKnight(mine);
                case ROOK -> markRook(mine);
                case QUEEN -> markQueen(mine);
                case KING -> markKing(mine);
            }
        }
        this.minedCells = mines;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (cells[i][j].getStatus() == CellStatus.UNKNOWN) {
                    cells[i][j].empty();
                }
            }
        }
    }

    private void markQueen(Cell mine) {
        markBishop(mine);
        markRook(mine);
    }

    private void markKing(Cell mine) {
        safeBeat(mine.x+1, mine.y, mine);
        safeBeat(mine.x+1, mine.y+1, mine);
        safeBeat(mine.x-1, mine.y, mine);
        safeBeat(mine.x-1, mine.y-1, mine);
        safeBeat(mine.x, mine.y+1, mine);
        safeBeat(mine.x-1, mine.y+1, mine);
        safeBeat(mine.x, mine.y-1, mine);
        safeBeat(mine.x+1, mine.y-1, mine);
    }

    private void markRook(Cell mine) {
        for (int i = 1; i < size - mine.x; i++) {
            cells[mine.x+i][mine.y].beat(mine);
            if (cells[mine.x+i][mine.y].status == CellStatus.PIECED) break;
        }
        for (int i = 1; i <= mine.x; i++) {
            cells[mine.x-i][mine.y].beat(mine);
            if (cells[mine.x-i][mine.y].status == CellStatus.PIECED) break;
        }
        for (int i = 1; i < size - mine.y; i++) {
            cells[mine.x][mine.y+i].beat(mine);
            if (cells[mine.x][mine.y+i].status == CellStatus.PIECED) break;
        }
        for (int i = 1; i <= mine.y; i++) {
            cells[mine.x][mine.y-i].beat(mine);
            if (cells[mine.x][mine.y-i].status == CellStatus.PIECED) break;
        }
    }

    private void markKnight(Cell mine) {
        safeBeat(mine.x+2, mine.y+1, mine);
        safeBeat(mine.x+1, mine.y+2, mine);

        safeBeat(mine.x-2, mine.y+1, mine);
        safeBeat(mine.x-1, mine.y+2, mine);

        safeBeat(mine.x+2, mine.y-1, mine);
        safeBeat(mine.x+1, mine.y-2, mine);

        safeBeat(mine.x-2, mine.y-1, mine);
        safeBeat(mine.x-1, mine.y-2, mine);
    }

    private void safeBeat(int x, int y, Cell mine) {
        if (x < 0 || x >= size || y < 0 || y >= size) {
            return;
        }
        cells[x][y].beat(mine);
    }

    private void markBishop(Cell mine) {
        boolean moveNW = true;
        boolean moveNE = true;
        boolean moveSW = true;
        boolean moveSE = true;
        for (int i = 1; i < size; i++) {
            if ((moveNW = moveNW && mine.x+i < size && mine.y+i < size)) {
                cells[mine.x+i][mine.y+i].beat(mine);
                moveNW = cells[mine.x+i][mine.y+i].status != CellStatus.PIECED;
            }
            if ((moveSW = moveSW && mine.x+i < size && mine.y-i >= 0)) {
                cells[mine.x+i][mine.y-i].beat(mine);
                moveSW = cells[mine.x+i][mine.y-i].status != CellStatus.PIECED;
            }
            if ((moveNE = moveNE && mine.x-i >= 0 && mine.y+i < size)) {
                cells[mine.x-i][mine.y+i].beat(mine);
                moveNE = cells[mine.x-i][mine.y+i].status != CellStatus.PIECED;
            }
            if ((moveSE = moveSE && mine.x-i >= 0 && mine.y - i >= 0)) {
                cells[mine.x-i][mine.y-i].beat(mine);
                moveSE = cells[mine.x-i][mine.y-i].status != CellStatus.PIECED;
            }
        }
    }

    private void unmarkFigure(Cell aggressor, Cell victim) {
        for (int i = 0; i < size; i++){
            int x = aggressor.x == victim.x ? victim.x : (aggressor.x < victim.x ? victim.x + i : victim.x - i);
            int y = aggressor.y == victim.y ? victim.y : (aggressor.y < victim.y ? victim.y + i : victim.y - i);
            if (x < 0 || x >= size || y < 0 || y >= size) {
                break;
            }
            cells[x][y].unbeat(aggressor);
        }
    }

    public static class Cell {

        private final int x;

        private final int y;
        private CellStatus status;
        private CellVisibility visibility;
        private int beaten;
        private Piece piece;

        private Piece flag;

        private final Set<Cell> beatenBy;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
            this.status = CellStatus.UNKNOWN;
            this.visibility = CellVisibility.CLOSED;
            this.piece = null;
            this.beatenBy = new HashSet<>();
        }

        public Piece getFlag() {
            return flag;
        }

        public void setFlag(Piece flag) {
            this.flag = flag;
            this.visibility = CellVisibility.FLAGGED;
        }

        public int getBeaten() {
            return beaten;
        }

        public CellStatus getStatus() {
            return status;
        }

        public CellVisibility getVisibility() {
            return visibility;
        }

        public void setVisibility(CellVisibility visibility) {
            this.visibility = visibility;
        }

        public Piece getPiece() {
            return piece;
        }

        public void setPiece(Piece piece) {
            this.piece = piece;
            this.status = CellStatus.PIECED;
        }

        public void empty() {
            this.status = CellStatus.EMPTY;
            this.piece = null;
            this.beaten = 0;
            this.beatenBy.clear();
        }

        public void beat(Cell cell) {
            if (this.status != CellStatus.PIECED) {
                this.status = CellStatus.BEATEN;
            }
            this.beaten++;
            this.beatenBy.add(cell);
        }

        public Set<Cell> getBeatenBy() {
            return this.beatenBy;
        }

        public void unbeat(Cell aggressor) {
            this.beatenBy.remove(aggressor);
            this.beaten--;
        }
    }

    public enum CellStatus {
        EMPTY,
        BEATEN,
        PIECED,
        UNKNOWN
    }

    public enum CellVisibility {
        CLOSED,
        OPENED,
        FLAGGED
    }
}
