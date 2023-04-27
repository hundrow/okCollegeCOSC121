package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

//Author: Andrew Johnson
//Date: March 17, 2023
//Version: 3
/*
* v3
* Now with a Main menu to select difficulty.
* The first click of every game will now be on a tile with zero bombs nearby.
* The first click will start a timer in the top left.
* Left clicks on tiles that already have there number of flags will open the rest of
* the tiles around.
* Any tiles that have been misflagged are now marked with a red X.
* When you win the game you are presented with a message box that asks if you want to
* add your score to the high score list.
* 
* v2
* The board will now randomly generate the locations for the mines and
* then it it will count up the mines that each tile touches.
* When the player clicks a tile that has zero mines nearby the game will
* clear the tile covers until numbers are revealed.
* The top right counter will count down the number of mines that are left.
* The player can now right click on tiles that they think are mines to both disable 
* the tile from being clicked and also mark mine locations and decrease the counter.
* 
* v1
* The happy face button, in the header, changes state depending on the state of the game.
* Smiling = game state normal and still running.
* X eyes = bomb has been selected and game state stopped.
* Sunglasses = Game has been won and game state stopped.
* Clicking the happy face resets the game regardless of the state of the game.
* 
*/

public class MineSweeper3 extends Application {
	static int count = 0; // this variable determines the win condition.
	static int bombCountMemory; // this is how the game is reset with the correct number of mines.
	static int bombCount; // this is what the mine counter is based off of.
	static int bombCountTens; // bombCount / 10
	static int bombCountOnes; // bombCount % 10
	static boolean gameOver = false; // this is used in the clearTiles method to end the game if the tiles were
										// wrongly flagged.
	static int secondsElapsed = 0;
	protected Timeline timer;

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) {
		final String cssBase = "-fx-background-color: darkgrey;\n" + "-fx-border-radius: 3px;\n"
				+ "-fx-border-width: 5px;\n" + "-fx-border-color: white grey dimgrey whitesmoke;\n";
		final String cssMid = "-fx-background-color: lightgrey;\n" + "-fx-border-width: 5px;\n"
				+ "-fx-border-color: lightgrey;\n";
		final String cssTop = "-fx-background-color: lightgrey;\n" + "-fx-border-radius: 3px;\n"
				+ "-fx-border-width: 5px;\n" + "-fx-border-color: grey whitesmoke white dimgrey;\n";

		// The High Score scene is made here
		BorderPane highScorePane = new BorderPane();
		ArrayList<HighScore> topScores = new ArrayList<HighScore>();
		File scoresTXT = new File("res/highScoreList.txt");
		VBox highScoreVBox = new VBox();
		String highScoresString = "";
		try {
			readScores(topScores, scoresTXT);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		for (HighScore score : topScores) {
			highScoresString += score.getScoreScreenFormat();
		}
		Label scoresHeaderBar = new Label("     Fastest Times\n" + "Name           Time\n");
		Label scoresListLBL = new Label(highScoresString);
		TextField nameInputTBX = new TextField();
		Button enterNameBTN = new Button("Submit");
		Button backToGameBTN = new Button("Back to game.");
		enterNameBTN.setOnMouseClicked(e -> {
			String newScoresString = "";
			String name = nameInputTBX.getText();
			nameInputTBX.setVisible(false);
			enterNameBTN.setVisible(false);
			int score = secondsElapsed;
			topScores.add(new HighScore(name, score));
			Collections.sort(topScores);
			for (HighScore score2 : topScores) {
				newScoresString += score2.getScoreScreenFormat();
			}
			scoresListLBL.setText(newScoresString);
			nameInputTBX.setText("");
			try {
				writeScores(topScores, scoresTXT);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		});
		
		highScoreVBox.getChildren().add(scoresHeaderBar);
		highScoreVBox.getChildren().add(scoresListLBL);
		highScoreVBox.getChildren().add(nameInputTBX);
		highScoreVBox.getChildren().add(enterNameBTN);
		highScoreVBox.getChildren().add(backToGameBTN);
		highScoreVBox.setAlignment(Pos.CENTER);
		highScorePane.setStyle(cssBase);
		highScorePane.setCenter(highScoreVBox);
		Scene highScoreScene = new Scene(highScorePane, 150, 200);

		// The Gameplay scene is made here
		HBox headerBarHBox = new HBox();
		headerBarHBox.setAlignment(Pos.CENTER);
		headerBarHBox.setSpacing(5);
		StackPane midHeaderPaneForStylingBorder = new StackPane(headerBarHBox);
		GridPane gridPaneForGameplay = new GridPane();
		BorderPane borderPaneForGameScreen = new BorderPane();
		StackPane bottomPaneForStylingBorder = new StackPane(borderPaneForGameScreen);
		HappyFaceButton faceButton = new HappyFaceButton();
		ImageView bombCounterHundreds = new ImageView();
		ImageView bombCounterTens = new ImageView();
		ImageView bombCounterOnes = new ImageView();
		Scene gameScene = new Scene(bottomPaneForStylingBorder);
		backToGameBTN.setOnMouseClicked(e -> {
			stage.setScene(gameScene);
			nameInputTBX.setVisible(true);
			enterNameBTN.setVisible(true);
		});
		bottomPaneForStylingBorder.setStyle(cssBase);
		borderPaneForGameScreen.setStyle(cssMid);
		midHeaderPaneForStylingBorder.setStyle(cssTop);
		BorderPane.setMargin(midHeaderPaneForStylingBorder, new Insets(0, 0, 5, 0));
		gridPaneForGameplay.setStyle(cssTop);
		borderPaneForGameScreen.setTop(midHeaderPaneForStylingBorder);
		borderPaneForGameScreen.setCenter(gridPaneForGameplay);

		// The main menu scene is made here
		VBox menuChoicesVBox = new VBox();
		Button easyButton = new Button("Easy");
		easyButton.setOnMouseClicked(e -> {
			// 8x8 grid 10 mines
			bombCount = 10;
			initializeGame(8, 8, borderPaneForGameScreen, headerBarHBox, midHeaderPaneForStylingBorder,
					gridPaneForGameplay, faceButton, bombCounterTens, bombCounterOnes, bombCounterHundreds, stage,
					highScoreScene);
			stage.setScene(gameScene);
		});
		Button mediumButton = new Button("Medium");
		mediumButton.setOnMouseClicked(e -> {
			// 16x16 grid with 40 mines
			bombCount = 40;
			initializeGame(16, 16, borderPaneForGameScreen, headerBarHBox, midHeaderPaneForStylingBorder,
					gridPaneForGameplay, faceButton, bombCounterTens, bombCounterOnes, bombCounterHundreds, stage,
					highScoreScene);
			stage.setScene(gameScene);
		});
		Button hardButton = new Button("Hard");
		hardButton.setOnMouseClicked(e -> {
			// 16x32 grid with 99 mines
			bombCount = 99;
			initializeGame(16, 32, borderPaneForGameScreen, headerBarHBox, midHeaderPaneForStylingBorder,
					gridPaneForGameplay, faceButton, bombCounterTens, bombCounterOnes, bombCounterHundreds, stage,
					highScoreScene);
			stage.setScene(gameScene);
		});
		menuChoicesVBox.setAlignment(Pos.CENTER);
		menuChoicesVBox.setSpacing(20);
		menuChoicesVBox.getChildren().add(easyButton);
		menuChoicesVBox.getChildren().add(mediumButton);
		menuChoicesVBox.getChildren().add(hardButton);
		menuChoicesVBox.setStyle(cssBase);
		Scene mainMenu = new Scene(menuChoicesVBox, 150, 200);

		stage.setTitle("MineSweeper");
		stage.setScene(mainMenu);
		stage.setResizable(false);
		stage.initStyle(StageStyle.UTILITY);
		stage.show();
	}

	private void initializeGame(int rows, int cols, BorderPane borderPane, HBox headerBar, StackPane midHeaderPane,
			GridPane gridPane, HappyFaceButton faceButton, ImageView bombCounterTens, ImageView bombCounterOnes,
			ImageView bombCounterHundreds, Stage stage, Scene highScoreScene) {
		bombCountMemory = bombCount;
		createTopBar(rows, cols, borderPane, headerBar, midHeaderPane, faceButton, gridPane, bombCounterTens,
				bombCounterOnes, bombCounterHundreds, stage, highScoreScene);
		createGameGrid(rows, cols, borderPane, gridPane, faceButton, bombCounterTens, bombCounterOnes,
				bombCounterHundreds, stage, highScoreScene);
	}

	private void createTopBar(int rows, int cols, BorderPane borderPane, HBox headerBar, StackPane midHeaderPane,
			HappyFaceButton faceButton, GridPane gridPane, ImageView bombCounterTens, ImageView bombCounterOnes,
			ImageView bombCounterHundreds, Stage stage, Scene highScoreScene) {

		bombCountTens = bombCount / 10;
		bombCountOnes = bombCount % 10;

		HBox timerHBox = new HBox();
		ImageView timeCounterHundreds = new ImageView(new Image("file:res/digits1/digits/" + 0 + ".png"));
		timeCounterHundreds.setFitHeight(52);
		timeCounterHundreds.setFitWidth(29);
		ImageView timeCounterTens = new ImageView(new Image("file:res/digits1/digits/" + 0 + ".png"));
		timeCounterTens.setFitHeight(52);
		timeCounterTens.setFitWidth(29);
		ImageView timeCounterOnes = new ImageView(new Image("file:res/digits1/digits/" + 0 + ".png"));
		timeCounterOnes.setFitHeight(52);
		timeCounterOnes.setFitWidth(29);
		timerHBox.getChildren().add(timeCounterHundreds);
		timerHBox.getChildren().add(timeCounterTens);
		timerHBox.getChildren().add(timeCounterOnes);

		timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
			timeCounterHundreds
					.setImage(new Image("file:res/digits1/digits/" + ((secondsElapsed / 100) % 10) + ".png"));
			timeCounterTens.setImage(new Image("file:res/digits1/digits/" + ((secondsElapsed / 10) % 10) + ".png"));
			timeCounterOnes.setImage(new Image("file:res/digits1/digits/" + (secondsElapsed % 10) + ".png"));
			secondsElapsed++;
		}));

		HBox mineCounter = new HBox();
		bombCounterHundreds.setImage(new Image("file:res/digits1/digits/" + 0 + ".png"));
		bombCounterHundreds.setFitHeight(52);
		bombCounterHundreds.setFitWidth(29);
		bombCounterTens.setImage(new Image("file:res/digits1/digits/" + bombCountTens + ".png"));
		bombCounterTens.setFitHeight(52);
		bombCounterTens.setFitWidth(29);
		bombCounterOnes.setImage(new Image("file:res/digits1/digits/" + bombCountOnes + ".png"));
		bombCounterOnes.setFitHeight(52);
		bombCounterOnes.setFitWidth(29);
		mineCounter.getChildren().add(bombCounterHundreds);
		mineCounter.getChildren().add(bombCounterTens);
		mineCounter.getChildren().add(bombCounterOnes);

		faceButton.setOnMouseClicked(e -> {
			timeCounterHundreds.setImage(new Image("file:res/digits1/digits/" + 0 + ".png"));
			timeCounterTens.setImage(new Image("file:res/digits1/digits/" + 0 + ".png"));
			timeCounterOnes.setImage(new Image("file:res/digits1/digits/" + 0 + ".png"));
			timer.stop();
			secondsElapsed = 0;
			count = 0;
			faceButton.setGraphic(faceButton.happyFace);
			resetBoard();
			bombCounterHundreds.setImage(new Image("file:res/digits1/digits/0.png"));
			bombCounterTens.setImage(new Image("file:res/digits1/digits/" + bombCountTens + ".png"));
			bombCounterOnes.setImage(new Image("file:res/digits1/digits/" + bombCountOnes + ".png"));

			createGameGrid(rows, cols, borderPane, gridPane, faceButton, bombCounterTens, bombCounterOnes,
					bombCounterHundreds, stage, highScoreScene);
		});
		headerBar.getChildren().add(timerHBox);
		headerBar.getChildren().add(faceButton);
		headerBar.getChildren().add(mineCounter);
	}

	private void createGameGrid(int rows, int cols, BorderPane borderPane, GridPane gridPane,
			HappyFaceButton faceButton, ImageView bombCounterTens, ImageView bombCounterOnes,
			ImageView bombCounterHundreds, Stage stage, Scene highScoreScene) {
		SwitchingButton[][] buttonMatrix = new SwitchingButton[rows][cols];
		int[][] gb = new int[rows][cols];

		for (int i = 0; i < gb.length; i++) {
			for (int j = 0; j < gb[0].length; j++) {
				int[] locationRowCol = { i, j };
				buttonMatrix[i][j] = new SwitchingButton();
				SwitchingButton gameTileButton = buttonMatrix[i][j];

				gameTileButton.setOnMouseClicked(e -> {
					if (e.getButton() == MouseButton.PRIMARY) {
						setUpGameBoard(locationRowCol[0], locationRowCol[1], rows, cols, bombCount, faceButton,
								borderPane, gridPane, bombCounterTens, bombCounterOnes, bombCounterHundreds, stage,
								highScoreScene);
						count++;
					}
				});
				gridPane.add(buttonMatrix[i][j], j, i);
			}
		}
		borderPane.setCenter(gridPane);
	}

	private void setUpGameBoard(int locY, int locX, int rows, int cols, int numMines, HappyFaceButton faceButton,
			BorderPane borderPane, GridPane gridPane, ImageView bombCounterTens, ImageView bombCounterOnes,
			ImageView bombCounterHundreds, Stage stage, Scene highScoreScene) {
		SwitchingButton[][] buttonMatrix = new SwitchingButton[rows][cols];
		timer.setCycleCount(Animation.INDEFINITE);
		timer.play();

		int[][] gb = new int[rows][cols];

		placeMines(gb, numMines, locY, locX);
		countMines(gb);

		for (int i = 0; i < gb.length; i++) {
			for (int j = 0; j < gb[0].length; j++) {
				int[] points = { i, j };
				int tileValue = gb[i][j];
				buttonMatrix[i][j] = new SwitchingButton();
				SwitchingButton gameTileButton = buttonMatrix[i][j];
				if (gb[i][j] == 10) {
					gb[locY][locX] = 0;
					buttonMatrix[i][j].setTile(0);
					buttonMatrix[i][j].state = false;
				}
				// This lambda click event is where most of the game logic happens
				gameTileButton.setOnMousePressed(e -> {
					faceButton.setGraphic(faceButton.ohFace);
				});
				gameTileButton.setOnMouseReleased(e -> {
					faceButton.setGraphic(faceButton.happyFace);
				});
				gameTileButton.setOnMouseClicked(e -> {
					if (e.getButton() == MouseButton.SECONDARY) {
						if (!gameTileButton.flagged && gameTileButton.state) {
							gameTileButton.setGraphic(gameTileButton.flagTile);
							gameTileButton.flagged = true;
							gameTileButton.state = false;
							decreaseBombCounter(bombCounterTens, bombCounterOnes, bombCounterHundreds);
						} else if (gameTileButton.flagged) {
							gameTileButton.setGraphic(gameTileButton.coverTile);
							gameTileButton.flagged = false;
							gameTileButton.state = true;
							increaseBombCounter(bombCounterTens, bombCounterOnes, bombCounterHundreds);
						}
					}
					if (e.getButton() == MouseButton.PRIMARY) {
						if (gameTileButton.state && tileValue != 0) {
							gameTileButton.setTile(tileValue);
							gameTileButton.state = false;
							count++;
						} else if (gameTileButton.state) {
							clearZeros(gb, buttonMatrix, points[0], points[1]);
							count++;
						} else {
							clearTiles(gb, buttonMatrix, points[0], points[1]);
						}
						if ((tileValue == 9 && gameTileButton.flagged == false) || gameOver) {
							// GAME OVER CONDITION
							timer.stop();
							faceButton.setGraphic(faceButton.sadFace);
							for (int row = 0; row < buttonMatrix.length; row++) {
								for (int col = 0; col < buttonMatrix[row].length; col++) {
									if (gb[row][col] == 9 && buttonMatrix[row][col].state) {
										buttonMatrix[row][col].setGraphic(buttonMatrix[row][col].greyMine);
									}
									if (gb[row][col] != 9 && buttonMatrix[row][col].flagged) {
										buttonMatrix[row][col].setGraphic(buttonMatrix[row][col].misFlagged);
									}
									buttonMatrix[row][col].state = false;
								}
							}
						}
						if (count == (cols * rows) - numMines && !gameOver) {
							// WIN CONDITION
							timer.stop();
							faceButton.setGraphic(faceButton.coolFace);
							for (int row = 0; row < buttonMatrix.length; row++) {
								for (int col = 0; col < buttonMatrix[row].length; col++) {
									buttonMatrix[row][col].state = false;
								}
							}
							Alert alert = new Alert(AlertType.CONFIRMATION);
							alert.setTitle("You Won");
							alert.setHeaderText("Congratulations!");
							alert.setContentText("Would you like to add your score?");
							alert.showAndWait().ifPresent(mb -> {
								if (mb == ButtonType.OK) {
									stage.setScene(highScoreScene);
								}
							});
						}
					}

				});
				gridPane.add(buttonMatrix[i][j], j, i);
			}
		}
		// borderPane.setCenter(gridPane);
		clearZeros(gb, buttonMatrix, locY, locX);
	}

	private void clearTiles(int[][] gb, SwitchingButton[][] bm, int row, int col) {
		int flagCount = 0;
		for (int i = row - 1; i <= row + 1; i++) {
			for (int j = col - 1; j <= col + 1; j++) {
				if (i >= 0 && i <= gb.length - 1 && j >= 0 && j <= gb[i].length - 1 && bm[i][j].flagged) {// top tile
					flagCount++;
				}
			}
		}
		if (flagCount >= gb[row][col]) {
			if (row > 0 && bm[row - 1][col].state) {// top tile
				bm[row - 1][col].setTile(gb[row - 1][col]);
				bm[row - 1][col].state = false;
				if (gb[row - 1][col] == 9) {
					gameOver = true;
				}
				if (gb[row - 1][col] == 0) {
					clearZeros(gb, bm, row - 1, col);
				}
				count++;
			}
			if (row < gb.length - 1 && bm[row + 1][col].state) {// bottom tile
				bm[row + 1][col].setTile(gb[row + 1][col]);
				bm[row + 1][col].state = false;
				if (gb[row + 1][col] == 9) {
					gameOver = true;
				}
				if (gb[row + 1][col] == 0) {
					clearZeros(gb, bm, row + 1, col);
				}
				count++;
			}
			if (col > 0 && bm[row][col - 1].state) {// left tile
				bm[row][col - 1].setTile(gb[row][col - 1]);
				bm[row][col - 1].state = false;
				if (gb[row][col - 1] == 9) {
					gameOver = true;
				}
				if (gb[row][col - 1] == 0) {
					clearZeros(gb, bm, row, col - 1);
				}
				count++;
			}
			if (col < gb[0].length - 1 && bm[row][col + 1].state) {// right tile
				bm[row][col + 1].setTile(gb[row][col + 1]);
				bm[row][col + 1].state = false;
				if (gb[row][col + 1] == 9) {
					gameOver = true;
				}
				if (gb[row][col + 1] == 0) {
					clearZeros(gb, bm, row, col + 1);
				}
				count++;
			}
			if (row > 0 && col > 0 && bm[row - 1][col - 1].state) {// top-left tile
				bm[row - 1][col - 1].setTile(gb[row - 1][col - 1]);
				bm[row - 1][col - 1].state = false;
				if (gb[row - 1][col - 1] == 9) {
					gameOver = true;
				}
				if (gb[row - 1][col - 1] == 0) {
					clearZeros(gb, bm, row - 1, col - 1);
				}
				count++;
			}
			if (row < gb.length - 1 && col < gb[0].length - 1 && bm[row + 1][col + 1].state) {// bottom-right tile
				bm[row + 1][col + 1].setTile(gb[row + 1][col + 1]);
				bm[row + 1][col + 1].state = false;
				if (gb[row + 1][col + 1] == 9) {
					gameOver = true;
				}
				if (gb[row + 1][col + 1] == 0) {
					clearZeros(gb, bm, row + 1, col + 1);
				}
				count++;
			}
			if (row > 0 && col < gb[0].length - 1 && bm[row - 1][col + 1].state) {// top-right tile
				bm[row - 1][col + 1].setTile(gb[row - 1][col + 1]);
				bm[row - 1][col + 1].state = false;
				if (gb[row - 1][col + 1] == 9) {
					gameOver = true;
				}
				if (gb[row - 1][col + 1] == 0) {
					clearZeros(gb, bm, row - 1, col + 1);
				}
				count++;
			}
			if (row < gb.length - 1 && col > 0 && bm[row + 1][col - 1].state) {// bottom-left tile
				bm[row + 1][col - 1].setTile(gb[row + 1][col - 1]);
				bm[row + 1][col - 1].state = false;
				if (gb[row + 1][col - 1] == 9) {
					gameOver = true;
				}
				if (gb[row + 1][col - 1] == 0) {
					clearZeros(gb, bm, row + 1, col - 1);
				}
				count++;
			}
		}
	}

	private void clearZeros(int[][] gb, SwitchingButton[][] bm, int row, int col) {
		bm[row][col].setTile(gb[row][col]);
		bm[row][col].state = false;
		if (gb[row][col] == 0) {
			if (row > 0 && bm[row - 1][col].state) {// top tile
				clearZeros(gb, bm, row - 1, col);
				count++;
			}
			if (row < gb.length - 1 && bm[row + 1][col].state) {// bottom tile
				clearZeros(gb, bm, row + 1, col);
				count++;
			}
			if (col > 0 && bm[row][col - 1].state) {// left tile
				clearZeros(gb, bm, row, col - 1);
				count++;
			}
			if (col < gb[0].length - 1 && bm[row][col + 1].state) {// right tile
				clearZeros(gb, bm, row, col + 1);
				count++;
			}
			if (row > 0 && col > 0 && bm[row - 1][col - 1].state) {// top-left tile
				clearZeros(gb, bm, row - 1, col - 1);
				count++;
			}
			if (row < gb.length - 1 && col < gb[0].length - 1 && bm[row + 1][col + 1].state) {// bottom-right tile
				clearZeros(gb, bm, row + 1, col + 1);
				count++;
			}
			if (row > 0 && col < gb[0].length - 1 && bm[row - 1][col + 1].state) {// top-right tile
				clearZeros(gb, bm, row - 1, col + 1);
				count++;
			}
			if (row < gb.length - 1 && col > 0 && bm[row + 1][col - 1].state) {// bottom-left tile
				clearZeros(gb, bm, row + 1, col - 1);
				count++;
			}
		}
	}

	private void countMines(int[][] gb) {
		for (int row = 0; row < gb.length; row++) {
			for (int col = 0; col < gb[0].length; col++) {
				if (gb[row][col] == 9) {
					if (row > 0 && gb[row - 1][col] < 8) {// top tile
						gb[row - 1][col]++;
					}
					if (row < gb.length - 1 && gb[row + 1][col] < 8) {// bottom tile
						gb[row + 1][col]++;
					}
					if (col > 0 && gb[row][col - 1] < 8) {// left tile
						gb[row][col - 1]++;
					}
					if (col < gb[0].length - 1 && gb[row][col + 1] < 8) {// right tile
						gb[row][col + 1]++;
					}
					if (row > 0 && col > 0 && gb[row - 1][col - 1] < 8) {// top-left tile
						gb[row - 1][col - 1]++;
					}
					if (row < gb.length - 1 && col < gb[0].length - 1 && gb[row + 1][col + 1] < 8) {// bottom-right tile
						gb[row + 1][col + 1]++;
					}
					if (row > 0 && col < gb[0].length - 1 && gb[row - 1][col + 1] < 8) {// top-right tile
						gb[row - 1][col + 1]++;
					}
					if (row < gb.length - 1 && col > 0 && gb[row + 1][col - 1] < 8) {// bottom-left tile
						gb[row + 1][col - 1]++;
					}
				}
			}
		}
	}

	private void placeMines(int[][] gb, int numMines, int locY, int locX) {
		Random rand = new Random();
		gb[locY][locX] = 10;
		// place the mines randomly across the game board at least one tile away from
		// clicked location
		int placed = 0;
		while (placed < numMines) {
			int row = rand.nextInt(gb.length);
			int col = rand.nextInt(gb[0].length);
			// skip the tile if there is already a bomb there
			if ((row == locY - 1 || row == locY + 1 || row == locY)
					&& (col == locX - 1 || col == locX + 1 || col == locX)) {
				continue;
			} else if (gb[row][col] < 9) {
				gb[row][col] = 9;
				placed++;
			}
		}
	}

	private void resetBoard() {
		gameOver = false;
		bombCount = bombCountMemory;
		bombCountTens = bombCount / 10;
		bombCountOnes = bombCount % 10;
	}

	private void decreaseBombCounter(ImageView bombCounterTens, ImageView bombCounterOnes,
			ImageView bombCounterHundreds) {
		bombCount--;
		bombCountTens = bombCount / 10;
		bombCountOnes = bombCount % 10;
		if (bombCount < 0) {
			bombCounterHundreds.setImage(new Image("file:res/digits1/digits/a.png"));
		}
		bombCounterTens.setImage(new Image("file:res/digits1/digits/" + Math.abs(bombCountTens) + ".png"));
		bombCounterOnes.setImage(new Image("file:res/digits1/digits/" + Math.abs(bombCountOnes) + ".png"));
	}

	private void increaseBombCounter(ImageView bombCounterTens, ImageView bombCounterOnes,
			ImageView bombCounterHundreds) {
		bombCount++;
		bombCountTens = bombCount / 10;
		bombCountOnes = bombCount % 10;
		if (bombCount > 0) {
			bombCounterHundreds.setImage(new Image("file:res/digits1/digits/0.png"));
		}
		bombCounterTens.setImage(new Image("file:res/digits1/digits/" + Math.abs(bombCountTens) + ".png"));
		bombCounterOnes.setImage(new Image("file:res/digits1/digits/" + Math.abs(bombCountOnes) + ".png"));
	}

	public static void readScores(ArrayList<HighScore> scores, File filename) throws FileNotFoundException {
		Scanner fileIn = new Scanner(filename);
		String name = "";
		int score = 0;
		while (fileIn.hasNext()) {
			name = fileIn.nextLine();
			score = Integer.parseInt(fileIn.nextLine());
			scores.add(new HighScore(name, score));
		}
		fileIn.close();
	}

	public static void writeScores(ArrayList<HighScore> scores, File filename) throws FileNotFoundException {
		PrintWriter fileOut = new PrintWriter(filename);
		for (int i = 0; i < scores.size(); i++) {
			fileOut.print(scores.get(i).toString() + "\n");
		}
		fileOut.close();
	}
}

