package app.ewallet;

/**
 * Created by Seth Legaspi on 11/15/2015.
 * Java file that handles getting the terminal's balance from the local database
 */
public class Balance {

    private String _shopID;
    private Double _balance;

    public Balance() {
        //Empty Constructor
    }

    public Balance(String id, Double bal) {
        this._shopID = id;
        this._balance = bal;
    }

    public void setShopID(String id) {
        this._shopID = id;
    }

    public String getShopID(){
        return this._shopID;
    }

    public void setBal(Double bal) {
        this._balance = bal;
    }

    public Double getBalance(){
        return this._balance;
    }


}
