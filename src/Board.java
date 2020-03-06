import javafx.animation.FadeTransition;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Board extends Application {
    private int[][] originalBoard = new int[9][9];          //Sudoku board that holds the solution
    private GridPane gridBoard = new GridPane();            //Contains the textfields for the board
    private TextField[][] playBoard = new TextField[9][9];  //holds the contents of the textfields that are shown to user
    private ImageView image;                                //Image for left side of board
    private ImageView image2;                               //Image for right side of board
    private Label label = new Label("");               //Label to go on the bottom of the board describing feed
    private Text text = new Text("");                       //Text that moves across the bottom of the board
    private Pane flowPane = new Pane();                     //Pane at the bottom of board to hold feeds
    private Line path;                                      //Line for the path of the text on bottom
    private PathTransition pt = new PathTransition();       //Path transition for the text
    private int WIN = 81;                                   //Number of correct answers needed to win
    private StackPane bottom = new StackPane();             //Stackpane for the sudoku board
    private VBox vBox;

    public void start(Stage stage) throws Exception {
        boardSetup();
        createGrid();
        Line h1 = new Line(200, 180, 752, 180);
        Line h2 = new Line(200, 356, 752, 356);
        Line v1 = new Line(385, 10, 385, 530);
        Line v2 = new Line(567, 10, 567, 530);
        h1.setStrokeWidth(2.0);
        h2.setStrokeWidth(2.0);
        v1.setStrokeWidth(2.0);
        v2.setStrokeWidth(2.0);

        setImage("https://www.lacma.org/sites/default/files/styles/exhibition_image/public/primary_image/2019-01/M91_148_2b%20%282%29.jpg?itok=LEWU8CgV");

        //Create side panes with images
        StackPane seasonsPane = new StackPane();
        StackPane hintPane = new StackPane();
        seasonsPane.getChildren().add(image);
        hintPane.getChildren().add(image2);

        //create bottom pane with news feed
        createNewsFeed();
        Button news = new Button("CNN news");
        news.setLayoutY(5);
        Button weather = new Button("Weather");
        weather.setLayoutY(5);
        weather.setLayoutX(75);
        flowPane.getChildren().addAll(news, weather);
        flowPane.setPrefSize(952, 100);
        flowPane.setBackground(new Background(new BackgroundFill(Color.TAN, CornerRadii.EMPTY, Insets.EMPTY)));

        news.setOnAction(e -> {
            String newsHeadlines = null;
            try {
                newsHeadlines = RSSReader.readRSS("http://rss.cnn.com/rss/cnn_topstories.rss");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            pt.stop();
            text.setText(newsHeadlines);
            label.setText("CNN Headlines:");
            path.setEndX(newsHeadlines.length() * 3.5);
            path.setStartX(newsHeadlines.length() * -3.5);
            pt.setDuration(Duration.minutes(7));
            pt.playFromStart();
        });

        weather.setOnAction(e -> {
            String weatherHeadlines = null;
            try {
                weatherHeadlines = RSSReader.readWeatherRSS("https://w1.weather.gov/xml/current_obs/KLGU.rss");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            pt.stop();
            text.setText(weatherHeadlines);
            label.setText("Logan Airport Weather:");
            path.setEndX(flowPane.getWidth());
            path.setStartX(0);
            pt.setDuration(Duration.millis(40000));
            pt.playFromStart();
        });

        //put all panes together in border pane
        BorderPane pane = new BorderPane();
        Pane lines = new Pane();
        pane.setLeft(seasonsPane);
        pane.setRight(hintPane);
        pane.setCenter(gridBoard);
        pane.setBottom(flowPane);
        lines.getChildren().addAll(v1, v2, h1, h2);
        bottom.getChildren().addAll(pane, lines);
        lines.setMouseTransparent(true);

        //Create hint button
        Button hint = new Button("Hint");
        hint.setFont(new Font("Ink Free", 20));
        hintPane.getChildren().add(hint);

        //Create radio buttons and toggle group for the seasons pane
        ToggleGroup group = new ToggleGroup();
        Font font = new Font("Ink Free", 16);
        RadioButton springButton = new RadioButton("Spring");
        springButton.setToggleGroup(group);
        springButton.setFont(font);
        RadioButton summerButton = new RadioButton("Summer");
        summerButton.setToggleGroup(group);
        summerButton.setFont(font);
        RadioButton fallButton = new RadioButton("Fall");
        fallButton.setToggleGroup(group);
        fallButton.setFont(font);
        RadioButton winterButton = new RadioButton("Winter");
        winterButton.setToggleGroup(group);
        winterButton.setFont(font);

        //Create VBox to hold the radio buttons
        Text seasonsText = new Text("Choose a season theme:");
        seasonsText.setFont(font);
        vBox = new VBox(seasonsText, springButton, summerButton, fallButton, winterButton);
        vBox.setAlignment(Pos.CENTER_LEFT);
        vBox.setMaxSize(150, 100);
        vBox.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        seasonsPane.getChildren().add(vBox);

        //Bind radio button text color to the color of the label in the flowpane at bottom
        //Bind VBox background to the flowpane background
        vBox.backgroundProperty().bind(flowPane.backgroundProperty());
        springButton.textFillProperty().bind(label.textFillProperty());
        summerButton.textFillProperty().bind(label.textFillProperty());
        fallButton.textFillProperty().bind(label.textFillProperty());
        winterButton.textFillProperty().bind(label.textFillProperty());
        seasonsText.fillProperty().bind(label.textFillProperty());
        text.fillProperty().bind(label.textFillProperty());

        if (solve(originalBoard)) {
            printBoard();
            Scene scene = new Scene(bottom, 952, 640);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle("SUDOKU");
            stage.show();

            hint.setOnAction(e -> {
                int clues = 0;
                int correct_count = 0;
                //If the numbers entered are wrong, turn them red
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        if (!playBoard[i][j].getText().isEmpty()) {
                            if (Integer.parseInt(playBoard[i][j].getText()) != originalBoard[i][j]) {
                                playBoard[i][j].setStyle("-fx-text-fill: red");
                            }
                        }
                    }
                }
                //If red numbers are corrected to the right numbers, they turn black again
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        if (!playBoard[i][j].getText().isEmpty()) {
                            if (playBoard[i][j].getStyle().equals("-fx-text-fill: red") && Integer.parseInt(playBoard[i][j].getText()) == originalBoard[i][j]) {
                                playBoard[i][j].setStyle("-fx-text-fill: black");
                            }
                        }
                    }
                }
                //Two correct numbers are given in blue to fill in blank spaces
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                       if (playBoard[i][j].getText().isEmpty()) {
                           playBoard[i][j].setText(String.valueOf(originalBoard[i][j]));
                           playBoard[i][j].setStyle("-fx-text-fill: blue");
                           clues++;
                       }
                       if (clues >= 2)
                           break;
                    }
                    if (clues >= 2)
                        break;
                }
            });

            /**
             * For each radio button, the listener will change the images according to the
             * season picked
             */
            group.selectedToggleProperty().addListener(ov -> {
               if (group.getSelectedToggle() == springButton) {
                   seasonsPane.getChildren().remove(image);
                   hintPane.getChildren().remove(image2);
                   setImage("https://i.pinimg.com/236x/ac/16/f1/ac16f1937000241d90db73d8ac9f65f8--chinese-background-anime-wallpaper-fantasy.jpg");
                   seasonsPane.getChildren().add(image);
                   hintPane.getChildren().add(image2);
                   image.toBack();
                   image2.toBack();
                   flowPane.setBackground(new Background(new BackgroundFill(Color.PINK, CornerRadii.EMPTY, Insets.EMPTY)));
                   label.setTextFill(Color.BLACK);
               }
               if (group.getSelectedToggle() == summerButton) {
                   seasonsPane.getChildren().remove(image);
                   hintPane.getChildren().remove(image2);
                   setImage("https://i.pinimg.com/originals/14/4d/c3/144dc3d145821e772fe26d6ff2e22724.jpg");
                   seasonsPane.getChildren().add(image);
                   hintPane.getChildren().add(image2);
                   image.toBack();
                   image2.toBack();
                   flowPane.setBackground(new Background(new BackgroundFill(Color.STEELBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
                   label.setTextFill(Color.WHITE);
               }
               if (group.getSelectedToggle() == fallButton) {
                   seasonsPane.getChildren().remove(image);
                   hintPane.getChildren().remove(image2);
                   setImage("https://www.lacma.org/sites/default/files/styles/exhibition_image/public/primary_image/2019-01/M91_148_2b%20%282%29.jpg?itok=LEWU8CgV");
                   seasonsPane.getChildren().add(image);
                   hintPane.getChildren().add(image2);
                   image2.toBack();
                   image.toBack();
                   flowPane.setBackground(new Background(new BackgroundFill(Color.TAN, CornerRadii.EMPTY, Insets.EMPTY)));
                   label.setTextFill(Color.BLACK);
               }
               if (group.getSelectedToggle() == winterButton) {
                   seasonsPane.getChildren().remove(image);
                   hintPane.getChildren().remove(image2);
                   setImage("https://i.etsystatic.com/7423915/r/il/ef2342/1213122936/il_570xN.1213122936_ejsk.jpg");
                   seasonsPane.getChildren().add(image);
                   hintPane.getChildren().add(image2);
                   image.toBack();
                   image2.toBack();
                   flowPane.setBackground(new Background(new BackgroundFill(Color.DARKRED, CornerRadii.EMPTY, Insets.EMPTY)));
                   label.setTextFill(Color.WHITE);
               }
            });
            //Check for winners
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    playBoard[i][j].textProperty().addListener(ov -> {
                        checkWinner();
                    });
                }
            }
        }
    }

    /**
     * Reads text file to create sudoku board
     * @throws FileNotFoundException
     * @throws IOException
     */
    void boardSetup() throws FileNotFoundException, IOException {
        Scanner scanner1 = new Scanner(new File("C:/Data/Repos/CS2410/FinalProject/src/Sudoku5.txt"));
        ArrayList<Integer> numbers = new ArrayList<Integer>();
        while (scanner1.hasNextInt()) {
            numbers.add(scanner1.nextInt());
        }

        //add numbers to board
        int numCount = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                originalBoard[i][j] = numbers.get(numCount);
                System.out.print(originalBoard[i][j] + " ");
                numCount++;
            }
            System.out.println();
        }
    }

    /**
     * Sets up GridPane with textFields to create the sudoku board
     */
    void createGrid() {
        gridBoard.setAlignment(Pos.CENTER);
        gridBoard.setVgap(0.7);
        gridBoard.setHgap(0.7);
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                TextField square = new TextField();
                square.setMaxWidth(60.5);
                gridBoard.add(square, j, i);
                playBoard[i][j] = square;
                square.setFont(new Font("Rockwell", 30));
                if (originalBoard[i][j] != 0) {
                    square.setText(String.valueOf(originalBoard[i][j]));
                }
            }
        }
    }

    /**
     * solve sudoku board and put solutions in solved board
     * Reference : https://www.geeksforgeeks.org/sudoku-backtracking-7/
     */
    boolean solve(int [][] board) {
        int row = -1;
        int col = -1;
        boolean isEmpty = true;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] == 0) {
                    row = i;
                    col = j;
                    isEmpty = false;
                    break;
                }
            }
            if (!isEmpty)
                break;
        }
        if (isEmpty)
            return true;
        for (int data = 1; data <= 9; data++) {
            if (isSafe(board, row, col, data)) {
                board[row][col] = data;
                if (solve(board))
                    return true;
                else
                    board[row][col] = 0;
            }
        }
        return false;
    }

    /**
     * Reference : https://www.geeksforgeeks.org/sudoku-backtracking-7/
     * Looks to see if the number given is already in the row, column, or box.
     * If the number is already there, it is not safe.
     * @param board
     * @param row
     * @param col
     * @param data
     * @return
     */
    boolean isSafe(int [][] board, int row, int col, int data) {
        //check row
        for (int i = 0; i < 9; i++) {
            if (board[row][i] == data)
                return false;
        }
        //check column
        for (int j = 0; j < 9; j++) {
            if (board[j][col] == data)
                return false;
        }
        //check box
        int box = 3;
        int boxRowStart = row - row % 3;
        int boxColStart = col - col % 3;
        for (int r = boxRowStart; r < boxRowStart + box; r++)
            for (int c = boxColStart; c < boxColStart + box; c++)
                if (board[r][c] == data)
                    return false;
        return true;
    }

    void printBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++)
                System.out.print(originalBoard[i][j] + " ");
            System.out.println();
        }
    }

    /**
     * Uses RSS reader to start news feed at the bottom of the pane
     * Path transition gives a scroll feel to the feed
     * @throws Exception
     */
    void createNewsFeed() throws Exception {
        RSSReader newsFeed = new RSSReader();
        String news = newsFeed.readRSS("http://rss.cnn.com/rss/cnn_topstories.rss");
        text.setText(news);
        text.setFont(new Font("Bodoni MT", 18));
        label.setText("CNN Headlines:");
        label.setFont(new Font("Bodoni MT", 18));
        label.setLayoutY(30);
        pt.setDuration(Duration.minutes(7));
        path = new Line(news.length() * -3.5, 65, news.length() * 3.5, 65);
        path.setStroke(Color.TRANSPARENT);
        flowPane.getChildren().addAll(label, path, text);
        pt.setPath(path);
        pt.setNode(text);
        //pt.playFrom(news);
        pt.setCycleCount(Timeline.INDEFINITE);
        pt.play();
    }

    /**
     * Sets image and image2 to the URL image link that is passed in
     */
    public void setImage(String link) {
        image = new ImageView(new Image(link));
        image2 = new ImageView(new Image(link));
        image.setFitHeight(540);
        image.setFitWidth(200);
        image2.setFitHeight(540);
        image2.setFitWidth(200);
    }

    /**
     * When the board is solved, display the winnerLabel and make it flash
     */
    public void winner() {
        Pane winnerPane = new Pane();
        Label winnerLabel = new Label("WINNER!!");
        winnerLabel.setFont(new Font("Rockwell", 100));
        winnerPane.getChildren().add(winnerLabel);
        winnerLabel.setLayoutX(250);
        winnerLabel.setLayoutY(250);
        bottom.getChildren().add(winnerPane);
        winnerPane.setMouseTransparent(true);
        FadeTransition fade = new FadeTransition(Duration.millis(1000), winnerLabel);
        fade.setFromValue(1.0);
        fade.setToValue(0.2);
        fade.setCycleCount(Timeline.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        //Get color for winner label
        Background spring = new Background(new BackgroundFill(Color.PINK, CornerRadii.EMPTY, Insets.EMPTY));
        Background summer = new Background(new BackgroundFill(Color.STEELBLUE, CornerRadii.EMPTY, Insets.EMPTY));
        Background fall = new Background(new BackgroundFill(Color.TAN, CornerRadii.EMPTY, Insets.EMPTY));
        Background winter = new Background(new BackgroundFill(Color.DARKRED, CornerRadii.EMPTY, Insets.EMPTY));
        winnerLabel.textFillProperty().addListener(ov ->{
            if (flowPane.getBackground().equals(spring))
                winnerLabel.setTextFill(Color.PINK);
            else if (flowPane.getBackground().equals(summer))
                winnerLabel.setTextFill(Color.STEELBLUE);
            else if (flowPane.getBackground().equals(fall))
                winnerLabel.setTextFill(Color.TAN);
            else
                winnerLabel.setTextFill(Color.DARKRED);
        });
    }

    /**
     * Check for each spot in the player board and original board to be equal
     */
    public void checkWinner() {
        int correct_count = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (!playBoard[i][j].getText().isEmpty() && Integer.parseInt(playBoard[i][j].getText()) == originalBoard[i][j])
                    correct_count++;
            }
            //System.out.println(correct_count);
            if (correct_count == WIN)
                winner();
        }
    }
}

