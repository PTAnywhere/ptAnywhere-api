package uk.ac.open.kmi.forge.ptAnywhere.session;

public interface FileLoadingTask {

    String getInputFilePath();
    void markAsLoaded();

}
