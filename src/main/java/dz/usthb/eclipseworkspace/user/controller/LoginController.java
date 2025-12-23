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

        // Enable JavaScript console messages
        engine.setOnAlert(event -> {
            System.out.println("JavaScript Alert: " + event.getData());
        });

        // Load login page
        String loginPageUrl = getClass().getResource("/ressources/view/register.html").toExternalForm();
        System.out.println("Loading login page from: " + loginPageUrl);
        engine.load(loginPageUrl);

        // Expose Java bridge to JS after page load
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            System.out.println("WebEngine state changed: " + oldState + " -> " + newState);

            if (newState == Worker.State.SUCCEEDED) {
                try {
                    // Get window object
                    JSObject window = (JSObject) engine.executeScript("window");
                    System.out.println("Window object obtained: " + (window != null));

                    // Create bridge
                    JavaAuthBridge bridge = new JavaAuthBridge(authService, engine);

                    // Expose bridge to JavaScript
                    window.setMember("javaAuth", bridge);
                    System.out.println("✓ Java bridge successfully exposed to JavaScript");

                    // Test if bridge exists
                    Boolean bridgeExists = (Boolean) engine.executeScript("typeof window.javaAuth !== 'undefined'");
                    System.out.println("Bridge exists: " + bridgeExists);

                    // Test if test method is callable
                    try {
                        Object testResult = engine.executeScript("window.javaAuth.test()");
                        System.out.println("Bridge test() method result: " + testResult);
                    } catch (Exception e) {
                        System.err.println("Failed to call test() method: " + e.getMessage());
                    }

                    // Check what methods are available
                    try {
                        engine.executeScript(
                                "console.log('Available methods on javaAuth:', Object.getOwnPropertyNames(window.javaAuth));"
                        );
                    } catch (Exception e) {
                        System.err.println("Could not list methods: " + e.getMessage());
                    }

                    // Test if login method exists
                    try {
                        Boolean loginExists = (Boolean) engine.executeScript("typeof window.javaAuth.login === 'function'");
                        System.out.println("login() method exists: " + loginExists);
                    } catch (Exception e) {
                        System.err.println("Failed to check login method: " + e.getMessage());
                    }

                    // Test if register method exists
                    try {
                        Boolean registerExists = (Boolean) engine.executeScript("typeof window.javaAuth.register === 'function'");
                        System.out.println("register() method exists: " + registerExists);
                    } catch (Exception e) {
                        System.err.println("Failed to check register method: " + e.getMessage());
                    }

                } catch (Exception e) {
                    System.err.println("Error exposing Java bridge: " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (newState == Worker.State.FAILED) {
                System.err.println("Failed to load page");
            }
        });

        // Log any JavaScript errors
        engine.getLoadWorker().exceptionProperty().addListener((obs, oldEx, newEx) -> {
            if (newEx != null) {
                System.err.println("JavaScript error: " + newEx.getMessage());
                newEx.printStackTrace();
            }
        });
    }
}