package ku.cs.controllers.student;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ku.cs.controllers.AboutUsController;
import ku.cs.controllers.ParentController;
import ku.cs.controllers.SettingController;
import ku.cs.controllers.UserProfileCardController;
import ku.cs.models.user.Student;
import ku.cs.models.user.User;
import ku.cs.services.*;
import ku.cs.views.components.SquareImage;

import java.io.IOException;

public class StudentPageController implements ParentController {
    @FXML private BorderPane contentBorderPane;
    @FXML private ImageView tabProfilePicImageView;
    @FXML private Label tabAccountNameLabel;
    @FXML private AnchorPane mainAnchorPane;
    @FXML private Button allRequestButton;
    @FXML private Button settingButton;

    private Student loginUser;
    ImageDatasource datasource;

    @FXML
    public void initialize() {
        updateStyle();

        SetTransition.setButtonBounce(allRequestButton);
        SetTransition.setButtonBounce(settingButton);

        if (FXRouter.getData() instanceof Student)
        {
            loginUser = (Student)FXRouter.getData();
        }
        loadProfile();
        onSideProfileClicked();
        tabAccountNameLabel.setText(loginUser.getName());
    }

    public void loadProfile() {
        datasource = new ImageDatasource("users");
        SquareImage profilePic = new SquareImage(tabProfilePicImageView);
        profilePic.setClipImage(150,150);
        profilePic.setImage(datasource.openImage(loginUser.getAvatar()));
    }
    @FXML
    public void onRequestsButtonClicked(){
        try {
            String viewPath = "/ku/cs/views/student-requests-pane.fxml";
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource(viewPath));
            Pane pane = fxmlLoader.load();
            StudentRequestsController controller = fxmlLoader.getController();
            controller.setLoginUser(loginUser);
            controller.initialize();
            controller.setBorderPane(this.contentBorderPane);
            contentBorderPane.setCenter(pane);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void onAboutUsClicked() {
        try {
            String viewPath = "/ku/cs/views/about-us-pane.fxml";
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource(viewPath));
            Pane pane = fxmlLoader.load();
            AboutUsController controller = fxmlLoader.getController();
            controller.setLoginUser(loginUser);
            controller.initialize();
            contentBorderPane.setCenter(pane);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void onSideProfileClicked(){
        try {
            String viewPath = "/ku/cs/views/user-profile-card.fxml";
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource(viewPath));
            Pane pane = fxmlLoader.load();
            UserProfileCardController controller = fxmlLoader.getController();
            controller.setLoginUser(loginUser);
            controller.setParentController(this);
            controller.initialize();
            contentBorderPane.setCenter(pane);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setLoginUser(User loginUser) {
        if (loginUser == null) {return;}
        if (loginUser instanceof Student) {
            this.loginUser = (Student)loginUser;
        }
    }

    @FXML
    protected void onLogoutClicked() {
        try {
            FXRouter.goTo("login");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void goToSetting() {
        try {
            Stage currentPopupStage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ku/cs/views/setting.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            SettingController controller = fxmlLoader.getController();
            controller.setMainAnchorPane(mainAnchorPane);
            controller.setStage(currentPopupStage);
            controller.setMainCSS(getClass().getResource("/ku/cs/styles/general-dark.css").toString()
                    , getClass().getResource("/ku/cs/styles/general.css").toString());

            currentPopupStage.setScene(scene);
            currentPopupStage.initModality(Modality.APPLICATION_MODAL);
            currentPopupStage.setTitle("ตั้งค่า");
            addImageToPopup(currentPopupStage);
            currentPopupStage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateStyle() {
        Theme.getInstance().loadCssToPage(mainAnchorPane, new PathGenerator() {
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

    private void addImageToPopup(Stage currentPopupStage) {
        Image logo16 = new Image(getClass().getResourceAsStream("/images/logos/application-logo16x16.png"));
        Image logo32 = new Image(getClass().getResourceAsStream("/images/logos/application-logo32x32.png"));
        Image logo48 = new Image(getClass().getResourceAsStream("/images/logos/application-logo48x48.png"));
        Image logo64 = new Image(getClass().getResourceAsStream("/images/logos/application-logo64x64.png"));
        Image logo128 = new Image(getClass().getResourceAsStream("/images/logos/application-logo128x128.png"));
        Image logo500 = new Image(getClass().getResourceAsStream("/images/logos/application-logo500x500.png"));
        currentPopupStage.getIcons().addAll(logo16, logo32, logo48, logo64, logo128, logo500);
    }
}
