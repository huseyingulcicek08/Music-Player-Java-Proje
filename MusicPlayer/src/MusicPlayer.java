
/**
 * @author: Hüseyin Gülçiçek
 * last Edited: October 28, 2019
 *
 * Music Player class. Acts as the client in the Java RMI system. 
 * Will open a JFrame for the user, all functions can be performed in the window.
 * jaco mp3 library used for playing .mp3 file type.
 */

import java.io.*;
import java.nio.file.Files;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.rmi.*;
import java.util.ArrayList;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;

import jaco.mp3.player.MP3Player;

public class MusicPlayer {

    private static final int FRAMEX = 640, FRAMEY = 480, HALFX = 320, HALFY = 240;
    private static final Font buttonFont = new Font("Arial", Font.BOLD, 24);
    private static final Font uploadButtonFont = new Font("Arial", Font.BOLD, 12);
    private static final Font nowPlayingFont = new Font("Arial", Font.BOLD, 16);
    private static final Font playlistFont = new Font("Arial", Font.BOLD, 18);
    private static final String dataFolder = "songs/";
    private static ArrayList<String> playlist;
    private static int nowPlayingID;
    private static MP3Player player;
    private static JTable list;
    private static JScrollPane sp;
    private static JLabel image;

    /**
     * Get album art from the server. Convert the data stream to a buffered image
     * and add it to the JLabel component.
     *
     * @param fi
     * @throws Exception
     */
    public static void getAlbumArt(FileInterface fi) throws Exception {
        byte[] imagedata = fi.downloadImage(80);
        ByteArrayInputStream bis = new ByteArrayInputStream(imagedata);
        image = new JLabel();
        image.setIcon(new ImageIcon(ImageIO.read(bis)));
        image.setBounds(20, FRAMEY - 120, 80, 80);
    }

    /**
     * Get playlist data from the server, make sure client has all songs in the
     * playlist. If any are missing, download them.
     *
     * @param fi
     * @throws Exception
     */
    public static void syncPlaylist(FileInterface fi) throws Exception {
        System.out.println("Syncing playlist from server");
        for (String s : fi.checkAvailableSongs()) {
            if (new File(dataFolder + s).exists())
                continue;
            byte[] filedata = fi.downloadSong(s);
            BufferedOutputStream output = new BufferedOutputStream(
                    new FileOutputStream("songs/" + new File(s).getName()));
            output.write(filedata, 0, filedata.length);
            output.flush();
            output.close();
        }
    }

    /**
     * Initialize the mp3 player object, add the playlist data to the player and
     * start playing.
     *
     * @throws Exception
     */
    public static void startPlayer() throws Exception {
        player = new MP3Player();
        playlist = new ArrayList<String>();
        for (File fileEntry : new File(dataFolder).listFiles()) {
            player.addToPlayList(new File(dataFolder + fileEntry.getName()));
            playlist.add(fileEntry.getName());
        }
        nowPlayingID = 0;
        player.setRepeat(true);
        player.play();
    }

    /**
     * Read playlist data, convert to 2d array that can be read into a JTable
     * component. add table to a JScrollPane and add scroll pane to the window.
     *
     * @param window
     * @throws Exception
     */
    public static void configurePlaylist(JFrame window) throws Exception {
        // convert playlist data to 2D array
        String[][] data = new String[playlist.size()][2];
        String[] cols = { "#", "Name" };
        for (int i = 0; i < playlist.size(); i++) {
            data[i][0] = String.valueOf(i + 1);
            data[i][1] = playlist.get(i);
        }
        // initialize and configure components, add them to window
        list = new JTable(data, cols);
        sp = new JScrollPane(list);
        list.setFont(playlistFont);
        list.setRowHeight(40);
        list.getColumnModel().getColumn(0).setMaxWidth(40);
        list.getColumnModel().getColumn(0).setHeaderValue("#");
        list.getColumnModel().getColumn(1).setHeaderValue("Name");
        list.setEnabled(false); // rows can't be clicked
        list.addRowSelectionInterval(nowPlayingID, nowPlayingID); // highlight current track
        sp.setBounds(10, 10, 620, 300);
        window.add(sp);
    }

