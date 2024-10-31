package org.example.projectt6;

/**
 *
 * This program allows users to manage a virtual garden by planting, growing, harvesting, picking, and cutting various types of plants.
 * The program supports multiple commands to interact with the garden and provides output reflecting the state of the garden.
 * Usage:
 * - The user must specify the input file name as a command-line argument when running the program.
 * - Example: `java App myGarden.in`
 * - The input file should contain the garden configuration and commands as specified below.
 * Input File Format:
 * - Each command must be on a separate line.
 * - Coordinates for plotting (x, y) are zero-indexed.
 * - The garden grid allows for a maximum width of 80 characters, accommodating a maximum of 16 plots across.
 * Commands:
 * 1. PLANT (x,y) <type>
 *    - Example: PLANT (0,0) rose
 *    - This command plants the specified type of plant in the garden at the given coordinates (x,y).
 * 2. PRINT
 *    - Example: PRINT
 *    - Prints the entire garden to standard output.
 * 3. GROW <num>
 *    - Example: GROW 1
 *    - Increases the growth of all plants in the garden by the specified number of times.
 * 4. GROW <num> (x,y)
 *    - Example: GROW 1 (2,3)
 *    - Grows the plant located at (x,y) by the specified number of times.
 * 5. GROW <num> <type>
 *    - Example: GROW 1 rose
 *    - Grows all plants of the specified type by the specified number of times.
 * 6. HARVEST
 *    - Removes all vegetables from the garden.
 * 7. HARVEST (x,y)
 *    - Example: HARVEST (2,3)
 *    - Harvests the vegetable located at (x,y) if it is a vegetable.
 * 8. HARVEST <type>
 *    - Example: HARVEST tomato
 *    - Harvests all vegetables of the specified type from the garden.
 * 9. PICK
 *    - Removes all flowers from the garden.
 * 10. PICK (x,y)
 *     - Example: PICK (2,3)
 *     - Picks the flower located at (x,y) if it is a flower.
 * 11. PICK <type>
 *     - Example: PICK rose
 *     - Picks all flowers of the specified type from the garden.
 * 12. CUT
 *     - Removes all trees from the garden.
 * 13. CUT (x,y)
 *     - Example: CUT (2,3)
 *     - Cuts the tree located at (x,y) if it is a tree.
 * 14. CUT <type>
 *     - Example: CUT PINE
 *     - Cuts all trees of the specified type from the garden.
 * Example:
 * Input file (e.g., `myGarden.in`):
 * rows: 1
 * cols: 1
 * PLANT (0,0) apple
 * PRINT
 * GROW 1
 * PRINT

 *
 * Author: Sherif Ashraf Ali
 *
 */
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;


import java.io.*;
import java.util.*;

public class App extends Application {

    static Garden garden = new Garden();
    // Command queue to store garden actions
    static Queue<String> commandQueue = new LinkedList<>();
    // variables that will be read in from file
    private static double delay = 2;
    // constants for the program
    private final static int TEXT_SIZE = 120;
    private final static int RECT_SIZE = 7;
    private final static int RECT_SIZE_Plant = 10;
    // temporary constants for starter code
    private static int SIZE_ACROSS;
    private static int SIZE_DOWN;
    public static void setDelay(double delay) {
        App.delay = delay;
    }
    public static void setSizeAcross(int colNum) {
        SIZE_ACROSS = colNum * 5 * (RECT_SIZE_Plant + 10);
    }
    public static void setSizeDown(int rowNum) {
        SIZE_DOWN = rowNum * 5 * (RECT_SIZE_Plant + 10);
    }
    public static void main(String[] args) throws FileNotFoundException {
        // Initialize garden types from external file (vegetables, fruits, trees)
        RunGarden.addTypes(RunGarden.fruits, RunGarden.vegetables, RunGarden.trees, RunGarden.flowers);
        Scanner read = new Scanner(System.in);
        String fileName = read.next(); //"myGarden.in";
        read.close();
        File file = new File(fileName);
        Scanner myreader = new Scanner(file);
        while (myreader.hasNext()) {
            String lineString = myreader.nextLine();
            //System.out.println(lineString);
            String line[] = lineString.split(" ");
            // Sets number of rows in the garden
            if (line[0].equals("rows:")) {
                garden.setRows(Integer.valueOf(line[1]));
                setSizeDown(Integer.valueOf(line[1]));

            }// Sets number of columns in the garden
            else if (line[0].equalsIgnoreCase("cols:")) {
                garden.setCols(Integer.valueOf(line[1]));
                garden.setLand(garden.getRows(), garden.getCols());
                setSizeAcross(Integer.valueOf(line[1]));
            }// Sets delay
            else if (line[0].equalsIgnoreCase("delay:")) {
                App.setDelay(Double.valueOf(line[1]));
            } else {
                // Adds other lines to command queue
                commandQueue.add(lineString);
            }
        }
        myreader.close();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws FileNotFoundException {
        TextArea command = new TextArea();
        GraphicsContext gc = setupStage(primaryStage, command);
        primaryStage.show();
        simulateGarden(gc, command);
    }

    private void simulateGarden(GraphicsContext gc, TextArea command) throws FileNotFoundException {

        // Loops through each row in the garden
        for (int x = 0; x < garden.getRows(); x++) {
            for (int y = 0; y < garden.getCols(); y++) {
                // Draw a 5x5 block for each garden cell
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 5; j++) {
                        gc.setFill(Color.GRAY); // Set color
                        gc.fillRect((x * 5 + i) * 20, (y * 5 + j) * 20, RECT_SIZE, RECT_SIZE);
                    }
                }
            }
        }

