package io.Sriptirc_wp_989.invitereward.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    
    // 配置值
    private List<String> rewardCommands;
    private boolean allowMultipleUses;
    private boolean preventSelfUse;
    private boolean preventPlayerReuse;
    private String offlineHandling;
    private int queueMaxWait;
    private boolean requireInviteUse;
    private boolean requireUseCode;
    private boolean requireViewStats;
    
    // 消息
    private String prefix;
    private String codeGenerated;
    private String codeRegenerated;
    private String codeInvalid;
    private String codeSelfUse;
    private String codeAlreadyUsed;
    private String success;
    private String inviterOffline;
    private String statsHeader;
    private String statsLine;
    private String statsNone;
    private String noPermission;
    private String usageCinvc;
    private String usageIcode;
    private String usageInv;
    
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }
    
    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        // 检查配置版本
        int configVersion = config.getInt("ScriptIrc-config-version", 1);
        if (configVersion < 1) {
            plugin.getLogger().warning("配置文件版本过低，建议删除并重新生成！");
        }
        
        // 读取奖励指令
        rewardCommands = config.getStringList("reward-commands");
        if (rewardCommands.isEmpty()) {
            rewardCommands = List.of(
                "give %inviter% diamond 1",
                "give %invitee% iron_ingot 5"
            );
        }
        
        // 读取邀请码设置
        allowMultipleUses = config.getBoolean("invite-code.allow-multiple-uses", true);
        preventSelfUse = config.getBoolean("invite-code.prevent-self-use", true);
        preventPlayerReuse = config.getBoolean("invite-code.prevent-player-reuse", true);
        
        // 读取奖励执行设置
        offlineHandling = config.getString("reward.offline-handling", "queue");
        queueMaxWait = config.getInt("reward.queue-max-wait", 1440);
        
        // 读取权限设置
        requireInviteUse = config.getBoolean("permissions.require-invite-use", false);
        requireUseCode = config.getBoolean("permissions.require-use-code", false);
        requireViewStats = config.getBoolean("permissions.require-view-stats", true);
        
        // 读取消息
        prefix = colorize(config.getString("messages.prefix", "&6[邀请奖励]&f "));
        codeGenerated = colorize(config.getString("messages.code-generated", "&a你的邀请码已生成：&e%code%\n&7使用命令 &e/icode <邀请码> &7或 &e/invc <邀请码> &7让其他玩家输入来获得奖励。"));
        codeRegenerated = colorize(config.getString("messages.code-regenerated", "&a你的新邀请码已生成：&e%code%\n&c旧的邀请码已失效。"));
        codeInvalid = colorize(config.getString("messages.code-invalid", "&c邀请码无效或已失效。"));
        codeSelfUse = colorize(config.getString("messages.code-self-use", "&c你不能使用自己的邀请码。"));
        codeAlreadyUsed = colorize(config.getString("messages.code-already-used", "&c你已经使用过邀请码，无法再次使用。"));
        success = colorize(config.getString("messages.success", "&a邀请成功！奖励已发放。"));
        inviterOffline = colorize(config.getString("messages.inviter-offline", "&e邀请者当前不在线，奖励已加入队列，待其上线后发放。"));
        statsHeader = colorize(config.getString("messages.stats-header", "&6=== 邀请统计 ==="));
        statsLine = colorize(config.getString("messages.stats-line", "&e%player% &7- &a%count% &7次邀请"));
        statsNone = colorize(config.getString("messages.stats-none", "&c暂无邀请数据。"));
        noPermission = colorize(config.getString("messages.no-permission", "&c你没有权限执行此命令。"));
        usageCinvc = colorize(config.getString("messages.usage-cinvc", "&7用法：&e/cinvc"));
        usageIcode = colorize(config.getString("messages.usage-icode", "&7用法：&e/icode <邀请码> &7或 &e/invc <邀请码>"));
        usageInv = colorize(config.getString("messages.usage-inv", "&7用法：&e/inv"));
    }
    
    /**
     * 颜色代码转换
     */
    private String colorize(String message) {
        if (message == null) return "";
        return message.replace('&', '§');
    }
    
    // Getter 方法
    
    public List<String> getRewardCommands() {
        return rewardCommands;
    }
    
    public boolean isAllowMultipleUses() {
        return allowMultipleUses;
    }
    
    public boolean isPreventSelfUse() {
        return preventSelfUse;
    }
    
    public boolean isPreventPlayerReuse() {
        return preventPlayerReuse;
    }
    
    public String getOfflineHandling() {
        return offlineHandling;
    }
    
    public int getQueueMaxWait() {
        return queueMaxWait;
    }
    
    public boolean isRequireInviteUse() {
        return requireInviteUse;
    }
    
    public boolean isRequireUseCode() {
        return requireUseCode;
    }
    
    public boolean isRequireViewStats() {
        return requireViewStats;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public String getCodeGenerated() {
        return codeGenerated;
    }
    
    public String getCodeRegenerated() {
        return codeRegenerated;
    }
    
    public String getCodeInvalid() {
        return codeInvalid;
    }
    
    public String getCodeSelfUse() {
        return codeSelfUse;
    }
    
    public String getCodeAlreadyUsed() {
        return codeAlreadyUsed;
    }
    
    public String getSuccess() {
        return success;
    }
    
    public String getInviterOffline() {
        return inviterOffline;
    }
    
    public String getStatsHeader() {
        return statsHeader;
    }
    
    public String getStatsLine() {
        return statsLine;
    }
    
    public String getStatsNone() {
        return statsNone;
    }
    
    public String getNoPermission() {
        return noPermission;
    }
    
    public String getUsageCinvc() {
        return usageCinvc;
    }
    
    public String getUsageIcode() {
        return usageIcode;
    }
    
    public String getUsageInv() {
        return usageInv;
    }
}