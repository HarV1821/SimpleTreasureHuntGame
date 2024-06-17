import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.sound.sampled.*;
import java.io.IOException;
import java.util.Random;

public class TreasureHuntGame extends JFrame {
    private static final int GRID_SIZE = 5; // Example: 5x5 grid
    private JButton[][] buttons;
    private JLabel levelLabel;
    private JLabel scoreLabel;
    private JLabel highScoreLabel;
    private JLabel statusLabel;
    private JPanel gridPanel;
    private JPanel topPanel;
    private JPanel bottomPanel;
    private int currentLevel;
    private int score;
    private static int highScore; // Make highScore static
    private boolean[][] treasureMap;
    private boolean gameOver;
    private int treasureX;
    private int treasureY;
    private Clip backgroundClip;

    public TreasureHuntGame() {
        super("Treasure Hunt Game");
        currentLevel = 1;
        score = 0;
        // Initialize highScore if it's the first game session
        if (highScore == 0) {
            highScore = 0;
        }
        initializeUI();
        startLevel(currentLevel);
        playBackgroundMusic("background.wav");

        setSize(800, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        setVisible(true);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setIconImage(new ImageIcon("icon.png").getImage());

        // Top Panel
        topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(new Color(255, 223, 186));
        levelLabel = new JLabel("Level: " + currentLevel);
        levelLabel.setFont(new Font("Arial", Font.BOLD, 18));
        scoreLabel = new JLabel("Score: " + score);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 18));
        highScoreLabel = new JLabel("High Score: " + highScore);
        highScoreLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(levelLabel);
        topPanel.add(scoreLabel);
        topPanel.add(highScoreLabel);
        add(topPanel, BorderLayout.NORTH);

        // Center Panel (Grid)
        gridPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE, 5, 5));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        gridPanel.setBackground(new Color(240, 240, 240));
        add(gridPanel, BorderLayout.CENTER);

        // Create grid buttons
        buttons = new JButton[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].setPreferredSize(new Dimension(80, 80)); // Adjust button size
                buttons[i][j].setBackground(Color.LIGHT_GRAY); // Default color
                buttons[i][j].setFont(new Font("Arial", Font.BOLD, 12));
                buttons[i][j].setToolTipText("Click to search here");
                buttons[i][j].addActionListener(new ButtonClickListener(i, j));
                gridPanel.add(buttons[i][j]);
            }
        }

        // Bottom Panel
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(new Color(255, 223, 186));
        statusLabel = new JLabel("Find the treasure!");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 18));
        bottomPanel.add(statusLabel);
        
        JButton restartButton = new JButton("Restart");
        restartButton.setFont(new Font("Arial", Font.BOLD, 16));
        restartButton.setPreferredSize(new Dimension(120, 40));
        restartButton.setBackground(new Color(173, 216, 230));
        restartButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        restartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                restartGame();
                playSound("restart.wav");
            }
        });
        bottomPanel.add(restartButton);
        
        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Arial", Font.BOLD, 16));
        exitButton.setPreferredSize(new Dimension(120, 40));
        exitButton.setBackground(new Color(255, 160, 122));
        exitButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        bottomPanel.add(exitButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void startLevel(int level) {
        // Initialize game state for the current level
        Random random = new Random();
        treasureMap = new boolean[GRID_SIZE][GRID_SIZE];

        // Place treasure randomly ensuring it's not in the same place as last time
        do {
            treasureX = random.nextInt(GRID_SIZE);
            treasureY = random.nextInt(GRID_SIZE);
        } while (treasureMap[treasureX][treasureY]);

        treasureMap[treasureX][treasureY] = true;
    }

    private void restartGame() {
        // Reset game variables and UI for a new game
        currentLevel = 1;
        score = 0;
        gameOver = false;
        updateUI();
        startLevel(currentLevel);
    }

    private void updateUI() {
        levelLabel.setText("Level: " + currentLevel);
        scoreLabel.setText("Score: " + score);
        highScoreLabel.setText("High Score: " + highScore);
        statusLabel.setText("Find the treasure!");
        // Reset button colors and text
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                buttons[i][j].setBackground(Color.LIGHT_GRAY);
                buttons[i][j].setText("");
                buttons[i][j].setEnabled(true);
            }
        }
    }

    private class ButtonClickListener implements ActionListener {
        private int x;
        private int y;

        public ButtonClickListener(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void actionPerformed(ActionEvent e) {
            if (!gameOver) {
                if (treasureMap[x][y]) {
                    buttons[x][y].setBackground(Color.GREEN);
                    buttons[x][y].setText("TREASURE"); // Display text inside button
                    buttons[x][y].setEnabled(false);
                    statusLabel.setText("Congratulations! You found the treasure!");
                    score += 100;
                    playSound("treasure.wav");
                    currentLevel++;
                    updateHighScore();
                    if (currentLevel <= 12) {
                        updateUI();
                        startLevel(currentLevel);
                    } else {
                        statusLabel.setText("You have completed all levels!");
                        gameOver = true;
                    }
                } else {
                    int distance = Math.abs(x - treasureX) + Math.abs(y - treasureY);
                    buttons[x][y].setBackground(Color.RED);
                    buttons[x][y].setText("Distance: " + distance);
                    buttons[x][y].setEnabled(false);
                    statusLabel.setText("Oops! Try again.");
                    score -= 10; // Penalty for hitting a wrong cell
                    playSound("wrong.wav");
                    score = Math.max(score, 0); // Score cannot be negative
                    scoreLabel.setText("Score: " + score);
                }
            }
        }
    }

    private void updateHighScore() {
        if (score > highScore) {
            highScore = score;
            highScoreLabel.setText("High Score: " + highScore);
            JOptionPane.showMessageDialog(this, "New High Score: " + highScore);
        }
    }

    private void playSound(String soundFile) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(new File(soundFile)));
            clip.start();
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }

    private void playBackgroundMusic(String soundFile) {
        try {
            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(AudioSystem.getAudioInputStream(new File(soundFile)));
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }
}
