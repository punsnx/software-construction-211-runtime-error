package ku.cs.controllers.faculty;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ku.cs.controllers.ChangePasswordController;
import ku.cs.controllers.advisor.AdvisorStudentRequestsController;
import ku.cs.models.request.Request;
import ku.cs.models.request.approver.Approver;
import ku.cs.models.request.approver.ApproverList;
import ku.cs.models.user.FacultyUser;
import ku.cs.models.user.Student;
import ku.cs.models.user.User;
import ku.cs.services.ApproverListFileDatasource;
import ku.cs.services.FXRouter;
import ku.cs.views.components.CropImage;
import ku.cs.views.components.DefaultButton;
import ku.cs.views.components.DefaultLabel;
import ku.cs.views.components.RouteButton;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class FacultyApproverController {
    @FXML
    private Label pageTitleLabel;

    @FXML
    private HBox tableHeaderHBox;
    @FXML
    private Label tableViewLabel;
    @FXML
    private Button addApproverButton;
    @FXML
    private TableView approverTableView;

    @FXML
    private VBox imageEditorVBox;
    @FXML
    private ImageView approverImageView;
    @FXML
    private Label approverNameLabel;
    @FXML
    private Label approverPositionLabel;

    @FXML
    private HBox uploadHBox;
    @FXML
    private ImageView iconPdfImageView;
    @FXML
    private Label fileNameLabel;
    @FXML
    private Button removeFileButton;
    @FXML
    private ImageView iconRemoveFileImageView;

    @FXML
    private Button uploadFileButton;

    @FXML
    private Button backButton;
    @FXML
    private Stage currentPopupStage;

    private FacultyUser loginUser;

    @FXML
    private TextField searchTextField;


    @FXML
    public void initialize() {
        loadApprover();
        showTable();
        approverEditPopUp();
        initLabel();
        initButton();
        initTableHeaderHBox();
        initImageEditorVBox();
        loginUser = (FacultyUser) FXRouter.getData();
        new CropImage(approverImageView).setClipImage(50, 50);

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            search(newValue);
        });
    }

    private void initLabel() {
        new DefaultLabel(pageTitleLabel);
        new DefaultLabel(tableViewLabel);
        new DefaultLabel(approverNameLabel);
        new DefaultLabel(approverPositionLabel);
        new DefaultLabel(fileNameLabel);
    }

    private void initButton() {
        new RouteButton(backButton, "faculty-page", "transparent", "#a6a6a6", "#000000");
        new DefaultButton(addApproverButton, "#FFE0A4", "#a6a6a6", "#000000").changeBackgroundRadius(15);
        new DefaultButton(removeFileButton, "transparent", "#a6a6a6", "#000000");
        new DefaultButton(uploadFileButton, "#ABFFA4", "#a6a6a6", "#000000").changeBackgroundRadius(15);
    }

    private void search(String newValue) {
        ApproverListFileDatasource approverListFileDatasource = new ApproverListFileDatasource("approver");
        String tier = "faculty";
        ApproverList approverList = approverListFileDatasource.query(tier, null);

        Set<Approver> filteredApprovers = approverList.getApprovers()
                .stream()
                .filter(approver -> approver.getFirstname().toLowerCase().contains(newValue.toLowerCase()) ||
                        approver.getLastname().toLowerCase().contains(newValue.toLowerCase()))
                .collect(Collectors.toSet());

        approverTableView.getItems().clear();
        approverTableView.getItems().addAll(filteredApprovers);
    }


    private void initTableHeaderHBox() {
        tableHeaderHBox.setSpacing(20);
    }

    private void initImageEditorVBox() {
        imageEditorVBox.setSpacing(5);
        approverNameLabel.setPadding(new Insets(15, 0, 0, 0));
    }

    private void showTable() {
        approverTableView.getColumns().clear();
        Label placeHolder = new Label("ไม่พบข้อมูล");
        approverTableView.setPlaceholder(placeHolder);
        approverTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Approver, String> nameColumn = new TableColumn<>("ชื่อ");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("Firstname"));

        TableColumn<Approver, String> lastnameColumn = new TableColumn<>("นามสกุล");
        lastnameColumn.setCellValueFactory(new PropertyValueFactory<>("Lastname"));

        TableColumn<Approver, String> tierColumn = new TableColumn<>("ตำแหน่ง");
        tierColumn.setCellValueFactory(new PropertyValueFactory<>("role"));


        approverTableView.getColumns().addAll(nameColumn, lastnameColumn, tierColumn);
        search(searchTextField.getText());
    }
    private void approverEditPopUp() {
        approverTableView.setOnMouseClicked(e -> {
            Object selected = approverTableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String popUpPath = "/ku/cs/views/faculty-edit-approver-pane.fxml";
                try {
                    if (currentPopupStage == null || !currentPopupStage.isShowing()) {
                        currentPopupStage = new Stage();
                    }

                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(popUpPath));
                    Pane editPane = fxmlLoader.load();

                    Approver selectedApprover = (Approver) selected;
                    EditApproverController controller = fxmlLoader.getController();
                    controller.setApprover(selectedApprover);
                    controller.setStage(currentPopupStage);
                    controller.setApproverDetail(
                            selectedApprover.getFirstname(),
                            selectedApprover.getLastname(),
                            selectedApprover.getRole()
                    );

                    Scene scene = new Scene(editPane);
                    currentPopupStage.setScene(scene);
                    currentPopupStage.setTitle("Edit Approver");
                    currentPopupStage.initModality(Modality.APPLICATION_MODAL);

                    currentPopupStage.setOnHidden(event -> {
                        loadApprover();
                        approverTableView.refresh();
                    });

                    currentPopupStage.show();

                } catch (IOException ex) {
                    System.err.println("Error loading popup: " + ex.getMessage());
                }
            }
        });
    }

    private void loadApprover() {
        ApproverListFileDatasource approverListFileDatasource = new ApproverListFileDatasource("approver");
        String tier = "faculty";
        ApproverList approverList = approverListFileDatasource.query(tier, null);

        approverTableView.getItems().setAll(approverList.getApprovers());
    }

    @FXML
    private void onAddApproverButtonClicked() {
        try {
            if (currentPopupStage == null || !currentPopupStage.isShowing()) {
                currentPopupStage = new Stage();
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ku/cs/views/faculty-add-approver-pane.fxml"));
                Scene scene = new Scene(fxmlLoader.load());

                AddApproverController controller = fxmlLoader.getController();
                controller.setLoginUser(loginUser);
                controller.setStage(currentPopupStage);

                currentPopupStage.setScene(scene);
                currentPopupStage.initModality(Modality.APPLICATION_MODAL);
                currentPopupStage.setTitle("Add approver");

                currentPopupStage.setOnHidden(event -> {
                    loadApprover();
                    approverTableView.refresh();
                });

                currentPopupStage.show();
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


}