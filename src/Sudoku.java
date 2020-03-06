import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Sudoku extends Application {
    Pane board = new Pane();
    TextField[][] text = new TextField[9][9];
    Rectangle [][] rectangles = new Rectangle[9][9];
    TextField textField = new TextField();
    int column;
    int row;
    int r;
    int c;
    public void start(Stage stage) {

        Scene scene = new Scene(board, 600, 550);

        Line top = new Line(30, 30, 480, 30);
        Line bottom = new Line(30, 480, 480, 480);
        Line right = new Line(480, 30, 480, 480);
        Line left = new Line (30, 30, 30, 480);

        Line h1 = new Line(30, 180, 480, 180);
        Line h2 = new Line(30, 330, 480, 330);

        Line v1 = new Line(180, 30, 180, 480);
        Line v2 = new Line(330, 30, 330, 480);

        top.setStrokeWidth(2.0);
        h1.setStrokeWidth(2.0);
        h2.setStrokeWidth(2.0);
        bottom.setStrokeWidth(2.0);
        left.setStrokeWidth(2.0);
        v1.setStrokeWidth(2.0);
        v2.setStrokeWidth(2.0);
        right.setStrokeWidth(2.0);

        int hCount = 30;
        for (int i = 0; i < 9; i++) {
           for (int j = 0, vCount = 30; j < 9; j++) {
               Rectangle square = new Rectangle(hCount, vCount, 50, 50);
               square.setFill(Color.WHITE);
               square.setStroke(Color.BLACK);
               board.getChildren().add(square);
               //board.getChildren().add(square);
               rectangles[i][j] = square;
               vCount += 50;
           }
           hCount += 50;
        }
        board.getChildren().addAll(h1, h2, top, bottom, right, left);

        try {
            boardSetup();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        Button hint = new Button("Hint");
        Label label = new Label("Pick a box to add a number");
        board.getChildren().add(label);
        label.setLayoutX(60);
        label.setLayoutY(525);

        board.getChildren().add(textField);
        textField.setLayoutX(200);
        textField.setLayoutY(525);
        Button submit = new Button("Submit");
        submit.setLayoutX(400);
        submit.setLayoutY(525);
        board.getChildren().add(submit);


        for ( row = 0; row < 8; row++) {
            for ( column = 0; column < 8; column++) {
                rectangles[row][column].setOnMouseClicked(MouseEvent -> {
                    label.setText("Enter a number");
                    r = row;
                    c = column;
                    //getNumber(row, column);
                });
            }
        }
        submit.setOnAction(e -> {
            String input = textField.getText();
            //System.out.println(input);
            System.out.println(r);
            System.out.println(c);
            text[r][c].setText(input);
            text[r][c].setLayoutX(r * 50 + 45);
            text[r][c].setLayoutY(c * 50 + 45);
            board.getChildren().add(text[r][c]);
            label.setText("Pick a box");
            textField.clear();
        });


        hint.setLayoutX(525);
        hint.setLayoutY(50);
        board.getChildren().add(hint);
        stage.setScene(scene);
        stage.setTitle("Sudoku");
        stage.show();
    }

    void boardSetup() throws FileNotFoundException, IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the file name:");
        String fileName = (scanner.nextLine());
        BufferedReader in = new BufferedReader(new FileReader(fileName));
        ArrayList<String> numbers = new ArrayList<String>();
        String[] nums = new String[9];
        String line;
        while((line = in.readLine()) != null) {
            nums = line.split("\\s");
            for (int i = 0; i < nums.length; i++) {
                numbers.add(nums[i]);
            }
        }

        //add numbers to board
        int hCount = 45;
        int numCount = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0, vCount = 65; j < 9; j++) {
                    TextField num = new TextField(numbers.get(numCount));
                    text[i][j] = num;
                    text[i][j].setLayoutX(hCount);
                    text[i][j].setLayoutY(vCount);
                    text[i][j].setFont(new Font("Rockwell", 40));
                if (!numbers.get(numCount).equals("0")) {
                    board.getChildren().add(text[i][j]);
                }
                vCount += 50;
                numCount++;
            }
            hCount += 50;
        }

    }
    void getNumber(int r, int c) {


    }

}
