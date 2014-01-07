package com.github.monzou.grinder.apt;

//CHECKSTYLE:OFF
import java.io.Serializable;

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

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
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

    public void setVersion(long version) {
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

}
