package ua.oleksiienko.services;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.vaadin.pekkam.CanvasRenderingContext2D;
import org.apache.commons.lang3.tuple.Pair;
import ua.oleksiienko.entities.Board;
import ua.oleksiienko.entities.Piece;
import ua.oleksiienko.utils.Canvas;

import java.util.Locale;

@Service
@Scope("prototype")
public class PainterService {
    private static double C_SIZE = 0;
    private final static int C_COUNT = 8;
    private Canvas canvas;

    private Board board;
    private boolean over = false;

    private TextField score;

    private Button finishButton;

    private Pair<Integer, Integer> lastCursorPosition = Pair.of(0, 0);



    public void init(HasComponents component, Board board, TextField score, Button finishButton, double maxSize) {
        C_SIZE = Math.min(60, maxSize / C_COUNT);
        this.canvas = new Canvas((int) (C_SIZE * C_COUNT + 10), (int) (C_SIZE * C_COUNT + 10));
        this.board = board;
        this.score = score;
        this.finishButton = finishButton;
        component.add(this.canvas);
        paintBoard();
        addMoveListener();
        addClickListener();
        addFinishHandler();
        score.setValue("0/" + board.getNumberOfMines());
    }

    private void addFinishHandler() {
        Dialog win = createWin();
        Dialog nearlyWin = createNearly();
        this.finishButton.addSingleClickListener(event -> {
            if (over) {
                UI.getCurrent().getPage().reload();
                return;
            }
            if (board.checkWin()) {
                win.open();
            } else if (board.checkAllFlagged()) {
                nearlyWin.open();
            } else {
                board.endGame();
                paintBoard();
            }
        });
    }

    private Dialog createLose() {
        Dialog lose = new Dialog();
        lose.setModal(true);
        lose.setHeaderTitle("Бабах");
        lose.add(new Paragraph("Ви потрапили на шахоміну :("));
        Button close = new Button("Закрити", e -> {
            lose.close();
        });
        lose.getFooter().add(close);
        return lose;
    }

