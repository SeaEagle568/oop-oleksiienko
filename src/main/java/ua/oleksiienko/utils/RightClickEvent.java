package ua.oleksiienko.utils;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import org.vaadin.pekkam.Canvas;
import org.vaadin.pekkam.event.MouseEvent;

@DomEvent("contextmenu")
public class RightClickEvent extends MouseEvent {

    public RightClickEvent(Canvas source,
                           boolean fromClient,
                           @EventData("event.offsetX") int clientX,
                           @EventData("event.offsetY") int clientY,
                           @EventData("event.button") int button) {

        super(source, fromClient, clientX, clientY, button);
    }
}
