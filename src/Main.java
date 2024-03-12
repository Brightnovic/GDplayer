import java.awt.*;
import java.awt.event.*;
import javax.swing.*;



import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class Main {
    public static void main(String[] args) {

        SplashDemo test = new SplashDemo();

    }
}

class SplashDemo extends Window {
    public SplashDemo() {
        super(null); // Use null layout

        setSize(1200, 700);
        setLocationRelativeTo(null);

        Panel panel = new Panel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(0, 128, 128));

        Label label = new Label("Relax, " +
                "unwind and play Music"
        );


        Font font = new Font("Arial", Font.BOLD, 21);
        label.setFont(font);
        label.setAlignment(Label.CENTER);
        label.setForeground(Color.WHITE);
        panel.add(label, BorderLayout.CENTER);

        add(panel);

        setVisible(true);

        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        dispose();
        new MainWindow();
    }
}





