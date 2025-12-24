package dz.usthb.eclipseworkspace.user.controller;

import dz.usthb.eclipseworkspace.user.service.AuthService;
import dz.usthb.eclipseworkspace.user.service.UserService;
import dz.usthb.eclipseworkspace.user.util.Session;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

/**
 * Main Controller - Manages the entire application navigation
 * Single WebView instance that loads different pages based on user actions
 */
public class MainController {

    private final WebView webView;
    private final WebEngine webEngine;
    private final AuthService authService;
    private JavaBridge javaBridge;
    private final UserService userService;

    public MainController(WebView webView, AuthService authService,UserService userService) {
        this.webView = webView;
        this.webEngine = webView.getEngine();
        this.authService = authService;
        this.userService = userService;
        initialize();
    }

    private void initialize() {
        // Enable JavaScript console messages
        webEngine.setOnAlert(event -> {
            System.out.println("JavaScript Alert: " + event.getData());
        });

        // Load initial page (login if not authenticated, workspace if authenticated)
        loadInitialPage();

        // Expose Java bridge to JS after each page load
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            System.out.println("WebEngine state changed: " + oldState + " -> " + newState);

            if (newState == Worker.State.SUCCEEDED) {
                exposeBridge();
            } else if (newState == Worker.State.FAILED) {
                System.err.println("Failed to load page");
            }
        });

        // Log any JavaScript errors
        webEngine.getLoadWorker().exceptionProperty().addListener((obs, oldEx, newEx) -> {
            if (newEx != null) {
                System.err.println("JavaScript error: " + newEx.getMessage());
                newEx.printStackTrace();
            }
        });
    }

    /**
     * Load initial page based on authentication status
     */
    private void loadInitialPage() {
        Session session = Session.getInstance();
        if (session.isAuthenticated()) {
            loadPage("workspace.html");
        } else {
            loadPage("register.html");
        }
    }

    /**
     * Expose Java bridge to JavaScript
     */
    private void exposeBridge() {
        try {
            JSObject window = (JSObject) webEngine.executeScript("window");

            // Create or reuse the bridge
            if (javaBridge == null) {
                javaBridge = new JavaBridge(this, authService,userService);
            }

            window.setMember("javaBridge", javaBridge);
            System.out.println("✓ Java bridge successfully exposed to JavaScript");

            // Verify bridge
            Boolean bridgeExists = (Boolean) webEngine.executeScript("typeof window.javaBridge !== 'undefined'");
            System.out.println("Bridge verification: " + bridgeExists);

        } catch (Exception e) {
            System.err.println("Error exposing Java bridge: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load a page in the WebView
     * This is called by the bridge when navigating between pages
     */
    public void loadPage(String pageName) {
        Platform.runLater(() -> {
            try {
                String url = getClass()
                        .getResource("/ressources/view/" + pageName)
                        .toExternalForm();

                System.out.println("Loading page: " + url);
                webEngine.load(url);

            } catch (Exception e) {
                System.err.println("Error loading page: " + pageName);
                e.printStackTrace();
            }
        });
    }


    /**
     * Check authentication before loading a page
     */
    public boolean checkAuthAndLoad(String pageName) {
        Session session = Session.getInstance();

        if (!session.isAuthenticated()) {
            System.out.println("User not authenticated, redirecting to login");
            loadPage("register.html");
            return false;
        }

        loadPage(pageName);
        return true;
    }

    /**
     * Logout and return to login page
     */
    public void logout() {
        Session.getInstance().clear();
        loadPage("register.html");
    }

    public WebView getWebView() {
        return webView;
    }

    public WebEngine getWebEngine() {
        return webEngine;
    }
}