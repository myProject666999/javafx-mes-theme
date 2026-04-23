package com.mes.config;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Theme;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ThemeManager {

    private static ThemeManager instance;
    
    public enum ThemeType {
        DARK("深色主题 (VSCode风格)", "/css/style.css", new PrimerDark()),
        VSCODE_LIGHT("VSCode浅色主题", "/css/theme-vscode-light.css", new PrimerLight()),
        LIGHT("浅色主题", "/css/theme-light.css", new PrimerLight()),
        BLUE("蓝色主题", "/css/theme-blue.css", new PrimerLight());
        
        private final String displayName;
        private final String cssPath;
        private final Theme baseTheme;
        
        ThemeType(String displayName, String cssPath, Theme baseTheme) {
            this.displayName = displayName;
            this.cssPath = cssPath;
            this.baseTheme = baseTheme;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getCssPath() {
            return cssPath;
        }
        
        public Theme getBaseTheme() {
            return baseTheme;
        }
    }
    
    private ThemeType currentTheme;
    private String uiFontFamily;
    private double uiFontSize;
    private String tableFontFamily;
    private double tableFontSize;
    
    private Stage primaryStage;
    private Scene currentScene;
    
    private ThemeManager() {
        loadSettings();
    }
    
    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
    
    public void setCurrentScene(Scene scene) {
        this.currentScene = scene;
        applyTheme(currentTheme);
    }
    
    public ObservableList<ThemeType> getAvailableThemes() {
        return FXCollections.observableArrayList(ThemeType.values());
    }
    
    public ThemeType getCurrentTheme() {
        return currentTheme;
    }
    
    public void setTheme(ThemeType themeType) {
        this.currentTheme = themeType;
        applyTheme(themeType);
        saveSettings();
    }
    
    private void applyTheme(ThemeType themeType) {
        if (themeType.getBaseTheme() != null) {
            Application.setUserAgentStylesheet(themeType.getBaseTheme().getUserAgentStylesheet());
        }
        
        if (currentScene != null) {
            currentScene.getStylesheets().clear();
            if (themeType.getCssPath() != null) {
                currentScene.getStylesheets().add(themeType.getCssPath());
            }
            applyFontStyles();
        }
    }
    
    public String getUiFontFamily() {
        return uiFontFamily;
    }
    
    public void setUiFontFamily(String uiFontFamily) {
        this.uiFontFamily = uiFontFamily;
        applyFontStyles();
        saveSettings();
    }
    
    public double getUiFontSize() {
        return uiFontSize;
    }
    
    public void setUiFontSize(double uiFontSize) {
        this.uiFontSize = uiFontSize;
        applyFontStyles();
        saveSettings();
    }
    
    public String getTableFontFamily() {
        return tableFontFamily;
    }
    
    public void setTableFontFamily(String tableFontFamily) {
        this.tableFontFamily = tableFontFamily;
        applyFontStyles();
        saveSettings();
    }
    
    public double getTableFontSize() {
        return tableFontSize;
    }
    
    public void setTableFontSize(double tableFontSize) {
        this.tableFontSize = tableFontSize;
        applyFontStyles();
        saveSettings();
    }
    
    public void applyFontStyles() {
        if (currentScene != null) {
            String rootStyle = String.format("-fx-font-family: '%s'; -fx-font-size: %.1fpx;", 
                    uiFontFamily, uiFontSize);
            currentScene.getRoot().setStyle(rootStyle);
            
            applyTableFontToAllTables();
        }
    }
    
    private void applyTableFontToAllTables() {
        if (currentScene != null && currentScene.getRoot() != null) {
            List<TableView<?>> allTables = findAllTableViews(currentScene.getRoot());
            for (TableView<?> tableView : allTables) {
                applyTableStyle(tableView);
            }
        }
    }
    
    private List<TableView<?>> findAllTableViews(Parent parent) {
        List<TableView<?>> tables = new ArrayList<>();
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof TableView<?>) {
                tables.add((TableView<?>) node);
            }
            if (node instanceof Parent) {
                tables.addAll(findAllTableViews((Parent) node));
            }
        }
        return tables;
    }
    
    public void applyTableStyle(TableView<?> tableView) {
        if (tableView != null) {
            String tableStyle = String.format("-fx-font-family: '%s'; -fx-font-size: %.1fpx;", 
                    tableFontFamily, tableFontSize);
            tableView.setStyle(tableStyle);
        }
    }
    
    public void applyToDialogPane(DialogPane dialogPane) {
        if (dialogPane == null) {
            return;
        }
        
        dialogPane.getStylesheets().clear();
        if (currentTheme != null && currentTheme.getCssPath() != null) {
            dialogPane.getStylesheets().add(currentTheme.getCssPath());
        }
        
        String fontStyle = String.format("-fx-font-family: '%s'; -fx-font-size: %.1fpx;", 
                uiFontFamily, uiFontSize);
        dialogPane.setStyle(fontStyle);
        
        Scene dialogScene = dialogPane.getScene();
        if (dialogScene != null) {
            dialogScene.getStylesheets().clear();
            if (currentTheme != null && currentTheme.getCssPath() != null) {
                dialogScene.getStylesheets().add(currentTheme.getCssPath());
            }
        }
    }
    
    private void loadSettings() {
        Properties props = new Properties();
        File configFile = new File(System.getProperty("user.home"), ".mes-theme.properties");
        
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        String themeName = props.getProperty("theme", "DARK");
        try {
            currentTheme = ThemeType.valueOf(themeName);
        } catch (IllegalArgumentException e) {
            currentTheme = ThemeType.DARK;
        }
        
        uiFontFamily = props.getProperty("ui.font.family", "System");
        uiFontSize = Double.parseDouble(props.getProperty("ui.font.size", "13.0"));
        tableFontFamily = props.getProperty("table.font.family", "System");
        tableFontSize = Double.parseDouble(props.getProperty("table.font.size", "12.0"));
    }
    
    private void saveSettings() {
        Properties props = new Properties();
        props.setProperty("theme", currentTheme.name());
        props.setProperty("ui.font.family", uiFontFamily);
        props.setProperty("ui.font.size", String.valueOf(uiFontSize));
        props.setProperty("table.font.family", tableFontFamily);
        props.setProperty("table.font.size", String.valueOf(tableFontSize));
        
        File configFile = new File(System.getProperty("user.home"), ".mes-theme.properties");
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            props.store(fos, "MES Theme Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
