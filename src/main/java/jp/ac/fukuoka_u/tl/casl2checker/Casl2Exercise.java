package jp.ac.fukuoka_u.tl.casl2checker;

/**
 * Created by furusho on 2016/11/16.
 */

public class Casl2Exercise {

    String fileName;
    int number;

    public Casl2Exercise(String fileName, int number) {
        this.fileName = fileName;
        this.number = number;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getFileName() {

        return fileName;
    }

    public int getNumber() {
        return number;
    }
}
