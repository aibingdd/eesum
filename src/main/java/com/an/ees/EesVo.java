package com.an.ees;

public class EesVo implements Comparable<EesVo> {
    private String code;
    private float grossEarning;
    private float earning;
    private float commission;

    public EesVo() {
    }

    public EesVo(String code, float grossEarning, float earning, float commission) {
        this.code = code;
        this.grossEarning = grossEarning;
        this.earning = earning;
        this.commission = commission;
    }

    @Override
    public int compareTo(EesVo o) {
        if (AppMain.SORT_BY_GROSS_EARNING) {
            if ((this.grossEarning - o.grossEarning) > 0f) {
                return -1;
            } else if ((this.grossEarning - o.grossEarning) < 0f) {
                return 1;
            } else {
                return 0;
            }
        } else {
            if ((this.earning - o.earning) > 0f) {
                return -1;
            } else if ((this.earning - o.earning) < 0f) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public float getGrossEarning() {
        return grossEarning;
    }

    public void setGrossEarning(float grossEarning) {
        this.grossEarning = grossEarning;
    }

    public float getEarning() {
        return earning;
    }

    public void setEarning(float earning) {
        this.earning = earning;
    }

    public float getCommission() {
        return commission;
    }

    public void setCommission(float commission) {
        this.commission = commission;
    }
}
