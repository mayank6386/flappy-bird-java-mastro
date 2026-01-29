import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Random;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    double birdAngle = 0;
    int cloudX1 = 50;
    int cloudX2 = 250;
    

    int boardWidth = 360;
    int boardHeight = 640;

    Image birdImg;

    int birdX = boardWidth/8;
    int birdY = boardWidth/2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;
        Bird(Image img) { this.img = img; }
    }

    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        boolean passed = false;

        Pipe() {}
    }

    Bird bird;
    int velocityX = -4;
    int velocityY = 0;
    int gravity = 1;

    ArrayList<Pipe> pipes = new ArrayList<>();
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;

    boolean gameOver = false;
    double score = 0;

    FlappyBird() {

        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();

        // JUMP SOUND
createWavFile("jump.wav", new int[] {
    82,73,70,70,68,0,0,0,87,65,86,69,102,109,116,32,16,0,0,0,
    1,0,1,0,68,172,0,0,136,88,1,0,2,0,16,0,100,97,116,97,
    32,0,0,0,0,0,20,60,40,100,60,120,40,100,20,60,0,0,
    -20,-60,-40,-100,-60,-120,-40,-100,-20,-60
});

// SCORE SOUND
createWavFile("score.wav", new int[] {
    82,73,70,70,68,0,0,0,87,65,86,69,102,109,116,32,16,0,0,0,
    1,0,1,0,68,172,0,0,136,88,1,0,2,0,16,0,100,97,116,97,
    32,0,0,0,0,20,40,80,120,80,40,20,0,-20,-40,-80,-120,-80,-40,-20
});


// HIT SOUND
createWavFile("hit.wav", new int[] {
    82,73,70,70,68,0,0,0,87,65,86,69,102,109,116,32,16,0,0,0,
    1,0,1,0,34,136,0,0,68,34,1,0,2,0,16,0,100,97,116,97,
    32,0,0,0,0,0,-10,-30,-60,-80,-60,-30,-10,0,10,30,60,80,60,30,10,0
});


// BACKGROUND MUSIC (simple tone)
// createWavFile("bgmusic.wav", new int[] {
//     82,73,70,70,220,1,0,0,87,65,86,69,102,109,116,32,16,0,0,0,
//     1,0,1,0,68,172,0,0,136,88,1,0,2,0,16,0,100,97,116,97,
//     196,1,0,0,

//     // ---- Mario-style upbeat chip melody ----
//     // pattern A (happy upward scale)
//     0,20,40,60,80,100,110,120,110,100,80,60,40,20,0,-20,-40,-60,-80,-100,-110,-120,-110,-100,-80,-60,-40,-20,

//     // pattern B (jump-like notes)
//     0,30,60,90,120,90,60,30,0,-30,-60,-90,-120,-90,-60,-30,
//     0,40,80,120,80,40,0,-40,-80,-120,-80,-40,

//     // pattern C (Mario flowing wave)
//     0,15,30,45,60,75,90,100,110,100,90,75,60,45,30,15,0,
//     -15,-30,-45,-60,-75,-90,-100,-110,-100,-90,-75,-60,-45,-30,-15,

//     // repeat patterns for longer music
//     0,20,40,60,80,100,110,120,110,100,80,60,40,20,0,-20,-40,-60,-80,-100,-110,-120,-110,-100,-80,-60,-40,-20,
//     0,30,60,90,120,90,60,30,0,-30,-60,-90,-120,-90,-60,-30,
//     0,40,80,120,80,40,0,-40,-80,-120,-80,-40,
// });


        playSound("bgmusic.wav", true);
        // playSound("bgmusic.wav", true);

        bird = new Bird(birdImg);

        // Correct pipe placement timer
        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipeTimer.start();

        // Main game loop
        gameLoop = new Timer(1000/60, this);
        gameLoop.start();
       
    }


 public void createWavFile(String fileName, int[] data) {
    try {
        byte[] bytes = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            bytes[i] = (byte)(data[i] & 0xFF);  // convert int → byte safely
        }

        java.io.FileOutputStream fos = new java.io.FileOutputStream(fileName);
        fos.write(bytes);
        fos.close();

        System.out.println("Created: " + fileName);

    } catch (Exception e) {
        e.printStackTrace();
    }
}

