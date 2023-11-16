package ua.oleksiienko.utils;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import org.vaadin.pekkam.event.MouseMoveEvent;

public class Canvas extends org.vaadin.pekkam.Canvas {
    /**
     * Creates a new canvas component with the given size.
     * <p>
     * Use the API provided by {@link #getContext()} to render graphics on the
     * canvas.
     * <p>
     * The width and height parameters will be used for the canvas' coordinate
     * system. They will determine the size of the component in pixels, unless
     * you explicitly set the component's size with {@link #setWidth(String)} or
     * {@link #setHeight(String)}.
     *
     * @param width  the width of the canvas
     * @param height the height of the canvas
     */
    public Canvas(int width, int height) {
        super(width, height);
        this.getElement().executeJs("$0.oncontextmenu = function(e) { e.preventDefault();}", this.getElement());
    }

    public Registration addRightClickListener(ComponentEventListener<RightClickEvent> listener) {
        return addListener(RightClickEvent.class, listener);
    }
}
