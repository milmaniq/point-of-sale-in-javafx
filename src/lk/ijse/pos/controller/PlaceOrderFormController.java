/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.ijse.pos.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lk.ijse.pos.db.DBConnection;
import lk.ijse.pos.main.StartUp;
import lk.ijse.pos.view.util.tblmodel.OrderDetailTM;

/**
 *
 * @author ranjith-suranga
 */
public class PlaceOrderFormController implements Initializable {

    @FXML
    private AnchorPane root;
    @FXML
    private JFXTextField txtOrderId;
    @FXML
    private JFXDatePicker txtOrderDate;
    @FXML
    private JFXComboBox<String> cmbCustomerId;
    @FXML
    private JFXTextField txtCustomerName;
    @FXML
    private JFXComboBox<String> cmbItemCode;
    @FXML
    private JFXTextField txtDescription;
    @FXML
    private JFXTextField txtStock;
    @FXML
    private JFXTextField txtUnitPrice;
    @FXML
    private JFXTextField txtQty;
    @FXML
    private JFXButton btnAdd;
    @FXML
    private JFXButton btnRemove;
    @FXML
    private TableView<OrderDetailTM> tblOrderDetails;
    @FXML
    private Label lblTotal;
    @FXML
    private JFXButton btnPlaceOrder;

    private void loadAllItemCodes() {

        try {
            Connection connection = DBConnection.getInstance().getConnection();

            Statement stm = connection.createStatement();

            ResultSet rst = stm.executeQuery("SELECT code FROM Item");

            ArrayList<String> alItemCodes = new ArrayList<>();

            while (rst.next()) {
                String code = rst.getString(1);
                alItemCodes.add(code);
            }

            ObservableList<String> olItemCodes = FXCollections.observableArrayList(alItemCodes);

            cmbItemCode.setItems(olItemCodes);
        } catch (Exception ex) {
            Logger.getLogger(PlaceOrderFormController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void loadAllCustomerIds() {

        try {
            Connection connection = DBConnection.getInstance().getConnection();

            Statement stm = connection.createStatement();

            ResultSet rst = stm.executeQuery("SELECT id FROM Customer");

            ArrayList<String> alCustomerIds = new ArrayList<>();

            while (rst.next()) {
                String id = rst.getString(1);

                alCustomerIds.add(id);
            }

            ObservableList<String> olCustomerIds = FXCollections.observableArrayList(alCustomerIds);

            cmbCustomerId.setItems(olCustomerIds);

        } catch (Exception ex) {
            Logger.getLogger(PlaceOrderFormController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        tblOrderDetails.getItems().addListener(new ListChangeListener<OrderDetailTM>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends OrderDetailTM> c) {
                
                BigDecimal total = BigDecimal.ZERO;
                
                for (OrderDetailTM item : tblOrderDetails.getItems()) {
                    total = total.add(item.getTotal());
                }
                
                lblTotal.setText("Total : " + total);
                
            }
        });

        tblOrderDetails.getColumns().get(0).setStyle("-fx-alignment: center");
        tblOrderDetails.getColumns().get(2).setStyle("-fx-alignment: center-right");
        tblOrderDetails.getColumns().get(4).setStyle("-fx-alignment: center-right");
        tblOrderDetails.getColumns().get(3).setStyle("-fx-alignment: center");

        tblOrderDetails.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("code"));
        tblOrderDetails.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("description"));
        tblOrderDetails.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        tblOrderDetails.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("qty"));
        tblOrderDetails.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("total"));

        loadAllCustomerIds();
        loadAllItemCodes();

        cmbCustomerId.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                if (newValue == null) {
                    txtCustomerName.setText("");
                    return;
                }
                try {
                    Connection connection = DBConnection.getInstance().getConnection();

                    PreparedStatement pstm = connection.prepareStatement("SELECT name FROM Customer WHERE id=?");

                    pstm.setObject(1, newValue);

                    ResultSet rst = pstm.executeQuery();

                    if (rst.next()) {

                        txtCustomerName.setText(rst.getString(1));

                    }
                } catch (Exception ex) {
                    Logger.getLogger(PlaceOrderFormController.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });

        cmbItemCode.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                if (newValue == null) {
                    clearItemRelatedFields();
                    return;
                }
                System.out.println(newValue);
                try {
                    Connection connection = DBConnection.getInstance().getConnection();

                    PreparedStatement pstm = connection.prepareStatement("SELECT * FROM Item WHERE code=?");

                    pstm.setObject(1, newValue);

                    ResultSet rst = pstm.executeQuery();

                    if (rst.next()) {

                        txtDescription.setText(rst.getString(2));
                        txtUnitPrice.setText(rst.getBigDecimal(3).toPlainString());
                        txtStock.setText(rst.getInt(4) + "");
                        txtQty.setText("");

                    }
                } catch (Exception ex) {
                    Logger.getLogger(PlaceOrderFormController.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });

