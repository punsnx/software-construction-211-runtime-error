package ku.cs.controllers.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ku.cs.models.department.Department;
import ku.cs.models.department.DepartmentList;
import ku.cs.models.faculty.Faculty;
import ku.cs.models.faculty.FacultyList;
import ku.cs.models.user.*;
import ku.cs.models.user.exceptions.UserException;
import ku.cs.services.*;
import java.util.Set;
import java.util.stream.Collectors;

public class AddFormController{
    // store controller ref for user their method example. reload main page when edit data
    private AdminManageStaffController adminStaffController;
    private AdminManageFacultyController adminFacultyController;

    // store temp data for interact with behavior in scene
    private String prevFacaltyChose;
    private String currentRole;


    // dataList for write data to file csv
    private FacultyList facultyList;
    private DepartmentList departmentList;
    private UserList userList;

    // JavaFX component
    @FXML
    private Stage stage;
    @FXML
    private TextField facultyNameTextField;
    @FXML
    private TextField facultyIdTextField;
    @FXML
    private TextField departmentNameTextField;
    @FXML
    private TextField departmentIdTextField;
    @FXML
    private Label errorLabel;
    @FXML
    private ChoiceBox<String> departmentChoiceBox;
    @FXML
    private ChoiceBox<String> facultyChoiceBox;
    @FXML
    private TextField advisorIdTextField;
    @FXML
    private TextField firstNameTextField;
    @FXML
    private TextField lastNameTextField;
    @FXML
    private TextField userNameTextField;
    @FXML
    private TextField startPassword;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Button closeButton;
    @FXML
    private Button saveButton;

