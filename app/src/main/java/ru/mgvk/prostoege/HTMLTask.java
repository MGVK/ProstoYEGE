package ru.mgvk.prostoege;

public class HTMLTask {

    public boolean hasImage;
    public int    ID          = 0;
    public String Description = "";

    public HTMLTask() {

    }

    public HTMLTask(int ID, String description, boolean hasImage, Constants type) {
        this.ID = ID;
        this.Description = description;
        if (this.hasImage = hasImage) {
            DataLoader.loadTaskPicture(ID, type);
        }
    }

    public int getID() {
        return ID;
    }

    public String getDescription() {
        return Description;
    }

    boolean hasImage() {
        return hasImage;
    }


}