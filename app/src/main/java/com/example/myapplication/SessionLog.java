package com.example.myapplication;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Vector;

public class SessionLog {

    private Context context;
    private String buffer;

    public SessionLog(Context context) {
        this.context = context;
        reset();
    }

    public void write(DataPoint dataPoint) {

        buffer += dataPoint.toCSV();

        if(buffer.length() > 1024) {
            flush();
        }
    }

    public void reset() {
        buffer = "";
        context.getFileStreamPath("session.txt").delete();
    }

    public void flush() {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(context.openFileOutput("session.txt", Context.MODE_APPEND));
            writer.write(buffer);
            buffer = "";
            writer.close();
        }
        catch (IOException e) {
        }
    }
}