    @FXML
    private void initialize() {
        updateStyle();

        SetTransition.setButtonBounce(closeButton);
        SetTransition.setButtonBounce(saveButton);

        Datasource<DepartmentList> datasourceDepartment = new DepartmentListFileDatasource("data");
        departmentList = datasourceDepartment.readData();
        if (facultyChoiceBox != null && departmentChoiceBox != null) {
            facultyChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    departmentChoiceBox.setValue("");
                }
            });
        }
        if (departmentChoiceBox != null) {
            departmentChoiceBox.setOnMouseClicked(e -> {
                showDepartmentInChoiceBox();
            });
            departmentChoiceBox.setOnKeyPressed(e -> {
                showDepartmentInChoiceBox();
            });
        }

        anchorPane.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                if (adminFacultyController != null) {
                    onAcceptFacultyDepartmentClick();
                } else if (adminStaffController != null) {
                    onAcceptClick();
                }
            }
        });
    }

    public void updateStyle() {
        Theme.getInstance().loadCssToPage(anchorPane, new PathGenerator() {
            @Override
            public String getThemeDarkPath() {
                return getClass().getResource("/ku/cs/styles/general-dark.css").toString();
            }
            @Override
            public String getThemeLightPath() {
                return getClass().getResource("/ku/cs/styles/general.css").toString();
            }
        });
    }

    private void showDepartmentInChoiceBox() {
        if (facultyChoiceBox.getValue() != null && prevFacaltyChose != facultyChoiceBox.getValue()) {
            prevFacaltyChose = facultyChoiceBox.getValue();
            departmentChoiceBox.getItems().clear();
            Set<String> filter = departmentList.getDepartments()
                    .stream()
                    .filter(department -> department.getFaculty().equals(facultyChoiceBox.getValue()))
                    .map(Department::getName)
                    .collect(Collectors.toSet());

            departmentChoiceBox.getItems().addAll(filter);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setRole(String role) {
        currentRole = role;
    }
    public void setListForWrite(Object objectlist) {
        if (objectlist instanceof DepartmentList) {
            this.departmentList = (DepartmentList) objectlist;
        } else if (objectlist instanceof FacultyList) {
            this.facultyList = (FacultyList) objectlist;
        } else if (objectlist instanceof UserList) {
            this.userList = (UserList) objectlist;
        }
    }

    public void setCurrentControllpage(Object object) {
        if (object instanceof AdminManageStaffController) {
            this.adminStaffController = (AdminManageStaffController) object;
        } else if (object instanceof AdminManageFacultyController) {
            this.adminFacultyController = (AdminManageFacultyController) object;
        }
    }

    public void setChoiceBox() {
        if (facultyChoiceBox != null) {
            Datasource<FacultyList> datasourceFaculty = new FacultyListFileDatasource("data");
            FacultyList list =  datasourceFaculty.readData();
            for (Faculty faculty : list.getFacultyList()) {
                facultyChoiceBox.getItems().add(faculty.getName());
            }
        }
        if (departmentChoiceBox != null) {
            Datasource<DepartmentList> datasourceDepartment = new DepartmentListFileDatasource("data");
            DepartmentList departmentList = datasourceDepartment.readData();
            for (Department department : departmentList.getDepartments()) {
                departmentChoiceBox.getItems().add(department.getName());
            }
        }
    }

    @FXML
    public void onExitClick() {
        stage.close();
    }

    @FXML
    public void onAcceptClick() {
        Datasource<UserList> datasource = new UserListFileDatasource("data", currentRole+".csv");
        try {
            if (facultyChoiceBox.getValue() == null || facultyChoiceBox.getValue().isEmpty()) throw new UserException("กรุณาเลือกคณะ");
            if (facultyChoiceBox.getValue() != null && departmentChoiceBox != null && (departmentChoiceBox.getValue() == null || departmentChoiceBox.getValue().isEmpty())) throw new UserException("กรุณาเลือกภาควิชา");
            if (currentRole.equals("faculty-staff")) {
                FacultyUser facultyUser = new FacultyUser("0000000000", userNameTextField.getText(), "faculty-staff", firstNameTextField.getText(), lastNameTextField.getText(), "0001-01-01:00:00:00", "fscixxa@ku.th", startPassword.getText().isEmpty() ? "DEFAULT" : startPassword.getText(), facultyChoiceBox.getValue());
                if (!startPassword.getText().isEmpty()) {
                    facultyUser.setDefaultPassword(startPassword.getText());
                }
                userList.addUser(facultyUser);
                datasource.writeData(userList.getUserList(currentRole));
                adminStaffController.loadFacultyStaff();
            } else if (currentRole.equals("department-staff")) {
                DepartmentUser departmentUser = new DepartmentUser("0000000000", userNameTextField.getText(), "department-staff", firstNameTextField.getText(), lastNameTextField.getText(), "0001-01-01:00:00:00", "fscixxa@ku.th", startPassword.getText().isEmpty() ? "DEFAULT" : startPassword.getText(), facultyChoiceBox.getValue(), departmentChoiceBox.getValue());
                if (!startPassword.getText().isEmpty()) {
                    departmentUser.setDefaultPassword(startPassword.getText());
                }
                userList.addUser(departmentUser);
                datasource.writeData(userList.getUserList(currentRole));
                adminStaffController.loadDepartmentStaff();
            } else if (currentRole.equals("advisor")) {
                Advisor advisor = new Advisor(advisorIdTextField.getText(), userNameTextField.getText(), "advisor", firstNameTextField.getText(), lastNameTextField.getText(), "0001-01-01:00:00:00", "fscixxa@ku.th", startPassword.getText().isEmpty() ? "DEFAULT" : startPassword.getText(), facultyChoiceBox.getValue(), departmentChoiceBox.getValue());
                if (!startPassword.getText().isEmpty()) {
                    advisor.setDefaultPassword(startPassword.getText());
                }
                userList.addUser(advisor);
                datasource.writeData(userList.getUserList(currentRole));
                adminStaffController.loadAdvisor();
            }
            adminStaffController.resetSearch();
            stage.close();
        } catch (UserException e) {
            errorLabel.setVisible(true);
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    public void onAcceptFacultyDepartmentClick() {
        try {
            if (departmentList != null && departmentNameTextField != null) {
                Datasource<DepartmentList> datasource = new DepartmentListFileDatasource("data");
                departmentList.addDepartment(departmentNameTextField.getText(), departmentIdTextField.getText(), facultyChoiceBox.getValue());
                datasource.writeData(departmentList);
                adminFacultyController.loadDepartment();
            } else if (facultyList != null && facultyNameTextField != null) {
                Datasource<FacultyList> datasource = new FacultyListFileDatasource("data");
                facultyList.addFaculty(facultyNameTextField.getText(), facultyIdTextField.getText());
                datasource.writeData(facultyList);
                adminFacultyController.loadFaculty();
            }
            adminFacultyController.resetSearch();
            stage.close();
        } catch (IllegalArgumentException e) {
            errorLabel.setVisible(true);
            errorLabel.setText(e.getMessage());
        }
    }


}
