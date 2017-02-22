package com.anonymous.codetimemachine;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.net.URL;


public class CommitsInfoLayerUI extends LayerUI<JComponent> implements ImageObserver
{
    boolean isThereSomthingToDisplay = false;
    CommitWrapper commitToDisplay = null;

    boolean showJustMessage = false;

    final Color bgColor = new Color(33,33,33,230);
    //final Color TITLE_COLOR = Color.LIGHT_GRAY;
    final Color TEXT_COLOR = Color.WHITE;

    final Color DARK_GREEN = new Color(100,200,150);
    final Color LIGHT_BLUE = new Color(180,210,240);
    final Color BROWN = new Color(190,130,40);

    Font NORM_FONT = new Font("Arial", Font.PLAIN, 12);
    Font BOLD_FONT = new Font("Arial", Font.BOLD, 12);
    Font BOLDER_FONT = new Font("Arial", Font.BOLD, 20);

    int LEFT_MARGIN_ICON_AND_COMMIT_MESSEGE = 0;
    int LEFT_MARGIN = 0;
    int LEFT_TEXT_MARGIN = 0;
    int RIGHT_MARGIN = 0;

    private BufferedImage dateImage = null, commitIDImage = null, authorImage =null;

    public CommitsInfoLayerUI()
    {
        URL dateImageURL = getClass().getResource("/images/time.png");
        URL commitIDImageURL = getClass().getResource("/images/commitID.png");
        URL authorImageURL = getClass().getResource("/images/author.png");

        try {
            dateImage = ImageIO.read(dateImageURL);
            commitIDImage = ImageIO.read(commitIDImageURL);
            authorImage = ImageIO.read(authorImageURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paint(Graphics g, JComponent c)
    {
        super.paint(g, c);
        if(!isThereSomthingToDisplay) return;

        LEFT_MARGIN_ICON_AND_COMMIT_MESSEGE = 10;
        LEFT_MARGIN = 30;
        LEFT_TEXT_MARGIN = LEFT_MARGIN+45;
        RIGHT_MARGIN = 20;

        Graphics2D g2d = (Graphics2D) g.create();

        int w = c.getWidth();
        int h = c.getHeight();

        // Background
        g2d.setColor(bgColor);
        g2d.fillRect(0,0,w, h-0 /*10: because the scroll-handle is not black, so if we make it black it's not pretty*/);


        if(showJustMessage == false)
            draw_allCommitInfo(g, g2d, w);
        else
            draw_justCommitMessage(g2d, w);

        g2d.dispose();
    }

    private void draw_justCommitMessage(Graphics2D g2d, int w)
    {
        ////////4
        String s1="Message: ";
        String commitMessage = commitToDisplay.getCommitMessage();
        int Y = 20;

        g2d.setFont(NORM_FONT);
        g2d.setColor(BROWN);
        g2d.drawString(s1,LEFT_MARGIN_ICON_AND_COMMIT_MESSEGE, Y);
        int k = g2d.getFontMetrics().stringWidth(s1);

        g2d.setFont(NORM_FONT);
        g2d.setColor(TEXT_COLOR);
        int maxAvailableWidth = w-k-LEFT_MARGIN-RIGHT_MARGIN;
        if(maxAvailableWidth<5/*something near 0*/) return;// otherwise the below 'n-3' would be negative and cause bugs.

        int line =0; //we have 5 lines
        final int TOTAL_AVAILABLE_LINES = 5;
        while(line<TOTAL_AVAILABLE_LINES)
        {
            int n =  DrawingHelper.howManyCharFitsInWidth(g2d,commitMessage, maxAvailableWidth);
            if(n<commitMessage.length() /*text doesn't fit*/ && line==TOTAL_AVAILABLE_LINES-1 /*last line*/)
            {
                commitMessage = commitMessage.substring(0, n - 3/*for "..."*/) + "...";
                g2d.drawString(commitMessage,LEFT_TEXT_MARGIN,Y);
                break;
            }
            else
            {
                g2d.drawString(commitMessage.substring(0,n),LEFT_TEXT_MARGIN,Y);
                commitMessage = commitMessage.substring(n);
                Y += 20;
                line++;
            }
        }
    }

    private void draw_allCommitInfo(Graphics g, Graphics2D g2d, int w)
    {
        // Text 1-2-3-4
        ////////1
        g.drawImage(commitIDImage, LEFT_MARGIN_ICON_AND_COMMIT_MESSEGE-2/*this icon width is longer than others*/,11,this);
        g2d.setFont(NORM_FONT);
        g2d.setColor(LIGHT_BLUE);
        g2d.drawString("ID: ",LEFT_MARGIN,20);
        g2d.setFont(BOLD_FONT);
        g2d.setColor(TEXT_COLOR);
        g2d.drawString(commitToDisplay.getCommitID(),LEFT_TEXT_MARGIN,20);

        ////////2
        g.drawImage(authorImage, LEFT_MARGIN_ICON_AND_COMMIT_MESSEGE,26,this);
        g2d.setFont(NORM_FONT);
        g2d.setColor(Color.PINK);
        g2d.drawString("Author: ",LEFT_MARGIN,35);
        g2d.setFont(BOLD_FONT);
        g2d.setColor(TEXT_COLOR);
        g2d.drawString(commitToDisplay.getAuthor(),LEFT_TEXT_MARGIN,35);

        ////////3
        g.drawImage(dateImage, LEFT_MARGIN_ICON_AND_COMMIT_MESSEGE,42,this);
        g2d.setFont(NORM_FONT);
        g2d.setColor(DARK_GREEN);
        g2d.drawString("Date: ",LEFT_MARGIN,50);
        g2d.setFont(BOLD_FONT);
        g2d.setColor(TEXT_COLOR);
        g2d.drawString(CalendarHelper.convertDateToStringYmDHM(commitToDisplay.getDate()),LEFT_TEXT_MARGIN,50);

        //////// line
        g2d.drawLine(LEFT_MARGIN,55,w-RIGHT_MARGIN, 55);

        ////////4
        String s1="Message: ";
        String s2=commitToDisplay.getCommitMessage();
        final int Y = 80;

        g2d.setFont(NORM_FONT);
        g2d.setColor(BROWN);
        g2d.drawString(s1,LEFT_MARGIN_ICON_AND_COMMIT_MESSEGE,Y-2 /* Because following text's size will be much bigger*/);
        int k = g2d.getFontMetrics().stringWidth(s1);

        g2d.setFont(BOLDER_FONT);
        g2d.setColor(TEXT_COLOR);
        int maxAvailableWidth = w-k-LEFT_MARGIN-RIGHT_MARGIN;
        if(maxAvailableWidth<5/*something near 0*/) return;// otherwise the below 'n-3' would be negative and cause bugs.
        int n =  DrawingHelper.howManyCharFitsInWidth(g2d,s2, maxAvailableWidth);
        if(n<s2.length())
            s2 = s2.substring(0,n-3/*for "..."*/)+"...";
        g2d.drawString(s2,LEFT_TEXT_MARGIN,Y);
    }

    void invisble()
    {
        isThereSomthingToDisplay = false;
    }

    void displayInfo(CommitWrapper commit)
    {
        isThereSomthingToDisplay = true;

        commitToDisplay = commit;
    }

    void toggleInfoDisplayingType()
    {
        showJustMessage = !showJustMessage;
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
    {
        return false;
    }
}
