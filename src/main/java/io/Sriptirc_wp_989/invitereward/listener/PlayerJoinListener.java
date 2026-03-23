package io.Sriptirc_wp_989.invitereward.listener;

import io.Sriptirc_wp_989.invitereward.manager.ConfigManager;
import io.Sriptirc_wp_989.invitereward.manager.DataManager;
import io.Sriptirc_wp_989.invitereward.util.RewardExecutor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerJoinListener implements Listener {
    private final JavaPlugin plugin;
    private final DataManager dataManager;
    private final ConfigManager configManager;
    private final RewardExecutor rewardExecutor;
    
    public PlayerJoinListener(JavaPlugin plugin, DataManager dataManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.configManager = configManager;
        this.rewardExecutor = new RewardExecutor(plugin);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        
        // 检查该玩家是否有待处理的奖励队列
        List<UUID> pendingInvitees = dataManager.takePendingRewards(playerUuid);
        
        if (!pendingInvitees.isEmpty()) {
            plugin.getLogger().info("玩家 " + player.getName() + " 上线，处理 " + pendingInvitees.size() + " 个待处理奖励");
            
            // 为每个待处理的被邀请者执行奖励
            for (UUID inviteeUuid : pendingInvitees) {
                try {
                    // 执行奖励指令
                    rewardExecutor.executeRewards(playerUuid, inviteeUuid, configManager.getRewardCommands());
                    
                    // 增加邀请计数
                    dataManager.incrementInviteCount(playerUuid);
                    
                    // 通知邀请者
                    player.sendMessage(configManager.getPrefix() + 
                        "§a玩家 §e" + Bukkit.getOfflinePlayer(inviteeUuid).getName() + 
                        " §a在你离线时使用了你的邀请码，奖励已发放！");
                    
                    plugin.getLogger().info("已为邀请者 " + player.getName() + " 和被邀请者 " + 
                        Bukkit.getOfflinePlayer(inviteeUuid).getName() + " 发放奖励");
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "处理待处理奖励时出错", e);
                }
            }
        }
        
        // 定期清理过期的待处理奖励（每天一次）
        if (Math.random() < 0.01) { // 大约1%的概率执行清理
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                dataManager.cleanupExpiredRewards(configManager.getQueueMaxWait());
            });
        }
    }
}