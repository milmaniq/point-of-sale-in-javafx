/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.ijse.pos.controller;

import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lk.ijse.pos.db.DBConnection;
import lk.ijse.pos.main.StartUp;
import lk.ijse.pos.view.util.tblmodel.CustomerTM;

/**
 * FXML Controller class
 *
 * @author ranjith-suranga
 */
public class ManageCustomerFormController implements Initializable {

    @FXML
    private AnchorPane root;
    @FXML
    private JFXTextField txtCustomerId;
    @FXML
    private JFXTextField txtCustomerName;
    @FXML
    private JFXTextField txtCustomerAddress;

    @FXML
    private TableView<CustomerTM> tblCustomers;
    
    boolean addnew = true;

    private void loadAllCustomers() {

        try {

            Connection connection = DBConnection.getInstance().getConnection();

            Statement stm = connection.createStatement();

            ResultSet rst = stm.executeQuery("SELECT * FROM Customer");

            ArrayList<CustomerTM> alCustomers = new ArrayList<>();

            while (rst.next()) {

                CustomerTM customer = new CustomerTM(
                        rst.getString(1),
                        rst.getString(2),
                        rst.getString(3));

                alCustomers.add(customer);

            }

            ObservableList<CustomerTM> olCustomers = FXCollections.observableArrayList(alCustomers);

            tblCustomers.setItems(olCustomers);

        } catch (Exception ex) {
            Logger.getLogger(ManageCustomerFormController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tblCustomers.getColumns().get(0).setStyle("-fx-alignment:center");
        tblCustomers.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblCustomers.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblCustomers.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("address"));
          
        tblCustomers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CustomerTM>() {
            @Override
            public void changed(ObservableValue<? extends CustomerTM> observable, CustomerTM oldValue, CustomerTM newValue) {
                
                if (newValue == null){
                    clearTextFields();
                    addnew = true;
                    return;
                }
                
                txtCustomerId.setText(newValue.getId());
                txtCustomerName.setText(newValue.getName());
                txtCustomerAddress.setText(newValue.getAddress());
                
                addnew = false;
                
            }
        });
        
        loadAllCustomers();
    }

    @FXML
    private void navigateToHome(MouseEvent event) {
        StartUp.navigateToHome(root, (Stage) this.root.getScene().getWindow());
    }

    @FXML
    private void btnDelete_OnAction(ActionEvent event) {
        
        Alert confirmAlert = new Alert(Alert.AlertType.WARNING,"Are you sure whether you want to delete the customer?",ButtonType.YES,ButtonType.NO);
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        
        if (result.get() == ButtonType.YES){

            String customerID = tblCustomers.getSelectionModel().getSelectedItem().getId();

            try {
                Connection connection = DBConnection.getInstance().getConnection();

                PreparedStatement pstm = connection.prepareStatement("DELETE FROM Customer WHERE id=?");
                pstm.setObject(1, customerID);

                int affectedRows = pstm.executeUpdate();

                if (affectedRows > 0) {
                    loadAllCustomers();
                } else {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Failed to delete the customer", ButtonType.OK);
                    a.show();
                }
            } catch (Exception ex) {
                Logger.getLogger(ManageCustomerFormController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }

    }
    
    private void clearTextFields(){
        txtCustomerId.setText("");
        txtCustomerName.setText("");
        txtCustomerAddress.setText("");        
    }

    @FXML
    private void btnAddNewCustomer_OnAction(ActionEvent event) {        
        txtCustomerId.requestFocus();
        tblCustomers.getSelectionModel().clearSelection();
        
        addnew = true;
    }

    @FXML
    private void btnSave_OnAction(ActionEvent event) {
        
        if (addnew){
            
            try {
                Connection connection = DBConnection.getInstance().getConnection();
                
                PreparedStatement pstm = connection.prepareStatement("INSERT INTO Customer VALUES (?,?,?)");
                
                pstm.setObject(1, txtCustomerId.getText());
                pstm.setObject(2, txtCustomerName.getText());
                pstm.setObject(3, txtCustomerAddress.getText());
                
                int affectedRows = pstm.executeUpdate();
                
                if (affectedRows > 0){
                    loadAllCustomers();
                }else{
                    new Alert(Alert.AlertType.ERROR,"Unable to add new customer",ButtonType.OK).show();
                }
            } catch (Exception ex) {
                Logger.getLogger(ManageCustomerFormController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }else{
            try {
                //Update
                Connection connection = DBConnection.getInstance().getConnection();
                
                PreparedStatement pstm = connection.prepareStatement("UPDATE Customer SET name=?, address=? WHERE id=?");
                pstm.setObject(1, txtCustomerName.getText());
                pstm.setObject(2, txtCustomerAddress.getText());
                pstm.setObject(3, txtCustomerId.getText());
                
                int affectedRows = pstm.executeUpdate();
                
                if (affectedRows > 0){
                    loadAllCustomers();
                }else{
                    new Alert(Alert.AlertType.ERROR,"Unable to update the customer",ButtonType.OK).show();
                }
                
                
            } catch (Exception ex) {
                Logger.getLogger(ManageCustomerFormController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }

}
