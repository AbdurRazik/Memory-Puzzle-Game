import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.*;

public class MemoryPuzzleGame extends JFrame {
    private int size = 4; // Default 4x4 grid for Easy mode
    private JButton[][] buttons;
    private String[][] board;
    private boolean[][] revealed;
    private int pairsFound = 0;
    private JButton firstButton, secondButton;
    private String firstCard, secondCard;
    private long startTime;
    private int score;
    private int hideDelay = 1000; // Default delay for Medium difficulty
    private boolean isTimeAttack = false; // Flag for Time Attack mode
    private Timer countdownTimer;
    private int timeLeft = 60; // Default time limit for Time Attack in seconds

    public MemoryPuzzleGame() {
        setTitle("Memory Puzzle Game");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        initializeMenu();
        setVisible(true);
    }

    private void initializeMenu() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(5, 1));

        JLabel welcomeLabel = new JLabel("Welcome to Memory Puzzle Game!", SwingConstants.CENTER);
        menuPanel.add(welcomeLabel);

        JButton startButton = new JButton("Start Game");
        startButton.addActionListener(e -> startGame());
        menuPanel.add(startButton);

        JButton difficultyButton = new JButton("Select Difficulty");
        difficultyButton.addActionListener(e -> selectDifficulty());
        menuPanel.add(difficultyButton);

        JButton timeAttackButton = new JButton("Time Attack Mode");
        timeAttackButton.addActionListener(e -> startTimeAttackMode());
        menuPanel.add(timeAttackButton);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));
        menuPanel.add(exitButton);

        add(menuPanel, BorderLayout.CENTER);
    }

    private void selectDifficulty() {
        String[] options = {"Easy", "Medium", "Hard"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Select Difficulty Level",
                "Difficulty",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[1]);

        switch (choice) {
            case 0:
                size = 4;
                hideDelay = 1500; // Easy mode
                break;
            case 1:
                size = 6;
                hideDelay = 1000; // Medium mode
                break;
            case 2:
                size = 8;
                hideDelay = 500;  // Hard mode
                break;
            default:
                size = 4;
                hideDelay = 1000; // Default to Medium
                break;
        }
        isTimeAttack = false; // Ensure time attack is off
    }

    private void startTimeAttackMode() {
        isTimeAttack = true;
        timeLeft = 60; // Set time limit to 60 seconds for Time Attack
        startGame();
    }

    private void startGame() {
        getContentPane().removeAll();
        setLayout(new GridLayout(size, size));
        buttons = new JButton[size][size];
        board = new String[size][size];
        revealed = new boolean[size][size];
        initializeBoard();
        createButtons();
        startTime = System.currentTimeMillis();

        if (isTimeAttack) {
            startCountdownTimer();
        }
        
        setVisible(true);
    }

    private void startCountdownTimer() {
        countdownTimer = new Timer(1000, e -> {
            timeLeft--;
            setTitle("Memory Puzzle Game - Time Left: " + timeLeft + "s");
            if (timeLeft <= 0) {
                countdownTimer.stop();
                JOptionPane.showMessageDialog(this, "Time's up! Game Over.");
                resetGame();
            }
        });
        countdownTimer.start();
    }

    private void initializeBoard() {
        ArrayList<String> cards = new ArrayList<>();
        int numPairs = (size * size) / 2;
    
        // Load a fixed set of images and repeat them as needed to fill the board
        int imageCount = 8; // Number of unique images available, adjust this as needed
        for (int i = 1; i <= imageCount; i++) {
            String imagePath = "C:\\Users\\moham\\OneDrive\\Documents\\Razik_java\\MemoryPuzzleGame\\images\\image" + i + ".jpg"; // Update with your image path
            cards.add(imagePath);
            cards.add(imagePath); 
        }
        while (cards.size() < size * size) {
            for (int i = 1; i <= imageCount && cards.size() < size * size; i++) {
                String imagePath = "C:\\Users\\moham\\OneDrive\\Documents\\Razik_java\\MemoryPuzzleGame\\images\\image" + i + ".jpg";
                cards.add(imagePath);
                cards.add(imagePath); 
            }
        }
        Collections.shuffle(cards);
    
        // Fill the board array with the shuffled cards
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = cards.remove(0);
            }
        }
    }
    
    private void createButtons() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].setIcon(resizeImageIcon(new ImageIcon("C:\\Users\\moham\\OneDrive\\Documents\\Razik_java\\MemoryPuzzleGame\\images\\back.jpg"))); // Back image
                int finalI = i, finalJ = j;
                buttons[i][j].addActionListener(e -> handleButtonClick(finalI, finalJ));
                add(buttons[i][j]);
            }
        }
    }

    private ImageIcon resizeImageIcon(ImageIcon icon) {
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(600 / size, 600 / size, Image.SCALE_SMOOTH); // Adjust to fit the grid size
        return new ImageIcon(resizedImage);
    }

    private void handleButtonClick(int row, int col) {
        if (revealed[row][col] || pairsFound == (size * size) / 2 || firstButton != null && secondButton != null) return;

        buttons[row][col].setIcon(resizeImageIcon(new ImageIcon(board[row][col]))); // Show card
        revealed[row][col] = true;

        if (firstButton == null) {
            firstButton = buttons[row][col];
            firstCard = board[row][col];
        } else {
            secondButton = buttons[row][col];
            secondCard = board[row][col];
            disableButtons(); // Temporarily disable buttons

            if (firstCard.equals(secondCard)) {
                pairsFound++;
                firstButton = null;
                secondButton = null;
                enableButtons(); // Re-enable if a match is found
                if (pairsFound == (size * size) / 2) {
                    if (isTimeAttack && countdownTimer != null) {
                        countdownTimer.stop();
                    }
                    long timeTaken = System.currentTimeMillis() - startTime;
                    score = calculateScore(timeTaken);
                    JOptionPane.showMessageDialog(this, "Congratulations! You've matched all the pairs! Your score: " + score);
                    resetGame();
                }
            } else {
                Timer timer = new Timer(hideDelay, e -> {
                    firstButton.setIcon(resizeImageIcon(new ImageIcon("images/back.jpg"))); // Hide card
                    secondButton.setIcon(resizeImageIcon(new ImageIcon("images/back.jpg"))); // Hide card
                    revealed[getButtonRow(firstButton)][getButtonCol(firstButton)] = false;
                    revealed[row][col] = false;
                    firstButton = null;
                    secondButton = null;
                    enableButtons(); // Re-enable after hiding
                });
                timer.setRepeats(false);
                timer.start();
            }
        }
    }

    private void enableButtons() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                buttons[i][j].setEnabled(true);
            }
        }
    }

    private void disableButtons() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                buttons[i][j].setEnabled(false);
            }
        }
    }

    private int calculateScore(long timeTaken) {
        return Math.max(0, 1000 - (int)(timeTaken / 1000));
    }

    private int getButtonRow(JButton button) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (buttons[i][j] == button) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int getButtonCol(JButton button) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (buttons[i][j] == button) {
                    return j;
                }
            }
        }
        return -1;
    }

    private void resetGame() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        getContentPane().removeAll();
        initializeMenu();
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MemoryPuzzleGame::new);
    }
}
