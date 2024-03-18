import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import javax.swing.Timer;

public class MainWindow extends JFrame {
    private String[] musicFilePaths = {
            "path/to/file1.mp3",
            "path/to/file2.mp3",
            "path/to/file3.mp3"
            // Add more file paths as needed
    };
    private JLabel imageLabel;
    private JLabel statusLabel;
    private Timer timer;
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

    private int currentFileIndex = 0;

    public MainWindow() {

        super("Music Player");

        setSize(1100, 600);
        setLocationRelativeTo(null);

        timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (player != null && playerThread != null) {
                    try {
                        pausedPosition = fis.getChannel().position();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

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
            if (fis != null) {
                fis.close(); // Close the previous FileInputStream if open
            }
            fis = new FileInputStream(file); // Initialize the instance variable fis
            BufferedInputStream bis = new BufferedInputStream(fis);

            // Create a new Player instance for the current file
            player = new Player(bis);

            // Start the Timer to update playback position
            timer.start();

            playerThread = new Thread(() -> {
                try {
                    // Skip the initial part to resume playback from the paused position
                    fis.skip(pausedPosition);
                    player.play();
                } catch (JavaLayerException e) {
                    // Error handling...
                } catch (IOException e) {
                    // Error handling...
                } finally {
                    if (player != null) {
                        try {
                            pausedPosition = fis.available(); // Update pausedPosition with the current position
                            player.close(); // Close the player after playback
                        } catch (IOException e) {
                            // Error handling...
                        }
                    }
                    // Stop the Timer after playback ends
                    timer.stop();
                }
            });
            playerThread.start();
        } catch (FileNotFoundException e) {
            // File not found handling...
        } catch (IOException e) {
            // Error initializing player handling...
        } catch (JavaLayerException e) {
            // Exception handling...
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
            player.close();
            playerThread.interrupt();
            timer.stop(); // Stop the Timer immediately on pause
            statusLabel.setText("Paused");
        }
    }

    private void play() {
        if (selectedFilePath == null || selectedFilePath.isEmpty()) {
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
            return; // Player is already playing, do nothing
        }

        // Start playing from the saved position
        playSelectedFile(file);
        // Resume playback from the exact saved position
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
        currentFileIndex = (currentFileIndex - 1 + musicFilePaths.length) % musicFilePaths.length;
        selectedFilePath = musicFilePaths[currentFileIndex];
        playSelectedFile(new File(selectedFilePath));
    }

    private void next() {
        currentFileIndex = (currentFileIndex + 1) % musicFilePaths.length;
        selectedFilePath = musicFilePaths[currentFileIndex];
        playSelectedFile(new File(selectedFilePath));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow());
    }

}
