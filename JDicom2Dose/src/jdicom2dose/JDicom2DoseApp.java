/*
 * JDicom2DoseApp.java
 */

package jdicom2dose;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * La clase ejecutable del proyecto.
 */
public class JDicom2DoseApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        show(new JDicom2DoseView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of JDicom2DoseApp
     */
    public static JDicom2DoseApp getApplication() {
        return Application.getInstance(JDicom2DoseApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(JDicom2DoseApp.class, args);
    }
}
