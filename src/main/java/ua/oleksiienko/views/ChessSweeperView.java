package ua.oleksiienko.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.model.Subtitle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.Theme;
import org.springframework.beans.factory.annotation.Autowired;
import ua.oleksiienko.entities.Board;
import ua.oleksiienko.services.PainterService;

@PageTitle("Chess Sweeper")
@Route(value = "chess-sweeper")
@RouteAlias(value = "")
public class ChessSweeperView extends VerticalLayout {

    public ChessSweeperView(@Autowired PainterService painter) {
        configureView();
        H3 title = new H3("ШахоСапер");
        title.getStyle().set("margin", "0px 20px 20px");
        H4 subtitle = new H4("Курсова робота з ООП, Виконав: Олексієнко Павло");
        subtitle.getStyle().set("margin", "0px 20px 20px");
        H4 instruction = new H4("ЛКМ - відкрити незайняту клітинку, ПКМ - поставити прапорець міни");
        instruction.getStyle().set("margin", "0px 20px 20px");
        add(title);
        add(subtitle);
        add(instruction);
        HorizontalLayout ui = new HorizontalLayout();
        TextField score = createScoreField();
        Button finishButton = createFinishButton();
        ui.add(score);
        ui.add(finishButton);
        ui.setAlignSelf(Alignment.END, finishButton);
        add(ui);
        Board board = new Board(8);
        UI.getCurrent().getPage().retrieveExtendedClientDetails(
                details -> painter.init(this, board, score, finishButton, details.getWindowInnerWidth() * 0.9));
    }

    private static TextField createScoreField() {
        TextField score = new TextField();
        score.setEnabled(false);
        score.setLabel("Міни");
        score.addThemeVariants(TextFieldVariant.LUMO_ALIGN_CENTER);
        score.setValue("0/5");
        return score;
    }

    private static Button createFinishButton() {
        Button button = new Button("Закінчити гру");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.setEnabled(false);
        return button;
    }

    private void configureView() {
        setSpacing(false);
        setPadding(false);
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }

}
