/*
 * © Copyright 2009, 2011–2012 by Edgar Kalkowski <eMail@edgar-kalkowski.de>
 * 
 * This file is part of the Matrix.
 * 
 * The Matrix is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package erki.matrix.java2d;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JPanel;

import erki.api.util.CommandLineParser;

public class Matrix extends JFrame implements Runnable {
    
    private static final long serialVersionUID = -4774684618798887330L;
    
    private static Color green = new Color(0, 200, 0);
    private static Color green1 = new Color(100, 255, 100);
    private static Color green2 = new Color(255, 255, 255);
    
    private LinkedList<List<String>> quotes;
    private LinkedList<List<String>> quotes2;
    
    private String[][] matrix;
    private String[][] quote;
    private String[][] quote2;
    private int[][] colours;
    
    private Object quoteLock = new Object();
    private boolean quoted = false;
    
    private List<String> chars;
    
    private boolean killed = false;
    
    private int deletingStrings, creatingStrings, quotePause, quote2Pause, randomNoise;
    
    /**
     * Create a new matrix.
     * 
     * @param dStrings
     *        Count of deleting strings created per second.
     * @param cStrings
     *        Count of creating strings created per second.
     * @param qPause
     *        Maximal pause time in seconds between two quotes. The exact time is computed as
     *        {@code Math.random() * qPause}.
     * @param q2Pause
     *        Maximal pause time in seconds between two hidden quotes. The exact time is computes as
     *        {@code Math.random() * q2Pause}.
     * @param rNoise
     *        Random noise generated in 0.02 seconds (roughly a frame of your screen).
     * @param charFile
     *        File to use the chars from
     * @param quoteFile
     *        File that contains the main quotes that are displayed centered on the screen with a
     *        little border around them.
     * @param quote2File
     *        File that contains the hidden quotes that are displayed at random position without
     *        border.
     * @param fontSize
     *        The size of the font that is used for the displayed matrix.
     */
    public Matrix(int dStrings, int cStrings, int qPause, int q2Pause, int rNoise, String charFile,
            String quoteFile, String quote2File, final int fontSize) {
        deletingStrings = dStrings;
        creatingStrings = cStrings;
        quotePause = qPause;
        quote2Pause = q2Pause;
        randomNoise = rNoise;
        
        loadQuotes(quoteFile, quote2File);
        loadChars(charFile);
        
        addWindowListener(new WindowAdapter() {
            
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                killed = true;
                // TODO: HACK!
                System.exit(0);
            }
        });
        
        JPanel panel = new JPanel() {
            
            private static final long serialVersionUID = 8552540647574223876L;
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2;
                
                if (g instanceof Graphics2D) {
                    g2 = (Graphics2D) g;
                } else {
                    throw new IllegalStateException("Fatal drawing error!");
                }
                
                g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                if (matrix == null) {
                    int width = g2.getFontMetrics().charWidth('w');
                    int height = g2.getFontMetrics().getHeight();
                    matrix = new String[Toolkit.getDefaultToolkit().getScreenSize().width / width][Toolkit
                            .getDefaultToolkit().getScreenSize().height
                            / height];
                    colours = new int[matrix.length][matrix[0].length];
                    quote = new String[matrix.length][matrix[0].length];
                    quote2 = new String[matrix.length][matrix[0].length];
                    
                    for (int i = 0; i < matrix.length; i++) {
                        
                        for (int j = 0; j < matrix[i].length; j++) {
                            // matrix[i][j] = getChar();
                            matrix[i][j] = " ";
                            quote[i][j] = "";
                            quote2[i][j] = "";
                        }
                    }
                    
                    new Thread(Matrix.this).start();
                }
                
                for (int i = 0; i < matrix.length; i++) {
                    
                    for (int j = 0; j < matrix[i].length; j++) {
                        
                        if (colours[i][j] == 1) {
                            g2.setColor(green1);
                        } else if (colours[i][j] == 2) {
                            g2.setColor(green2);
                        } else {
                            g2.setColor(green);
                        }
                        
                        g2.drawString("" + matrix[i][j], (float) (i * g2.getFontMetrics()
                                .stringWidth("" + matrix[i][j])), (float) (g2.getFontMetrics()
                                .getAscent() + j * g2.getFontMetrics().getHeight()));
                    }
                }
            }
        };
        
        Container cp = getContentPane();
        cp.add(panel);
        panel.setBackground(Color.BLACK);
        
        addKeyListener(new KeyAdapter() {
            
            private int key;
            
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                key = e.getKeyCode();
            }
            
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                
                if (key == KeyEvent.VK_Q) {
                    killed = true;
                    dispose();
                }
                
                if (key == KeyEvent.VK_S) {
                    
                    synchronized (quoteLock) {
                        
                        if (!quoted) {
                            
                            new Thread() {
                                
                                @Override
                                public void run() {
                                    super.run();
                                    quote();
                                }
                                
                            }.start();
                        }
                    }
                }
            }
        });
    }
    
    @Override
    public void run() {
        
        new Thread() {
            
            @Override
            public void run() {
                super.run();
                
                while (!killed) {
                    
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                    }
                    
                    for (int k = 0; k < randomNoise; k++) {
                        int i = (int) (Math.random() * matrix.length);
                        int j = (int) (Math.random() * matrix[i].length);
                        
                        if (!quote[i][j].equals("")) {
                            matrix[i][j] = quote[i][j];
                        } else if (!quote2[i][j].equals("")) {
                            matrix[i][j] = quote2[i][j];
                        } else {
                            matrix[i][j] = getChar();
                        }
                    }
                    
                    repaint();
                }
            }
            
        }.start();
        
        new Thread() {
            
            @Override
            public void run() {
                super.run();
                
                while (!killed) {
                    
                    try {
                        Thread.sleep((int) (Math.random() * (1000 / creatingStrings))
                                + (1000 / creatingStrings));
                    } catch (InterruptedException e) {
                    }
                    
                    new Thread() {
                        
                        int i = (int) (Math.random() * matrix.length);
                        int len = (int) ((Math.random() * matrix[i].length / 2.0) + matrix[i].length / 2.0);
                        int pos = 0;
                        
                        @Override
                        public void run() {
                            super.run();
                            
                            while (pos <= matrix[i].length + len) {
                                
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                }
                                
                                if (pos < matrix[i].length) {
                                    
                                    if (!quote[i][pos].equals("")) {
                                        matrix[i][pos] = quote[i][pos];
                                    } else if (!quote2[i][pos].equals("")) {
                                        matrix[i][pos] = quote2[i][pos];
                                    } else {
                                        matrix[i][pos] = chars.get((int) (Math.random() * chars
                                                .size()));
                                    }
                                    
                                    colours[i][pos] = 2;
                                }
                                
                                if (pos - 1 >= 0 && pos - 1 < matrix[i].length) {
                                    colours[i][pos - 1] = 1;
                                }
                                
                                if (pos - len >= 0 && pos - len < matrix[i].length) {
                                    colours[i][pos - len] = 0;
                                }
                                
                                pos++;
                            }
                        }
                        
                    }.start();
                }
            }
            
        }.start();
        
        new Thread() {
            
            @Override
            public void run() {
                super.run();
                
                while (!killed) {
                    
                    try {
                        Thread.sleep(((int) (Math.random() * (1000 / deletingStrings)) + (1000 / deletingStrings)));
                    } catch (InterruptedException e) {
                    }
                    
                    new Thread() {
                        
                        int i = (int) (Math.random() * matrix.length);
                        int pos = 0;
                        
                        @Override
                        public void run() {
                            super.run();
                            
                            while (pos < matrix[i].length) {
                                
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                }
                                
                                if (!quote[i][pos].equals("")) {
                                    matrix[i][pos] = quote[i][pos];
                                } else if (!quote2[i][pos].equals("")) {
                                    matrix[i][pos] = quote2[i][pos];
                                } else {
                                    matrix[i][pos] = " ";
                                }
                                
                                pos++;
                            }
                        }
                        
                    }.start();
                }
            }
            
        }.start();
        
        new Thread() {
            
            @Override
            public void run() {
                super.run();
                
                while (!killed) {
                    
                    try {
                        Thread.sleep((long) (Math.random() * quotePause * 1000));
                    } catch (InterruptedException e) {
                    }
                    
                    synchronized (quoteLock) {
                        
                        if (!quoted) {
                            quote();
                        }
                    }
                }
            }
            
        }.start();
        
        new Thread() {
            
            @Override
            public void run() {
                super.run();
                
                while (!killed) {
                    
                    try {
                        Thread.sleep((long) (Math.random() * quote2Pause * 1000));
                    } catch (InterruptedException e) {
                    }
                    
                    List<String> quote = quotes2.get((int) (Math.random() * quotes2.size()));
                    
                    int i = (int) (Math.random() * matrix.length);
                    int j = (int) (Math.random() * matrix[i].length);
                    
                    for (int k = j; k < matrix[i].length && k - j < quote.size(); k++) {
                        String line = quote.get(k - j);
                        
                        for (int l = i; l < matrix.length && l - i < line.length(); l++) {
                            
                            if (!line.substring(l - i, l - i + 1).equals(" ")) {
                                quote2[l][k] = line.substring(l - i, l - i + 1);
                            }
                        }
                    }
                    
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                    }
                    
                    for (int k = j; k < matrix[i].length && k - j < quote.size(); k++) {
                        String line = quote.get(k - j);
                        
                        for (int l = i; l < matrix.length && l - i < line.length(); l++) {
                            quote2[l][k] = "";
                        }
                    }
                }
            }
            
        }.start();
    }
    
    /** Has to be called from a synchronized context (via {@link #quoteLock})! */
    private void quote() {
        quoted = true;
        List<String> quote = quotes.get((int) (Math.random() * quotes.size()));
        int len = 0;
        
        for (String s : quote) {
            
            if (s.length() > len) {
                len = s.length();
            }
        }
        
        for (int i = matrix.length / 2 - (len + 4) / 2; i < matrix.length / 2 + (len + 4) / 2; i++) {
            
            for (int j = matrix[i].length / 2 - (quote.size() + 2) / 2; j < matrix[i].length / 2
                    + (quote.size() + 3) / 2; j++) {
                Matrix.this.quote[i][j] = " ";
            }
        }
        
        for (int i = 0; i < quote.size(); i++) {
            String line = quote.get(i);
            int k = (matrix[0].length / 2) - (quote.size() / 2) + i;
            
            for (int j = 0; j < line.length(); j++) {
                int l = (matrix.length / 2) - (line.length() / 2) + j;
                Matrix.this.quote[l][k] = line.substring(j, j + 1);
            }
        }
        
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
        }
        
        for (int i = matrix.length / 2 - (len + 4) / 2; i < matrix.length / 2 + (len + 4) / 2; i++) {
            
            for (int j = matrix[i].length / 2 - (quote.size() + 2) / 2; j < matrix[i].length / 2
                    + (quote.size() + 3) / 2; j++) {
                Matrix.this.quote[i][j] = "";
            }
        }
        
        quoted = false;
    }
    
    private String getChar() {
        int rnd = (int) (Math.random() * 4);
        
        if (rnd == 0) {
            return " ";
        } else {
            return chars.get((int) (Math.random() * chars.size()));
        }
    }
    
    private void loadQuotes(String quotesFile, String quotes2File) {
        File file = new File(quotesFile).getAbsoluteFile();
        File file2 = new File(quotes2File).getAbsoluteFile();
        quotes = new LinkedList<List<String>>();
        quotes2 = new LinkedList<List<String>>();
        
        try {
            BufferedReader fileIn = new BufferedReader(new InputStreamReader(new FileInputStream(
                    file), "UTF-8"));
            BufferedReader fileIn2 = new BufferedReader(new InputStreamReader(new FileInputStream(
                    file2), "UTF-8"));
            String line;
            List<String> lines = null;
            
            while ((line = fileIn.readLine()) != null) {
                
                if (line.equals("BEGIN Quote")) {
                    lines = new LinkedList<String>();
                    continue;
                }
                
                if (line.equals("END Quote")) {
                    quotes.add(lines);
                    lines = null;
                    continue;
                }
                
                if (lines == null) {
                    continue;
                }
                
                if (!line.equals("")) {
                    lines.add(line);
                }
            }
            
            while ((line = fileIn2.readLine()) != null) {
                
                if (line.equals("BEGIN Quote")) {
                    lines = new LinkedList<String>();
                    continue;
                }
                
                if (line.equals("END Quote")) {
                    quotes2.add(lines);
                    lines = null;
                    continue;
                }
                
                if (lines == null) {
                    continue;
                }
                
                if (!line.equals("")) {
                    lines.add(line);
                }
            }
            
            fileIn.close();
            fileIn2.close();
        } catch (UnsupportedEncodingException e) {
            System.err.println("Your system seems not to support the UTF-8 "
                    + "encoding which is needed by this program!");
            System.exit(-1);
        } catch (FileNotFoundException e) {
            System.err.println("The file »" + file + "« could not be found!");
            System.exit(-1);
        } catch (IOException e) {
            System.err.println("The file »" + file + "« could not be read!");
            System.exit(-1);
        }
    }
    
    private void loadChars(String charsFile) {
        File file = new File(charsFile).getAbsoluteFile();
        chars = new ArrayList<String>();
        TreeSet<String> set = new TreeSet<String>();
        
        try {
            BufferedReader fileIn = new BufferedReader(new InputStreamReader(new FileInputStream(
                    file), "UTF-8"));
            String c;
            
            while ((c = fileIn.readLine()) != null) {
                
                if (!c.startsWith("//") && !c.startsWith("#")) {
                    
                    for (int i = 0; i < c.length(); i++) {
                        set.add(c.substring(i, i + 1));
                    }
                }
            }
            
            chars.addAll(set);
            fileIn.close();
        } catch (UnsupportedEncodingException e) {
            System.err.println("Your system seems not to support the UTF-8 "
                    + "encoding which is needed for this program!");
            System.exit(-1);
        } catch (FileNotFoundException e) {
            System.err.println("The file »" + file + "« could not be found!");
            System.exit(-1);
        } catch (IOException e) {
            System.err.println("The file »" + file + "« could not be found!");
            System.exit(-1);
        }
    }
    
    private static void printHelp() {
        System.out.println("This is the matrix.");
        System.out.println();
        System.out.println("The following optional command line arguments "
                + "are available to configure it's");
        System.out.println("behaviour:");
        System.out.println();
        System.out.println("--char-file FILE       Every character "
                + "contained in the UTF-8 encoded FILE is");
        System.out.println("                       used as a matrix "
                + "character (defaults to chars.txt in");
        System.out.println("                       the current directory).");
        System.out.println("--creating-strings NUMBER  Count of creatings "
                + "strings created per second");
        System.out.println("                       (defaults to 15).");
        System.out.println("--deleting-strings NUMBER  Count of deleting "
                + "strings created per second");
        System.out.println("                       (defaults to 20).");
        System.out.println("--font-size NUMBER     The size of the font "
                + "that is used to display the matrix");
        System.out.println("                       (defaults to 16).");
        System.out.println("--quote-file FILE      FILE contains the main "
                + "quotes that are displayed in the");
        System.out.println("                       middle of the screen "
                + "with a little border around them");
        System.out.println("                       (defaults to quotes.txt "
                + "in the current directory).");
        System.out.println("--quote2-file FILE     FILE contains the hidden "
                + "quotes that are diplayed at");
        System.out.println("                       random positions with no "
                + "border around them (defaults to");
        System.out.println("                       quotes2.txt in the " + "current directory).");
        System.out.println("--quote-pause NUMBER   Maximal pause in seconds "
                + "between two quotes (defaults to");
        System.out.println("                       300). The exact pause is "
                + "a uniformly distributed random");
        System.out.println("                       number taken from ]0, "
                + "NUMBER] interpreted as seconds.");
        System.out.println("--random-noise NUMBER  Every 0.02 seconds NUMBER "
                + "random patterns are generated");
        System.out.println("                       as random noise in the "
                + "matrix (defaults to 6).");
        System.out.println();
        System.out.println("Hints:");
        System.out.println("· Press the Q key when no quote is visible and "
                + "a quote will be shown.");
        System.out.println();
        System.out.println("© 2008 by Edgar Kalkowski (bugs to " + "eMail@edgar-kalkowski.de)");
    }
    
    public static void main(String[] arguments) {
        int randomNoise = 6, creatingStrings = 15, deletingStrings = 20, quotePause = 300, quote2Pause = 600, fontSize = 16;
        String charFile = "chars.txt", quoteFile = "quotes.txt", quote2File = "quotes2.txt";
        boolean errors = false;
        
        TreeMap<String, String> args = CommandLineParser.parse(arguments);
        
        if (args.containsKey("--help") || args.containsKey("-h") || args.containsKey("-help")) {
            printHelp();
            return;
        }
        
        if (args.containsKey("--creating-strings")) {
            String cStrings = args.get("--creating-strings");
            
            try {
                creatingStrings = (int) (Double.parseDouble(cStrings));
            } catch (NumberFormatException e) {
                System.err.println("ERROR: The number of creating strings (" + cStrings
                        + ") must be numerical!");
                errors = true;
            }
            
            args.remove("--creating-strings");
        }
        
        if (args.containsKey("--deleting-strings")) {
            String dStrings = args.get("--deleting-strings");
            
            try {
                deletingStrings = (int) (Double.parseDouble(dStrings));
            } catch (NumberFormatException e) {
                System.err.println("ERROR: The number of deleting strings (" + dStrings
                        + ") must be numerical!");
                errors = true;
            }
            
            args.remove("--deleting-strings");
        }
        
        if (args.containsKey("--font-size")) {
            String fSize = args.get("--font-size");
            
            try {
                fontSize = (int) (Double.parseDouble(fSize));
            } catch (NumberFormatException e) {
                System.err.println("ERROR: The font size must be a number!");
                errors = true;
            }
            
            args.remove("--font-size");
        }
        
        if (args.containsKey("--random-noise")) {
            String rNoise = args.get("--random-noise");
            
            try {
                randomNoise = (int) (Double.parseDouble(rNoise));
            } catch (NumberFormatException e) {
                System.err.println("ERROR: The number of random noise " + "patterns (" + rNoise
                        + ") must be numerical!");
                errors = true;
            }
            
            args.remove("--random-noise");
        }
        
        if (args.containsKey("--quote-pause")) {
            String qPause = args.get("--quote-pause");
            
            try {
                quotePause = (int) (Double.parseDouble(qPause));
            } catch (NumberFormatException e) {
                System.err.println("ERROR: The maximal pause between two " + "quotes (" + qPause
                        + ") must be numerical!");
                errors = true;
            }
            
            args.remove("--quote-pause");
        }
        
        if (args.containsKey("--quote2-pause")) {
            String q2Pause = args.get("--quote2-pause");
            
            try {
                quote2Pause = (int) (Double.parseDouble(q2Pause));
            } catch (NumberFormatException e) {
                System.err.println("ERROR: The maximal pause between two " + "hidden quotes ("
                        + q2Pause + ") must be numerical!");
                errors = true;
            }
            
            args.remove("--quote2-pause");
        }
        
        if (args.containsKey("--quote-file")) {
            quoteFile = args.get("--quote-file");
            args.remove("--quote-file");
        }
        
        if (args.containsKey("--quote2-file")) {
            quote2File = args.get("--quote2-file");
            args.remove("--quote2-file");
        }
        
        if (args.containsKey("--char-file")) {
            charFile = args.get("--char-file");
            args.remove("--char-file");
        }
        
        if (!args.keySet().isEmpty()) {
            
            for (String key : args.keySet()) {
                System.err.println("WARNING: Unknown argument: " + key + " " + args.get(key));
            }
        }
        
        if (errors) {
            System.err.println("Some errors encountered (see messages above) "
                    + "and the program cannot be run.");
            System.err.println("If you are not sure what to do try --help!");
            System.exit(-1);
        }
        
        Matrix matrix = new Matrix(deletingStrings, creatingStrings, quotePause, quote2Pause,
                randomNoise, charFile, quoteFile, quote2File, fontSize);
        matrix.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        matrix.setDefaultCloseOperation(EXIT_ON_CLOSE);
        matrix.setUndecorated(true);
        matrix.setBackground(Color.BLACK);
        matrix.setVisible(true);
    }
}
