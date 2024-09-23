package ku.cs.controllers.student;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import ku.cs.controllers.requests.information.*;
import ku.cs.models.request.*;
import ku.cs.models.user.Student;
import ku.cs.models.user.User;
import ku.cs.services.RequestListFileDatasource;
import ku.cs.views.components.DefaultTableView;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class StudentRequestInfoController {
    private Student loginUser;
    private Request request;
    private DefaultTableView<Request> tableView;
    private RequestListFileDatasource datasource;
    private DateTimeFormatter dateTimeFormatter;
    @FXML TableView requestLogTableView;
    @FXML Label requestTypeLabel;
    @FXML Label createdDateLabel;
    @FXML Label requestNumberLabel;
    @FXML Button requestStatus1;
    @FXML Button requestStatus2;
    @FXML BorderPane borderPane;
    @FXML ImageView statusIconImageView;

    @FXML
    public void initialize() {
        tableView = new DefaultTableView<>(requestLogTableView);
        datasource = new RequestListFileDatasource("data");
        dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    }

    public void showInfo(){
        requestTypeLabel.setText(request.getRequestType());
        createdDateLabel.setText(request.getDate().format(dateTimeFormatter));
        requestNumberLabel.setText(request.getUuid().toString());
        String status1 = request.getStatusNow(), status2 = request.getStatusNext();
        Pattern rejected = Pattern.compile(".*ปฏิเสธ.*");
        Pattern newlyCreated = Pattern.compile(".*ใหม่.*");
        Pattern inProgress = Pattern.compile(".*ต่อ.*");
        Pattern completed = Pattern.compile(".*ครบถ้วน.*");
        if (newlyCreated.matcher(status1).matches()){
            requestStatus1.setStyle(
                    "-fx-background-color: #EBEEFF; " +
                            "-fx-border-color: #4E7FFF; " +
                            "-fx-text-fill: #4E7FFF;"
            );

            requestStatus2.setStyle(
                    "-fx-background-color: #FFF6E8;" +
                            " -fx-border-color: #ED9B22;" +
                            " -fx-text-fill: #ED9B22;"
            );

        }
        if (inProgress.matcher(status2).matches()){
            requestStatus2.setStyle(
                    "-fx-background-color: #FFF6E8; " +
                            "-fx-border-color: #ED9B22; " +
                            "-fx-text-fill: #ED9B22;"
            );

        }

        if (rejected.matcher(status1).matches()){
            requestStatus1.setStyle(
                    "-fx-background-color: #FFDEDE; " +
                            "-fx-border-color: #FE6463; " +
                            "-fx-text-fill: #FE6463;"
            );

            requestStatus2.setStyle(
                    "-fx-background-color: #FFDEDE; " +
                            "-fx-border-color: #FE6463; " +
                            "-fx-text-fill: #FE6463;"
            );
            Image statusIcon = new Image(getClass().getResourceAsStream("/images/icons/request-status-rejected.png"));
            statusIconImageView.setImage(statusIcon);

        }

        if (completed.matcher(status2).matches()){
            Image statusIcon = new Image(getClass().getResourceAsStream("/images/icons/request-status-approved.png"));
            statusIconImageView.setImage(statusIcon);
        }

        requestStatus1.setText(request.getStatusNow());
        requestStatus2.setText(request.getStatusNext());
    }

    public void showTable() {
        tableView.getTableView().getColumns().clear();
        tableView.getTableView().getStylesheets().clear();
        TableColumn<Request, LocalDateTime> date = new TableColumn<>("วันที่และเวลา");
        date.setCellValueFactory(new PropertyValueFactory<>("TimeStamp"));
        setTableDateTime(date);
        TableColumn<Request, String> statusNow = new TableColumn<>("สถานะคำร้อง");
        statusNow.setCellValueFactory(new PropertyValueFactory<>("statusNow"));
        TableColumn<Request, String> statusNext = new TableColumn<>("");
        statusNext.setCellValueFactory(new PropertyValueFactory<>("statusNext"));
        date.setSortType(TableColumn.SortType.DESCENDING);
        requestLogTableView.getColumns().add(date);
        requestLogTableView.getColumns().add(statusNow);
        requestLogTableView.getColumns().add(statusNext);
        date.setMaxWidth(300);

        setTableStatus(statusNow, "now");
        setTableStatus(statusNext, "next");
        tableView.getTableView().getSortOrder().add(date);
        tableView.addStyleSheet("/ku/cs/styles/admin-page-style.css");

        RequestList requestList = datasource.queryLog(request);
        for (Request req : requestList.getRequests()) {
            tableView.getTableView().getItems().add(req);
        }
        tableView.getTableView().sort();
        date.setSortable(false);
    }

    public void onBackButtonClick() {
        try {
            String viewPath = "/ku/cs/views/student-requests-pane.fxml";
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource(viewPath));
            Pane pane = fxmlLoader.load();
            StudentRequestsController controller = fxmlLoader.getController();
            controller.setLoginUser(loginUser);
            controller.initialize();
            borderPane.setCenter(pane);
            controller.setBorderPane(borderPane);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setLoginUser(Student loginUser) {
        this.loginUser = loginUser;
    }

    private void setTableDateTime(TableColumn<Request, LocalDateTime> date) {
        date.setCellFactory(column -> new TextFieldTableCell<>(new StringConverter<LocalDateTime>() {
            @Override
            public String toString(LocalDateTime lastLogin) {
                return lastLogin != null ? lastLogin.format(dateTimeFormatter) : "";
            }

            @Override
            public LocalDateTime fromString(String string) {
                return LocalDateTime.parse(string, dateTimeFormatter);
            }
        }));
    }
    private void setTableStatus(TableColumn<Request, String> status, String whichStatus) {
        status.setMaxWidth(250);
        status.setSortable(false);
        status.setCellFactory(c -> new TableCell<Request, String>() {
            Request request;
            Button statusCell = new Button();
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                statusCell.setMouseTransparent(true);
                statusCell.setFocusTraversable(false);
                String buttonStyle = "-fx-border-radius: 3;" +
                        "-fx-font-size: 18;" +
                        "-fx-text-alignment: center;" +
                        "-fx-cursor: none;" +
                        "-fx-pref-width: 234;" +
                        "-fx-pref-height: 35;";
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    request = getTableView().getItems().get(getIndex());
                    if (whichStatus.equals("now")) {
                        statusCell.setText(request.getStatusNow());
                    } else if (whichStatus.equals("next")) {
                        statusCell.setText(request.getStatusNext());
                    }
                    setGraphic(statusCell);
                    statusCell.setStyle("-fx-background-color: #F2FFF7;" +
                            "-fx-border-color: #00B448;" +
                            "-fx-text-fill: #00B448;" +
                            buttonStyle);
                    Pattern rejected = Pattern.compile(".*ปฏิเสธ.*");
                    Pattern newlyCreated = Pattern.compile(".*ใหม่.*");
                    Pattern inProgress = Pattern.compile(".*ต่อ.*");
                    if (newlyCreated.matcher(statusCell.getText()).matches()){
                        statusCell.setStyle(
                                "-fx-background-color: #EBEEFF; " +
                                        "-fx-border-color: #4E7FFF; " +
                                        "-fx-text-fill: #4E7FFF;"+
                                        buttonStyle
                        );
                    }
                    if (rejected.matcher(statusCell.getText()).matches()) {
                        statusCell.setStyle(
                                "-fx-background-color: #FFDEDE; " +
                                        "-fx-border-color: #FE6463; " +
                                        "-fx-text-fill: #FE6463;" +
                                        buttonStyle
                        );
                    }
                    if (inProgress.matcher(statusCell.getText()).matches()){
                        statusCell.setStyle(
                                "-fx-background-color: #FFF6E8; " +
                                        "-fx-border-color: #ED9B22; " +
                                        "-fx-text-fill: #ED9B22;" +
                                        buttonStyle
                        );

                    }

                    if (rejected.matcher(statusCell.getText()).matches()) {
                        statusCell.setStyle(
                                "-fx-background-color: #FFDEDE; " +
                                        "-fx-border-color: #FE6463; " +
                                        "-fx-text-fill: #FE6463;" +
                                        buttonStyle
                        );
                    }
                }
            }
        });
    }


    public void setRequest(Request request) {
        this.request = request;
    }

    public void setBorderPane(BorderPane borderPane) {
        this.borderPane = borderPane;
    }

    @FXML
    private void seeInformation() {
        goToInformationPage();
    }

    private void goToInformationPage() {
        try {
            String viewPath = "/ku/cs/views/main-information.fxml";
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource(viewPath));
            Pane pane = fxmlLoader.load();
            MainInformationController controller = fxmlLoader.getController();
            controller.setLoginUser(loginUser);
            controller.setRequest(request);
            controller.setBorderPane(borderPane);
            controller.setBackPage("student");
            controller.initializeMainInformation();
            if (request instanceof GeneralRequestForm) {
                controller.setTitleLabel("ใบคำร้องทั่วไป");
            } else if (request instanceof RegisterRequestForm) {
                controller.setTitleLabel("คำร้องขอลงทะเบียน");
            } else if (request instanceof AcademicLeaveRequestForm) {
                controller.setTitleLabel("ใบคำร้องขอลาพักการศึกษา");
            } else if (request instanceof Ku1AndKu3RequestForm) {
                if (request.getRequestType().equalsIgnoreCase("KU1")) {
                    controller.setTitleLabel("แบบลงทะเบียนเรียน KU1");
                } else {
                    controller.setTitleLabel("แบบขอเปลี่ยนแปลงการลงทะเบียนเรียน KU3");
                }
            }
            borderPane.setCenter(pane);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
