package com.blueridgebinary.terra.data;

/**
 * Created by dorra on 8/25/2017.
 */

// holds session data
public class CurrentSession extends CurrentDataset{

    private int sessionId;
    private String sessionName;
    private String sessionNotes;
    private String createdTimestamp;
    private String updatedTimestamp;

    public CurrentSession(int sessionId,String sessionName,String sessionNotes,String createdTimestamp,String updatedTimestap) {
        this.sessionId = sessionId;
        this.sessionName = sessionName;
        this.sessionNotes = sessionNotes;
        this.createdTimestamp = createdTimestamp;
        this.updatedTimestamp = updatedTimestap;
    }

    public int getSessionId() {
        return sessionId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public String getSessionNotes() {
        return sessionNotes;
    }

    public String getCreatedTimestamp() {
        return createdTimestamp;
    }

    public String getUpdatedTimestamp() {
        return updatedTimestamp;
    }

}