public void playSound(String fileName, boolean loop) {
    try {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("Sound file not found: " + fileName);
            return;
        }

        AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
        Clip clip = AudioSystem.getClip();
        clip.open(audioStream);

        if (loop) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            clip.start();
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}




    void placePipes() {

        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        int openingSpace = boardHeight/4;

        Pipe topPipe = new Pipe();
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe();
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        // Background Gradient
        GradientPaint skyGradient = new GradientPaint(
            0, 0, new Color(135, 206, 250),
            0, boardHeight, new Color(255, 182, 193)
        );
        g2d.setPaint(skyGradient);
        g2d.fillRect(0, 0, boardWidth, boardHeight);

        // Clouds
        g.setColor(new Color(255, 255, 255, 120));
        g.fillOval(cloudX1, 80, 160, 60);
        g.fillOval(cloudX2, 140, 200, 70);

        // Pipes
        for (Pipe pipe : pipes) {

            GradientPaint pipeGradient = new GradientPaint(
                pipe.x, pipe.y, new Color(144, 238, 144),
                pipe.x, pipe.y + pipe.height, new Color(60, 179, 113)
            );

            g2d.setPaint(pipeGradient);
            g2d.fillRoundRect(pipe.x, pipe.y, pipe.width, pipe.height, 30, 30);

            g.setColor(new Color(0, 0, 0, 40));
            g.fillRoundRect(pipe.x + 4, pipe.y + 4, pipe.width, pipe.height, 30, 30);
        }

        // Bird shadow
        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval(bird.x - 10, bird.y + bird.height - 5, bird.width + 20, 15);

        // Rotating bird
        Graphics2D g2 = (Graphics2D) g;
        g2.rotate(Math.toRadians(birdAngle), bird.x + bird.width / 2, bird.y + bird.height / 2);
        g2.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);
        g2.rotate(Math.toRadians(-birdAngle), bird.x + bird.width / 2, bird.y + bird.height / 2);

        // Score UI
        g.setColor(new Color(255, 255, 255, 220));
        g.fillRoundRect(10, 10, 60, 50, 20, 20);

        g.setColor(Color.PINK);
        g.setFont(new Font("Comic Sans MS", Font.BOLD, 28));
        g.drawString(String.valueOf((int) score), 30, 45);

        // Game Over UI
        if (gameOver) {
            g.setColor(new Color(255, 255, 255, 230));
            g.fillRoundRect(40, 200, 280, 220, 40, 40);

            g.setColor(Color.DARK_GRAY);
            g.setFont(new Font("Comic Sans MS", Font.BOLD, 32));
            g.drawString("Game Over", 85, 250);

            g.setFont(new Font("Comic Sans MS", Font.PLAIN, 26));
            g.drawString("Score: " + (int) score, 120, 300);

            g.setFont(new Font("Comic Sans MS", Font.PLAIN, 20));
            g.drawString("Press SPACE to restart", 65, 350);
        }
    }

    public void move() {

        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        if (velocityY < 0) birdAngle = -20;
        else birdAngle = Math.min(birdAngle + 3, 90);

        for (Pipe pipe : pipes) {
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                pipe.passed = true;
                playSound("score.wav", false);
            }

            if (collision(bird, pipe)) {
                playSound("hit.wav", false);
                gameOver = true;
            }
        }

        if (bird.y > boardHeight) gameOver = true;

        cloudX1 -= 1;
        cloudX2 -= 1;

        if (cloudX1 < -200) cloudX1 = boardWidth;
        if (cloudX2 < -200) cloudX2 = boardWidth;
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            playSound("jump.wav", false);
            velocityY = -9;

            if (gameOver) {
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                gameOver = false;
                score = 0;
                gameLoop.start();
                placePipeTimer.start();
            }
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}
