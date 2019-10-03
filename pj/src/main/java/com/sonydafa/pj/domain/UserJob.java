package com.sonydafa.pj.domain;

public class UserJob {
    private String scuSession;
    private long lastUpdateTime;
    private boolean isValid;
    public UserJob(){}
    public UserJob(String scuSession,long lastUpdateTime,boolean isValid){
        this.scuSession=scuSession;
        this.lastUpdateTime=lastUpdateTime;
        this.isValid=isValid;
    }
    public void setScuSession(String scuSession) {
        this.scuSession = scuSession;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String getScuSession() {
        return scuSession;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public boolean isValid() {
        return isValid;
    }
}