        // Creates a pause transition
        PauseTransition wait = new PauseTransition(Duration.seconds(delay));
        wait.setOnFinished((e) -> {
            // Checks if there are remaining commands
            if (!commandQueue.isEmpty()) {
                // Retrieves and removes the next command from the queue
                String line = commandQueue.poll(); // Fetch and remove the next command
                processCommand(line, gc, command);   // Process the command
                gardenDraw(gc, command);                      // Redraw garden after each command
                wait.playFromStart();                // Replay if there are more commands
            } else {
                wait.stop();
            }
        });
        wait.play();
    }

    // Processes each command and applies it to the garden
    private void processCommand(String lineString, GraphicsContext gc, TextArea command) {
        String[] line = lineString.split(" ");
        if (line[0].equalsIgnoreCase("plant")) {
            int row = Integer.parseInt(String.valueOf(line[1].charAt(1)));
            int col = Integer.parseInt(String.valueOf(line[1].charAt(3)));
            garden.plantGarden(row, col, line[2]);
        } else if (line[0].equalsIgnoreCase("GROW")) {
            int num = Integer.parseInt(line[1]);
            if (line.length == 2) {
                garden.growAll(num);
                //check if the input is (2,2) Format
            } else if (line[2].matches("\\(\\d+,\\d+\\)")) {
                int row = Integer.parseInt(String.valueOf(line[2].charAt(1)));
                int col = Integer.parseInt(String.valueOf(line[2].charAt(3)));
                garden.growAtPosition(num, row, col);
            } else if (line[2].equalsIgnoreCase("flower") || line[2].equalsIgnoreCase("tree") || line[2].equalsIgnoreCase("fruit") || line[2].equalsIgnoreCase("vegetable")) {
                garden.growByClass(num, line[2]);
            } else {
                garden.growByType(num, line[2]);
            }
        } else if (line[0].equalsIgnoreCase("HARVEST") || line[0].equalsIgnoreCase("CUT") || line[0].equalsIgnoreCase("PICK") || line[0].equalsIgnoreCase("RIPE")) {
            if (line.length == 1) {
                garden.remove(line[0]);
                //check if the input is (2,2) Format
            } else if (line[1].matches("\\(\\d+,\\d+\\)")) {
                int row = Integer.parseInt(String.valueOf(line[1].charAt(1)));
                int col = Integer.parseInt(String.valueOf(line[1].charAt(3)));
                garden.remove(row, col, line[0]);
            } else {
                garden.remove(line[1]);
            }
        }
        command.appendText(lineString + "\n");
    }
    private void gardenDraw(GraphicsContext gc, TextArea command) {
        // Retrieves garden color array
        Color arr[][] = garden.PrintGarden();
        // Clears canvas for redrawing garden
        gc.clearRect(0, 0, SIZE_ACROSS, SIZE_DOWN);
        // Draws each cell with appropriate color
        for (int x = 0; x < garden.getRows() * 5; x++) {
            for (int y = 0; y < garden.getCols() * 5; y++) {
                // checks if the cell is null
                if (arr[x][y] != null) {
                    // get the colr for the plant by calling the land arr and the class of the cell
                    Color color = garden.getLand()[x / 5][y / 5].getColor();
                    gc.setFill(color);
                    gc.fillRect(y * 20, x * 20, RECT_SIZE_Plant, RECT_SIZE_Plant);
                } else {
                    gc.setFill(Color.GRAY);// Set color for the rectangle
                    gc.fillRect(y * 20, x * 20, RECT_SIZE, RECT_SIZE);
                }
            }
        }
    }
    public GraphicsContext setupStage(Stage primaryStage, TextArea command) {
        // Border pane will contain canvas for drawing and text area underneath
        BorderPane p = new BorderPane();
        StackPane root = new StackPane();
        root.setPadding(new Insets(5));
        root.getChildren().add(p);
        // Now set the scene with the StackPane instead of directly with the BorderPane
        Scene scene = new Scene(root, SIZE_ACROSS + 10, SIZE_DOWN + TEXT_SIZE + 10);
        // Canvas(pixels across, pixels down)
        Canvas canvas = new Canvas(SIZE_ACROSS, SIZE_DOWN);
        // Command TextArea will hold the commands from the file
        command.setPrefHeight(TEXT_SIZE);
        command.setEditable(false);
        // Place the canvas and command output areas in pane.
        p.setCenter(canvas);
        p.setBottom(command);
        primaryStage.setTitle("Garden");
        primaryStage.setScene(scene);
        return canvas.getGraphicsContext2D();
    }

}