    private static Dialog createNearly() {
        Dialog lose = new Dialog();
        lose.setModal(false);
        lose.setHeaderTitle("Майже...");
        lose.add(new Paragraph("Ви правильно визначили положення шахомін але невірно вказали тип якихось з них."));
        Button restart = new Button("Спробувати заново", e -> {
            lose.close();
            UI.getCurrent().getPage().reload();
        });
        restart.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_ERROR);
        restart.getStyle().set("margin-right", "auto");
        Button close = new Button("Закрити", e -> {
            lose.close();
        });
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        lose.getFooter().add(restart, close);
        return lose;
    }

    private static Dialog createWin() {
        Dialog win = new Dialog();
        win.setModal(false);
        win.setHeaderTitle("Перемога!");
        win.add(new Paragraph("Ви успішно віднайшли усі шахо-міни!"));
        Button restart = new Button("Грати ще", e -> {
            win.close();
            UI.getCurrent().getPage().reload();
        });
        win.getFooter().add(restart);
        return win;
    }

    private void addClickListener() {
        this.canvas.addRightClickListener(it -> {
            if (over) return;
            int x = it.getOffsetX();
            int y = it.getOffsetY();
            if (x > 5 && x < C_SIZE * C_COUNT - 5 && y > 5 && y < C_SIZE * C_COUNT - 5) {
                x = (x - 5) / (int) C_SIZE;
                y = (y - 5) / (int) C_SIZE;
                board.flagNext(x, y);
                updateScore();
                paintBoard();
            }
        });
        this.canvas.addMouseClickListener(it -> {
            if (over) return;
            int x = it.getOffsetX();
            int y = it.getOffsetY();
            if (x > 5 && x < C_SIZE * C_COUNT - 5 && y > 5 && y < C_SIZE * C_COUNT - 5) {
                x = (x - 5) / (int) C_SIZE;
                y = (y - 5) / (int) C_SIZE;
                board.  openCell(x, y, true);
                updateScore();
                paintBoard();
            }
        });
    }

    private void updateScore() {
        score.setValue(board.getFlaggedCount() + "/" + board.getNumberOfMines());
        score.setInvalid(board.getFlaggedCount() > board.getNumberOfMines());
        finishButton.setEnabled(board.getFlaggedCount() == board.getNumberOfMines());
    }

    private void addMoveListener() {
        this.canvas.addMouseMoveListener(it -> {
            if (over) return;
            int x = it.getOffsetX();
            int y = it.getOffsetY();
            if (x > 5 && x < C_SIZE * C_COUNT - 5 && y > 5 && y < C_SIZE * C_COUNT - 5) {
                x = (x - 5) / (int) C_SIZE;
                y = (y - 5) / (int) C_SIZE;
                int oldX = lastCursorPosition.getLeft();
                int oldY = lastCursorPosition.getRight();
                if (x != oldX || y != oldY) {
                    paintTile(oldX, oldY);
                    paintHollowRect(x, y, "#39547D");
                    lastCursorPosition = Pair.of(x, y);
                }
            }
        });
    }

    private void paintBoard() {
        CanvasRenderingContext2D ctx = canvas.getContext();
        for (int i = 0; i < C_COUNT; i++) {
            for (int j = 0; j < C_COUNT; j++) {
                paintTile(i, j);
            }
        }
    }

    private void paintTile(int x, int y) {
        var cell = board.getCell(x, y);
        switch (cell.getVisibility()) {
            case CLOSED -> paintRect(x, y, getClosedColor(x, y));
            case OPENED -> paintOpenTile(x, y);
            case FLAGGED -> paintFlaggedTile(x, y);
        }
    }

    private void paintFlaggedTile(int x, int y) {
        paintRect(x, y, getClosedColor(x, y));
        var cell = board.getCell(x, y);
        var flag = cell.getFlag();
        paintPiece(x, y, flag, "white");
    }

    private void paintOpenTile(int x, int y) {
        paintRect(x, y, "#ADB9C9");
        paintHollowRect(x, y, "black");
        var cell = board.getCell(x, y);
        switch (cell.getStatus()) {
            case PIECED -> {
                if (cell.getVisibility() == Board.CellVisibility.FLAGGED && cell.getFlag() == cell.getPiece()) {
                    paintPiece(x, y, cell.getPiece(), "white");
                }
                paintPiece(x, y, cell.getPiece(), "black");
                gameOver();
            }
            case BEATEN -> paintBeats(x, y, cell.getBeaten());
            default -> {}
        }
    }


    private String getClosedColor(int x, int y) {
        return (x % 2 + y % 2) % 2 == 0 ? "#076271" : "#20457D";
    }
    private void paintRect(int x, int y, String color) {
        var ctx = canvas.getContext();
        ctx.setFillStyle(color);
        ctx.fillRect(x * C_SIZE + 5, y * C_SIZE + 5, C_SIZE, C_SIZE);
    }

    private void paintHollowRect(int x, int y, String color) {
        var ctx = canvas.getContext();
        ctx.setStrokeStyle(color);
        ctx.setLineWidth(4);
        ctx.strokeRect(x * C_SIZE + 7, y * C_SIZE + 7, C_SIZE-4, C_SIZE-4);
    }

    private void paintBeats(int x, int y, int beaten) {
        var ctx = canvas.getContext();
        ctx.setFillStyle("black");
        ctx.setFont(C_SIZE / 2 + "px serif");
        ctx.fillText(String.valueOf(beaten), x * C_SIZE + 5 + C_SIZE / 2.6087, y * C_SIZE + 5 + C_SIZE / 1.5);
    }

    private void paintPiece(int x, int y, Piece piece, String color) {
        var ctx = canvas.getContext();
        String name = "question";
        if (piece != null) {
            name = piece.name().toLowerCase(Locale.ENGLISH);
        }
        ctx.drawImage("resources/pieces/"
                + name
                + "_" + color + ".png", x * C_SIZE + 5 + C_SIZE * 0.03, y * C_SIZE + 5 + C_SIZE * 0.03, C_SIZE * 0.94, C_SIZE * 0.94);
    }

    private void gameOver() {
        if (this.over) return;
        this.over = true;
        finishButton.setText("Почати заново");
        finishButton.setEnabled(true);
        createLose().open();
    }
}
