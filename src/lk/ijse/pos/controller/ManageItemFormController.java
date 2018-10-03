/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.ijse.pos.controller;

import com.jfoenix.controls.JFXTextField;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
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
import lk.ijse.pos.view.util.tblmodel.ItemTM;

/**
 *
 * @author ranjith-suranga
 */
public class ManageItemFormController implements Initializable{

    @FXML
    private JFXTextField txtItemCode;
    @FXML
    private JFXTextField txtDescription;
    @FXML
    private JFXTextField txtUnitPrice;
    @FXML
    private JFXTextField txtQty;
    @FXML
    private AnchorPane root;
    @FXML
    private TableView<ItemTM> tblItems;
    
    private boolean addNew = true;
    
    private void loadAllItems(){
        
        try {
            Connection connection = DBConnection.getInstance().getConnection();
            
            Statement stm = connection.createStatement();
            
            ResultSet rst = stm.executeQuery("SELECT * FROM Item");
            
            ArrayList<ItemTM> alItems = new ArrayList<>();
            
            while (rst.next()){
                
                ItemTM item = new ItemTM(rst.getString(1),
                        rst.getString(2),
                        rst.getBigDecimal(3),
                        rst.getInt(4));
                
                alItems.add(item);
                
            }
            
            ObservableList<ItemTM> olItems = FXCollections.observableArrayList(alItems);
            
            tblItems.setItems(olItems);
            
        } catch (Exception ex) {
            Logger.getLogger(ManageItemFormController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        tblItems.getColumns().get(0).setStyle("-fx-alignment: center");
        tblItems.getColumns().get(2).setStyle("-fx-alignment: center-right");
        tblItems.getColumns().get(3).setStyle("-fx-alignment: center-right");
        
        tblItems.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("code"));
        tblItems.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("description"));
        tblItems.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        tblItems.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("qtyOnHand"));
        
        loadAllItems();
        
        tblItems.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ItemTM>() {
            @Override
            public void changed(ObservableValue<? extends ItemTM> observable, ItemTM oldValue, ItemTM newValue) {
                if (newValue == null){
                    addNew = true;
                    clearTextFields();
                    return;
                }
                
                txtItemCode.setText(newValue.getCode());
                txtDescription.setText(newValue.getDescription());
                txtUnitPrice.setText(newValue.getUnitPrice().toPlainString());
                txtQty.setText(newValue.getQtyOnHand() + "");
                
                addNew = false;
                
            }
        });
    }
    
    private void clearTextFields(){
        txtItemCode.setText("");
        txtDescription.setText("");
        txtUnitPrice.setText("");
        txtQty.setText("");
    }

    @FXML
    private void navigateToHome(MouseEvent event) {
        StartUp.navigateToHome(root, (Stage) root.getScene().getWindow());
    }

    @FXML
    private void btnAddNewItem_OnAction(ActionEvent event) {
        
        tblItems.getSelectionModel().clearSelection();
        txtItemCode.requestFocus();
        addNew = true;
        
    }

    @FXML
    private void btnSave_OnAction(ActionEvent event) {
        
        if (addNew){
            
            try {
                
                Connection connection = DBConnection.getInstance().getConnection();
                
                PreparedStatement pstm = connection.prepareStatement("INSERT INTO Item VALUES (?,?,?,?)");
                
                pstm.setObject(1, txtItemCode.getText());
                pstm.setObject(2, txtDescription.getText());
                pstm.setObject(3, new BigDecimal(txtUnitPrice.getText()));
                pstm.setObject(4, Integer.parseInt(txtQty.getText()));
                
                int affectedRows = pstm.executeUpdate();
                
                if (affectedRows > 0){
                    loadAllItems();
                }else{
                    new Alert(Alert.AlertType.ERROR, "Failed to add the item", ButtonType.OK).show();
                }
            } catch (Exception ex) {
                Logger.getLogger(ManageItemFormController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
        }else{
            
            try {
                Connection connection = DBConnection.getInstance().getConnection();
                
                PreparedStatement pstm = connection.prepareStatement("UPDATE Item SET description=?, unitPrice=?, qtyOnHand=? WHERE code=?");
                
                pstm.setObject(1, txtDescription.getText());
                pstm.setObject(2, new BigDecimal(txtUnitPrice.getText()));
                pstm.setObject(3, Integer.parseInt(txtQty.getText()));
                pstm.setObject(4, txtItemCode.getText());
                
                int affectedRows = pstm.executeUpdate();
                
                if (affectedRows > 0){
                    loadAllItems();
                }else{
                    new Alert(Alert.AlertType.ERROR, "Failed to update the item", ButtonType.OK).show();
                }
            } catch (Exception ex) {
                Logger.getLogger(ManageItemFormController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
        }
        
        
    }

    @FXML
    private void btnDelete_OnAction(ActionEvent event) {
        
        if (tblItems.getSelectionModel().getSelectedIndex() == -1) return;
        
        String code = tblItems.getSelectionModel().getSelectedItem().getCode();
        
        try {
            Connection connection = DBConnection.getInstance().getConnection();
            
            PreparedStatement pstm = connection.prepareStatement("DELETE FROM Item WHERE code=?");
            
            pstm.setObject(1, code);
            
            int affectedRows = pstm.executeUpdate();
            
            if (affectedRows > 0){
                loadAllItems();
            }else{
                new Alert(Alert.AlertType.ERROR,"Unable to delete the customer", ButtonType.OK).show();
            }
        } catch (Exception ex) {
            Logger.getLogger(ManageItemFormController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
