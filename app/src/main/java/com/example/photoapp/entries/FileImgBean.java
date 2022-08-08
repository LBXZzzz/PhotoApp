package com.example.photoapp.entries;

public class FileImgBean {
    private String ThumbPath;
    private String FilePath;
    private String MimeType;
    private String Title;
    private String TakeTime;
    private String ImgSize;

    public String getThumbPath() {
        return ThumbPath;
    }

    public void setThumbPath(String thumbPath) {
        ThumbPath = thumbPath;
    }

    public String getFilePath() {
        return FilePath;
    }

    public void setFilePath(String filePath) {
        FilePath = filePath;
    }

    public String getMimeType() {
        return MimeType;
    }

    public void setMimeType(String mimeType) {
        MimeType = mimeType;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getTakeTime() {
        return TakeTime;
    }

    public void setTakeTime(String takeTime) {
        TakeTime = takeTime;
    }

    public String getImgSize() {
        return ImgSize;
    }

    public void setImgSize(String imgSize) {
        ImgSize = imgSize;
    }

}
