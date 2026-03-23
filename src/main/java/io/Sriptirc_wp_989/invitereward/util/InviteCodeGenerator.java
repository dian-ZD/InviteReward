package io.Sriptirc_wp_989.invitereward.util;

import io.Sriptirc_wp_989.invitereward.manager.DataManager;

import java.util.Random;

public class InviteCodeGenerator {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int PART_LENGTH = 4;
    private static final String SEPARATOR = "-";
    private static final Random RANDOM = new Random();
    
    /**
     * 生成一个新的唯一邀请码
     * @param dataManager 数据管理器，用于检查唯一性
     * @return 唯一邀请码
     */
    public static String generateUniqueCode(DataManager dataManager) {
        String code;
        int attempts = 0;
        int maxAttempts = 100; // 防止无限循环
        
        do {
            code = generateCode();
            attempts++;
            if (attempts > maxAttempts) {
                throw new IllegalStateException("无法生成唯一邀请码，请检查数据是否已满");
            }
        } while (dataManager.isCodeValid(code));
        
        return code;
    }
    
    /**
     * 生成一个随机邀请码（不检查唯一性）
     */
    private static String generateCode() {
        StringBuilder sb = new StringBuilder();
        
        // 第一部分：4个大写字母
        for (int i = 0; i < PART_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        
        sb.append(SEPARATOR);
        
        // 第二部分：4个大写字母
        for (int i = 0; i < PART_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        
        return sb.toString();
    }
    
    /**
     * 验证邀请码格式是否正确
     * @param code 待验证的邀请码
     * @return 格式是否正确
     */
    public static boolean isValidFormat(String code) {
        if (code == null || code.length() != 9) {
            return false;
        }
        
        // 检查分隔符位置
        if (code.charAt(4) != '-') {
            return false;
        }
        
        // 检查所有字符是否为大写字母或分隔符
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            if (i == 4) {
                if (c != '-') return false;
            } else {
                if (c < 'A' || c > 'Z') return false;
            }
        }
        
        return true;
    }
    
    /**
     * 标准化邀请码（转为大写，确保格式）
     */
    public static String normalizeCode(String code) {
        if (code == null) return null;
        return code.toUpperCase();
    }
}