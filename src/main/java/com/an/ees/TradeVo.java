package com.an.ees;

public class TradeVo {
    public static final int INVALID = -1;
    public static final int BUY = 1;
    public static final int SELL = 2;
    public static final int NEW = 3;
    public static final int BANK2SECURITY = 4;
    public static final int SECURITY2BANK = 5;

    private String time;// 2016-12-06 14:57:55
    private String code;// 002850
    private String name;// 科达利
    private int operation;// 1-buy,2-sell
    private int amount;// 成交数量
    private float price;// 成交价格
    private float turnover;// 成交金额
    private int netPosition;

    public String getOperationStr() {
        if (operation == 1) {
            return "买入";
        } else if (operation == 2) {
            return "卖出";
        } else if (operation == 3) {
            return "配售";
        } else if (operation == 4) {
            return "转入";
        } else if (operation == 5) {
            return "转出";
        }
        return "";
    }

    public void appendOut(StringBuilder out) {
        out.append(time).append(",").append(code + " -").append(",").append(name).append(",").append(getOperationStr())
                .append(",").append(turnover).append(",").append(amount).append(",").append(price).append(",")
                .append(netPosition).append("\n");
    }

    public float getGrossTurnover() {
        return ((float) amount) * price;
    }

    public boolean isBuy() {
        return operation == BUY;
    }

    public boolean isSell() {
        return operation == SELL;
    }

    public boolean isNew() {
        return operation == NEW;
    }

    public boolean isBank2Security() {
        return operation == BANK2SECURITY;
    }

    public boolean isSecurity2Bank() {
        return operation == SECURITY2BANK;
    }

    public TradeVo() {
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;     
    }

    public float getTurnover() {
        return turnover;
    }

    public void setTurnover(float turnover) {
        this.turnover = turnover;
    }

    public int getNetPosition() {
        return netPosition;
    }

    public void setNetPosition(int netPosition) {
        this.netPosition = netPosition;
    }
}