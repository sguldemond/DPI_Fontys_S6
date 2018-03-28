package mix.model.bank;

import mix.model.loan.LoanRequest;

import java.io.Serializable;

/**
 *
 * This class stores all information about an request from a bank to offer
 * a loan to a specific client.
 */
public class BankInterestRequest implements Serializable {

    private int amount; // the requested loan amount
    private int time; // the requested loan period
    private int aggregationId;

    public BankInterestRequest() {
        super();
        this.amount = 0;
        this.time = 0;
    }

    public BankInterestRequest(int amount, int time) {
        super();
        this.amount = amount;
        this.time = time;
    }

    public BankInterestRequest(LoanRequest loanRequest) {
        super();
        this.amount = loanRequest.getAmount();
        this.time = loanRequest.getTime();
    }

    @Override
    public String toString() {
        return " amount=" + amount + " time=" + time;
    }


    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getAggregationId() {
        return aggregationId;
    }

    public void setAggregationId(int aggregationId) {
        this.aggregationId = aggregationId;
    }
}
