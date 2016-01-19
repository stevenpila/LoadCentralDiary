package com.example.stevenpila.loadcentraldiary;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;

import java.io.File;

/**
 * Created by stevenjefferson.pila on 1/19/2016.
 */
public class MyBackup extends BackupAgentHelper {
    private static final String BACKUP_FILE_NAME = DatabaseHandler.DATABASE_NAME;
    private static final String PREFS_BACKUP_KEY = "dbs";

    @Override
    public void onCreate() {
        FileBackupHelper helper = new FileBackupHelper(this, BACKUP_FILE_NAME);
        addHelper(PREFS_BACKUP_KEY, helper);
    }

    @Override
    public File getFilesDir() {
        File path = getDatabasePath(BACKUP_FILE_NAME);
        return path.getParentFile();
    }
}