    /**
     * Main method, will run when client starts.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // RMI connect to music server
        FileInterface fi = (FileInterface) Naming.lookup("//localhost/MusicServer");
        // initialize window
        JFrame window = new JFrame("Music Player");
        // get album art and add it to the window
        getAlbumArt(fi);
        window.add(image);
        // download new songs from server
        syncPlaylist(fi);
        // read files, add them to playlist, set now playing, start player
        startPlayer();
        // initialize and configure playlist, add to window
        configurePlaylist(window);

        // initialize now playing message and add to window
        JLabel nowPlayingLabel = new JLabel();
        nowPlayingLabel.setText("Now Playing: " + playlist.get(nowPlayingID));
        nowPlayingLabel.setFont(nowPlayingFont);
        nowPlayingLabel.setBounds(10, 330, 620, 20);
        window.add(nowPlayingLabel);

        // initialize play/pause button and add to window
        JButton playButton = new JButton("❚❚");
        playButton.setFont(buttonFont);
        playButton.setBounds(HALFX - 40, FRAMEY - 120, 80, 80);
        window.add(playButton);

        // initialize next button and add to window
        JButton nextButton = new JButton("▶|");
        nextButton.setFont(buttonFont);
        nextButton.setBounds(HALFX + 40, FRAMEY - 120, 80, 80);
        window.add(nextButton);

        // initialize previous button and add to window
        JButton previousButton = new JButton("|◀");
        previousButton.setFont(buttonFont);
        previousButton.setBounds(HALFX - 120, FRAMEY - 120, 80, 80);
        window.add(previousButton);

        // initialize upload button and add to window
        JButton uploadButton = new JButton("Upload");
        uploadButton.setFont(uploadButtonFont);
        uploadButton.setBounds(FRAMEX - 140, FRAMEY - 115, 100, 30);
        window.add(uploadButton);

        // initialize delete button and add to window
        JButton deleteButton = new JButton("Delete");
        deleteButton.setFont(uploadButtonFont);
        deleteButton.setBounds(FRAMEX - 140, FRAMEY - 75, 100, 30);
        window.add(deleteButton);

        // initialize file chooser, add filter for .mp3 files
        JFileChooser uploadChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("MP3 Files", "mp3");
        uploadChooser.setFileFilter(filter);
        uploadChooser.setBounds(0, 0, 620, 460);

        // configure JFrame
        window.setSize(FRAMEX, FRAMEY);
        window.setResizable(false);
        window.setLayout(null);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // action listeners for buttons
        playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                // if currently playing, pause
                if (!player.isPaused()) {
                    player.pause();
                    playButton.setText("▶");
                }
                // if currently paused, play
                else {
                    player.play();
                    playButton.setText("❚❚");
                }
            }
        });

        // listen for when next button is pressed
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                // if player was paused, set it to play
                if (player.isPaused()) {
                    player.play();
                    playButton.setText("❚❚");
                }
                // go to next track
                player.skipForward();
                // if we're on last track, go to first
                if (nowPlayingID == playlist.size() - 1)
                    nowPlayingID = 0;
                else
                    nowPlayingID++;
                // set now playing and playlist highlight
                nowPlayingLabel.setText("Now Playing: " + playlist.get(nowPlayingID));
                list.clearSelection();
                list.addRowSelectionInterval(nowPlayingID, nowPlayingID);
            }
        });

        // listen for when previous button is pressed
        previousButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                // if player was paused, set it to play
                if (player.isPaused()) {
                    player.play();
                    playButton.setText("❚❚");
                }
                // go to previous track
                player.skipBackward();
                // if we're on first track, go to last
                if (nowPlayingID == 0)
                    nowPlayingID = playlist.size() - 1;
                else
                    nowPlayingID--;
                // set now playing and playlist highlight
                nowPlayingLabel.setText("Now Playing: " + playlist.get(nowPlayingID));
                list.clearSelection();
                list.addRowSelectionInterval(nowPlayingID, nowPlayingID);
            }
        });

        // listen for when upload button is pressed
        uploadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                // show file chooser window
                int returnVal = uploadChooser.showOpenDialog(window);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = uploadChooser.getSelectedFile();
                    System.out.println("Uploading: " + file.getName() + ".");
                    try {
                        // request upload to server
                        fi.uploadSong(Files.readAllBytes(file.toPath()), file.getName());
                        // reconfigure playlist and restart player
                        player.stop();
                        player = null;
                        syncPlaylist(fi);
                        startPlayer();
                        playButton.setText("❚❚");
                        configurePlaylist(window);
                    } catch (Exception e) {
                        System.out.println("Client error uploading file: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        // listen for when delete button is pressed
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                System.out.println("Deleting: " + playlist.get(nowPlayingID));
                try {
                    // delete from client and request delete on server
                    new File(dataFolder + playlist.get(nowPlayingID)).delete();
                    fi.deleteSong(playlist.get(nowPlayingID));
                    // reconfigure playlist and restart player
                    player.stop();
                    player = null;
                    syncPlaylist(fi);
                    startPlayer();
                    playButton.setText("❚❚");
                    configurePlaylist(window);
                } catch (Exception e) {
                    System.out.println("Client error deleting file: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
}