        tblOrderDetails.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<OrderDetailTM>() {
            @Override
            public void changed(ObservableValue<? extends OrderDetailTM> observable, OrderDetailTM oldValue, OrderDetailTM newValue) {

                if (newValue == null) {
                    return;
                }

                cmbItemCode.setValue(newValue.getCode());
                txtQty.setText(newValue.getQty() + "");

            }
        });

    }

    private void clearItemRelatedFields() {
        txtDescription.setText("");
        txtStock.setText("");
        txtUnitPrice.setText("");
        txtQty.setText("");
    }

    @FXML
    private void navigateToHome(MouseEvent event) {
        StartUp.navigateToHome(root, (Stage) root.getScene().getWindow());
    }

    @FXML
    private void btnAdd_OnAction(ActionEvent event) {

        ObservableList<OrderDetailTM> items = tblOrderDetails.getItems();
        int qty = Integer.parseInt(txtQty.getText());
        BigDecimal unitPrice = new BigDecimal(txtUnitPrice.getText());
        BigDecimal total = unitPrice.multiply(new BigDecimal(qty));

        OrderDetailTM orderDetail = new OrderDetailTM(cmbItemCode.getValue(),
                txtDescription.getText(),
                unitPrice,
                qty,
                total);
        System.out.println(orderDetail);
        for (OrderDetailTM item : items) {
            if (item.getCode().equals(cmbItemCode.getValue())) {

                if (tblOrderDetails.getSelectionModel().getSelectedIndex() == -1) {
                    orderDetail.setQty(qty + item.getQty());
                }

                int index = items.indexOf(item);

                items.set(index, orderDetail);
                cmbItemCode.getSelectionModel().clearSelection();
                cmbItemCode.requestFocus();

                return;
            }
        }

        items.add(orderDetail);

        cmbItemCode.getSelectionModel().clearSelection();
        cmbItemCode.requestFocus();
    }

    @FXML
    private void txtQty_OnKeyPressed(KeyEvent event) {

        if (event.getCode() == KeyCode.ENTER) {
            btnAdd.fire();
        }

    }

    @FXML
    private void btnRemove_OnAction(ActionEvent event) {

        int selectedIndex = tblOrderDetails.getSelectionModel().getSelectedIndex();
        tblOrderDetails.getItems().remove(selectedIndex);

        cmbItemCode.getSelectionModel().clearSelection();
        cmbItemCode.requestFocus();

    }

    @FXML
    private void btnPlaceOrder_OnAction(ActionEvent event) {
        
        Connection connection = null;
        
        try {
            
            connection = DBConnection.getInstance().getConnection();
            
            connection.setAutoCommit(false);
            
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO `Order` VALUES (?,?,?)");
            
            pstm.setObject(1, txtOrderId.getText());
            pstm.setObject(2, txtOrderDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            pstm.setObject(3, cmbCustomerId.getValue());
            
            int affectedRows = pstm.executeUpdate();
            
            if (affectedRows == 0){
                new Alert(Alert.AlertType.ERROR, "Failed to place the order", ButtonType.OK).show();
                return;
            }
            
            ObservableList<OrderDetailTM> items = tblOrderDetails.getItems();
            
            for (OrderDetailTM item : items) {
                
                pstm = connection.prepareStatement("INSERT INTO ItemDetail VALUES (?,?,?,?)");
                
                pstm.setObject(1, txtOrderId.getText());
                pstm.setObject(2, item.getCode());
                pstm.setObject(3, item.getQty());
                pstm.setObject(4, item.getUnitPrice());
                
                affectedRows = pstm.executeUpdate();
                
                if (affectedRows == 0){
                    connection.rollback();
                    new Alert(Alert.AlertType.ERROR, "Failed to place the order", ButtonType.OK).show();
                    return;
                }
                
                pstm = connection.prepareStatement("UPDATE Item SET qtyOnHand=qtyOnHand-" + item.getQty() + " WHERE code=?");
                pstm.setObject(1, item.getCode());
                
                affectedRows = pstm.executeUpdate();
                
                if (affectedRows == 0){
                    connection.rollback();
                    new Alert(Alert.AlertType.ERROR, "Failed to place the order", ButtonType.OK).show();
                    return;
                }
                
            }
            
            connection.commit();
            new Alert(Alert.AlertType.CONFIRMATION, "Order has been placed successfully", ButtonType.OK).showAndWait();
            clearItemRelatedFields();
            cmbCustomerId.getSelectionModel().clearSelection();
            txtOrderId.setText("");
            txtOrderDate.setValue(null);
            
        } catch (Exception ex) {
            try {
                connection.rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(PlaceOrderFormController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            new Alert(Alert.AlertType.ERROR, "Failed to place the order", ButtonType.OK).show();
            Logger.getLogger(PlaceOrderFormController.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                Logger.getLogger(PlaceOrderFormController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }

}
