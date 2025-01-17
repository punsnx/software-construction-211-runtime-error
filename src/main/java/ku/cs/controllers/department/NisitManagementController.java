package ku.cs.controllers.department;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;
import ku.cs.models.Session;
import ku.cs.models.request.Request;
import ku.cs.models.request.RequestList;
import ku.cs.models.request.approver.Approver;
import ku.cs.models.request.approver.ApproverList;
import ku.cs.models.user.DepartmentUser;
import ku.cs.models.user.Student;
import ku.cs.models.user.User;
import ku.cs.models.user.UserList;
import ku.cs.models.user.exceptions.UserException;
import ku.cs.services.*;
import ku.cs.services.Observer;
import ku.cs.services.Theme;
import ku.cs.services.utils.DateTools;
import ku.cs.views.components.*;
import java.util.*;

public class NisitManagementController implements Observer<HashMap<String, String>> {
    @FXML private Label pageTitleLabel;
    @FXML private StackPane mainStackPane;

    @FXML private Label tableViewLabel;
    @FXML private TableView<User> nisitTableView;
    @FXML private HBox tableTopHBox;
    private DefaultSearchBox<User> searchBox;
    private UserList filterList;

    @FXML private VBox nisitEditorVBox;

    private ImageView nisitImageView;
    private TextFieldStack nisitFirstnameTextField;
    private TextFieldStack nisitLastnameTextField;
    private TextFieldStack nisitIdTextField;
    private TextFieldStack nisitEmailTextField;
    private TextFieldStack nisitPasswordTextField;
    private TextFieldStack nisitDefaultPasswordTextField;

    @FXML private Button addNisitButton;
    @FXML private Button backButton;
    @FXML private Button refreshButton;

    private double editorHBoxWidth;
    private double editorHBoxHeight;

    private boolean editMode;
    private boolean showEdit;
    private UserList users;
    private UserListFileDatasource datasource;
    private User selectedUser;
    private DefaultLabel editorErrorLabel;
    private UploadImageStack editorUploadImageStack;
    private Session session;
    private Theme theme = Theme.getInstance();

    private void initRouteData(){
        Object object = FXRouter.getData();
        if(object instanceof Session){
            this.session = (Session) object;
        }else{
            session = null;
        }
    }

