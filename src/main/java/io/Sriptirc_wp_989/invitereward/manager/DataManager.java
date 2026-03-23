package io.Sriptirc_wp_989.invitereward.manager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class DataManager {
    private final JavaPlugin plugin;
    private File dataFile;
    private FileConfiguration data;

    // 数据结构
    private Map<UUID, String> inviterCodes; // 邀请者 -> 邀请码
    private Map<String, UUID> codeToInviter; // 邀请码 -> 邀请者
    private Map<UUID, Integer> inviterCounts; // 邀请者 -> 邀请次数
    private Set<UUID> usedPlayers; // 已经使用过邀请码的玩家
    private Map<UUID, List<PendingReward>> pendingRewards; // 邀请者 -> 待处理奖励队列

    public DataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.inviterCodes = new HashMap<>();
        this.codeToInviter = new HashMap<>();
        this.inviterCounts = new HashMap<>();
        this.usedPlayers = new HashSet<>();
        this.pendingRewards = new HashMap<>();
        load();
    }

    /**
     * 加载数据文件
     */
    public void load() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            plugin.saveResource("data.yml", false);
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        
        // 清空内存数据
        inviterCodes.clear();
        codeToInviter.clear();
        inviterCounts.clear();
        usedPlayers.clear();
        pendingRewards.clear();

        // 加载邀请码映射
        ConfigurationSection inviterCodesSection = data.getConfigurationSection("inviter-codes");
        if (inviterCodesSection != null) {
            for (String uuidStr : inviterCodesSection.getKeys(false)) {
                UUID inviter = UUID.fromString(uuidStr);
                String code = inviterCodesSection.getString(uuidStr);
                inviterCodes.put(inviter, code);
                codeToInviter.put(code, inviter);
            }
        }

        // 加载邀请次数
        ConfigurationSection inviterCountsSection = data.getConfigurationSection("inviter-counts");
        if (inviterCountsSection != null) {
            for (String uuidStr : inviterCountsSection.getKeys(false)) {
                UUID inviter = UUID.fromString(uuidStr);
                int count = inviterCountsSection.getInt(uuidStr);
                inviterCounts.put(inviter, count);
            }
        }

        // 加载已使用玩家
        List<String> usedPlayersList = data.getStringList("used-players");
        for (String uuidStr : usedPlayersList) {
            usedPlayers.add(UUID.fromString(uuidStr));
        }

        // 加载待处理奖励
        ConfigurationSection pendingSection = data.getConfigurationSection("pending-rewards");
        if (pendingSection != null) {
            for (String inviterUuidStr : pendingSection.getKeys(false)) {
                UUID inviter = UUID.fromString(inviterUuidStr);
                List<PendingReward> list = new ArrayList<>();
                ConfigurationSection rewardListSection = pendingSection.getConfigurationSection(inviterUuidStr);
                if (rewardListSection != null) {
                    for (String key : rewardListSection.getKeys(false)) {
                        ConfigurationSection rewardSection = rewardListSection.getConfigurationSection(key);
                        if (rewardSection != null) {
                            UUID invitee = UUID.fromString(rewardSection.getString("invitee"));
                            long timestamp = rewardSection.getLong("timestamp", System.currentTimeMillis());
                            list.add(new PendingReward(invitee, timestamp));
                        }
                    }
                }
                pendingRewards.put(inviter, list);
            }
        }
        
        plugin.getLogger().info("数据加载完成：邀请码 " + inviterCodes.size() + " 个，邀请记录 " + inviterCounts.size() + " 条，已使用玩家 " + usedPlayers.size() + " 个，待处理奖励 " + pendingRewards.size() + " 组");
    }

    /**
     * 保存数据到文件
     */
    public void save() {
        if (data == null || dataFile == null) {
            return;
        }

        // 清空现有数据
        for (String key : data.getKeys(false)) {
            data.set(key, null);
        }

        // 保存邀请码映射
        for (Map.Entry<UUID, String> entry : inviterCodes.entrySet()) {
            data.set("inviter-codes." + entry.getKey().toString(), entry.getValue());
        }

        // 保存邀请次数
        for (Map.Entry<UUID, Integer> entry : inviterCounts.entrySet()) {
            data.set("inviter-counts." + entry.getKey().toString(), entry.getValue());
        }

        // 保存已使用玩家
        List<String> usedPlayersList = new ArrayList<>();
        for (UUID uuid : usedPlayers) {
            usedPlayersList.add(uuid.toString());
        }
        data.set("used-players", usedPlayersList);

        // 保存待处理奖励
        for (Map.Entry<UUID, List<PendingReward>> entry : pendingRewards.entrySet()) {
            String inviterKey = "pending-rewards." + entry.getKey().toString();
            List<PendingReward> list = entry.getValue();
            for (int i = 0; i < list.size(); i++) {
                PendingReward reward = list.get(i);
                data.set(inviterKey + "." + i + ".invitee", reward.getInvitee().toString());
                data.set(inviterKey + "." + i + ".timestamp", reward.getTimestamp());
            }
        }

        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "保存数据文件失败", e);
        }
    }

    /**
     * 为邀请者生成新的邀请码
     * @param inviter 邀请者UUID
     * @param newCode 新的邀请码
     * @return 旧的邀请码（如果存在）
     */
    public String setInviteCode(UUID inviter, String newCode) {
        String oldCode = inviterCodes.get(inviter);
        if (oldCode != null) {
            codeToInviter.remove(oldCode);
        }
        inviterCodes.put(inviter, newCode);
        codeToInviter.put(newCode, inviter);
        
        // 确保邀请者有一个计数条目
        if (!inviterCounts.containsKey(inviter)) {
            inviterCounts.put(inviter, 0);
        }
        
        save();
        return oldCode;
    }

    /**
     * 获取邀请者的邀请码
     */
    public String getInviteCode(UUID inviter) {
        return inviterCodes.get(inviter);
    }

    /**
     * 根据邀请码获取邀请者
     */
    public UUID getInviterByCode(String code) {
        return codeToInviter.get(code.toUpperCase());
    }

    /**
     * 检查邀请码是否存在
     */
    public boolean isCodeValid(String code) {
        return codeToInviter.containsKey(code.toUpperCase());
    }

    /**
     * 增加邀请者的邀请计数
     */
    public void incrementInviteCount(UUID inviter) {
        int count = inviterCounts.getOrDefault(inviter, 0);
        inviterCounts.put(inviter, count + 1);
        save();
    }

    /**
     * 获取邀请者的邀请次数
     */
    public int getInviteCount(UUID inviter) {
        return inviterCounts.getOrDefault(inviter, 0);
    }

    /**
     * 获取所有邀请者的邀请次数（用于统计）
     */
    public Map<UUID, Integer> getAllInviteCounts() {
        return new HashMap<>(inviterCounts);
    }

    /**
     * 标记玩家已使用过邀请码
     */
    public void markPlayerUsed(UUID player) {
        usedPlayers.add(player);
        save();
    }

    /**
     * 检查玩家是否已使用过邀请码
     */
    public boolean hasPlayerUsed(UUID player) {
        return usedPlayers.contains(player);
    }

    /**
     * 添加待处理奖励
     */
    public void addPendingReward(UUID inviter, UUID invitee) {
        List<PendingReward> list = pendingRewards.computeIfAbsent(inviter, k -> new ArrayList<>());
        list.add(new PendingReward(invitee, System.currentTimeMillis()));
        save();
    }

    /**
     * 获取并清空指定邀请者的待处理奖励
     */
    public List<UUID> takePendingRewards(UUID inviter) {
        List<PendingReward> list = pendingRewards.remove(inviter);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<UUID> invitees = new ArrayList<>();
        for (PendingReward reward : list) {
            invitees.add(reward.getInvitee());
        }
        save();
        return invitees;
    }

    /**
     * 获取所有有邀请码的邀请者UUID
     */
    public Set<UUID> getAllInviterWithCodes() {
        return new HashSet<>(inviterCodes.keySet());
    }

    /**
     * 清除过期待处理奖励（超过最大等待时间）
     * @param maxWaitMinutes 最大等待时间（分钟）
     */
    public void cleanupExpiredRewards(int maxWaitMinutes) {
        if (maxWaitMinutes <= 0) {
            return; // 无限等待，不清理
        }
        
        long cutoffTime = System.currentTimeMillis() - (maxWaitMinutes * 60 * 1000L);
        int removedCount = 0;
        
        Iterator<Map.Entry<UUID, List<PendingReward>>> iterator = pendingRewards.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, List<PendingReward>> entry = iterator.next();
            List<PendingReward> list = entry.getValue();
            list.removeIf(reward -> reward.getTimestamp() < cutoffTime);
            
            if (list.isEmpty()) {
                iterator.remove();
            } else {
                removedCount += (entry.getValue().size() - list.size());
            }
        }
        
        if (removedCount > 0) {
            plugin.getLogger().info("清理了 " + removedCount + " 个过期待处理奖励");
            save();
        }
    }

    /**
     * 待处理奖励的内部类
     */
    public static class PendingReward {
        private final UUID invitee;
        private final long timestamp;

        public PendingReward(UUID invitee, long timestamp) {
            this.invitee = invitee;
            this.timestamp = timestamp;
        }

        public UUID getInvitee() {
            return invitee;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}