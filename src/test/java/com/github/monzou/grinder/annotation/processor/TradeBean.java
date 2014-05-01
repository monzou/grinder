package com.github.monzou.grinder.annotation.processor;

//CHECKSTYLE:OFF
import java.io.Serializable;
import java.math.BigDecimal;

import com.github.monzou.grinder.annotation.Grind;

/**
 * TradeBean
 */
@Grind
@SuppressWarnings("serial")
public class TradeBean implements Serializable {

    /** Status */
    public static enum Status {
        /** Plan */
        PLAN,
        /** Authorized */
        AUTHORIZED;
    }

    /** Const **/
    public static final String CONST = "foo";

    private String tradeNo;

    private Status status;

    private long version;

    private boolean deleted;

    private Boolean flag;

    private final String readonly = "readonly";

    // getter only
    public String getTradeNoAndStatus() {
        return String.format("%s%s", getTradeNo(), getStatus().toString());
    }

    // virtual property
    public String getFoo() {
        return tradeNo;
    }

    public void setFoo(String foo) {
        this.tradeNo = foo;
    }

    // setter only
    public void setBar(String bar) {
        this.tradeNo = bar;
    }

    // method
    public String calculateBaz() {
        return tradeNo;
    }

    // read only
    public String getReadonly() {
        return readonly;
    }

    // not public
    String getTradeNo() {
        return tradeNo;
    }

    // not public
    void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getVersion() {
        return version;
    }

    // not public (write method)
    void setVersion(long version) {
        this.version = version;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    // inner-class
    @Grind
    public static class CashFlowBean {

        private BigDecimal amount;

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

    }

}