class HighScore implements Comparable<HighScore> {
	private String name;
	private int score;

	public HighScore(String name, int score) {
		this.name = name;
		this.score = score;
	}

	public String getScoreScreenFormat() {
		return String.format("%-10s%10s\n", name, score);
	}

	public int getScore() {
		return score;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name + "\n" + score;
	}

	@Override
	public int compareTo(HighScore o) {
		if (score > o.score) {
			return 1;
		} else if (score < o.score) {
			return -1;
		} else {
			return 0;
		}
	}
}

class SwitchingButton extends Button {
	boolean state; // true means button is clickable.
	boolean flagged; // true means tile is flagged
	ImageView coverTile, flagTile, redMine, greyMine, misFlagged;
	ImageView zeroTile, oneTile, twoTile, threeTile, fourTile, fiveTile, sixTile, sevenTile, eightTile;

	public SwitchingButton() {
		state = true;
		flagged = false;
		double size = 32;
		setMinWidth(size);
		setMaxWidth(size);
		setMinHeight(size);
		setMaxHeight(size);

		coverTile = new ImageView(new Image("file:res/minesweeper-basic2/cover.png"));
		flagTile = new ImageView(new Image("file:res/minesweeper-basic2/flag.png"));
		zeroTile = new ImageView(new Image("file:res/minesweeper-basic2/0.png"));
		oneTile = new ImageView(new Image("file:res/minesweeper-basic2/1.png"));
		twoTile = new ImageView(new Image("file:res/minesweeper-basic2/2.png"));
		threeTile = new ImageView(new Image("file:res/minesweeper-basic2/3.png"));
		fourTile = new ImageView(new Image("file:res/minesweeper-basic2/4.png"));
		fiveTile = new ImageView(new Image("file:res/minesweeper-basic2/5.png"));
		sixTile = new ImageView(new Image("file:res/minesweeper-basic2/6.png"));
		sevenTile = new ImageView(new Image("file:res/minesweeper-basic2/7.png"));
		eightTile = new ImageView(new Image("file:res/minesweeper-basic2/8.png"));
		redMine = new ImageView(new Image("file:res/minesweeper-basic2/mine-red.png"));
		greyMine = new ImageView(new Image("file:res/minesweeper-basic2/mine-grey.png"));
		misFlagged = new ImageView(new Image("file:res/minesweeper-basic2/mine-misflagged.png"));

		setGraphic(coverTile);
	}

