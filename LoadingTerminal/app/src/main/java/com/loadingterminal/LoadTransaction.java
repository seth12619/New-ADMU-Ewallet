package com.loadingterminal;

/**
 * Created by Seth Legaspi on 11/15/2015.
 */
public class LoadTransaction {

    private int _idTransaction;
    private Double _amountLoaded;
    private String _timeStamp;
    private int _idNumber;
    private String _terminalID;
    private String _sendStamp;

    public LoadTransaction() {
        //Empty Constructor
    }

    public LoadTransaction(int idT, Double amt, String ts, int idN, String tID, String sendStamp) {
        this._idTransaction = idT;
        this._amountLoaded = amt;
        this._timeStamp = ts;
        this._idNumber = idN;
        this._terminalID = tID;
        this._sendStamp = sendStamp;
    }

    public void setSendStamp(String stamp) {
        this._sendStamp = stamp;
    }

    public String getSendStamp() {
        return this._sendStamp;
    }

    public void setIDTransaction(int idT) {
        this._idTransaction = idT;
    }

    public int getIDTransaction() {
        return this._idTransaction;
    }

    public void setAmountLoaded(Double amt) {
        this._amountLoaded = amt;
    }

    public Double getAmt() {
        return this._amountLoaded;
    }

    public void setTS(String ts) {
        this._timeStamp = ts;
    }

    public String getTS() {
        return this._timeStamp;
    }

    public void setIDnum(int id) {
        this._idNumber = id;
    }

    public int getIDNum(){
        return this._idNumber;
    }

    public void setTerminalID(String id) {
        this._terminalID = id;
    }

    public String getTerminalID(){
        return this._terminalID;
    }
}
