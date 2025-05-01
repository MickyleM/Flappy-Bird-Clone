import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import javax.sound.sampled.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener{
    int boardWidth = 360;
    int boardHeight = 640;

    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image botPipeImg;

    //bird resources
    int birdX = boardWidth/8;
    int birdY = boardHeight/2;  
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird{
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img){
            this.img = img;
        }
    }

    class SoundPlayer{
        Clip clip;

        SoundPlayer(String filePath){
            try{
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));
                clip = AudioSystem.getClip();
                clip.open(audioInputStream);
            }catch(UnsupportedAudioFileException | IOException | LineUnavailableException e){
                e.printStackTrace();
            }
        }

        public void play(){
            if(clip != null){
                clip.setFramePosition(0); // Reset to start
                clip.start();
            }
        }


        public void stop(){
            if(clip != null){
                clip.stop();
            }
        }
    }

    //pipe resources
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    class Pipe{
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img){
            this.img = img;
        }
    }

    //game logic
    Bird bird;
    int velocityX = -4; //moving the pipe images to the left
    int velocityY = 0; //moving the bird up/down
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    SoundPlayer flap;
    SoundPlayer point;
    SoundPlayer hit;
    SoundPlayer die;

    Timer gameLoop;
    Timer placePipesTimer;

    boolean gameOver = false;
    double score = 0;
    double highScore = 0; // New variable for high score

    FlappyBird(){
        loadHighScore();
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        botPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        flap = new SoundPlayer("./Everything/sfx_wing.wav");
        point = new SoundPlayer("./Everything/sfx_point.wav");
        hit = new SoundPlayer("./Everything/sfx_hit.wav");
        die = new SoundPlayer("./Everything/sfx_die.wav");

        placePipesTimer = new Timer(1500, new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipesTimer.start();

        gameLoop = new Timer(1000/60, this);
        gameLoop.start();

    }

    public void placePipes(){
        int randomPipeY = (int)(pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        int pipeSpace = boardHeight/4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe botPipe = new Pipe(botPipeImg);
        botPipe.y = topPipe.y + pipeHeight + pipeSpace;
        pipes.add(botPipe);
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g){
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        for(int i = 0; i < pipes.size(); i++){
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        //score tracking
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if(gameOver){
            g.drawString("Game Over: " + String.valueOf((int)score), 10, 35);
            if (score > highScore){
                highScore = score;
                saveHighScore(); //Save highscore to the txt file for persistency
            }
        }
        else{
            g.drawString(String.valueOf((int)score), 10, 35);
        }
        g.drawString("High Score: " + (int) highScore, 10, 70); // Display high score
    }

    public void move(){
        //moving the bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        //moving the pipes
        for(int i = 0; i < pipes.size(); i++){
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if(!pipe.passed && bird.x > pipe.x + pipe.width){
                point.play();
                pipe.passed = true;
                score += 0.5;
            }

            if (collision(bird, pipe)){
                hit.play();
                gameOver = true;
            }
        }

        if(bird.y > boardHeight){
            die.play();
            gameOver = true;
        }
    }

    public boolean collision(Bird b, Pipe p){
        return b.x < p.x + p.width &&
               b.x + b.width > p.x &&
               b.y < p.y + p.height &&
               b.y + b.height > p.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if(gameOver){
            placePipesTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE){
            velocityY = -9;
            flap.play();
            //reseting the game
            if(gameOver){
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                placePipesTimer.start();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    // Load high score from file
    public void loadHighScore() {
        try (BufferedReader br = new BufferedReader(new FileReader("highscore.txt"))) {
            String line = br.readLine();
            if (line != null) {
                highScore = Double.parseDouble(line);
            }
        } catch (IOException e) {
            // File not found or error reading, set high score to 0
            highScore = 0;
        }
    }

    // Save high score to file
    public void saveHighScore() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("highscore.txt"))) {
            bw.write(String.valueOf(highScore));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