	public void setTile(int tileNumber) {
		if (tileNumber == 1) {
			setGraphic(oneTile);
		} else if (tileNumber == 2) {
			setGraphic(twoTile);
		} else if (tileNumber == 3) {
			setGraphic(threeTile);
		} else if (tileNumber == 4) {
			setGraphic(fourTile);
		} else if (tileNumber == 5) {
			setGraphic(fiveTile);
		} else if (tileNumber == 6) {
			setGraphic(sixTile);
		} else if (tileNumber == 7) {
			setGraphic(sevenTile);
		} else if (tileNumber == 8) {
			setGraphic(eightTile);
		} else if (tileNumber == 9) {
			setGraphic(redMine);
		} else if (tileNumber == 0) {
			setGraphic(zeroTile);
		}
	}
}

class HappyFaceButton extends Button {
	ImageView happyFace, sadFace, coolFace, ohFace;

	public HappyFaceButton() {

		double size = 52;
		setMinWidth(size);
		setMinHeight(size);
		setMaxWidth(size);
		setMaxHeight(size);

		happyFace = new ImageView("file:res/minesweeper-basic2/face-smile.png");
		sadFace = new ImageView("file:res/minesweeper-basic2/face-dead.png");
		coolFace = new ImageView("file:res/minesweeper-basic2/face-win.png");
		ohFace = new ImageView("file:res/mineSweeper-basic2/face-oh.png");

		happyFace.setFitWidth(size);
		happyFace.setFitHeight(size);
		sadFace.setFitWidth(size);
		sadFace.setFitHeight(size);
		coolFace.setFitWidth(size);
		coolFace.setFitHeight(size);

		setGraphic(happyFace);
	}
}