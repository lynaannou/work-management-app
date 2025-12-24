package dz.usthb.eclipseworkspace.user.controller;

import dz.usthb.eclipseworkspace.user.service.AuthService;
import dz.usthb.eclipseworkspace.user.service.UserService;
import dz.usthb.eclipseworkspace.user.util.Session;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class MainController {

    // ==============================
    // CALLBACK INTERFACE
    // ==============================
    public interface LoginSuccessListener {
        void onLoginSuccess();
    }

    private LoginSuccessListener loginSuccessListener;

    public void setLoginSuccessListener(LoginSuccessListener listener) {
        this.loginSuccessListener = listener;
    }

    // ==============================
    // FIELDS
    // ==============================
    private final WebView webView;
    private final WebEngine webEngine;
    private final AuthService authService;
    private final UserService userService;
    private JavaBridge javaBridge;

    // ==============================
    // CONSTRUCTOR
    // ==============================
    public MainController(WebView webView, AuthService authService, UserService userService) {
        this.webView = webView;
        this.webEngine = webView.getEngine();
        this.authService = authService;
        this.userService = userService;
        initialize();
    }

    // ==============================
    // INITIALIZATION
    // ==============================
    private void initialize() {

        webEngine.setOnAlert(event ->
                System.out.println("JavaScript Alert: " + event.getData())
        );

        loadInitialPage();

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            System.out.println("WebEngine state changed: " + oldState + " -> " + newState);

            if (newState == Worker.State.SUCCEEDED) {
                exposeBridge();
            }
        });
    }

    // ==============================
    // INITIAL PAGE
    // ==============================
    private void loadInitialPage() {
        if (Session.getInstance().isAuthenticated()) {
            loadPage("projects.html");
        } else {
            loadPage("register.html");
        }
    }

    // ==============================
    // BRIDGE
    // ==============================
    private void exposeBridge() {
        try {
            JSObject window = (JSObject) webEngine.executeScript("window");

            if (javaBridge == null) {
                javaBridge = new JavaBridge(this, authService, userService);
            }

            window.setMember("javaBridge", javaBridge);

            System.out.println("✓ Java bridge successfully exposed");
            System.out.println("Bridge verification: " +
                    webEngine.executeScript("typeof window.javaBridge !== 'undefined'")
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==============================
    // NAVIGATION
    // ==============================
    public void loadPage(String pageName) {
        Platform.runLater(() -> {
            String url = getClass()
                    .getResource("/ressources/view/" + pageName)
                    .toExternalForm();

            System.out.println("Loading page: " + url);
            webEngine.load(url);
        });
    }

    // ==============================
    // LOGIN CALLBACK (CALLED BY JavaBridge)
    // ==============================
    public void onLoginSuccess() {
        System.out.println("✅ Login success callback received");

        if (loginSuccessListener != null) {
            loginSuccessListener.onLoginSuccess();
        }

        loadPage("projects.html");
    }

    // ==============================
    // LOGOUT
    // ==============================
    public void logout() {
        Session.getInstance().clear();
        loadPage("register.html");
    }
}
