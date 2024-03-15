import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;

public class MainWindow extends JFrame {

    private JLabel imageLabel;
    private JLabel statusLabel;
    private JButton playButton;
    private JButton recentButton;
    private FileInputStream fis;
    private long pausedPosition = 0;


    private JButton stopButton;
    private JButton pauseButton;
    private JButton prevButton;
    private JButton nextButton;
    private JButton openFileButton;
    private JPanel imageContainer;
    private String selectedFilePath;
    private Player player;
    private Thread playerThread;

    public MainWindow() {
        super("Music Player");

        setSize(1100, 600);
        setLocationRelativeTo(null);

        playButton = new JButton("Play");
        stopButton = new JButton("Stop");
        pauseButton = new JButton("Pause");
        prevButton = new JButton("Prev");
        nextButton = new JButton("Next");
        openFileButton = new JButton("Open File");
        statusLabel = new JLabel("No file selected");
        recentButton = new JButton("Recently Played ");

        imageContainer = new JPanel();
        imageContainer.setBackground(Color.BLACK);
        imageContainer.setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(playButton);
        controlPanel.add(stopButton);
        controlPanel.add(pauseButton);
        controlPanel.add(prevButton);
        controlPanel.add(nextButton);
        controlPanel.add(recentButton);
        controlPanel.add(openFileButton);
        controlPanel.add(statusLabel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(imageContainer, BorderLayout.CENTER);
        getContentPane().add(controlPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        openFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });

        playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                play();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stop();
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pause();
            }
        });

        prevButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                prev();
            }
        });

        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                next();
            }
        });
    }


    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedFilePath = selectedFile.getAbsolutePath(); // Store the selected file path
            statusLabel.setText("Playing: " + selectedFile.getName());
            updateAlbumImage(selectedFile);
            playSelectedFile(selectedFile);
        }
    }

    private void playSelectedFile(File file) {
        try {
            fis = new FileInputStream(file); // Initialize the instance variable fis
            BufferedInputStream bis = new BufferedInputStream(fis);

            // Create a new Player instance for the current file
            player = new Player(bis);

            // Start a new thread to play the audio in the background
            playerThread = new Thread(() -> {
                try {
                    // Skip the initial part to resume playback from the paused position
                    fis.skip(pausedPosition);
                    player.play();
                } catch (JavaLayerException e) {
                    System.out.println("Error playing audio: " + e);
                    statusLabel.setText("Error playing audio: " + e.getMessage());
                } catch (IOException e) {
                    System.out.println("Error reading file: " + e);
                    statusLabel.setText("Error reading file: " + e.getMessage());
                } finally {
                    if (player != null) {
                        try {
                            pausedPosition = fis.available(); // Update pausedPosition with the current position
                            player.close(); // Close the player after playback
                        } catch (IOException e) {
                            System.out.println("Error closing player: " + e);
                        }
                    }
                }
            });
            playerThread.start();
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + file.getAbsolutePath());
            statusLabel.setText("File not found: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error initializing player: " + e);
            statusLabel.setText("Error initializing player: " + e.getMessage());
        } catch (JavaLayerException e) {
            throw new RuntimeException(e);
        }
    }


    private void stop() {
        if (player != null && playerThread != null) {
            player.close();
            playerThread.interrupt();
            statusLabel.setText("Stopped");
            pausedPosition = 0; // Reset the paused position
        }
    }

    private void pause() {
        if (player != null && playerThread != null && playerThread.isAlive()) {
            try {
                pausedPosition = fis.available(); // Store the position where playback was paused
                player.close();
                playerThread.interrupt();
                statusLabel.setText("Paused");
            } catch (IOException e) {
                System.out.println("Error getting player position: " + e);
            }
        }
    }

    private void play() {
        if (selectedFilePath == null || selectedFilePath.isEmpty()) {
            // No file selected
            System.out.println("No file selected");
            return;
        }

        File file = new File(selectedFilePath);
        if (!file.exists()) {
            System.out.println("File not found: " + selectedFilePath);
            statusLabel.setText("File not found: " + selectedFilePath);
            return;
        }

        if (player != null && playerThread != null && playerThread.isAlive()) {
            // Player is already playing, do nothing
            return;
        }

        // Start playing the selected file
        playSelectedFile(file);
    }

    private void updateAlbumImage(File audioFile) {
        try {
            AudioFile file = AudioFileIO.read(audioFile);
            Tag tag = file.getTag();
            byte[] imageData = tag.getFirstArtwork().getBinaryData();
            ImageIcon imageIcon = new ImageIcon(imageData);
            JLabel backgroundImageLabel = new JLabel(imageIcon);
            imageContainer.removeAll(); // Remove any existing components
            imageContainer.add(backgroundImageLabel, BorderLayout.CENTER);
            imageContainer.revalidate(); // Refresh the layout
        } catch (Exception e) {
            System.out.println("Error loading album image: " + e);
        }
    }

    private void prev() {
        // Add logic to play the previous media file
        statusLabel.setText("Previous");
    }

    private void next() {
        // Add logic to play the next media file
        statusLabel.setText("Next");
    }
}
