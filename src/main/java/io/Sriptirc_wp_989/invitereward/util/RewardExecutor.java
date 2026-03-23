package io.Sriptirc_wp_989.invitereward.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class RewardExecutor {
    private final JavaPlugin plugin;
    
    public RewardExecutor(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 执行奖励指令列表
     * @param inviter 邀请者UUID
     * @param invitee 被邀请者UUID
     * @param rewardCommands 奖励指令列表
     */
    public void executeRewards(UUID inviter, UUID invitee, List<String> rewardCommands) {
        Player inviterPlayer = Bukkit.getPlayer(inviter);
        Player inviteePlayer = Bukkit.getPlayer(invitee);
        
        String inviterName = inviterPlayer != null ? inviterPlayer.getName() : Bukkit.getOfflinePlayer(inviter).getName();
        String inviteeName = inviteePlayer != null ? inviteePlayer.getName() : Bukkit.getOfflinePlayer(invitee).getName();
        
        for (String command : rewardCommands) {
            String processedCommand = command
                .replace("%inviter%", inviterName)
                .replace("%invitee%", inviteeName);
            
            // 如果指令以 "give " 开头，可能需要处理离线玩家，但 Bukkit 的 give 命令需要玩家在线
            // 使用控制台执行
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
                plugin.getLogger().info("执行奖励指令: " + processedCommand);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "执行奖励指令失败: " + processedCommand, e);
            }
        }
    }
    
    /**
     * 执行奖励指令（使用玩家对象）
     */
    public void executeRewards(Player inviter, Player invitee, List<String> rewardCommands) {
        if (inviter == null || invitee == null) {
            plugin.getLogger().warning("执行奖励时玩家对象为空");
            return;
        }
        
        for (String command : rewardCommands) {
            String processedCommand = command
                .replace("%inviter%", inviter.getName())
                .replace("%invitee%", invitee.getName());
            
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
                plugin.getLogger().info("执行奖励指令: " + processedCommand);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "执行奖励指令失败: " + processedCommand, e);
            }
        }
    }
}