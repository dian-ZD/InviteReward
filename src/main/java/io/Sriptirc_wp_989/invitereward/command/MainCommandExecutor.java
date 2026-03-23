package io.Sriptirc_wp_989.invitereward.command;

import io.Sriptirc_wp_989.invitereward.manager.ConfigManager;
import io.Sriptirc_wp_989.invitereward.manager.DataManager;
import io.Sriptirc_wp_989.invitereward.util.InviteCodeGenerator;
import io.Sriptirc_wp_989.invitereward.util.RewardExecutor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class MainCommandExecutor implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final DataManager dataManager;
    private final ConfigManager configManager;
    private final RewardExecutor rewardExecutor;
    
    public MainCommandExecutor(JavaPlugin plugin, DataManager dataManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.configManager = configManager;
        this.rewardExecutor = new RewardExecutor(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();
        
        switch (cmd) {
            case "cinvc":
                return handleCinvc(sender, args);
            case "icode":
            case "invc":
                return handleUseCode(sender, args, label);
            case "inv":
                return handleInv(sender, args);
            default:
                return false;
        }
    }
    
    /**
     * 处理生成邀请码命令
     */
    private boolean handleCinvc(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家才能执行此命令。");
            return true;
        }
        
        Player player = (Player) sender;
        
        // 权限检查
        if (configManager.isRequireInviteUse() && !player.hasPermission("invite.use")) {
            player.sendMessage(configManager.getNoPermission());
            return true;
        }
        
        // 生成新邀请码
        String newCode = InviteCodeGenerator.generateUniqueCode(dataManager);
        String oldCode = dataManager.setInviteCode(player.getUniqueId(), newCode);
        
        // 发送消息
        if (oldCode != null) {
            player.sendMessage(configManager.getPrefix() + 
                configManager.getCodeRegenerated().replace("%code%", newCode));
        } else {
            player.sendMessage(configManager.getPrefix() + 
                configManager.getCodeGenerated().replace("%code%", newCode));
        }
        
        return true;
    }
    
    /**
     * 处理使用邀请码命令
     */
    private boolean handleUseCode(CommandSender sender, String[] args, String label) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家才能执行此命令。");
            return true;
        }
        
        Player invitee = (Player) sender;
        
        // 权限检查
        if (configManager.isRequireUseCode() && !invitee.hasPermission("invite.use")) {
            invitee.sendMessage(configManager.getNoPermission());
            return true;
        }
        
        // 参数检查
        if (args.length != 1) {
            invitee.sendMessage(configManager.getPrefix() + configManager.getUsageIcode());
            return true;
        }
        
        String code = InviteCodeGenerator.normalizeCode(args[0]);
        
        // 检查邀请码格式
        if (!InviteCodeGenerator.isValidFormat(code)) {
            invitee.sendMessage(configManager.getPrefix() + configManager.getCodeInvalid());
            return true;
        }
        
        // 检查邀请码是否存在
        UUID inviterUuid = dataManager.getInviterByCode(code);
        if (inviterUuid == null) {
            invitee.sendMessage(configManager.getPrefix() + configManager.getCodeInvalid());
            return true;
        }
        
        // 检查是否禁止邀请者使用自己的邀请码
        if (configManager.isPreventSelfUse() && inviterUuid.equals(invitee.getUniqueId())) {
            invitee.sendMessage(configManager.getPrefix() + configManager.getCodeSelfUse());
            return true;
        }
        
        // 检查玩家是否已使用过邀请码（防止重复使用）
        if (configManager.isPreventPlayerReuse() && dataManager.hasPlayerUsed(invitee.getUniqueId())) {
            invitee.sendMessage(configManager.getPrefix() + configManager.getCodeAlreadyUsed());
            return true;
        }
        
        // 检查邀请者是否在线
        Player inviter = Bukkit.getPlayer(inviterUuid);
        boolean inviterOnline = inviter != null && inviter.isOnline();
        
        // 处理奖励
        String offlineHandling = configManager.getOfflineHandling();
        
        if (inviterOnline) {
            // 邀请者在线，立即执行奖励
            rewardExecutor.executeRewards(inviter, invitee, configManager.getRewardCommands());
            
            // 增加邀请计数
            dataManager.incrementInviteCount(inviterUuid);
            
            // 标记玩家已使用过邀请码
            if (configManager.isPreventPlayerReuse()) {
                dataManager.markPlayerUsed(invitee.getUniqueId());
            }
            
            // 发送成功消息
            invitee.sendMessage(configManager.getPrefix() + configManager.getSuccess());
            if (inviter != null) {
                inviter.sendMessage(configManager.getPrefix() + 
                    "§a玩家 §e" + invitee.getName() + " §a使用了你的邀请码！");
            }
        } else {
            // 邀请者不在线
            if ("queue".equals(offlineHandling)) {
                // 加入等待队列
                dataManager.addPendingReward(inviterUuid, invitee.getUniqueId());
                
                // 标记玩家已使用过邀请码（即使奖励还没发放，防止重复使用）
                if (configManager.isPreventPlayerReuse()) {
                    dataManager.markPlayerUsed(invitee.getUniqueId());
                }
                
                invitee.sendMessage(configManager.getPrefix() + configManager.getInviterOffline());
            } else if ("skip_inviter".equals(offlineHandling)) {
                // 跳过邀请者，只给被邀请者奖励
                rewardExecutor.executeRewards(inviterUuid, invitee.getUniqueId(), configManager.getRewardCommands());
                
                // 增加邀请计数
                dataManager.incrementInviteCount(inviterUuid);
                
                // 标记玩家已使用过邀请码
                if (configManager.isPreventPlayerReuse()) {
                    dataManager.markPlayerUsed(invitee.getUniqueId());
                }
                
                invitee.sendMessage(configManager.getPrefix() + configManager.getSuccess());
            } else { // execute_now
                // 立即执行（可能失败）
                rewardExecutor.executeRewards(inviterUuid, invitee.getUniqueId(), configManager.getRewardCommands());
                
                // 增加邀请计数
                dataManager.incrementInviteCount(inviterUuid);
                
                // 标记玩家已使用过邀请码
                if (configManager.isPreventPlayerReuse()) {
                    dataManager.markPlayerUsed(invitee.getUniqueId());
                }
                
                invitee.sendMessage(configManager.getPrefix() + configManager.getSuccess());
            }
        }
        
        return true;
    }
    
    /**
     * 处理查看邀请统计命令
     */
    private boolean handleInv(CommandSender sender, String[] args) {
        // 权限检查
        if (configManager.isRequireViewStats() && !sender.hasPermission("invite.admin")) {
            sender.sendMessage(configManager.getPrefix() + configManager.getNoPermission());
            return true;
        }
        
        Map<UUID, Integer> allCounts = dataManager.getAllInviteCounts();
        
        if (allCounts.isEmpty()) {
            sender.sendMessage(configManager.getPrefix() + configManager.getStatsNone());
            return true;
        }
        
        // 转换为玩家名列表并排序
        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : allCounts.entrySet()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.getKey());
            String playerName = offlinePlayer.getName();
            if (playerName == null) {
                playerName = entry.getKey().toString();
            }
            sortedList.add(new AbstractMap.SimpleEntry<>(playerName, entry.getValue()));
        }
        
        // 按邀请次数降序排序
        sortedList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        // 发送统计
        sender.sendMessage(configManager.getPrefix() + configManager.getStatsHeader());
        for (Map.Entry<String, Integer> entry : sortedList) {
            String line = configManager.getStatsLine()
                .replace("%player%", entry.getKey())
                .replace("%count%", entry.getValue().toString());
            sender.sendMessage(configManager.getPrefix() + line);
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // 所有命令都不需要参数补全
        return Collections.emptyList();
    }
}