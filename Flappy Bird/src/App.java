import javax.swing.*;

public class App {
    public static void main(String[] args) throws Exception {
        int boardWidth = 360;
        int boardHeight = 640;

        JFrame gameFrame = new JFrame("Flappy Bird"); 
        gameFrame.setSize(boardWidth, boardHeight);
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setResizable(false);
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        FlappyBird flappyBird = new FlappyBird();
        gameFrame.add(flappyBird);
        gameFrame.pack();
        flappyBird.requestFocus();
        gameFrame.setVisible(true);
    }
}
