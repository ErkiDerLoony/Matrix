package erki.matrix;

import java.awt.Color;
import java.awt.Container;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.swing.JFrame;

public class Matrix {
    
    private static final int WIDTH = 300;
    private static final int HEIGHT = 300;
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Jogl-Test für die MATRIX");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Container cp = frame.getContentPane();
        
        GLCanvas glPanel = new GLCanvas();
        glPanel.setBackground(Color.WHITE);
        
        glPanel.addGLEventListener(new GLEventListener() {
            
            @Override
            public void display(GLAutoDrawable drawable) {
                GL gl = drawable.getGL();
                
                // Clear the screen with white
                gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
                gl.glClear(GL.GL_COLOR_BUFFER_BIT);
                
                gl.glColor3f(1.0f, 0.0f, 0.0f);
                gl.glRectd(10.0, 10.0, 290.0, 290.0);
            }
            
            @Override
            public void displayChanged(GLAutoDrawable drawable,
                    boolean modeChanged, boolean deviceChanged) {
            }
            
            @Override
            public void init(GLAutoDrawable drawable) {
            }
            
            @Override
            public void reshape(GLAutoDrawable drawable, int x, int y,
                    int width, int height) {
            }
        });
        
        cp.add(glPanel);
        frame.setSize(WIDTH, HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
