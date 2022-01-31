package client;

import javax.swing.*;
import java.awt.*;

public class Message {

    private static final int USER_MSG_FONT_SIZE = 14;
    private static final int SYSTEM_MSG_FONT_SIZE = 10;

    private String author;
    private String text;
    private int msgPosX;
    private int msgPosY;
    private int textAreaWidth;
    private int textAreaHeight;
    private int nextMsgPosX;
    private int nextMsgPosY;


    public Message(String author, String formattedMessage, int prevMsgX, int prevMsgY) {

        this.author = author;
        this.text = author + "\n" + formattedMessage;
        this.msgPosX = prevMsgX;
        this.msgPosY = prevMsgY;

        JTextArea areaText = new JTextArea(author + "\n" + formattedMessage);
        if (author.equals("System")) {
            areaText.setFont(new Font("Comics Sans MS", Font.PLAIN, SYSTEM_MSG_FONT_SIZE));
        } else {
            areaText.setFont(new Font("Comics Sans MS", Font.PLAIN, USER_MSG_FONT_SIZE));
        }
        areaText.setMargin(new Insets(5, 5, 5, 5));
        textAreaWidth = (int) areaText.getPreferredSize().getWidth();
        textAreaHeight = (int) areaText.getPreferredSize().getHeight();

        nextMsgPosX = msgPosX;
        nextMsgPosY = msgPosY + textAreaHeight;
    }


    // Getters
    public String getText() {
        return text;
    }

    public int getMsgPosX() {
        return msgPosX;
    }

    public int getMsgPosY() {
        return msgPosY;
    }

    public int getTextAreaWidth() {
        return textAreaWidth;
    }

    public int getTextAreaHeight() {
        return textAreaHeight;
    }

    public int getNextMsgPosX() {
        return nextMsgPosX;
    }

    public int getNextMsgPosY() {
        return nextMsgPosY;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        return "Message{" +
                "msgPosX=" + msgPosX +
                ", msgPosY=" + msgPosY +
                ", textAreaWidth=" + textAreaWidth +
                ", textAreaHeight=" + textAreaHeight +
                ", nextMsgPosX=" + nextMsgPosX +
                ", nextMsgPosY=" + nextMsgPosY +
                '}';
    }
}