    @FXML
    public void initialize() {
        theme.clearObservers();
        initRouteData();
        editorErrorLabel = new DefaultLabel("");
        initTableView();
        refreshTableData();
        initTableTopHBox();
        nisitImageView = new ImageView();

        selectedUser = null;
        selectedUserListener();

        this.editorHBoxWidth = 270;
        this.editorHBoxHeight = 50;

        initLabel();
        initButton();
        theme.addObserver(this);
        theme.notifyObservers(theme.getTheme());
    }
    private void initLabel(){
        new DefaultLabel(pageTitleLabel);
        new DefaultLabel(tableViewLabel);
    }
    private void initButton(){
        new RouteButton(backButton,"department-staff-request-list","transparent","#a6a6a6","#000000");
        new RouteButton(addNisitButton,"department-staff-add-nisit","#ABFFA4","#80BF7A","#000000",session).changeBackgroundRadius(100);
        DefaultButton refreshBt = new DefaultButton(refreshButton,"transparent","white","#000000"){
            @Override
            protected void handleClickEvent() {
                getButton().setOnMouseClicked(e->{
                   refreshTableData();
                });
            }
        };
        refreshBt.changeBackgroundRadius(15);
    }
    private void initTableTopHBox(){
        Map<String,StringExtractor<User>> filterList= new LinkedHashMap<>();
        filterList.put("รหัสนิสิต", obj -> obj.getId());
        filterList.put("ชื่อ-นามสกุล", obj -> obj.getName());
        filterList.put("ชื่อผู้ใช้", new StringExtractor<>() {
            @Override
            public String extract(User obj) {
                String username = obj.getUsername();
                return (username.equalsIgnoreCase("no-username")
                        && obj.getActiveStatus().equalsIgnoreCase("inactive")
                        ? "not register" : username);
            }
        });
        filterList.put("อีเมล", obj -> obj.getEmail());
        filterList.put("สถานะ", obj -> obj.getActiveStatus());
        filterList.put("ใช้งานล่าสุด", new StringExtractor<>() {
            @Override
            public String extract(User obj) {
                return DateTools.localDateTimeToFormatString("yyyy/MM/dd HH:mm", obj.getLastLogin());
            }
        });


        Map<String, Comparator<User>> comparatorList= new LinkedHashMap<>();
        Comparator<User> userTimestampComparator = new Comparator<>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getLastLogin().compareTo(o2.getLastLogin());
            }
        };
        comparatorList.put("ใช้งานล่าสุด",userTimestampComparator);

        searchBox = new DefaultSearchBox<>(new ArrayList<>(this.filterList.getUsers()), filterList,comparatorList,500,30){
            @Override
            protected void initialize(){
                super.initialize();
                filterBox.getSelectionModel().select(1);//IDX -> รหัสนิสิต
                compareBox.getSelectionModel().select(1);//IDX ASCENDING
            }
            @Override
            protected void searchAction(){
                refreshSearchTableData(getQueryItems());
            }
            @Override
            protected void initStyle(){
                super.initStyle();
                filterBox.changeFontSize(16);
                compareBox.changeFontSize(16);
                searchBox.setFontSize(16);
            }
        };
        tableTopHBox.getChildren().addFirst(searchBox);
    }
    private void initTableView(){
        DefaultTableView<User> nisitTable = new DefaultTableView(nisitTableView){
            @Override
            protected void handleClick() {
                getTableView().getSelectionModel().selectedItemProperty().addListener(new ChangeListener<User>() {
                    @Override
                    public void changed(ObservableValue<? extends User> observable, User oldValue, User newValue) {
                        if (newValue != null) {
                        selectedUser = newValue;
                        selectedUserListener();
                        }
                    }
                });
            }
            @Override
            public void updateAction(){
                if(theme.getTheme() != null){
                    if(theme.getTheme().get("name").equalsIgnoreCase("dark")){
                        setStyleSheet("/ku/cs/styles/department/pages/nisit-management/dark-department-nisit-management-table-stylesheet.css");
                    }else{
                        setStyleSheet("/ku/cs/styles/department/pages/nisit-management/department-nisit-management-table-stylesheet.css");
                    }

                }
            }
        };
        nisitTable.getTableView().getColumns().clear();
        nisitTable.getTableView().getItems().clear();

        nisitTable.getTableView().getColumns().add(newAvatarImageColumn());
        nisitTable.addColumn("รหัสนิสิต","id");
        nisitTable.addColumn("ชื่อ-นามสกุล","name");
        nisitTable.getTableView().getColumns().add(newUsernameEmailColumn("ชื่อผู้ใช้/อีเมล"));
        nisitTable.getTableView().getColumns().add(newStatusLatestColumn("สถานะ/ล่าสุด"));
        nisitTable.getTableView().getColumns().add(newDeleteColumn());
        nisitTable.addStyleSheet("/ku/cs/styles/department/pages/nisit-management/department-nisit-management-table-stylesheet.css");

        theme.addObserver(nisitTable);

    }
    private void selectedUserListener(){
        if(selectedUser != null){
            showEdit = true;
            editMode = true;
        }else {
            showEdit = false;
            editMode = false;
        }
        initNisitEditor(selectedUser);
        toggleEditFiled();
        nisitEditorVBox.setDisable(!showEdit);
    }
    private void refreshTableData(){
        nisitTableView.getItems().clear();
        datasource = new UserListFileDatasource("data","student.csv");
        users = datasource.readData();

        if(session != null && session.getUser() != null){
            filterList = new UserList();
            UUID currentDepartment = ((DepartmentUser)session.getUser()).getDepartmentUUID();
            for(User user : users.getUsers("student")){
                if(((Student)user).getDepartmentUUID().equals(currentDepartment)){
                    try {
                        filterList.addUser(user);
                    } catch (UserException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else{
            filterList = users;
        }



        if(searchBox!=null){
            searchBox.setSearchItems(new ArrayList<>(filterList.getUsers()));
            searchBox.forceSearch();

        }else {
            for(User user : filterList.getUsers("student")){
                if(user.isRole("student")){
                    nisitTableView.getItems().add(user);
                }
            }
        }

    }
    private void refreshSearchTableData(Collection<User> users){
        nisitTableView.getItems().clear();
        nisitTableView.getItems().addAll(users);
        nisitTableView.refresh();
    }
    private void initNisitEditor(User user){

        ObservableList<Node> children =  nisitEditorVBox.getChildren();
        children.clear();
        HBox container;
        if(user != null){
            //USER IMAGE
            container = newEditorContainerHBox();
            nisitImageView.setPreserveRatio(true);
            nisitImageView.setFitWidth(200);
            nisitImageView.setFitHeight(200);
            SquareImage nisitImage = new SquareImage(nisitImageView);
            nisitImage.setClipImage(50,50);
            if(!user.getAvatar().equalsIgnoreCase("no-image")){
                ImageDatasource imageDatasource = new ImageDatasource("users");
                nisitImage.setImage(imageDatasource.openImage(user.getAvatar()));
            }

            container.getChildren().add(nisitImageView);
            container.setAlignment(Pos.CENTER);
            children.add(container);

            //ERROR LABEL
            container = newEditorContainerHBox();
            setEditorErrorLabel("");
            container.setAlignment(Pos.CENTER);
            container.setPrefHeight(20);

            container.getChildren().add(editorErrorLabel);
            children.add(container);

            //TEXT FIELDS
            //FIRSTNAME AND LASTNAME
            container = newEditorLabelContainerHBox();
            container.getChildren().addAll(newEditorLabel("ชื่อ"),newEditorLabel("นามสกุล"));
            children.add(container);
            container = newEditorContainerHBox();
            container.getChildren().add(nisitFirstnameTextField = new TextFieldStack(user.getFirstname()));
            container.getChildren().add(nisitLastnameTextField = new TextFieldStack(user.getLastname()));
            children.add(container);
            //ID AND EMAIL
            container = newEditorLabelContainerHBox();
            container.getChildren().addAll(newEditorLabel("รหัสนิสิต"),newEditorLabel("อีเมล"));
            children.add(container);
            container = newEditorContainerHBox();
            container.getChildren().add(nisitIdTextField = new TextFieldStack(user.getId()));
            container.getChildren().add(nisitEmailTextField = new TextFieldStack(user.getEmail()));
            children.add(container);
            //PASSWORD AND DEFAULT PASSWORD
            container = newEditorLabelContainerHBox();
            container.getChildren().addAll(newEditorLabel("รหัสผ่าน"),newEditorLabel("รหัสผ่านเริ่มต้น"));
            children.add(container);
            container = newEditorContainerHBox();
            container.getChildren().add(nisitPasswordTextField = new TextFieldStack("PASSWORD"));
            container.getChildren().add(nisitDefaultPasswordTextField = new TextFieldStack(user.getDefaultPassword()));
            children.add(container);

            //UPLOAD IMAGE

            container = newEditorContainerHBox();
            container.setAlignment(Pos.CENTER);

            editorUploadImageStack = new UploadImageStack("users",user.getDefaultAvatarName(),user.getAvatar());
            container.getChildren().add(editorUploadImageStack);
            children.add(container);

            //EDIT SAVE CANCEL BUTTON
            container = newEditorContainerHBox();
            Button button;
            button = new Button();
            button.setId("editButton");
            container.getChildren().add(button);
            children.add(container);
            button = new Button();
            button.setId("cancelButton");
            container.getChildren().add(button);

        }else{
            nisitEditorVBox.setAlignment(Pos.CENTER);
            container = newEditorContainerHBox();
            DefaultLabel fallbackLabel = new DefaultLabel("");
            fallbackLabel.changeText("ยังไม่ได้เลือก",24,FontWeight.NORMAL);
            fallbackLabel.changeLabelColor("black");
            container.getChildren().add(fallbackLabel);
            container.setAlignment(Pos.CENTER);
            children.add(container);
        }

    }
    private HBox newEditorContainerHBox(){
        double w = editorHBoxWidth;
        double h = editorHBoxHeight;
        HBox container = new HBox();
        container.setPrefSize(w,h);
        return container;
    }
    private HBox newEditorLabelContainerHBox(){
        double w = editorHBoxWidth;
        HBox container = new HBox();
        container.setPrefWidth(w);
        return container;
    }
    private DefaultLabel newEditorLabel(String text){
        double fontSize = 18;
        double width = editorHBoxWidth;
        DefaultLabel label = new DefaultLabel("");
        label.changeText(text,fontSize,FontWeight.BOLD);
        label.setMaxWidth(width);
        label.setPrefWidth(width);
        if(theme.getTheme() != null){
            label.changeLabelColor(theme.getTheme().get("textColor"));
        }
        return label;
    }
    private void toggleEditFiled(){
        Class<?>[] notifyClass = {TextFieldStack.class,UploadImageStack.class};
        theme.notifyObservers(theme.getTheme(),notifyClass);
        editMode = !editMode;
        boolean editable = editMode;
        String editButtonColor = editable ? "#ABFFA4" : "#FFA4A4";
        String editButtonHoverColor = editable ? "#80BF7A" : "#E19494";
        String cancelButtonColor = "#FFA4A4";
        String cancelButtonHoverColor = "#E19494";

        String buttonLabelColor = editable ? "#000000" : "#000000";
        String editButtonLabel = editable ? "บันทึก" : "แก้ไข";
        String cancelButtonLabel = "ยกเลิก";

        nisitEditorVBox.getChildren().forEach(node -> {
            if(node instanceof HBox){
                HBox hbox = (HBox) node;
                hbox.setSpacing(20);
                VBox.setMargin(hbox,new Insets(5,0,0,0));
                for(int i = 0;i < hbox.getChildren().size();i++){
                    Node child = hbox.getChildren().get(i);
                    if(child instanceof TextFieldStack){
                        TextFieldStack t = (TextFieldStack) child;
                        t.toggleTextField(editMode);
                    }else if(child instanceof StackPane){
                        child.setVisible(editMode);
                        child.setDisable(!editMode);

                    }else if(child instanceof Button){
                        hbox.setAlignment(Pos.CENTER);
                        Button button = (Button) child;
                        if(button.getId().equals("editButton")){
                            DefaultButton b = new DefaultButton(button, editButtonColor, editButtonHoverColor, buttonLabelColor){
                                @Override
                                protected void handleClickEvent(){
                                    button.setOnMouseClicked(e -> {
                                        toggleEditFiled();
                                        if(!editMode){
                                            onSaveButton();
                                        }
                                    });
                                }
                            };
                            b.changeText(editButtonLabel,28, FontWeight.NORMAL);
                            b.changeBackgroundRadius(20);
                        }else if(button.getId().equals("cancelButton")){
                            DefaultButton b = new DefaultButton(button, cancelButtonColor, cancelButtonHoverColor, buttonLabelColor){
                                @Override
                                protected void handleClickEvent(){
                                    button.setOnMouseClicked(e -> {
                                        editorUploadImageStack.cancelUploadedImage();//IF CLICKED
                                        editorUploadImageStack.cancelDeleteImage();//IF CLICKED
                                        selectedUserListener();
                                    });
                                }
                            };
                            b.changeText(cancelButtonLabel,28, FontWeight.NORMAL);
                            b.changeBackgroundRadius(20);
                            button.setVisible(editMode);
                        }else if(button.getId().equals("deleteButton")){}
                    }
                }
            }
        });
    }
    private void onSaveButton()  {
        try {
            editorUploadImageStack.saveUploadedImage();//IF CLICKED
            editorUploadImageStack.performDeleteImage();//IF CLICKED

            selectedUser.setFirstname(nisitFirstnameTextField.getData());
            selectedUser.setLastname(nisitLastnameTextField.getData());
            selectedUser.setId(nisitIdTextField.getData());
            selectedUser.setEmail(nisitEmailTextField.getData());
            selectedUser.setAvatar(editorUploadImageStack.getCurFileName());

            if(!nisitPasswordTextField.getData().equalsIgnoreCase("PASSWORD")){
                selectedUser.setPassword(nisitPasswordTextField.getData());
            }
            if(!nisitDefaultPasswordTextField.getData().equalsIgnoreCase(selectedUser.getDefaultPassword())){
                selectedUser.setDefaultPassword(nisitDefaultPasswordTextField.getData());
            }


            datasource.writeData(users);
            nisitTableView.refresh();
            selectedUserListener();

            setEditorErrorLabel("");
        } catch (UserException e){
            selectedUserListener();
            setEditorErrorLabel(e.getMessage());
            System.out.println("UserException Error : " + e.getMessage());
            toggleEditFiled();
            e.printStackTrace();
        }
    }
    private void onDeleteButton(User user){
        try {
            if(selectedUser != null && selectedUser.equals(user)){
                selectedUser = null;
            }
            if(!user.getAvatar().equalsIgnoreCase("no-image")){
                ImageDatasource imageDatasource = new ImageDatasource("users");
                imageDatasource.deleteFile(user.getAvatar());
            }

            //remove associate requests
            RequestListFileDatasource requestListFileDatasource = new RequestListFileDatasource("data");
            ApproverListFileDatasource approverListFileDatasource = new ApproverListFileDatasource("request-approvers");
            PDFDatasource pdfDatasource = new PDFDatasource();
            RequestList requestList = requestListFileDatasource.readData();
            ApproverList approverList = approverListFileDatasource.readData();
            Iterator<Request> requestIterator = requestList.getRequests().iterator();

            while (requestIterator.hasNext()) {
                Request r = requestIterator.next();
                if (r.getOwnerUUID().equals(user.getUUID())) {
                    ApproverList tempApprovers = approverList.getApproverList(r.getUuid());
                    for (Approver a : tempApprovers.getApprovers()) {
                        if(!a.getSignatureFile().equalsIgnoreCase("no-image")){
                            pdfDatasource.deleteFile(a.getSignatureFile());
                        }
                        approverList.deleteApproverByObject(a);
                    }
                    requestIterator.remove();
                }
            }
            requestListFileDatasource.writeData(requestList);
            approverListFileDatasource.writeData(approverList);


            users.deleteUserByObject(user);
            datasource.writeData(users);
            refreshTableData();
            selectedUserListener();
        } catch (Exception e){
            e.printStackTrace();
        }

    }
    private TableColumn<User,?> newAvatarImageColumn(){
        TableColumn<User,VBox> column = new TableColumn<>("");
        column.setSortable(false);//BLOCK SORT BY CLICK
        column.setReorderable(false);//BLOCK DRAG BY MOUSE
        column.setCellFactory(c -> new TableCell<>(){
            private Hashtable<String,Image> imageCache = new Hashtable<>();
            @Override
            protected void updateItem(VBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView() == null || getTableView().getItems().get(getIndex()) == null) {
                    setGraphic(null);
                } else {
                    setAlignment(Pos.CENTER);
                    SquareImage avatar = new SquareImage(new ImageView());
                    avatar.getImageView().setFitHeight(50);
                    avatar.getImageView().setFitWidth(50);
                    avatar.getImageView().setPreserveRatio(true);
                    avatar.getImageView().setSmooth(true);
                    avatar.setClipImage(50,50);
                    setGraphic(avatar.getImageView());//DEFAULT IMAGE
                    Task<Image> loadImageTask = new Task<>() {
                        @Override
                        protected Image call() {
                            //NoNeed-synchronized -> because all resources are synchronized e.g. Hashtable
                            User user = getTableView().getItems().get(getIndex());
                            if(user != null & !user.getAvatar().equalsIgnoreCase("no-image")){
                                if(imageCache.keySet().contains(user.getAvatar())){
                                    return imageCache.get(user.getAvatar());
                                }
                                ImageDatasource imageDatasource = new ImageDatasource("users");
                                imageCache.put(user.getAvatar(),imageDatasource.openImage(user.getAvatar()));
                                return imageCache.get(user.getAvatar());
                            }
                            return null;
                        }
                    };
                    loadImageTask.setOnSucceeded(event ->{
                        if(loadImageTask.getValue() != null){
                            avatar.setImage(loadImageTask.getValue());
                        }
                        //IF NULL NO-IMAGE, SET TO DEFAULT
                    });
                    new Thread(loadImageTask).start();

                }
            }
        });
        return column;
    }
    private TableColumn<User,?> newUsernameEmailColumn(String colName){
        TableColumn<User,VBox> column = new TableColumn<>(colName);
        column.setSortable(false);//BLOCK SORT BY CLICK
        column.setReorderable(false);//BLOCK DRAG BY MOUSE
        column.setCellFactory(c -> new TableCell<>(){
            private VBox vBox = new VBox();
            private DefaultLabel line1 = new DefaultLabel("");
            private DefaultLabel line2 = new DefaultLabel("");
            {
                vBox.setAlignment(Pos.CENTER);
                line1.changeText("",18, FontWeight.NORMAL);
                line2.changeText("",18, FontWeight.NORMAL);
                if(theme.getTheme() != null){
                    line1.update(theme.getTheme());
                    line2.update(theme.getTheme());
                }
            }
            @Override
            protected void updateItem(VBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView() == null || getTableView().getItems().get(getIndex()) == null) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    String username = user.getUsername();
                    line1.changeText((username.equalsIgnoreCase("no-username")
                            && user.getActiveStatus().equalsIgnoreCase("inactive")
                            ? "not register" : username));
                    line2.changeText(user.getEmail());

                    vBox.getChildren().clear();
                    vBox.getChildren().addAll(line1, line2);

                    setGraphic(vBox);
                }
            }
        });
        return column;
    }
    private TableColumn<User,?> newStatusLatestColumn(String colName){
        TableColumn<User,VBox> column = new TableColumn<>(colName);
        column.setSortable(false);//BLOCK SORT BY CLICK
        column.setReorderable(false);//BLOCK DRAG BY MOUSE
        column.setCellFactory(c -> new TableCell<>(){
            private VBox vBox = new VBox();
            private DefaultLabel line1 = new DefaultLabel("");
            private DefaultLabel line2 = new DefaultLabel("");
            {
                vBox.setAlignment(Pos.CENTER);
                line1.changeText("",18,FontWeight.NORMAL);
                line2.changeText("",18,FontWeight.NORMAL);
                if(theme.getTheme() != null){
                    line1.update(theme.getTheme());
                    line2.update(theme.getTheme());
                }
            }
            @Override
            protected void updateItem(VBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView() == null || getTableView().getItems().get(getIndex()) == null) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    String status = user.getActiveStatus();
                    line1.changeText(status);
                    if(status.equalsIgnoreCase("active")){
                        line1.changeLabelColor("green");
                    }else {
                        line1.changeLabelColor("red");
                    }

                    line2.changeText(DateTools.localDateTimeToFormatString("yyyy/MM/dd HH:mm", user.getLastLogin()));

                    vBox.getChildren().clear();
                    vBox.getChildren().addAll(line1, line2);

                    setGraphic(vBox);
                }
            }
        });
        return column;
    }
    private TableColumn<User,?> newDeleteColumn(){
        TableColumn<User, HBox> column = new TableColumn<>("");
        column.setSortable(false);//BLOCK SORT BY CLICK
        column.setReorderable(false);//BLOCK DRAG BY MOUSE

        column.setCellFactory(c -> new TableCell<>() {
            private Button actionButton = new Button();
            private final HBox hbox = new HBox(actionButton);
            {
                hbox.setAlignment(Pos.CENTER_LEFT);
                hbox.setPrefSize(35,35);
                DefaultButton b =new DefaultButton(actionButton,"transparent", "#e0e0e0", "#000000"){
                    @Override
                    protected void handleClickEvent() {
                        button.setOnMouseClicked(e -> {
                            User user = getTableView().getItems().get(getIndex());
                            mainStackPane.getChildren().add(new ConfirmStack("ยืนยัน","คุณต้องการลบใช่มั้ย"){
                                @Override
                                protected void handleAcceptButton(){
                                    getAcceptButton().setOnMouseClicked(e -> {
                                        mainStackPane.getChildren().removeLast();
                                        onDeleteButton(user);
                                    });
                                }
                                @Override
                                protected void handleDeclineButton(){
                                    getDeclineButton().setOnMouseClicked(e -> {
                                        mainStackPane.getChildren().removeLast();
                                    });
                                }
                            });

                        });
                    }
                };
                Image deleteButtonImage = new Image(getClass().getResourceAsStream("/images/pages/department/global/red-bin.png"));
                b.changeBackgroundRadius(20);
                b.setImage(deleteButtonImage,35,35);
            }
            @Override
            protected void updateItem(HBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null); // No content for empty cells
                } else {
                    setGraphic(hbox); // Set the HBox with the button as the cell's graphic
                }
            }
        });
        return column;
    }
    private void setEditorErrorLabel(String error){
        this.editorErrorLabel.changeLabelColor("red");
        this.editorErrorLabel.changeText(error,18, FontWeight.BOLD);
    }

    @Override
    public void update(HashMap<String, String> data) {
        mainStackPane.setStyle(mainStackPane.getStyle()+"-fx-background-color: " + data.get("secondary") + ";");
    }
}


