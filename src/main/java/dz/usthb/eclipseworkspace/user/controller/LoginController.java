package dz.usthb.eclipseworkspace.user.controller;

import dz.usthb.eclipseworkspace.user.service.AuthService;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class LoginController {

    private final WebView webView;
    private final AuthService authService;

    public LoginController(WebView webView, AuthService authService) {
        this.webView = webView;
        this.authService = authService;
        initialize();
    }

    private void initialize() {
        WebEngine engine = webView.getEngine();

        // Load login page
        engine.load(getClass().getResource("/ressources/view/register.html").toExternalForm());

        // Expose Java bridge to JS after page load
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                JavaAuthBridge bridge = new JavaAuthBridge(authService, engine);
                window.setMember("javaAuth", bridge);
            }
        });
    }
}
