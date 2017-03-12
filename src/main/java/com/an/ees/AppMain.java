package com.an.ees;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppMain {
    private static String DIR_DATA = "D:\\data\\eesum\\";
    private static String[] mFileNames = new String[] { "m20160401-20160630.txt", "m20160701-20160930.txt",
            "m20161001-20161231.txt", "m20170101-20170331.txt" };
    private static String[] jFileNames = new String[] { "j20160701-20160930.txt", "j20161001-20161231.txt",
            "j20170101-20170331.txt" };

    private static Map<String, String> codeNameMap = new HashMap<>();
    // Code -> {Time -> Trade}
    private static Map<String, Map<String, TradeVo>> mTradeMap = new HashMap<>();
    private static Map<String, Map<String, TradeVo>> jTradeMap = new HashMap<>();
    private static Map<String, Float> jPositionMap = new HashMap<>();
    private static Map<String, Float> mPositionMap = new HashMap<>();

    public static final boolean SORT_BY_GROSS_EARNING = true;

    /**
     * @param args
     *            m<br>
     *            j<br>
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String user = null;
        if (args.length > 0) {
            user = args[0].trim();
            System.out.println("user: " + user);
        }
        if (user == null) {
            processM();
            processJ();
        } else if ("m".equals(user)) {
            processM();
        } else if ("j".equals(user)) {
            processJ();
        }
    }

    private static void processJ() throws IOException, Exception {
        loadPosition(DIR_DATA + "j_position.txt", jPositionMap);
        loadTrades(jFileNames, jTradeMap);
        exportAll(jTradeMap, DIR_DATA + "j_allin1.csv");
        exportSum(jTradeMap, jPositionMap, DIR_DATA + "j_sum.csv");
    }

    private static void processM() throws IOException, Exception {
        loadPosition(DIR_DATA + "m_position.txt", mPositionMap);
        loadTrades(mFileNames, mTradeMap);
        exportAll(mTradeMap, DIR_DATA + "m_allin1.csv");
        exportSum(mTradeMap, mPositionMap, DIR_DATA + "m_sum.csv");
    }

    private static void exportSum(Map<String, Map<String, TradeVo>> tradeMap, Map<String, Float> positionMap,
            String filePath) throws Exception {
        List<String> codeList = new ArrayList<String>(tradeMap.keySet());
        Collections.sort(codeList);

        StringBuilder header = new StringBuilder();
        List<EesVo> eesList = new ArrayList<>();
        for (String code : codeList) {
            List<String> timeList = new ArrayList<String>(tradeMap.get(code).keySet());
            Collections.sort(timeList);

            if ("--".equals(code)) {
                float principal = 0f;
                for (String t : timeList) {
                    TradeVo tvo = tradeMap.get(code).get(t);
                    if (tvo.getOperation() == TradeVo.BANK2SECURITY || tvo.getOperation() == TradeVo.SECURITY2BANK) {
                        principal += tvo.getTurnover();
                    }
                }
                header.append(code + " -").append(",").append(getName(code)).append(",").append(principal).append(",")
                        .append(0f).append(",").append(0f);
            } else {
                float grossEarning = 0f;
                float earning = 0f;
                float commission = 0f;
                for (String time : timeList) {
                    TradeVo t = tradeMap.get(code).get(time);
                    if (t.isBuy() || t.isNew()) {
                        grossEarning -= t.getGrossTurnover();
                    } else {
                        grossEarning += t.getGrossTurnover();
                    }

                    if (t.isNew()) {
                        earning -= t.getGrossTurnover();
                    } else if (t.isBuy() || t.isSell()) {
                        earning += t.getTurnover();
                    }

                    if (t.isBuy()) {
                        float one = -1f * t.getTurnover() - t.getGrossTurnover();
                        commission += one;
                    } else if (t.isSell()) {
                        float one = t.getGrossTurnover() - t.getTurnover();
                        commission += one;
                    }
                }

                // Current position
                if (positionMap.containsKey(code)) {
                    grossEarning += positionMap.get(code);
                    earning += positionMap.get(code);
                }

                eesList.add(new EesVo(code, grossEarning, earning, commission));
            }
        }

        Collections.sort(eesList);
        float grossEarningSum = 0f;
        float earningSum = 0f;
        float commissionSum = 0f;
        for (EesVo e : eesList) {
            grossEarningSum += e.getGrossEarning();
            earningSum += e.getEarning();
            commissionSum += e.getCommission();
        }

        StringBuilder out = new StringBuilder();
        header.append(",").append((int) earningSum).append(",").append((int) grossEarningSum).append(",")
                .append((int) commissionSum).append("\n");
        out.append(header.toString());

        for (EesVo e : eesList) {
            out.append(e.getCode() + " -").append(",").append(getName(e.getCode())).append(",")
                    .append((int) e.getGrossEarning()).append(",").append((int) e.getEarning()).append(",")
                    .append((int) e.getCommission()).append("\n");
        }

        FileUtil.writeFile(filePath, out.toString(), 1, false);
        FileUtil.writeFile(filePath + ".bak", out.toString(), 1, false);
    }

    private static void exportAll(Map<String, Map<String, TradeVo>> tradeMap, String filePath) throws IOException {
        List<String> codeList = new ArrayList<String>(tradeMap.keySet());
        Collections.sort(codeList);

        StringBuilder out = new StringBuilder();
        StringBuilder bank = new StringBuilder();
        for (String code : codeList) {
            List<String> timeList = new ArrayList<String>(tradeMap.get(code).keySet());
            Collections.sort(timeList);

            for (String t : timeList) {
                TradeVo tvo = tradeMap.get(code).get(t);
                // write bank notes at end of file
                if ("--".equals(code)) {
                    tvo.appendOut(bank);
                } else {
                    tvo.appendOut(out);
                }
            }
        }
        out.append(bank.toString());

        FileUtil.writeFile(filePath, out.toString(), 1, false);
        FileUtil.writeFile(filePath + ".bak", out.toString(), 1, false);
    }

    private static void loadTrades(String[] mFileNames, Map<String, Map<String, TradeVo>> tradeMap) throws Exception {
        List<TradeVo> mTradeList = new ArrayList<>();
        for (String f : mFileNames) {
            load(DIR_DATA + f, mTradeList);
        }
        for (TradeVo t : mTradeList) {
            if (!tradeMap.containsKey(t.getCode())) {
                tradeMap.put(t.getCode(), new HashMap<String, TradeVo>());
            }
            tradeMap.get(t.getCode()).put(t.getTime(), t);
        }
    }

    private static void loadPosition(String filePath, Map<String, Float> positionMap) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath)),
                "GBK"))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] strs = line.split("\\s+");
                positionMap.put(strs[0].trim(), Float.parseFloat(strs[1].trim()));
            }
        }
    }

    private static void load(String filePath, List<TradeVo> tradeList) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath)),
                "GBK"))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] strs = line.split("\\s+");

                String operStr = strs[4].trim();
                int oper = TradeVo.INVALID;
                if ("证券买入".equals(operStr)) {
                    oper = TradeVo.BUY;
                } else if ("证券卖出".equals(operStr)) {
                    oper = TradeVo.SELL;
                } else if ("配售股份".equals(operStr)) {
                    oper = TradeVo.NEW;
                } else if ("银行转证券".equals(operStr)) {
                    oper = TradeVo.BANK2SECURITY;
                } else if ("证券转银行".equals(operStr)) {
                    oper = TradeVo.SECURITY2BANK;
                }
                if (oper == TradeVo.INVALID) {
                    continue;
                }

                TradeVo t = new TradeVo();
                t.setTime(strs[0].trim() + " " + strs[1].trim());
                String code = strs[2].trim();
                if (code.startsWith("7")) {
                    if (newEnquityMapping.containsKey(code)) {
                        t.setCode(newEnquityMapping.get(code));
                    } else {
                        System.out.println("Need mapping code: " + code);
                    }
                } else {
                    t.setCode(code);
                }

                t.setName(strs[3].trim());
                t.setOperation(oper);// 4
                t.setTurnover(Float.parseFloat(strs[5].trim()));
                t.setNetPosition(Integer.parseInt(strs[7].trim()));
                t.setAmount(Integer.parseInt(strs[8].trim()));
                t.setPrice(Float.parseFloat(strs[9].trim()));

                if (!codeNameMap.containsKey(code)) {
                    codeNameMap.put(code, t.getName());
                }
                tradeList.add(t);
            }
        }
    }

    private static String getName(String code) {
        String getName = codeNameMap.get(code);
        return getName;
    }

    private static Map<String, String> newEnquityMapping = new HashMap<>();
    static {
        newEnquityMapping.put("730977", "600977");
        newEnquityMapping.put("730936", "600936");
        newEnquityMapping.put("780997", "601997");
        newEnquityMapping.put("780595", "601595");
        newEnquityMapping.put("732929", "603929");
    }
}
