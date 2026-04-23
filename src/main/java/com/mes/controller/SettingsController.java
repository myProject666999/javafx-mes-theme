package com.mes.controller;

import com.mes.config.ThemeManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SettingsController {

    private final ThemeManager themeManager;

    @FXML
    private ComboBox<ThemeManager.ThemeType> themeComboBox;

    @FXML
    private ComboBox<String> uiFontFamilyComboBox;

    @FXML
    private Spinner<Double> uiFontSizeSpinner;

    @FXML
    private ComboBox<String> tableFontFamilyComboBox;

    @FXML
    private Spinner<Double> tableFontSizeSpinner;

    @FXML
    private Label previewLabel;

    @FXML
    private TableView<PreviewData> previewTable;

    @FXML
    private TableColumn<PreviewData, String> col1;

    @FXML
    private TableColumn<PreviewData, String> col2;

    @FXML
    private TableColumn<PreviewData, String> col3;

    public SettingsController() {
        this.themeManager = ThemeManager.getInstance();
    }

    @FXML
    public void initialize() {
        initPreviewTable();
        initThemeComboBox();
        initFontComboBoxes();
        initFontSizeSpinners();
        loadCurrentSettings();
    }

    private void initPreviewTable() {
        if (previewTable.getColumns().size() >= 3) {
            TableColumn<PreviewData, String> col0 = (TableColumn<PreviewData, String>) previewTable.getColumns().get(0);
            TableColumn<PreviewData, String> col1 = (TableColumn<PreviewData, String>) previewTable.getColumns().get(1);
            TableColumn<PreviewData, String> col2 = (TableColumn<PreviewData, String>) previewTable.getColumns().get(2);

            col0.setCellValueFactory(cellData -> cellData.getValue().col1Property());
            col1.setCellValueFactory(cellData -> cellData.getValue().col2Property());
            col2.setCellValueFactory(cellData -> cellData.getValue().col3Property());
        }

        previewTable.setItems(FXCollections.observableArrayList(
                new PreviewData("1", "用户管理", "系统用户管理功能"),
                new PreviewData("2", "角色管理", "角色权限配置"),
                new PreviewData("3", "产品管理", "物料产品信息维护")
        ));
    }

    public static class PreviewData {
        private final SimpleStringProperty col1;
        private final SimpleStringProperty col2;
        private final SimpleStringProperty col3;

        public PreviewData(String col1, String col2, String col3) {
            this.col1 = new SimpleStringProperty(col1);
            this.col2 = new SimpleStringProperty(col2);
            this.col3 = new SimpleStringProperty(col3);
        }

        public SimpleStringProperty col1Property() { return col1; }
        public SimpleStringProperty col2Property() { return col2; }
        public SimpleStringProperty col3Property() { return col3; }
    }

    private void initThemeComboBox() {
        themeComboBox.setItems(themeManager.getAvailableThemes());
        themeComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ThemeManager.ThemeType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        themeComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ThemeManager.ThemeType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });

        themeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                themeManager.setTheme(newValue);
            }
        });
    }

    private void initFontComboBoxes() {
        List<String> fontFamilies = Font.getFamilies().stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        uiFontFamilyComboBox.setItems(FXCollections.observableArrayList(fontFamilies));
        tableFontFamilyComboBox.setItems(FXCollections.observableArrayList(fontFamilies));

        uiFontFamilyComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-family: '" + item + "';");
                }
            }
        });

        tableFontFamilyComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-family: '" + item + "';");
                }
            }
        });

        uiFontFamilyComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                themeManager.setUiFontFamily(newValue);
                updatePreview();
            }
        });

        tableFontFamilyComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                themeManager.setTableFontFamily(newValue);
                updateTablePreview();
            }
        });
    }

    private void initFontSizeSpinners() {
        SpinnerValueFactory.DoubleSpinnerValueFactory uiSizeFactory = 
                new SpinnerValueFactory.DoubleSpinnerValueFactory(8, 24, 13, 1);
        uiFontSizeSpinner.setValueFactory(uiSizeFactory);

        SpinnerValueFactory.DoubleSpinnerValueFactory tableSizeFactory = 
                new SpinnerValueFactory.DoubleSpinnerValueFactory(8, 24, 12, 1);
        tableFontSizeSpinner.setValueFactory(tableSizeFactory);

        uiFontSizeSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                themeManager.setUiFontSize(newValue);
                updatePreview();
            }
        });

        tableFontSizeSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                themeManager.setTableFontSize(newValue);
                updateTablePreview();
            }
        });

        uiFontSizeSpinner.setEditable(true);
        tableFontSizeSpinner.setEditable(true);
    }

    private void loadCurrentSettings() {
        themeComboBox.setValue(themeManager.getCurrentTheme());
        uiFontFamilyComboBox.setValue(themeManager.getUiFontFamily());
        uiFontSizeSpinner.getValueFactory().setValue(themeManager.getUiFontSize());
        tableFontFamilyComboBox.setValue(themeManager.getTableFontFamily());
        tableFontSizeSpinner.getValueFactory().setValue(themeManager.getTableFontSize());

        updatePreview();
        updateTablePreview();
    }

    private void updatePreview() {
        String style = String.format("-fx-font-family: '%s'; -fx-font-size: %.1fpx;",
                themeManager.getUiFontFamily(), themeManager.getUiFontSize());
        previewLabel.setStyle(style);
    }

    private void updateTablePreview() {
        String style = String.format("-fx-font-family: '%s'; -fx-font-size: %.1fpx;",
                themeManager.getTableFontFamily(), themeManager.getTableFontSize());
        previewTable.setStyle(style);
    }

    @FXML
    public void resetToDefaults() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认重置");
        alert.setHeaderText("确定要重置所有设置吗？");
        alert.setContentText("这将恢复为默认主题和字体设置。");
        
        ThemeManager.getInstance().applyToDialogPane(alert.getDialogPane());

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                themeManager.setTheme(ThemeManager.ThemeType.DARK);
                themeManager.setUiFontFamily("System");
                themeManager.setUiFontSize(13.0);
                themeManager.setTableFontFamily("System");
                themeManager.setTableFontSize(12.0);
                loadCurrentSettings();
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        ThemeManager.getInstance().applyToDialogPane(alert.getDialogPane());
        
        alert.showAndWait();
    }
}
